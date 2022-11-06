/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.detect.nsd.register

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.detect.NetworkRegister
import com.thoughtworks.cconn.detect.nsd.definitions.SERVICE_INFO_ATTRIBUTE_DEVICE
import com.thoughtworks.cconn.detect.nsd.definitions.WIFI_INTERFACE_NAME
import java.net.NetworkInterface
import java.util.*

internal class NSDNetworkRegister(private val context: Context) : NetworkRegister {
    private var type = ""
    private var name = ""
    private var port = 0

    private val nsdManager: NsdManager by lazy { context.getSystemService(Context.NSD_SERVICE) as NsdManager }

    private var registrationListener: NsdManager.RegistrationListener? = null

    private var logger: Logger = DefaultLogger()

    override fun register(configProps: Properties) {
        initializeRegistrationListener()

        type = configProps.getProperty("type")?.toString() ?: ""
        name = configProps.getProperty("name")?.toString() ?: ""
        port = configProps.getProperty("port")?.toInt() ?: 0

        registerService(type, name, port)
    }

    override fun unregister() {
        registrationListener?.let {
            nsdManager.unregisterService(it)
            registrationListener = null
        }
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
    }

    private fun initializeRegistrationListener() {
        registrationListener = object : NsdManager.RegistrationListener {
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                logger.debug("onUnregistrationFailed serviceInfo: $serviceInfo ,errorCode:$errorCode")
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                logger.debug("onServiceUnregistered serviceInfo: $serviceInfo")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                logger.error("NsdServiceInfo onRegistrationFailed")
            }

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                logger.debug("onServiceRegistered: $serviceInfo")
            }
        }
    }

    private fun registerService(type: String, serviceName: String, port: Int) {
        val serviceInfo = NsdServiceInfo()
        serviceInfo.serviceName = serviceName
        serviceInfo.setAttribute(
            SERVICE_INFO_ATTRIBUTE_DEVICE, getMACAddress(
                WIFI_INTERFACE_NAME
            )
        )
        serviceInfo.port = port
        serviceInfo.serviceType = type
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    private fun getMACAddress(interfaceName: String?): String? {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf: NetworkInterface in interfaces) {
                if (interfaceName != null) {
                    if (!intf.name.equals(interfaceName, ignoreCase = true)) continue
                }
                val mac: ByteArray = intf.hardwareAddress ?: return ""
                val buf: StringBuilder = StringBuilder()
                for (aMac: Byte in mac) buf.append(String.format("%02X:", aMac))
                if (buf.isNotEmpty()) buf.deleteCharAt(buf.length - 1)
                return buf.toString()
            }
        } catch (ignored: Exception) {
        }

        return ""
    }
}