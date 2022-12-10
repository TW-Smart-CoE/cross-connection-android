/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.log

import android.util.Log
import com.thoughtworks.cconn.definitions.Constants.CCONN_TAG

internal class DefaultLogger : Logger {
    override fun verbose(message: String) {
        Log.v(CCONN_TAG, message)
    }

    override fun debug(message: String) {
        Log.d(CCONN_TAG, message)
    }

    override fun info(message: String) {
        Log.i(CCONN_TAG, message)
    }

    override fun warn(message: String) {
        Log.w(CCONN_TAG, message)
    }

    override fun error(message: String) {
        Log.e(CCONN_TAG, message)
    }

    override fun wtf(message: String) {
        Log.wtf(CCONN_TAG, message)
    }
}