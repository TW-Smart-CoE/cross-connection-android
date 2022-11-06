/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.tcp

import com.thoughtworks.cconn.comm.base.Comm
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

internal class TcpComm(private val tcpSocket: Socket,
                       private val address: String,
                       private val port: Int) : Comm {
    @Throws
    override fun inputStream(): InputStream {
        return tcpSocket.getInputStream()
    }

    @Throws
    override fun outputStream(): OutputStream {
        return tcpSocket.getOutputStream()
    }

    @Throws
    override fun connect() {
        val socketAddress = InetSocketAddress(address, port)
        tcpSocket.connect(socketAddress)
    }

    @Throws
    override fun close() {
        tcpSocket.close()
    }
}