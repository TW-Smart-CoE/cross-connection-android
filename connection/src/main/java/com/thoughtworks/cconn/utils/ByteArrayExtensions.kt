/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.utils

fun ByteArray.getByte(index: Int): Byte {
    return this[index]
}

fun ByteArray.putByte(index: Int, value: Byte) {
    this[index] = value
}

fun ByteArray.getShort(index: Int): Short {
    return ByteUtils.getShort(this, index)
}

fun ByteArray.putShort(index: Int, value: Short) {
    ByteUtils.putShort(this, index, value)
}

fun ByteArray.getInt(index: Int): Int {
    return ByteUtils.getInt(this, index)
}

fun ByteArray.putInt(index: Int, value: Int) {
    ByteUtils.putInt(this, index, value)
}

fun ByteArray.toHexString(index: Int, len: Int): String {
    val sb = StringBuffer()
    for (i in index until len - index) {
        sb.append(String.format("%02X", this[i]))
    }
    return sb.toString()
}

fun ByteArray.toHexString(index: Int = 0): String {
    return this.toHexString(index, this.size)
}


