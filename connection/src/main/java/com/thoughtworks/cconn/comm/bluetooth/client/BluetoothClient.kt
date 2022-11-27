/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.bluetooth.client

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.SystemClock
import com.thoughtworks.cconn.Connection
import com.thoughtworks.cconn.ConnectionState
import com.thoughtworks.cconn.Method
import com.thoughtworks.cconn.OnActionListener
import com.thoughtworks.cconn.OnConnectionStateChangeListener
import com.thoughtworks.cconn.OnDataListener
import com.thoughtworks.cconn.comm.base.CommHandler
import com.thoughtworks.cconn.comm.base.MSG_TYPE_PUBLISH
import com.thoughtworks.cconn.comm.base.MSG_TYPE_SUBSCRIBE
import com.thoughtworks.cconn.comm.base.MSG_TYPE_UNSUBSCRIBE
import com.thoughtworks.cconn.comm.base.Msg
import com.thoughtworks.cconn.comm.base.MsgHeader
import com.thoughtworks.cconn.comm.base.MsgType
import com.thoughtworks.cconn.comm.base.TopicMapper
import com.thoughtworks.cconn.comm.base.byteToMsgType
import com.thoughtworks.cconn.comm.base.calcCheckSum
import com.thoughtworks.cconn.comm.base.methodToByte
import com.thoughtworks.cconn.comm.base.msgFlag
import com.thoughtworks.cconn.comm.base.pubsub.ClientCommPubSubManager
import com.thoughtworks.cconn.comm.base.pubsub.Subscription
import com.thoughtworks.cconn.comm.bluetooth.BluetoothComm
import com.thoughtworks.cconn.comm.bluetooth.server.BluetoothServer
import com.thoughtworks.cconn.comm.thread.AndroidHandlerThread
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.utils.DataConverter
import com.thoughtworks.cconn.utils.toBoolean
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import kotlin.math.min

internal class BluetoothClient(private val context: Context) : Connection {
    private var uuid = BluetoothServer.SPP_UUID
    private var address = ""
    private var autoReconnect = false
    private val minReconnectRetryTime = 4
    private var maxReconnectRetryTime = 32
    private var currentReconnectRetryTime = minReconnectRetryTime

    private var isInit = false
    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val onConnectionStateChangedListenerList =
        CopyOnWriteArrayList<OnConnectionStateChangeListener>()

    private lateinit var commHandler: CommHandler
    private var logger: Logger = DefaultLogger()
    private val subscribeManager = ClientCommPubSubManager(logger)
    private val executor = Executors.newCachedThreadPool()

    private val thread = AndroidHandlerThread("bluetooth client thread")

    override fun init(configProps: Properties) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            logger.error("bluetooth state = ${bluetoothAdapter.state}")
            return
        }

        bluetoothDevice = try {
            address = configProps.getProperty(PROPERTY_ADDRESS)
            bluetoothAdapter.getRemoteDevice(address)
        } catch (t: Throwable) {
            logger.error("bluetooth getRemoteDevice failed: ${t.message}")
            return
        }

        uuid = BluetoothServer.SPP_UUID
        configProps.getProperty(PROPERTY_UUID)?.let {
            uuid = it
        }

        configProps.getProperty(PROPERTY_AUTO_RECONNECT)?.let {
            autoReconnect = it.toBoolean()
        }

        configProps.getProperty(PROPERTY_MAX_RECONNECT_RETRY_TIME)?.let {
            maxReconnectRetryTime = it.toInt()
            if (maxReconnectRetryTime < minReconnectRetryTime) {
                maxReconnectRetryTime = minReconnectRetryTime
            }
        }

        currentReconnectRetryTime = minReconnectRetryTime

        try {
            bluetoothSocket =
                bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
            bluetoothConnect()
            isInit = true
        } catch (e: IOException) {
            logger.error("bluetoothSocket init failed: ${e.message}")
            return
        }
    }

    override fun addOnConnectionStateChangedListener(onConnectionStateChangeListener: OnConnectionStateChangeListener) {
        onConnectionStateChangedListenerList.addIfAbsent(onConnectionStateChangeListener)
    }

    override fun removeOnConnectionStateChangedListener(onConnectionStateChangeListener: OnConnectionStateChangeListener) {
        onConnectionStateChangedListenerList.remove(onConnectionStateChangeListener)
    }

    override fun close() {
        thread.execute {
            if (!isInit) {
                return@execute
            }

            try {
                commHandler.close()
            } catch (e: IOException) {
                // swallow exceptions
            }

            subscribeManager.clear()
            eraseCachedData()
        }
    }

    override fun getState(): ConnectionState {
        return connectionState
    }

    override fun publish(
        topic: String,
        method: Method,
        data: ByteArray,
        onActionListener: OnActionListener?
    ) {
        if (!isInit) {
            onActionListener?.onFailure(Exception("BluetoothClient not init"))
            return
        }

        thread.execute {
            val fullTopic = TopicMapper.toFullTopic(topic, method)
            val fullTopicBytes = DataConverter.stringToByteArray(fullTopic)
            try {
                val msg = Msg(
                    MsgHeader(
                        msgFlag(),
                        MSG_TYPE_PUBLISH,
                        methodToByte(method),
                        fullTopicBytes.size.toUShort(),
                        data.size.toUShort()
                    ),
                    fullTopicBytes,
                    data
                )
                msg.header.checkSum = msg.calcCheckSum()
                commHandler.send(msg)

                onActionListener?.onSuccess()
            } catch (e: IOException) {
                logger.error("Error occurred when sending data: ${e.message}")
                onActionListener?.onFailure(e)
            }
        }
    }

    override fun subscribe(
        topic: String,
        method: Method,
        onDataListener: OnDataListener?,
        onActionListener: OnActionListener?
    ) {
        if (!isInit) {
            onActionListener?.onFailure(Exception("BluetoothClient not init"))
        }

        thread.execute {
            val fullTopic = TopicMapper.toFullTopic(topic, method)
            val fullTopicBytes = DataConverter.stringToByteArray(fullTopic)
            try {
                val msg = Msg(
                    MsgHeader(
                        msgFlag(),
                        MSG_TYPE_SUBSCRIBE,
                        methodToByte(method),
                        fullTopicBytes.size.toUShort()
                    ),
                    fullTopicBytes
                )
                msg.header.checkSum = msg.calcCheckSum()
                commHandler.send(msg)

                onActionListener?.onSuccess()
                subscribeManager.subscribe(Subscription(fullTopic, onDataListener))
            } catch (e: IOException) {
                logger.error("Error occurred when sending data: ${e.message}")
                onActionListener?.onFailure(e)
            }
        }
    }

    override fun unSubscribe(topic: String, method: Method) {
        if (!isInit) {
            return
        }

        thread.execute {
            val fullTopic = TopicMapper.toFullTopic(topic, method)
            val fullTopicBytes = DataConverter.stringToByteArray(fullTopic)
            try {
                val msg = Msg(
                    MsgHeader(
                        msgFlag(),
                        MSG_TYPE_UNSUBSCRIBE,
                        methodToByte(method),
                        fullTopicBytes.size.toUShort()
                    ),
                    fullTopicBytes
                )
                msg.header.checkSum = msg.calcCheckSum()
                commHandler.send(msg)
                subscribeManager.unsubscribe(fullTopic)
            } catch (e: IOException) {
                logger.error("Error occurred when sending data: ${e.message}")
            }
        }
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
        this.subscribeManager.setLogger(logger)
    }

    private fun bluetoothConnect() {
        thread.execute {
            subscribeManager.clear()

            commHandler =
                CommHandler(
                    true,
                    BluetoothComm(bluetoothSocket),
                    logger
                ).apply {
                    onCommCloseListener = { _, isPassive ->
                        if (isPassive && autoReconnect) {
                            scheduleReconnect()
                        }
                    }
                    onMsgArrivedListener = {
                        onMsgArrived(it)
                    }
                    onConnectionStateChangeListener = object : OnConnectionStateChangeListener {
                        override fun onConnectionStateChanged(
                            state: ConnectionState,
                            throwable: Throwable?
                        ) {
                            changeConnectionState(state, throwable)
                        }
                    }
                }

            executor.execute(commHandler)
        }
    }

    private fun onMsgArrived(msg: Msg) {
        if (byteToMsgType(msg.header.type) != MsgType.PUBLISH) {
            return
        }

        subscribeManager.invokeMatchedCallback(
            DataConverter.byteArrayToString(msg.topic),
            msg.data
        )
    }

    private fun scheduleReconnect() {
        thread.execute {
            changeConnectionState(ConnectionState.RECONNECTING)

            logger.info(
                "schedule bluetooth reconnect attempt in $currentReconnectRetryTime seconds."
            )

            SystemClock.sleep(currentReconnectRetryTime * SECOND_TO_MILLISECOND)
            currentReconnectRetryTime =
                min(currentReconnectRetryTime * 2, maxReconnectRetryTime)

            if (!bluetoothAdapter.isEnabled) {
                logger.error("bluetooth state = ${bluetoothAdapter.state}")
                scheduleReconnect()
                return@execute
            }

            logger.error("bluetooth do reconnecting ...")
            try {
                bluetoothSocket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid))
            } catch (e: IOException) {
                logger.error("bluetoothSocket reconnect failed: ${e.message}")
                scheduleReconnect()
                return@execute
            }

            bluetoothConnect()
        }
    }

    private fun eraseCachedData() {
        address = ""
        autoReconnect = false
        BluetoothServer.SPP_UUID
        isInit = false
    }

    private fun changeConnectionState(state: ConnectionState, throwable: Throwable? = null) {
        this.connectionState = state
        if (connectionState == ConnectionState.CONNECTED) {
            currentReconnectRetryTime = minReconnectRetryTime
        }

        onConnectionStateChangedListenerList.forEach {
            it.onConnectionStateChanged(state, throwable)
        }
    }

    companion object {
        const val PROPERTY_ADDRESS = "address"
        const val PROPERTY_UUID = "uuid"
        const val PROPERTY_AUTO_RECONNECT = "auto_reconnect"
        const val PROPERTY_MAX_RECONNECT_RETRY_TIME = "max_reconnect_retry_time"
        const val SECOND_TO_MILLISECOND = 1000L
    }
}