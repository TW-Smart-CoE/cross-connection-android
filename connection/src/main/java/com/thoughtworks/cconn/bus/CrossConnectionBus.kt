package com.thoughtworks.cconn.bus

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.thoughtworks.cconn.ConnectionFactory
import com.thoughtworks.cconn.ConnectionType
import com.thoughtworks.cconn.NetworkDiscoveryType
import com.thoughtworks.cconn.Server
import com.thoughtworks.cconn.comm.base.Msg
import com.thoughtworks.cconn.comm.bluetooth.server.BluetoothServer
import com.thoughtworks.cconn.comm.tcp.server.TcpServer
import java.util.*


internal class CrossConnectionBus(private val context: Context) : Bus {
    private val serverMap = mutableMapOf<ConnectionType, ServerStruct>()

    private var handlerThread = HandlerThread(CROSS_CONNECTION_BUS_HANDLER_THREAD_NAME)
    private lateinit var messageHandler: Handler
    private var initialized = false

    override fun initialize(): Boolean {
        if (initialized) {
            return true
        }

        createMessageProcessingThread()

        serverMap[ConnectionType.BLUETOOTH] =
            ServerStruct(
                BluetoothServer(context).apply {
                    setCallback(createServerCallback(this))
                },
                ConnectionFactory.createRegister(context, NetworkDiscoveryType.BLUETOOTH)
            )

        serverMap[ConnectionType.TCP] =
            ServerStruct(
                TcpServer(context).apply {
                    setCallback(createServerCallback(this))
                },
                ConnectionFactory.createRegister(context, NetworkDiscoveryType.UDP)
            )

        initialized = true
        return initialized
    }

    override fun start(
        connectionType: ConnectionType,
        serverConfig: Properties,
        networkRegisterConfig: Properties
    ): Boolean {
        val serverStruct = serverMap[connectionType] ?: return false

        serverStruct.let {
            val isStarted = it.server.start(serverConfig)
            if (!isStarted) {
                return false
            }

            it.register.register(networkRegisterConfig)
            return isStarted
        }
    }

    override fun stopAll() {
        serverMap.forEach { entry ->
            entry.value.register.unregister()
            entry.value.server.stop()
        }
    }

    private fun createServerCallback(server: Server) =
        object : Server.Callback {
            override fun onSubscribe(fullTopic: String) {
            }

            override fun onUnSubscribe(fullTopic: String) {
            }

            override fun onPublish(msg: Msg) {
                publishMsgToBus(msg, server)
            }
        }

    private fun createMessageProcessingThread() {
        if (!handlerThread.isAlive) {
            handlerThread.start()
            messageHandler = object : Handler(handlerThread.looper) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        MSG_PUBLISH -> {
                            val messageObjPublish = msg.obj as MessageObjPublish
                            serverMap.forEach { (_, v) ->
                                if (v.server != messageObjPublish.excludeServer) {
                                    v.server.handlePublishMessage(messageObjPublish.msg)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun publishMsgToBus(msg: Msg, excludeServer: Server) {
        messageHandler.sendMessage(Message.obtain().apply {
            what = MSG_PUBLISH
            obj = MessageObjPublish(msg, excludeServer)
        })
    }

    private data class MessageObjPublish(val msg: Msg, val excludeServer: Server)

    companion object {
        private const val CROSS_CONNECTION_BUS_HANDLER_THREAD_NAME =
            "CrossConnectionBusHandlerThread"

        private const val MSG_PUBLISH = 3
    }
}