/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.mqtt.client

internal data class MqttQueueMessage(
    val topic: String? = null,
    val message: ByteArray,
    val qos: Int = 0
)