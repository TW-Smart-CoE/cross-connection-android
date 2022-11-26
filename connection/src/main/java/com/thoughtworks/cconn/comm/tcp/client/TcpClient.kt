/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.tcp.client

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
import com.thoughtworks.cconn.comm.base.methodToByte
import com.thoughtworks.cconn.comm.base.msgFlag
import com.thoughtworks.cconn.comm.base.pubsub.ClientCommPubSubManager
import com.thoughtworks.cconn.comm.base.pubsub.Subscription
import com.thoughtworks.cconn.comm.tcp.TcpComm
import com.thoughtworks.cconn.comm.thread.AndroidHandlerThread
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.utils.DataConverter
import com.thoughtworks.cconn.utils.toBoolean
import java.io.IOException
import java.net.Socket
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import kotlin.math.min

internal class TcpClient(private val context: Context) : Connection {
    private var address = ""
    private var port = PROPERTY_PORT_DEFAULT
    private var autoReconnect = false
    private var minReconnectRetryTime = PROPERTY_MIN_RECONNECT_RETRY_TIME_DEFAULT
    private var maxReconnectRetryTime = PROPERTY_MAX_RECONNECT_RETRY_TIME_DEFAULT
    private var currentReconnectRetryTime = minReconnectRetryTime

    private var isInit = false
    private var connectionState: ConnectionState = ConnectionState.DISCONNECTED
    private val executor = Executors.newSingleThreadExecutor()
    private val onConnectionStateChangedListenerList =
        CopyOnWriteArrayList<OnConnectionStateChangeListener>()

    private var tcpSocket: Socket? = null
    private var commHandler: CommHandler? = null

    private var logger: Logger = DefaultLogger()
    private val subscribeManager = ClientCommPubSubManager(logger)

    private val thread = AndroidHandlerThread("tcp client thread")

    override fun init(configProps: Properties) {
        address = configProps[PROPERTY_IP]?.toString() ?: ""
        port = configProps[PROPERTY_PORT]?.toString()?.toInt() ?: PROPERTY_PORT_DEFAULT
        autoReconnect = configProps[PROPERTY_AUTO_RECONNECT]?.toString()?.toBoolean() ?: false
        minReconnectRetryTime =
            configProps[PROPERTY_MIN_RECONNECT_RETRY_TIME]?.toString()?.toInt()
                ?: PROPERTY_MIN_RECONNECT_RETRY_TIME_DEFAULT
        maxReconnectRetryTime = configProps[PROPERTY_MAX_RECONNECT_RETRY_TIME]?.toString()?.toInt()
            ?: PROPERTY_MAX_RECONNECT_RETRY_TIME_DEFAULT
        currentReconnectRetryTime = min(maxReconnectRetryTime, minReconnectRetryTime)

        tcpConnect()
        isInit = true
    }

    override fun close() {
        thread.execute {
            commHandler?.close()
            subscribeManager.clear()

            address = ""
            port = PROPERTY_PORT_DEFAULT
            autoReconnect = false
            isInit = false
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
            onActionListener?.onFailure(Exception("TcpClient not init"))
            return
        }

        thread.execute {
            val fullTopic = TopicMapper.toFullTopic(topic, method)
            val fullTopicBytes = DataConverter.stringToByteArray(fullTopic)
            try {
                commHandler?.send(
                    Msg(
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
                )

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
            onActionListener?.onFailure(Exception("TcpClient not init"))
            return
        }

        thread.execute {
            val fullTopic = TopicMapper.toFullTopic(topic, method)
            val fullTopicBytes = DataConverter.stringToByteArray(fullTopic)
            try {
                commHandler?.send(
                    Msg(
                        MsgHeader(
                            msgFlag(),
                            MSG_TYPE_SUBSCRIBE,
                            methodToByte(method),
                            fullTopicBytes.size.toUShort()
                        ),
                        fullTopicBytes
                    )
                )

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
                commHandler?.send(
                    Msg(
                        MsgHeader(
                            msgFlag(),
                            MSG_TYPE_UNSUBSCRIBE,
                            methodToByte(method),
                            fullTopicBytes.size.toUShort()
                        ),
                        fullTopicBytes
                    )
                )

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

    override fun addOnConnectionStateChangedListener(
        onConnectionStateChangeListener: OnConnectionStateChangeListener
    ) {
        onConnectionStateChangedListenerList.addIfAbsent(onConnectionStateChangeListener)
    }

    override fun removeOnConnectionStateChangedListener(
        onConnectionStateChangeListener: OnConnectionStateChangeListener
    ) {
        onConnectionStateChangedListenerList.remove(onConnectionStateChangeListener)
    }

    private fun tcpConnect() {
        thread.execute {
            subscribeManager.clear()

            val tcpSocket = Socket()
            this.tcpSocket = tcpSocket
            commHandler = CommHandler(
                true,
                TcpComm(tcpSocket, address, port),
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

    private fun scheduleReconnect() {
        thread.execute {
            changeConnectionState(ConnectionState.RECONNECTING)
            logger.info("schedule tcp reconnect attempt in $currentReconnectRetryTime seconds.")

            SystemClock.sleep(currentReconnectRetryTime * SECOND_TO_MILLISECOND)
            currentReconnectRetryTime = min(currentReconnectRetryTime * 2, maxReconnectRetryTime)

            logger.error("tcp do reconnecting ...")
            tcpConnect()
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
        const val PROPERTY_IP = "ip"
        const val PROPERTY_PORT = "port"
        const val PROPERTY_AUTO_RECONNECT = "auto_reconnect"
        const val PROPERTY_MIN_RECONNECT_RETRY_TIME = "min_reconnect_retry_time"
        const val PROPERTY_MAX_RECONNECT_RETRY_TIME = "max_reconnect_retry_time"

        const val PROPERTY_PORT_DEFAULT = 8884
        const val PROPERTY_MIN_RECONNECT_RETRY_TIME_DEFAULT = 4
        const val PROPERTY_MAX_RECONNECT_RETRY_TIME_DEFAULT = 32
        const val SECOND_TO_MILLISECOND = 1000L
    }
}