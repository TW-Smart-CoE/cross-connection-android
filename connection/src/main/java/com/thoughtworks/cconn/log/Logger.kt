/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.log

/**
 * Logger
 */
interface Logger {
    /**
     * Verbose
     *
     * @param message log message
     */
    fun verbose(message: String)

    /**
     * Debug
     *
     * @param message log message
     */
    fun debug(message: String)

    /**
     * Info
     *
     * @param message log message
     */
    fun info(message: String)

    /**
     * Warn
     *
     * @param message log message
     */
    fun warn(message: String)

    /**
     * Error
     *
     * @param message log message
     */
    fun error(message: String)

    /**
     * Wtf
     *
     * @param message log message
     */
    fun wtf(message: String)
}