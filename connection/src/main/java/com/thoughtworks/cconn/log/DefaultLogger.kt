/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.log

import android.util.Log
import com.thoughtworks.cconn.definitions.BLINDHMI_CONNECTION_TAG

internal class DefaultLogger : Logger {
    override fun verbose(message: String) {
        Log.v(BLINDHMI_CONNECTION_TAG, message)
    }

    override fun debug(message: String) {
        Log.d(BLINDHMI_CONNECTION_TAG, message)
    }

    override fun info(message: String) {
        Log.i(BLINDHMI_CONNECTION_TAG, message)
    }

    override fun warn(message: String) {
        Log.w(BLINDHMI_CONNECTION_TAG, message)
    }

    override fun error(message: String) {
        Log.e(BLINDHMI_CONNECTION_TAG, message)
    }

    override fun wtf(message: String) {
        Log.wtf(BLINDHMI_CONNECTION_TAG, message)
    }
}