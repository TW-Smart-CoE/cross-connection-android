/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.bluetooth.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.SystemClock
import com.thoughtworks.cconn.Server
import com.thoughtworks.cconn.comm.base.CommHandler
import com.thoughtworks.cconn.comm.base.CommServerWrapper
import com.thoughtworks.cconn.comm.base.Msg
import com.thoughtworks.cconn.comm.base.pubsub.ServerCommPubSubManager
import com.thoughtworks.cconn.comm.bluetooth.BluetoothComm
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

internal class BluetoothServer(private val context: Context) : Server {
    private var logger: Logger = DefaultLogger()

    private lateinit var bluetoothAdapter: BluetoothAdapter

    private val executor = Executors.newScheduledThreadPool(CORE_CONNECTION_COUNT)

    private var isKeepListening = false

    private var serverSocket: BluetoothServerSocket? = null

    private val serverPubSubManager = ServerCommPubSubManager(getLogger())

    private var name = BLUETOOTH_SERVER_NAME

    private var uuid = UUID.fromString(SPP_UUID)

    override fun start(configProps: Properties): Boolean {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (serverSocket != null || isKeepListening) {
            stop()
        }

        configProps.getProperty(PROPERTY_NAME)?.let {
            name = it
        }

        configProps.getProperty(PROPERTY_UUID)?.let {
            uuid = UUID.fromString(it)
        }

        serverSocket = createServerSocket()
        if (serverSocket == null) {
            return false
        }

        executor.execute {
            isKeepListening = true
            while (isKeepListening) {
                if (!bluetoothAdapter.isEnabled || serverSocket == null) {
                    logger.error("bluetoothAdapter is not enable, please turn it on")
                    SystemClock.sleep(BLUETOOTH_STATE_CHECK_TIME * SECOND_TO_MILLISECOND)
                    serverSocket = createServerSocket()
                }

                serverSocket?.apply {
                    val socket: BluetoothSocket? = try {
                        accept()
                    } catch (e: IOException) {
                        logger.error("bluetoothServerSocket's accept() method failed: ${e.message}")
                        clearClients()
                        null
                    }

                    socket?.also { clientSocket ->
                        logger.info("bluetooth connection from device ${clientSocket.remoteDevice.address} accepted")

                        CommServerWrapper(
                            CommHandler(
                                false,
                                BluetoothComm(clientSocket),
                                logger
                            )
                        ).apply {
                            commHandler.onCommCloseListener = { _, _ ->
                                serverPubSubManager.removeCommWrapper(this)
                                logger.debug("current client count = ${serverPubSubManager.clientCount()}")
                            }
                            commHandler.onMsgArrivedListener = { msg ->
                                serverPubSubManager.onServerDataArrive(this, msg)
                            }

                            serverPubSubManager.addCommWrapper(this)
                            logger.debug("current client count = ${serverPubSubManager.clientCount()}")
                            executor.execute(commHandler)
                        }
                    }
                }
            }
        }

        return true
    }

    private fun clearClients() {
        serverPubSubManager.clearAllCommWrappers()
        logger.debug("current client count = ${serverPubSubManager.clientCount()}")
    }

    override fun stop() {
        isKeepListening = false

        // close server socket
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            logger.error("bluetoothServerSocket close failed: ${e.message}")
        }

        serverSocket = null

        clearClients()

        eraseCachedData()
    }

    override fun handlePublishMessage(msg: Msg) {
        serverPubSubManager.handlePublishMsgSelf(msg)
    }

    override fun setCallback(callback: Server.Callback) {
        serverPubSubManager.serverCallback = callback
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
        this.serverPubSubManager.setLogger(logger)
    }

    private fun getLogger(): Logger {
        return logger
    }

    private fun createServerSocket(): BluetoothServerSocket? {
        if (!bluetoothAdapter.isEnabled) {
            return null
        }

        return try {
            val socket =
                bluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid)
            logger.info("bluetooth server started.")
            socket
        } catch (e: IOException) {
            logger.error("bluetoothServerSocket create (listenUsingRfcommWithServiceRecord) failed: ${e.message}")
            null
        }
    }

    private fun eraseCachedData() {
        name = BLUETOOTH_SERVER_NAME
        uuid = UUID.fromString(SPP_UUID)
        serverSocket = null
        isKeepListening = false
    }

    companion object {
        const val BLUETOOTH_SERVER_NAME = "BLINDHMI_BLUETOOTH_SERVER"
        const val SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
        const val CORE_CONNECTION_COUNT = 5
        const val PROPERTY_NAME = "name"
        const val PROPERTY_UUID = "uuid"
        const val BLUETOOTH_STATE_CHECK_TIME = 16
        const val SECOND_TO_MILLISECOND = 1000L
    }
}