package com.thoughtworks.cconn.network.udp.register

import android.content.Context
import com.thoughtworks.cconn.definitions.PropKeys
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.network.NetworkRegister
import com.thoughtworks.cconn.network.udp.BroadcastHeader
import com.thoughtworks.cconn.utils.getBroadcastAddress
import com.thoughtworks.cconn.utils.getLocalIpAddress
import com.thoughtworks.cconn.utils.ipv4StringToInt
import com.thoughtworks.cconn.utils.toHexString
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*

class UdpRegister(private val context: Context) : NetworkRegister {
    private var logger: Logger = DefaultLogger()
    private var isSendBroadcast: Boolean = false
    private var broadcastInterval = 0
    private var serverIp = 0
    private var serverPort = 0
    private var broadcastPort = 0
    private var flag: Int = 0
    private var data: ByteArray? = null
    private var debugMode: Boolean = false
    private var datagramSocket: DatagramSocket? = null

    private fun startUdpBroadCast() {
        datagramSocket = DatagramSocket(null)
        datagramSocket?.reuseAddress = true
        datagramSocket?.let { udpSocket ->
            udpSocket.broadcast = true
            isSendBroadcast = true
            Thread {
                while (isSendBroadcast) {
                    var bytes = buildBroadcastHeader()
                    data?.let {
                        bytes += it
                    }

                    if (debugMode) {
                        logger.debug("Send broadcast (len=${bytes.size}): ${bytes.toHexString()}")
                    }

                    val packet = DatagramPacket(bytes, bytes.size).apply {
                        address = getBroadcastAddress(context)
                        port = broadcastPort
                    }
                    try {
                        udpSocket.send(packet)
                    } catch (e: IOException) {
                        e.message?.let { logger.warn(it) }
                    }

                    Thread.sleep(broadcastInterval.toLong())
                }
            }.start()
        }
    }

    private fun buildBroadcastHeader(): ByteArray {
        val broadcastHeader = BroadcastHeader()
        broadcastHeader.flag = flag
        broadcastHeader.ip = serverIp
        broadcastHeader.port = serverPort.toShort()
        broadcastHeader.dataLen = (data?.size ?: 0).toShort()

        return broadcastHeader.toByteArray()
    }

    override fun register(configProps: Properties) {
        broadcastPort = configProps[PropKeys.PROP_BROADCAST_PORT]?.toString()?.toInt()
            ?: DEFAULT_BROADCAST_PORT

        broadcastInterval = configProps[PropKeys.PROP_BROADCAST_INTERVAL]?.toString()?.toInt()
            ?: DEFAULT_BROADCAST_INTERVAL

        flag = configProps[PropKeys.PROP_FLAG]?.toString()?.toLong()?.toInt() ?: DEFAULT_BROADCAST_FLAG

        val strIp = (configProps[PropKeys.PROP_SERVER_IP] ?: getLocalIpAddress()) as String?
        strIp?.let {
            serverIp = ipv4StringToInt(it)
        }

        serverPort = configProps[PropKeys.PROP_SERVER_PORT]?.toString()?.toInt() ?: 0

        configProps[PropKeys.PROP_BROADCAST_DATA]?.let {
            this.data = it as ByteArray
        }

        this.debugMode = configProps[PropKeys.PROP_BROADCAST_DEBUG_MODE]?.toString()?.toBoolean() ?: false

        startUdpBroadCast()
    }

    override fun unregister() {
        isSendBroadcast = false
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
    }

    companion object {
        private const val DEFAULT_BROADCAST_PORT = 12000
        private const val DEFAULT_BROADCAST_INTERVAL = 10000
        private const val DEFAULT_BROADCAST_FLAG = 0xFFFEC1E5.toInt()
    }
}