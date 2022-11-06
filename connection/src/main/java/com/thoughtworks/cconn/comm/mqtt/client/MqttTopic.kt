/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.comm.mqtt.client

import com.thoughtworks.cconn.OnDataListener

internal data class MqttTopic(val topic: String, val qos: Int, var callback: OnDataListener?)