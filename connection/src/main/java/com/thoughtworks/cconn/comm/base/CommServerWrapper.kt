package com.thoughtworks.cconn.comm.base

import com.thoughtworks.cconn.utils.TopicUtils

internal class CommServerWrapper(val commHandler: CommHandler) {
    private val subscribeTopics = mutableSetOf<String>()

    fun subscribe(topic: String) {
        if (!subscribeTopics.contains(topic)) {
            subscribeTopics.add(topic)
        }
    }

    fun unsubscribe(topic: String) {
        subscribeTopics.remove(topic)
    }

    fun isSubscribed(topic: String): Boolean {
        subscribeTopics.forEach {
            if (TopicUtils.isTopicMatch(it, topic)) {
                return true
            }
        }

        return false
    }

    fun clear() {
        subscribeTopics.clear()
    }
}