/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.tcp.server

import android.content.Context
import com.thoughtworks.cconn.Server
import com.thoughtworks.cconn.comm.base.CommHandler
import com.thoughtworks.cconn.comm.base.CommServerWrapper
import com.thoughtworks.cconn.comm.base.Msg
import com.thoughtworks.cconn.comm.base.pubsub.ServerCommPubSubManager
import com.thoughtworks.cconn.comm.tcp.TcpComm
import com.thoughtworks.cconn.definitions.Constants
import com.thoughtworks.cconn.definitions.PropKeys
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import java.io.IOException
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.Executors

internal class TcpServer(private val context: Context) : Server {
    private var serverSocket: ServerSocket? = null
    private var port: Int = DEFAULT_PORT
    private var logger: Logger = DefaultLogger()
    private val serverPubSubManager = ServerCommPubSubManager(logger)
    private val executor = Executors.newScheduledThreadPool(CORE_CONNECTION_COUNT)
    private var recvBufferSize = Constants.DEFAULT_BUFFER_SIZE

    override fun start(configProps: Properties): Boolean {
        port = configProps[PropKeys.PROP_PORT]?.toString()?.toInt() ?: DEFAULT_PORT
        recvBufferSize =
            configProps[PropKeys.PROP_RECV_BUFFER_SIZE]?.toString()?.toInt()
                ?: Constants.DEFAULT_BUFFER_SIZE

        if (serverSocket != null && serverSocket?.isClosed == false) {
            stop()
        }

        serverSocket = createServerSocket()
        if (serverSocket == null) {
            return false
        }

        executor.execute {
            while (serverSocket?.isClosed == false) {
                serverSocket?.apply {
                    val clientSocket = try {
                        accept()
                    } catch (e: IOException) {
                        logger.error("tcp server socket's accept() failed: ${e.message}")
                        clearClients()
                        null
                    }
                    clientSocket?.also { socket ->
                        CommServerWrapper(
                            CommHandler(
                                false,
                                TcpComm(socket, socket.inetAddress.hostName, socket.port),
                                logger,
                                recvBufferSize = recvBufferSize,
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

    override fun stop() {
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            logger.error("tcp server socket close failed: ${e.message}")
        }

        clearClients()
        serverSocket = null
        port = DEFAULT_PORT
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

    private fun createServerSocket(): ServerSocket? {
        return try {
            val socket = ServerSocket(port)
            logger.info("tcp server started.")
            return socket
        } catch (e: IOException) {
            logger.error("tcp server socket creation failed: ${e.message}")
            null
        }
    }

    private fun clearClients() {
        serverPubSubManager.clearAllCommWrappers()
        logger.debug("current client count = ${serverPubSubManager.clientCount()}")
    }

    companion object {
        const val PROPERTY_PORT = "port"

        const val DEFAULT_PORT = 8884
        const val CORE_CONNECTION_COUNT = 10
        const val TCP_STATE_CHECK_TIME = 16
        const val SECOND_TO_MILLISECOND = 1000L
    }
}