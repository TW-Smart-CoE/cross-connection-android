/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn

import java.util.*

/**
 * Connection message types
 */
enum class Method {
    /**
     * Report value periodically or report for value update.
     */
    REPORT,

    /**
     * Query value, always followed with a REPLY message. (read-only)
     */
    QUERY,

    /**
     * Reply value to query.
     */
    REPLY,

    /**
     * Rend a request, always followed with a response message. (write)
     */
    REQUEST,

    /**
     * Response to request.
     */
    RESPONSE,
}

/**
 * Connection state
 *
 * @constructor Create empty Connection state
 */
enum class ConnectionState {
    /**
     * Connecting
     */
    CONNECTING,

    /**
     * Connected
     */
    CONNECTED,

    /**
     * Disconnected
     */
    DISCONNECTED,

    /**
     * Reconnecting
     */
    RECONNECTING,
}

/**
 * On message data arrived listener
 */
typealias OnDataListener = (topic: String, method: Method, data: ByteArray) -> Unit

/**
 * On communication(publish/subscribe) success/failure action listener
 */
interface OnActionListener {
    /**
     * Communication success
     */
    fun onSuccess()

    /**
     * Communication failure
     *
     * @param throwable failure reason
     */
    fun onFailure(throwable: Throwable)
}

/**
 * On connection state change listener
 */
interface OnConnectionStateChangeListener {
    /**
     * On connection state changed
     *
     * @param state connection state
     * @param throwable exception which cause Reconnecting/Disconnected state.
     */
    fun onConnectionStateChanged(state: ConnectionState, throwable: Throwable? = null)
}


/**
 * Connection client
 *
 * @constructor Create empty Connection
 */
interface Connection : Module {
    /**
     * Add on connection state change listener
     *
     * @param onConnectionStateChangeListener connection state changed listener
     */
    fun addOnConnectionStateChangedListener(onConnectionStateChangeListener: OnConnectionStateChangeListener)

    /**
     * Remove on connection state changed listener
     *
     * @param onConnectionStateChangeListener connection state changed listener
     */
    fun removeOnConnectionStateChangedListener(onConnectionStateChangeListener: OnConnectionStateChangeListener)

    /**
     * Init
     *
     * @param configProps configs of connection, key-value format
     */
    @Throws(Exception::class)
    fun start(
        configProps: Properties)

    /**
     * Close connection
     */
    fun close()

    /**
     *  Get connection state
     */
    fun getState(): ConnectionState

    /**
     * Publish message
     *
     * @param topic message topic (same rule as mqtt protocol)
     * @param method message type
     * @param data message content
     * @param onActionListener communication event listener
     */
    @Throws(Exception::class)
    fun publish(
        topic: String,
        method: Method,
        data: ByteArray,
        onActionListener: OnActionListener? = null
    )

    /**
     * Subscribe message
     *
     * @param topic message topic (same rule as mqtt protocol)
     * @param method message type
     * @param onDataListener message arrive listener
     * @param onActionListener communication event listener
     */
    @Throws(Exception::class)
    fun subscribe(
        topic: String,
        method: Method,
        onDataListener: OnDataListener? = null,
        onActionListener: OnActionListener? = null
    )

    /**
     * Unsubscribe
     *
     * @param topic message topic (same rule as mqtt protocol)
     * @param method message type
     */
    @Throws(Exception::class)
    fun unsubscribe(topic: String, method: Method)
}