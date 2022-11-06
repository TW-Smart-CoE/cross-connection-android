package com.thoughtworks.cconn.utils

import java.util.*

fun String.toBoolean(): Boolean = this.lowercase(Locale.getDefault()) == "true"

fun String.hexToByteArray(): ByteArray {
    if (this.length % 2 != 0) {
        throw Exception("invalid hex string")
    }

    val byteArray = ByteArray(this.length / 2)
    for (i in this.indices step 2) {
        val byteString = this.substring(i, i + 2)
        byteArray.putByte(i / 2, byteString.toShort(16).toByte())
    }

    return byteArray
}
