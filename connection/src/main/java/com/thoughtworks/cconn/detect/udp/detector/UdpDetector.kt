package com.thoughtworks.cconn.detect.udp.detector

import com.thoughtworks.cconn.definitions.PROP_UDP_DETECTOR_BROADCAST_PORT
import com.thoughtworks.cconn.definitions.PROP_UDP_DETECTOR_FLAG
import com.thoughtworks.cconn.definitions.PROP_UDP_DETECTOR_ON_FOUND_SERVICE_IP
import com.thoughtworks.cconn.definitions.PROP_UDP_DETECTOR_ON_FOUND_SERVICE_PORT
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.detect.NetworkDetector
import com.thoughtworks.cconn.detect.OnFoundService
import com.thoughtworks.cconn.detect.udp.BroadcastMsg
import com.thoughtworks.cconn.utils.getInt
import com.thoughtworks.cconn.utils.intToIpv4String
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.util.*


class UdpDetector : NetworkDetector {
    private var logger: Logger = DefaultLogger()
    private var datagramSocket: DatagramSocket? = null
    private var isKeepReceiving = false
    private var broadcastPort = 0
    private var flag: Int = 0

    override fun startDiscover(configProps: Properties, onFoundService: OnFoundService) {
        broadcastPort = configProps[PROP_UDP_DETECTOR_BROADCAST_PORT]?.toString()?.toInt()
            ?: DEFAULT_BROADCAST_PORT
        flag = configProps[PROP_UDP_DETECTOR_FLAG]?.toString()?.toInt()
            ?: DEFAULT_BROADCAST_FLAG

        datagramSocket = DatagramSocket(broadcastPort, InetAddress.getByName(ANY_ADDRESS))
        datagramSocket?.broadcast = true
        isKeepReceiving = true

        Thread {
            datagramSocket?.let {
                while (isKeepReceiving && datagramSocket != null) {
                    logger.debug("Waiting for broadcast on port $broadcastPort")

                    datagramSocket?.let {
                        val buf = ByteArray(RECV_BUF_LEN)
                        val packet = DatagramPacket(buf, buf.size)
                        it.receive(packet)
                        logger.debug("Received broadcast from ${packet.address.hostAddress}:${packet.port}")

                        if (packet.length == BROADCAST_MSG_HEADER_LEN) {
                            val receiveMsgFlag = buf.getInt(0)
                            if (receiveMsgFlag == flag) {
                                val broadcastMsg = BroadcastMsg()
                                broadcastMsg.fromByteArray(buf)

                                val properties = Properties()
                                properties[PROP_UDP_DETECTOR_ON_FOUND_SERVICE_IP] = intToIpv4String(broadcastMsg.ip)
                                properties[PROP_UDP_DETECTOR_ON_FOUND_SERVICE_PORT] = broadcastMsg.port

                                onFoundService.invoke(properties)
                            }
                        }
                    }
                }
            }
        }.start()
    }

    override fun stopDiscover() {
        isKeepReceiving = false

        try {
            datagramSocket?.close()
            datagramSocket = null
        } catch (e: SocketException) {
            logger.error(e.toString())
        }
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
    }

    companion object {
        private const val ANY_ADDRESS = "0.0.0.0"
        private const val DEFAULT_BROADCAST_PORT = 12000
        private const val DEFAULT_BROADCAST_INTERVAL = 10000
        private const val DEFAULT_BROADCAST_FLAG = 0xFFFEC1E5.toInt()
        private const val BROADCAST_MSG_HEADER_LEN = 12
        private const val RECV_BUF_LEN = 32
    }
}