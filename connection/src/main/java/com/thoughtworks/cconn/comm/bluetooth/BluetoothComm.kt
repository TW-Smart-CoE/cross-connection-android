package com.thoughtworks.cconn.comm.bluetooth

import android.bluetooth.BluetoothSocket
import com.thoughtworks.cconn.comm.base.Comm
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

internal class BluetoothComm(private val bluetoothSocket: BluetoothSocket) : Comm {
    @Throws(IOException::class)
    override fun inputStream(): InputStream {
        return bluetoothSocket.inputStream
    }

    @Throws(IOException::class)
    override fun outputStream(): OutputStream {
        return bluetoothSocket.outputStream
    }

    @Throws(IOException::class)
    override fun connect() {
        bluetoothSocket.connect()
    }

    @Throws(IOException::class)
    override fun close() {
        bluetoothSocket.close()
    }
}