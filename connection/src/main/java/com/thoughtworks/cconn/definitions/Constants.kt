/*
 ** Copyright 2020, 思特沃克软件技术（北京）有限公司
 **
 */

package com.thoughtworks.cconn.definitions

const val CCONN_TAG = "CCONN"
const val CCONN_TOPIC_INVALID = "invalid"

object PropKeys {
    const val PROP_IP = "ip"
    const val PROP_PORT = "port"
    const val PROP_AUTO_CONNECT = "auto_reconnect"
    const val PROP_MIN_RECONNECT_RETRY_TIME = "min_reconnect_retry_time"
    const val PROP_MAX_RECONNECT_RETRY_TIME = "max_reconnect_retry_time"

    const val PROP_UDP_DETECTOR_BROADCAST_PORT = "broadcast_port"
    const val PROP_UDP_DETECTOR_FLAG = "flag"
    const val PROP_UDP_DETECTOR_ON_FOUND_SERVICE_IP = "server_ip"
    const val PROP_UDP_DETECTOR_ON_FOUND_SERVICE_PORT = "server_port"

    const val PROP_UDP_REGISTER_BROADCAST_PORT = "broadcast_port"
    const val PROP_UDP_REGISTER_BROADCAST_INTERVAL = "broadcast_interval"
    const val PROP_UDP_REGISTER_FLAG = "flag"
    const val PROP_UDP_REGISTER_SERVER_IP = "server_ip"
    const val PROP_UDP_REGISTER_SERVER_PORT = "server_port"
}

