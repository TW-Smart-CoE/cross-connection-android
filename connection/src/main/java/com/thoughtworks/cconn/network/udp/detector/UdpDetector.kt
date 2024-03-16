package com.thoughtworks.cconn.network.udp.detector

import android.util.Log
import com.thoughtworks.cconn.definitions.Constants.CCONN_TAG
import com.thoughtworks.cconn.definitions.PropKeys
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.network.NetworkDetector
import com.thoughtworks.cconn.network.OnFoundService
import com.thoughtworks.cconn.network.udp.BroadcastHeader
import com.thoughtworks.cconn.utils.getInt
import com.thoughtworks.cconn.utils.intToIpv4String
import com.thoughtworks.cconn.utils.toHexString
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException
import java.util.*


class UdpDetector : NetworkDetector {
    private var logger: Logger = DefaultLogger()
    private var datagramSocket: DatagramSocket? = null
    private var isKeepReceiving = false
    private var broadcastPort = 0
    private var flag: Int = 0
    private var debugMode: Boolean = false

    override fun startDiscover(configProps: Properties, onFoundService: OnFoundService) {
        broadcastPort = configProps[PropKeys.PROP_BROADCAST_PORT]?.toString()?.toInt() ?: DEFAULT_BROADCAST_PORT
        flag = configProps[PropKeys.PROP_FLAG]?.toString()?.toLong()?.toInt() ?: DEFAULT_BROADCAST_FLAG
        debugMode = configProps[PropKeys.PROP_BROADCAST_DEBUG_MODE]?.toString()?.toBoolean() ?: false

        datagramSocket = DatagramSocket(null)
        datagramSocket?.reuseAddress = true
        datagramSocket?.bind(InetSocketAddress(ANY_ADDRESS, broadcastPort))
        datagramSocket?.broadcast = true
        isKeepReceiving = true

        Thread {
            try {
                datagramSocket?.let {
                    while (isKeepReceiving && datagramSocket != null) {
                        logger.debug("Waiting for broadcast on port $broadcastPort")

                        datagramSocket?.let {
                            val buf = ByteArray(RECV_BUF_LEN)
                            val packet = DatagramPacket(buf, buf.size)
                            it.receive(packet)

                            if (debugMode) {
                                logger.debug("Received broadcast (len=${packet.length}): ${buf.toHexString(0, packet.length)}")
                            }

                            if (packet.length >= BROADCAST_MSG_HEADER_LEN) {
                                val receiveMsgFlag = buf.getInt(0)
                                if (receiveMsgFlag == flag) {
                                    val broadcastHeader = BroadcastHeader()
                                    broadcastHeader.fromByteArray(buf)

                                    if (broadcastHeader.dataLen.toInt() == packet.length - BROADCAST_MSG_HEADER_LEN) {
                                        val properties = Properties()
                                        properties[PropKeys.PROP_SERVER_IP] =
                                            intToIpv4String(broadcastHeader.ip)
                                        properties[PropKeys.PROP_SERVER_PORT] =
                                            broadcastHeader.port

                                        if (packet.length > BROADCAST_MSG_HEADER_LEN) {
                                            val data = ByteArray(packet.length - BROADCAST_MSG_HEADER_LEN)
                                            buf.copyInto(data, startIndex = BROADCAST_MSG_HEADER_LEN, endIndex = BROADCAST_MSG_HEADER_LEN + data.size)
                                            properties[PropKeys.PROP_BROADCAST_DATA] = data
                                        }

                                        onFoundService.invoke(properties)
                                    } else {
                                        if (debugMode) {
                                            logger.error("Invalid broadcast msg data len ${packet.length - BROADCAST_MSG_HEADER_LEN}, but broadcast_header.data_len is ${broadcastHeader.dataLen}")
                                        }
                                    }
                                } else {
                                    if (debugMode) {
                                        logger.error("Received broadcast flag does not match")
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                e.message?.let { Log.w(CCONN_TAG, it) }
            }
        }.start()
    }

    override fun stopDiscover() {
        isKeepReceiving = false

        try {
            datagramSocket?.close()
            datagramSocket = null
        } catch (e: SocketException) {
            logger.error(e.toString())
        }
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
    }

    companion object {
        private const val ANY_ADDRESS = "0.0.0.0"
        private const val DEFAULT_BROADCAST_PORT = 12000
        private const val DEFAULT_BROADCAST_FLAG = 0xFFFEC1E5.toInt()
        private const val BROADCAST_MSG_HEADER_LEN = 12
        private const val RECV_BUF_LEN = 4096
    }
}