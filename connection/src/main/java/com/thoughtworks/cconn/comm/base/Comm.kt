package com.thoughtworks.cconn.comm.base

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal interface Comm {
    @Throws(IOException::class)
    fun inputStream(): InputStream

    @Throws(IOException::class)
    fun outputStream(): OutputStream

    @Throws(IOException::class)
    fun connect()

    @Throws(IOException::class)
    fun close()
}