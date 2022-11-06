/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn

import android.content.Context
import com.thoughtworks.cconn.bus.Bus
import com.thoughtworks.cconn.bus.CrossNetworkBus
import com.thoughtworks.cconn.comm.bluetooth.client.BluetoothClient
import com.thoughtworks.cconn.comm.tcp.client.TcpClient
import com.thoughtworks.cconn.detect.NetworkDetector
import com.thoughtworks.cconn.detect.NetworkRegister
import com.thoughtworks.cconn.detect.bluetooth.detector.BluetoothDetector
import com.thoughtworks.cconn.detect.bluetooth.register.BluetoothRegister
import com.thoughtworks.cconn.detect.nsd.detector.NSDNetworkDetector
import com.thoughtworks.cconn.detect.nsd.register.NSDNetworkRegister
import com.thoughtworks.cconn.detect.udp.detector.UdpDetector
import com.thoughtworks.cconn.detect.udp.register.UdpRegister

/**
 * Connection type
 */
enum class ConnectionType {
    BLUETOOTH,
    TCP,
}

enum class NetworkDiscoveryType {
    BLUETOOTH,
    NSD,
    UDP
}

/**
 * Connection factory create connection modules.
 */
object ConnectionFactory {
    /**
     * Create connection client
     *
     * @param context android context
     * @param connectionType connection type
     * @return connection client
     */
    fun createConnection(context: Context, connectionType: ConnectionType): Connection =
        when (connectionType) {
            ConnectionType.BLUETOOTH -> BluetoothClient(context)
            ConnectionType.TCP -> TcpClient(context)
        }

    /**
     * Create register, used on server side. Which register server information (such as IP address, MAC ...)
     *
     * @param context android context
     * @param connectionType connectionType
     * @return network register
     */
    fun createRegister(
        context: Context,
        networkDiscoveryType: NetworkDiscoveryType
    ): NetworkRegister =
        when (networkDiscoveryType) {
            NetworkDiscoveryType.BLUETOOTH -> BluetoothRegister(context)
            NetworkDiscoveryType.NSD -> NSDNetworkRegister(context)
            NetworkDiscoveryType.UDP -> UdpRegister(context)
        }

    /**
     * Create detector, used on client side. Which detect server information (such as IP address, MAC ...)
     *
     * @param context android context
     * @param connectionType connection type
     * @return network detector
     */
    fun createDetector(
        context: Context,
        networkDiscoveryType: NetworkDiscoveryType
    ): NetworkDetector =
        when (networkDiscoveryType) {
            NetworkDiscoveryType.BLUETOOTH -> BluetoothDetector(context)
            NetworkDiscoveryType.NSD -> NSDNetworkDetector(context)
            NetworkDiscoveryType.UDP -> UdpDetector()
        }

    fun createBus(context: Context): Bus {
        return CrossNetworkBus(context)
    }
}
