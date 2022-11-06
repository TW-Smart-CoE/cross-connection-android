package com.thoughtworks.cconn.utils

import android.content.Context
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


fun intToIpv4String(ipAddress: Int): String? {
    val bytes = ByteArray(4)
    ByteUtils.putInt(bytes, 0, ipAddress)

    return InetAddress.getByAddress(bytes).hostAddress
}

fun ipv4StringToInt(ipAddress: String): Int {
    val address = Inet4Address.getByName(ipAddress)
    val bytes = address.address

    return ByteUtils.getInt(bytes, 0)
}

fun getLocalIpAddress(): String? {
    try {
        val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val intf: NetworkInterface = en.nextElement()
            val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress: InetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (ex: SocketException) {
        ex.printStackTrace()
    }

    return null
}

fun getBroadcastAddress(context: Context): InetAddress {
    val wifi: WifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val dhcp: DhcpInfo = wifi.dhcpInfo

    val broadcast = dhcp.ipAddress and dhcp.netmask or dhcp.netmask.inv()
    val quads = ByteArray(4)
    for (k in 0..3) quads[k] = (broadcast shr k * 8 and 0xFF).toByte()
    return InetAddress.getByAddress(quads)
}
