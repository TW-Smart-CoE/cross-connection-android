/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.detect.nsd.detector

import android.content.Context
import android.net.nsd.NsdManager
import com.thoughtworks.cconn.log.DefaultLogger
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.detect.NetworkDetector
import com.thoughtworks.cconn.detect.OnFoundService
import com.thoughtworks.cconn.detect.nsd.definitions.TCP_SERVICE_TYPE
import java.util.*

internal class NSDNetworkDetector(
    context: Context
) : NetworkDetector {
    private var nsdManager: NsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    private var nsdDiscovery: NSDDiscovery? = null

    private var logger: Logger = DefaultLogger()

    override fun startDiscover(configProps: Properties, onFoundService: OnFoundService) {
        stopDiscover()

        nsdDiscovery = NSDDiscovery(nsdManager, onFoundService)
        nsdDiscovery?.let {
            nsdManager.discoverServices(
                TCP_SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                it.nsdDiscoveryListener
            )
        }
    }

    override fun stopDiscover() {
        try {
            nsdDiscovery?.let {
                nsdManager.stopServiceDiscovery(it.nsdDiscoveryListener)
            }
        } catch (t: Throwable) {
            // swallow exception
        }
    }

    override fun setLogger(logger: Logger) {
        this.logger = logger
    }
}