package com.thoughtworks.cconn.comm.base

import com.thoughtworks.cconn.ConnectionState
import com.thoughtworks.cconn.OnConnectionStateChangeListener
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.utils.getInt
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal typealias OnCommCloseListener = (commHandler: CommHandler, isPassive: Boolean) -> Unit

internal class CommHandler(
    private val isClient: Boolean,
    private val comm: Comm,
    private val logger: Logger,
    var onCommCloseListener: OnCommCloseListener? = null,
    var onMsgArrivedListener: OnMsgArrivedListener? = null,
    var onConnectionStateChangeListener: OnConnectionStateChangeListener? = null
) : Runnable {
    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null
    private val buffer: ByteArray = ByteArray(BUFFER_SIZE)
    private var bufferDataStartOffset: Int = 0
    private var bufferDataLen: Int = 0
    private var isClose = false
    private var msgCompleteness =
        MSG_COMPLETENESS_NONE
    private val currentHeader = MsgHeader()
    private val currentMsg = Msg()

    override fun run() {
        if (isClient) {
            try {
                onConnectionStateChangeListener?.onConnectionStateChanged(ConnectionState.CONNECTING)
                comm.connect()
            } catch (e: IOException) {
                logger.error("Connect failed: ${e.message}")
                close(true)
                return
            }
        }

        try {
            inStream = comm.inputStream()
            outStream = comm.outputStream()
        } catch (e: IOException) {
            logger.error("Get input/output stream failed: ${e.message}")
            close(true)
            return
        }

        onConnectionStateChangeListener?.onConnectionStateChanged(ConnectionState.CONNECTED)

        isClose = false
        while (!isClose) {
            val msg = readMsgFlagFromBuffer()
            if (msg == null) {
                logger.warn("Connection is lost")
                close(true)
                isClose = true
            }

            msg?.let {
                if (msg.calcCheckSum() == msg.header.checkSum) {
                    onMsgArrivedListener?.invoke(it)
                }

                msgCompleteness = MSG_COMPLETENESS_NONE
            }
        }
    }

    @Throws(IOException::class)
    fun send(msg: Msg) {
        outStream?.apply {
            msg.header.checkSum = msg.calcCheckSum()
            write(msg.toByteArray())
            flush()
        }
    }

    fun close(isPassive: Boolean = false) {
        if (!isPassive) {
            onMsgArrivedListener = null
            onCommCloseListener = null
        }

        isClose = true

        try {
            comm.close()
            onConnectionStateChangeListener?.onConnectionStateChanged(ConnectionState.DISCONNECTED)
        } catch (e: IOException) {
            logger.error("Could not close the connection: ${e.message}")
            onConnectionStateChangeListener?.onConnectionStateChanged(
                ConnectionState.DISCONNECTED,
                e
            )
        } finally {
            onCommCloseListener?.invoke(this, isPassive)
        }
    }

    private fun readFromInputStream(): Msg? {
        val inStream = inStream ?: return null

        val len = try {
            inStream.read(buffer, bufferDataStartOffset + bufferDataLen, bufferLeftSize())
        } catch (e: IOException) {
            logger.warn("Stream.Read Exception: ${e.message}")
            return null
        }

        if (len <= 0) {
            logger.warn("stream.Read len == $len")
            return null
        }

        bufferDataLen += len

        return when (msgCompleteness) {
            MSG_COMPLETENESS_NONE -> {
                readMsgFlagFromBuffer()
            }
            MSG_COMPLETENESS_FLAG -> {
                readMsgHeaderFromBuffer()
            }
            MSG_COMPLETENESS_HEADER -> {
                readMsgBodyFromBuffer()
            }
            else -> {
                null
            }
        }
    }

    private fun readMsgFlagFromBuffer(): Msg? {
        var found = false
        for (i in bufferDataStartOffset..bufferDataStartOffset + bufferDataLen - Int.SIZE_BYTES) {
            if (buffer.getInt(bufferDataStartOffset) == msgFlag()) {
                found = true
                if (i != 0) {
                    buffer.copyInto(buffer, 0, i, bufferDataStartOffset + bufferDataLen)
                    bufferDataStartOffset = 0
                    bufferDataLen -= (i - bufferDataStartOffset)
                }
                break
            }
        }

        return if (found) {
            msgCompleteness =
                MSG_COMPLETENESS_FLAG
            readMsgHeaderFromBuffer()
        } else {
            // cannot find flag, reset buffer position and receive data again
            msgCompleteness =
                MSG_COMPLETENESS_NONE
            bufferDataStartOffset = 0
            bufferDataLen = 0
            readFromInputStream()
        }
    }

    private fun readMsgHeaderFromBuffer(): Msg? {
        if (bufferDataLen < MSG_HEADER_LEN) {
            return readFromInputStream()
        }

        val headerBuffer = ByteArray(MSG_HEADER_LEN)
        buffer.copyInto(
            headerBuffer,
            0,
            bufferDataStartOffset,
            bufferDataStartOffset + MSG_HEADER_LEN
        )
        currentHeader.fromByteArray(headerBuffer)
        msgCompleteness =
            MSG_COMPLETENESS_HEADER

        return readMsgBodyFromBuffer()
    }

    private fun readMsgBodyFromBuffer(): Msg? {
        if (bufferDataLen < MSG_HEADER_LEN + currentHeader.topicLen.toInt() + currentHeader.dataLen.toInt()) {
            return readFromInputStream()
        }

        val topicBuffer = ByteArray(currentHeader.topicLen.toInt())
        buffer.copyInto(
            topicBuffer,
            0,
            bufferDataStartOffset + MSG_HEADER_LEN,
            bufferDataStartOffset + MSG_HEADER_LEN + currentHeader.topicLen.toInt()
        )

        val dataBuffer = ByteArray(currentHeader.dataLen.toInt())
        buffer.copyInto(
            dataBuffer,
            0,
            bufferDataStartOffset + MSG_HEADER_LEN + currentHeader.topicLen.toInt(),
            bufferDataStartOffset + MSG_HEADER_LEN + currentHeader.topicLen.toInt() + currentHeader.dataLen.toInt()
        )

        currentMsg.header = currentHeader
        currentMsg.topic = topicBuffer
        currentMsg.data = dataBuffer

        val leftDataLen = (bufferDataLen - currentMsg.length())

        buffer.copyInto(
            buffer,
            0,
            bufferDataStartOffset + currentMsg.length(),
            bufferDataStartOffset + currentMsg.length() + leftDataLen
        )

        bufferDataStartOffset = 0
        bufferDataLen = leftDataLen

        return currentMsg.copyOf()
    }

    private fun bufferLeftSize(): Int {
        return BUFFER_SIZE - (bufferDataStartOffset + bufferDataLen)
    }

    companion object {
        private const val BUFFER_SIZE = 4096
        private const val MSG_COMPLETENESS_NONE = 0
        private const val MSG_COMPLETENESS_FLAG = 1
        private const val MSG_COMPLETENESS_HEADER = 2
    }
}