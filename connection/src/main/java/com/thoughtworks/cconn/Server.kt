/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn

import com.thoughtworks.cconn.comm.base.Msg
import java.util.*

/**
 * Server
 */
internal interface Server : Module {
    interface Callback {
        fun onSubscribe(fullTopic: String)
        fun onUnSubscribe(fullTopic: String)
        fun onPublish(msg: Msg)
    }

    /**
     * Start server
     *
     * @param configProps configs of server, key-value format
     * @return start server result, success/failure.
     */
    fun start(configProps: Properties): Boolean

    /**
     * Stop server
     *
     */
    fun stop()

    fun handlePublishMessage(msg: Msg)

    fun setCallback(callback: Callback)
}