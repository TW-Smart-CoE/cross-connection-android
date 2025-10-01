/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.mqtt.client

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import org.eclipse.paho.client.mqttv3.MqttPingSender
import org.eclipse.paho.client.mqttv3.internal.ClientComms

/**
 * Alarm ping sender
 *
 * @property context
 * @constructor Create empty Alarm ping sender
 */
internal class AlarmPingSender(private val context: Context) :
    MqttPingSender {
    private var comms: ClientComms? = null
    private var alarmReceiver: BroadcastReceiver? = null
    private var pendingIntent: PendingIntent? = null
    @Volatile
    private var hasStarted = false

    override fun init(comms: ClientComms) {
        this.comms = comms
        alarmReceiver = AlarmReceiver()
    }

    override fun start() {
        context.registerReceiver(
            alarmReceiver,
            IntentFilter(ALARM_ACTION)
        )
        pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ALARM_ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        schedule(comms!!.keepAlive)
        hasStarted = true
    }

    override fun stop() {
        if (hasStarted) {
            val alarmManager =
                context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent!!)
            hasStarted = false
            try {
                context.unregisterReceiver(alarmReceiver)
            } catch (e: Exception) {
            }
        }
    }

    override fun schedule(delayInMilliseconds: Long) {
        val nextAlarmInMilliseconds =
            System.currentTimeMillis() + delayInMilliseconds
        val alarmManager =
            context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        alarmManager[AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds] = pendingIntent!!
    }

    internal inner class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            comms!!.checkForActivity() ?: return
        }
    }

    companion object {
        private const val ALARM_ACTION = "com.thoughtworks.cconn.protocols.mqtt.client.AlarmPingSender"
    }
}

