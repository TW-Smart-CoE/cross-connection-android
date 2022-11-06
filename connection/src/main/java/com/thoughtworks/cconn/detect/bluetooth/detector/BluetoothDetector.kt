/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.detect.bluetooth.detector

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.detect.NetworkDetector
import com.thoughtworks.cconn.detect.OnFoundService
import java.util.*

internal class BluetoothDetector(private val context: Context) : NetworkDetector {
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var logger: Logger = DefaultLogger()

    private var onFoundService: OnFoundService? = null

    override fun startDiscover(configProps: Properties, onFoundService: OnFoundService) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(receiver, filter)

        if (bluetoothAdapter.isDiscovering) {
            return
        }

        this.onFoundService = onFoundService
        val result = bluetoothAdapter.startDiscovery()
        if (!result) {
            logger.error("BluetoothAdapter startDiscovery failed (state == ${bluetoothAdapter.state}, multipleAdvertisementSupported = ${bluetoothAdapter.isMultipleAdvertisementSupported})")
        }
    }

    override fun stopDiscover() {
        this.onFoundService = null

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        try {
            context.unregisterReceiver(receiver)
        } catch (t: Throwable) {
            // swallow exceptions
        }
    }

    override fun setLogger(logger: Logger) {

        this.logger = logger
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.apply {
                when (this) {
                    BluetoothDevice.ACTION_FOUND -> {
                        // Discovery has found a device. Get the BluetoothDevice
                        // object and its info from the Intent.
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                        device?.let {
                            val properties = Properties()
                            properties.setProperty(NAME, it.name ?: "")
                            properties.setProperty(ADDRESS, it.address)
                            onFoundService?.invoke(properties)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val NAME = "name"
        const val ADDRESS = "address"
    }
}