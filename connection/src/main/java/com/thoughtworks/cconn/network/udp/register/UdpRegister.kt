package com.thoughtworks.cconn.network.udp.register

import android.content.Context
import com.thoughtworks.cconn.definitions.PropKeys
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.network.NetworkRegister
import com.thoughtworks.cconn.network.udp.BroadcastMsg
import com.thoughtworks.cconn.utils.getBroadcastAddress
import com.thoughtworks.cconn.utils.getLocalIpAddress
import com.thoughtworks.cconn.utils.ipv4StringToInt
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class UdpRegister(private val context: Context) : NetworkRegister {
    private var logger: Logger = DefaultLogger()
    private var isSendBroadcast: Boolean = false
    private var broadcastInterval = 0
    private var serverIp = 0
    private var serverPort = 0
    private var broadcastPort = 0
    private var flag: Int = 0

    private var datagramSocket: DatagramSocket? = null

    private fun startUdpBroadCast() {
        datagramSocket =
            DatagramSocket(broadcastPort, InetAddress.getByName(ANY_ADDRESS))
        datagramSocket?.let { udpSocket ->
            udpSocket.broadcast = true
            isSendBroadcast = true
            Thread {
                while (isSendBroadcast) {
                    val bytes = buildBroadcastMsg()
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

    private fun buildBroadcastMsg(): ByteArray {
        val broadcastMsg = BroadcastMsg()
        broadcastMsg.flag = flag
        broadcastMsg.ip = serverIp
        broadcastMsg.port = serverPort.toShort()

        return broadcastMsg.toByteArray()
    }

    override fun register(configProps: Properties) {
        broadcastPort = configProps[PropKeys.PROP_UDP_REGISTER_BROADCAST_PORT]?.toString()?.toInt()
            ?: DEFAULT_BROADCAST_PORT
        broadcastInterval = configProps[PropKeys.PROP_UDP_REGISTER_BROADCAST_INTERVAL]?.toString()?.toInt()
            ?: DEFAULT_BROADCAST_INTERVAL

        flag = configProps[PropKeys.PROP_UDP_REGISTER_FLAG]?.toString()?.toInt() ?: DEFAULT_BROADCAST_FLAG

        val strIp = (configProps[PropKeys.PROP_UDP_REGISTER_SERVER_IP] ?: getLocalIpAddress()) as String?
        strIp?.let {
            serverIp = ipv4StringToInt(it)
        }

        serverPort = configProps[PropKeys.PROP_UDP_REGISTER_SERVER_PORT]?.toString()?.toInt() ?: 0

        startUdpBroadCast()
    }

    override fun unregister() {
        isSendBroadcast = false
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
    }

    companion object {
        private const val ANY_ADDRESS = "0.0.0.0"
        private const val DEFAULT_BROADCAST_PORT = 12000
        private const val DEFAULT_BROADCAST_INTERVAL = 10000
        private const val DEFAULT_BROADCAST_FLAG = 0xFFFEC1E5.toInt()
    }
}