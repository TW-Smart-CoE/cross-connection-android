package com.thoughtworks.cconn.comm.base.pubsub

import com.thoughtworks.cconn.comm.base.TopicMapper
import com.thoughtworks.cconn.log.Logger
import com.thoughtworks.cconn.utils.TopicUtils

internal class ClientCommPubSubManager(private var logger: Logger) {
    private val subscriptionMap: MutableMap<String, Subscription> = mutableMapOf()

    fun setLogger(logger: Logger) {
        this.logger = logger
    }

    @Synchronized
    fun subscribe(subscription: Subscription) {
        subscriptionMap[subscription.topic] = subscription
    }

    @Synchronized
    fun unsubscribe(topic: String) {
        subscriptionMap.remove(topic)
    }

    @Synchronized
    fun clear() {
        subscriptionMap.clear()
    }

    fun invokeMatchedCallback(fullTopic: String, data: ByteArray) {
        subscriptionMap.forEach {
            if (TopicUtils.isTopicMatch(it.key, fullTopic)) {
                it.value.callback?.apply {
                    try {
                        val result = TopicMapper.toAppTopic(fullTopic)
                        this.invoke(result.first, result.second, data)
                    } catch (t: Throwable) {
                        logger.error(t.toString())
                    }
                }
            }
        }
    }
}