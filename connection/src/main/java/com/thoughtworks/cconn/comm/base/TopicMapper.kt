/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.base

import com.thoughtworks.cconn.Method
import java.util.*

internal object TopicMapper {
    fun toFullTopic(appTopic: String, method: Method): String {
        return "$appTopic/${method.toString().lowercase(Locale.getDefault())}"
    }

    fun toAppTopic(fullTopic: String): Pair<String, Method> {
        return fullTopic.split("/").let {
            Pair(
                it.subList(0, it.lastIndex).joinToString("/"),
                Method.valueOf(it.last().uppercase(Locale.getDefault()))
            )
        }
    }
}