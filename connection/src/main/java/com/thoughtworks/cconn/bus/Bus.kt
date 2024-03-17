package com.thoughtworks.cconn.bus

import com.thoughtworks.cconn.ConnectionType
import com.thoughtworks.cconn.Module
import java.util.Properties

interface Bus: Module {
    fun initialize(): Boolean
    fun start(connectionType: ConnectionType, serverConfig: Properties, networkRegisterConfig: Properties): Boolean
    fun resetRegister(connectionType: ConnectionType, networkRegisterConfig: Properties): Boolean
    fun stopAll()
    fun cleanup()
}