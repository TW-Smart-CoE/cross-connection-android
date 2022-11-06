/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.detect.bluetooth.register

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.detect.NetworkRegister
import java.util.*

internal class BluetoothRegister(private val context: Context) : NetworkRegister {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var logger: Logger = DefaultLogger()

    override fun register(configProps: Properties) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val name = configProps.getProperty(NAME)
        if (bluetoothAdapter.isEnabled) {
            name?.let {
                bluetoothAdapter.name = name
            }
        }
    }

    override fun unregister() {
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
    }

    companion object {
        const val NAME = "name"
    }
}