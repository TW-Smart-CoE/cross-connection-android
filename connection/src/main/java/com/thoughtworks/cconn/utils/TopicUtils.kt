package com.thoughtworks.cconn.utils

internal object TopicUtils {
    fun isTopicMatch(topicFilter: String, topic: String): Boolean {
        val topicFilterTokens = topicFilter.split("/").toTypedArray()
        val topicTokens = topic.split("/").toTypedArray()
        if (topicFilterTokens.size > topicTokens.size) {
            return false
        }

        for (i in topicFilterTokens.indices) {
            val topicFilterToken = topicFilterTokens[i]
            val topicToken = topicTokens[i]

            if ("#" == topicFilterToken) {
                val filterLastToken = topicFilterTokens[topicFilterTokens.size - 1]
                val lastToken = topicTokens[topicTokens.size - 1]

                return if (filterLastToken == "#") {
                    true
                } else {
                    filterLastToken == lastToken
                }
            }

            if ("+" != topicFilterToken && topicFilterToken != topicToken) {
                return false
            }
        }

        return topicFilterTokens.size == topicTokens.size
    }
}