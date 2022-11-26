package com.thoughtworks.cconn.comm.base.pubsub

import com.thoughtworks.cconn.Server
import com.thoughtworks.cconn.comm.base.CommServerWrapper
import com.thoughtworks.cconn.comm.base.Msg
import com.thoughtworks.cconn.comm.base.MsgType
import com.thoughtworks.cconn.comm.base.byteToMsgType
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.utils.DataConverter
import java.util.concurrent.CopyOnWriteArrayList

internal class ServerCommPubSubManager(private var logger: Logger) {
    private val commServerWrapperList = CopyOnWriteArrayList<CommServerWrapper>()

    fun setLogger(logger: Logger) {
        this.logger = logger
    }

    var serverCallback: Server.Callback? = null

    fun onServerDataArrive(commServerWrapper: CommServerWrapper, msg: Msg) {
        when (byteToMsgType(msg.header.type)) {
            MsgType.PUBLISH -> handlePublish(msg)
            MsgType.SUBSCRIBE -> handleSubscribe(commServerWrapper, msg)
            MsgType.UNSUBSCRIBE -> handleUnsubscribe(commServerWrapper, msg)
        }
    }

    fun addCommWrapper(commServerWrapper: CommServerWrapper) {
        commServerWrapperList.add(commServerWrapper)
    }

    fun removeCommWrapper(commServerWrapper: CommServerWrapper) {
        commServerWrapperList.remove(commServerWrapper)
    }

    fun clientCount(): Int {
        return commServerWrapperList.size
    }

    fun clearAllCommWrappers() {
        commServerWrapperList.forEach {
            it.clear()
            it.commHandler.close()
        }
        commServerWrapperList.clear()
    }

    fun handlePublishMsgSelf(msg: Msg) {
        val fullTopic = DataConverter.byteArrayToString(msg.topic)

        commServerWrapperList.forEach {
            if (it.isSubscribed(fullTopic)) {
                it.commHandler.send(msg)
            }
        }
    }

    private fun handlePublish(msg: Msg) {
        handlePublishMsgSelf(msg)

        // publish msg to bus
        serverCallback?.onPublish(msg)
    }

    private fun handleSubscribe(commServerWrapper: CommServerWrapper, msg: Msg) {
        val fullTopic = DataConverter.byteArrayToString(msg.topic)
        // subscribe topic self
        commServerWrapper.subscribe(fullTopic)

        // subscribe topic to bus
        serverCallback?.onSubscribe(fullTopic)
    }

    private fun handleUnsubscribe(commServerWrapper: CommServerWrapper, msg: Msg) {
        val fullTopic = DataConverter.byteArrayToString(msg.topic)
        // unsubscribe topic self
        commServerWrapper.unsubscribe(fullTopic)

        // unsubscribe topic from bus
        serverCallback?.onUnSubscribe(fullTopic)
    }
}