/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.network

import com.thoughtworks.cconn.Module
import java.util.*

/**
 * Network register
 */
interface NetworkRegister : Module {
    /**
     * Register server
     *
     * @param configProps configs of register, key-value format
     */
    fun register(configProps: Properties)

    /**
     * Unregister server
     */
    fun unregister()
}