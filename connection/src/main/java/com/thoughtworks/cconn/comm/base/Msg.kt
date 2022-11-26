package com.thoughtworks.cconn.comm.base

import com.thoughtworks.cconn.Method
import com.thoughtworks.cconn.utils.DataConverter
import com.thoughtworks.cconn.utils.getByte
import com.thoughtworks.cconn.utils.getInt
import com.thoughtworks.cconn.utils.getShort
import com.thoughtworks.cconn.utils.putByte
import com.thoughtworks.cconn.utils.putInt
import com.thoughtworks.cconn.utils.putShort

internal const val MSG_HEADER_LEN = 16

internal const val MSG_TYPE_PUBLISH: Byte = 0
internal const val MSG_TYPE_SUBSCRIBE: Byte = 1
internal const val MSG_TYPE_UNSUBSCRIBE: Byte = 2

internal const val METHOD_REPORT: Byte = 0
internal const val METHOD_QUERY: Byte = 1
internal const val METHOD_REPLY: Byte = 2
internal const val METHOD_REQUEST: Byte = 3
internal const val METHOD_RESPONSE: Byte = 4

internal typealias OnMsgArrivedListener = (msg: Msg) -> Unit

internal enum class MsgType {
    PUBLISH,
    SUBSCRIBE,
    UNSUBSCRIBE
}

internal class MsgHeader(
    var flag: Int = msgFlag(),
    var type: Byte = MSG_TYPE_PUBLISH,
    var method: Byte = METHOD_REPORT,
    var topicLen: UShort = 0u,
    var dataLen: UShort = 0u,
    var checkSum: UInt = 0u,
    var reserved: UShort = 0u
) {
    fun fromByteArray(byteArray: ByteArray) {
        if (byteArray.size < MSG_HEADER_LEN) {
            throw Exception("byteArray size ${byteArray.size} smaller than MSG_HEADER_LEN $MSG_HEADER_LEN")
        }

        var index = 0
        flag = byteArray.getInt(index)
        index += Int.SIZE_BYTES

        type = byteArray.getByte(index)
        index += Byte.SIZE_BYTES

        method = byteArray.getByte(index)
        index += Byte.SIZE_BYTES

        topicLen = byteArray.getShort(index).toUShort()
        index += Short.SIZE_BYTES

        dataLen = byteArray.getShort(index).toUShort()
        index += Short.SIZE_BYTES

        checkSum = byteArray.getInt(index).toUInt()
        index += Int.SIZE_BYTES

        reserved = byteArray.getShort(index).toUShort()
        index += Short.SIZE_BYTES
    }

    fun toByteArray(dstByteArray: ByteArray?): ByteArray {
        val byteArray = dstByteArray ?: ByteArray(MSG_HEADER_LEN)

        var index = 0

        byteArray.putInt(index, msgFlag())
        index += Int.SIZE_BYTES

        byteArray.putByte(index, type)
        index += Byte.SIZE_BYTES

        byteArray.putByte(index, method)
        index += Byte.SIZE_BYTES

        byteArray.putShort(index, topicLen.toShort())
        index += Short.SIZE_BYTES

        byteArray.putShort(index, dataLen.toShort())
        index += Short.SIZE_BYTES

        byteArray.putInt(index, checkSum.toInt())
        index += Int.SIZE_BYTES

        byteArray.putShort(index, 0)
        index += Short.SIZE_BYTES

        return byteArray
    }

    fun copyOf(): MsgHeader {
        val msgHeader = MsgHeader()
        msgHeader.fromByteArray(toByteArray(null))
        return msgHeader
    }
}

internal class Msg(
    var header: MsgHeader = MsgHeader(),
    var topic: ByteArray = ByteArray(0),
    var data: ByteArray = ByteArray(0)
) {
    fun toByteArray(): ByteArray {
        val byteArray = ByteArray(MSG_HEADER_LEN + header.topicLen.toInt() + header.dataLen.toInt())
        var index = 0

        header.toByteArray(byteArray)
        index += MSG_HEADER_LEN

        topic.copyInto(byteArray, index)
        index += topic.size

        data.copyInto(byteArray, index)
        index += data.size

        return byteArray
    }

    fun length(): Int {
        return MSG_HEADER_LEN + topic.size + data.size
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Msg

        if (header != other.header) return false
        if (!topic.contentEquals(other.topic)) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + topic.contentHashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    fun copyOf(): Msg {
        return Msg(
            MsgHeader(
                header.flag,
                header.type,
                header.method,
                header.topicLen,
                header.dataLen,
                header.checkSum,
                header.reserved
            ), topic.copyOf(), data.copyOf()
        )
    }
}

internal fun msgFlag(): Int {
    return 0xFFFEB0D4.toInt()
}

internal fun methodToByte(method: Method): Byte {
    return when (method) {
        Method.REPORT -> METHOD_REPORT
        Method.QUERY -> METHOD_QUERY
        Method.REPLY -> METHOD_REPLY
        Method.REQUEST -> METHOD_REQUEST
        Method.RESPONSE -> METHOD_RESPONSE
    }
}

internal fun byteToMethod(method: Byte): Method {
    return when (method) {
        METHOD_REPORT -> Method.REPORT
        METHOD_QUERY -> Method.QUERY
        METHOD_REPLY -> Method.REPLY
        METHOD_REQUEST -> Method.REQUEST
        METHOD_RESPONSE -> Method.RESPONSE
        else -> throw Exception("Unknown method $method")
    }
}

internal fun msgTypeToByte(msgType: MsgType): Byte {
    return when (msgType) {
        MsgType.PUBLISH -> MSG_TYPE_PUBLISH
        MsgType.SUBSCRIBE -> MSG_TYPE_SUBSCRIBE
        MsgType.UNSUBSCRIBE -> MSG_TYPE_UNSUBSCRIBE
    }
}

internal fun byteToMsgType(msgType: Byte): MsgType {
    return when (msgType) {
        MSG_TYPE_PUBLISH -> MsgType.PUBLISH
        MSG_TYPE_SUBSCRIBE -> MsgType.SUBSCRIBE
        MSG_TYPE_UNSUBSCRIBE -> MsgType.UNSUBSCRIBE
        else -> throw Exception("Unknown msgType $msgType")
    }
}

internal fun createMsg(type: Byte, method: Method, topic: String, data: ByteArray): Msg {
    val topicBytes = DataConverter.stringToByteArray(topic)

    val header = MsgHeader()
    header.type = type
    header.method = methodToByte(method)
    header.topicLen = topicBytes.size.toUShort()
    header.dataLen = data.size.toUShort()

    return Msg(header, topicBytes, data)
}

internal fun createMsg(type: Byte, method: Method, topic: String, data: String): Msg {
    return createMsg(type, method, topic, DataConverter.stringToByteArray(data))
}

internal fun Msg.calcCheckSum(): UInt {
    var checkSum: UInt = 0u

    val headerCopy = this.header.copyOf()
    headerCopy.checkSum = 0u

    val headerBytes = headerCopy.toByteArray(null)

    for (byte: Byte in headerBytes) {
        checkSum += byte.toUByte()
    }

    for (byte: Byte in this.topic) {
        checkSum += byte.toUByte()
    }

    for (byte: Byte in this.data) {
        checkSum += byte.toUByte()
    }

    return checkSum
}