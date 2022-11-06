/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.detect

import com.thoughtworks.cconn.Module
import java.util.*

/**
 * Server found callback
 */
typealias OnFoundService = (serviceInfo: Properties) -> Unit

/**
 * Network detector
 */
interface NetworkDetector : Module {
    /*
    *
     * Start server discover
     *
     * @param configProps configs of NetworkDetector, key-value format
     * @param onFoundService found server callback
     */
    fun startDiscover(configProps: Properties, onFoundService: OnFoundService)

    /**
     * Stop server discover
     *
     */
    fun stopDiscover()
}