/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.wake

import android.content.Context
import android.os.PowerManager

internal class WakeLockManager(context: Context) : WakeUpManager {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock

    init {
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
    }

    override fun start() {
        wakeLock.acquire()
    }

    override fun stop() {
        wakeLock.release()
    }

    companion object {
        const val WAKELOCK_TAG = "BLINDHMI_CONNECTION:WAKELOCKMANAGER"
    }
}