package com.thoughtworks.cconn.comm.base.pubsub

import com.thoughtworks.cconn.OnDataListener

internal class Subscription(val topic: String, var callback: OnDataListener?)