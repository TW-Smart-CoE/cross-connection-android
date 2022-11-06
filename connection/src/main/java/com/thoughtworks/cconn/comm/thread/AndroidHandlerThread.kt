/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.thread

import android.os.Handler
import android.os.HandlerThread
import android.os.Message


internal typealias AndroidHandlerThreadExecutable = () -> Unit

internal class AndroidHandlerThreadObject(val executable: AndroidHandlerThreadExecutable? = null)

internal class AndroidHandlerThread(threadName: String) : Handler.Callback {
    private val clientThread = HandlerThread(threadName)
    private val handler: Handler

    init {
        clientThread.start()
        handler = Handler(clientThread.looper, this)
    }

    fun execute(executable: AndroidHandlerThreadExecutable? = null) {
        handler.sendMessage(
            Message.obtain().apply {
                obj = AndroidHandlerThreadObject(executable)
            }
        )
    }

    override fun handleMessage(msg: Message): Boolean {
        val obj = msg.obj
        if (obj !is AndroidHandlerThreadObject) {
            return false
        }

        obj.executable?.invoke()
        return true
    }
}