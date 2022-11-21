/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.network.nsd.detector

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.thoughtworks.cconn.definitions.CCONN_TAG
import com.thoughtworks.cconn.network.OnFoundService
import com.thoughtworks.cconn.network.nsd.definitions.TCP_SERVICE_NAME
import java.util.Properties

internal class NSDDiscovery(
    private val nsdManager: NsdManager,
    private val onFoundService: OnFoundService
) {
    var nsdDiscoveryListener: NsdManager.DiscoveryListener
    private val nsdResolveListeners = mutableListOf<NsdManager.ResolveListener>()

    init {
        nsdDiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(CCONN_TAG, "onServiceFound Info: --> $serviceInfo")
                if (serviceInfo.serviceName == TCP_SERVICE_NAME) {
                    val resolveListener = createResolveListener()
                    nsdManager.resolveService(serviceInfo, resolveListener)
                    nsdResolveListeners.add(resolveListener)
                }
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(CCONN_TAG, "onStopDiscoveryFailed errorCode: --> $errorCode")
            }

            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(CCONN_TAG, "onStartDiscoveryFailed errorCode: --> $errorCode")
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.d(CCONN_TAG, "onDiscoveryStarted")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.d(CCONN_TAG, "onDiscoveryStopped")
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                Log.e(CCONN_TAG, "onServiceLost")
            }
        }
    }

    private fun createResolveListener(): NsdManager.ResolveListener {
        return object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                Log.d(
                    CCONN_TAG,
                    "resolution : " + serviceInfo.serviceName + " \n host_from_server: " + serviceInfo.host +
                            "\n port from server: " + serviceInfo.port
                )

                resolveData(serviceInfo)
            }
        }
    }

    private fun resolveData(serviceInfo: NsdServiceInfo) {
        val address = serviceInfo.host.hostAddress
        val port = serviceInfo.port
        Log.d(CCONN_TAG, "$address:$port")

        val properties = Properties()
        properties.setProperty("address", address)
        properties.setProperty("port", port.toString())

        onFoundService(properties)
    }
}
