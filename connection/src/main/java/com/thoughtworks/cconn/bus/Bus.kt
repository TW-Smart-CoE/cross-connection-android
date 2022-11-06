package com.thoughtworks.cconn.bus

import com.thoughtworks.cconn.ConnectionType
import java.util.Properties

interface Bus {
    fun initialize(): Boolean
    fun start(connectionType: ConnectionType, serverConfig: Properties, networkRegisterConfig: Properties): Boolean
    fun stopAll()
}