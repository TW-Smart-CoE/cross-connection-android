package com.thoughtworks.cconn.detect.udp

import com.thoughtworks.cconn.utils.getInt
import com.thoughtworks.cconn.utils.getShort
import com.thoughtworks.cconn.utils.putInt
import com.thoughtworks.cconn.utils.putShort

const val DEFAULT_BROADCAST_FLAG = 0xFFFEC1E5.toInt()
const val BROADCAST_MSG_HEADER_LEN = 12

internal class BroadcastMsg(
    var flag: Int = DEFAULT_BROADCAST_FLAG,
    var ip: Int = 0,
    var port: Short = 0,
    var reserved: Short = 0
) {
    fun fromByteArray(byteArray: ByteArray) {
        if (byteArray.size < BROADCAST_MSG_HEADER_LEN) {
            throw Exception("byteArray size ${byteArray.size} smaller than BROADCAST_MSG_HEADER_LEN $BROADCAST_MSG_HEADER_LEN")
        }

        var index = 0
        flag = byteArray.getInt(index)
        index += Int.SIZE_BYTES

        ip = byteArray.getInt(index)
        index += Int.SIZE_BYTES

        port = byteArray.getShort(index)
        index += Short.SIZE_BYTES

        reserved = byteArray.getShort(index)
        index += Short.SIZE_BYTES
    }

    fun toByteArray(dstByteArray: ByteArray? = null): ByteArray {
        val byteArray = dstByteArray ?: ByteArray(BROADCAST_MSG_HEADER_LEN)

        var index = 0

        byteArray.putInt(index, DEFAULT_BROADCAST_FLAG)
        index += Int.SIZE_BYTES

        byteArray.putInt(index, ip)
        index += Int.SIZE_BYTES

        byteArray.putShort(index, port)
        index += Short.SIZE_BYTES

        byteArray.putShort(index, reserved)
        index += Short.SIZE_BYTES

        return byteArray
    }
}