/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.utils

/**
 * Message converter
 */
object MessageConverter {
    /**
     * String to byte array
     *
     * @param data string data
     * @return byte array data
     */
    fun stringToByteArray(data: String): ByteArray {
        return data.toByteArray(Charsets.UTF_8)
    }

    /**
     * Byte array to string
     *
     * @param data byte array data
     * @return string data
     */
    fun byteArrayToString(data: ByteArray): String {
        return String(data, Charsets.UTF_8)
    }
}