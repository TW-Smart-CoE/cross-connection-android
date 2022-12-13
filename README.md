# cross-connection-android

## How to import cross-connection-android into Android project

1. Add repository

settings.gradle.kts

```
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      maven { setUrl("https://jitpack.io") }
   }
}
```

settings.gradle

```
dependencyResolutionManagement {
   repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
   repositories {
      google()
      mavenCentral()
      maven { url 'https://jitpack.io' }
   }
}
```

2. Add dependency

build.gradle.kts

```
dependencies {
   implementation 'com.github.TW-Smart-CoE:cross-connection-android:0.2.0'
}
```

build.gradle

```
dependencies {
   implementation("com.github.TW-Smart-CoE:cross-connection-android:0.2.0")
}
```

## Quick start

Bus with Tcp Server

```kotlin

package com.thoughtworks.cconnapp.ui.flow.bus

import android.content.Context
import androidx.lifecycle.ViewModel
import com.thoughtworks.cconn.ConnectionFactory
import com.thoughtworks.cconn.ConnectionType
import com.thoughtworks.cconn.definitions.Constants
import com.thoughtworks.cconn.definitions.PropKeys
import com.thoughtworks.cconn.utils.getLocalIpAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

private const val DEFAULT_SERVER_PORT = "11001"
private const val DEFAULT_BROADCAST_PORT = "12000"
private const val DEFAULT_BROADCAST_INTERVAL = "3000"

data class BusUiState(
    val recvBufferSize: String = Constants.DEFAULT_BUFFER_SIZE.toString(),
    val registerFlag: String = "FFFE1234",
    val serverIp: String = getLocalIpAddress() ?: "127.0.0.1",
    val serverPort: String = DEFAULT_SERVER_PORT,
    val broadcastPort: String = DEFAULT_BROADCAST_PORT,
    val broadcastInterval: String = DEFAULT_BROADCAST_INTERVAL,
    val tcpServerStarted: Boolean = false
)

@HiltViewModel
class BusViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {
    private val _busUiState = MutableStateFlow(BusUiState())
    val busUiState: StateFlow<BusUiState>
        get() = _busUiState

    private val bus = ConnectionFactory.createBus(context)

    init {
        bus.initialize()
    }

    override fun onCleared() {
        bus.stopAll()
        super.onCleared()
    }

    fun startTcpServer() {
        val result = bus.start(
            ConnectionType.TCP,
            Properties().apply {
                this[PropKeys.PROP_PORT] = _busUiState.value.serverPort
                this[PropKeys.PROP_RECV_BUFFER_SIZE] = _busUiState.value.recvBufferSize
            },
            Properties().apply {
                this[PropKeys.PROP_FLAG] =
                    Integer.parseUnsignedInt(_busUiState.value.registerFlag, FLAG_RADIX)
                this[PropKeys.PROP_SERVER_IP] = _busUiState.value.serverIp
                this[PropKeys.PROP_SERVER_PORT] = _busUiState.value.serverPort
                this[PropKeys.PROP_BROADCAST_INTERVAL] =
                    _busUiState.value.broadcastInterval
            }
        )

        _busUiState.update {
            it.copy(tcpServerStarted = result)
        }
    }

    fun updateFlag(flag: String) {
        _busUiState.update {
            it.copy(registerFlag = flag)
        }
    }

    fun updateServerIp(text: String) {
        _busUiState.update {
            it.copy(serverIp = text)
        }
    }

    fun updateServerPort(text: String) {
        _busUiState.update {
            it.copy(serverPort = text)
        }
    }

    fun updateBroadcastPort(text: String) {
        _busUiState.update {
            it.copy(broadcastPort = text)
        }
    }

    fun updateBroadcastInterval(text: String) {
        _busUiState.update {
            it.copy(broadcastInterval = text)
        }
    }

    fun updateRecvBufferSize(text: String) {
        _busUiState.update {
            it.copy(recvBufferSize = text)
        }
    }

    companion object {
        private const val TAG = "BusViewModel"
        private const val FLAG_RADIX = 16
    }
}

```


Tcp Client

```kotlin

package com.thoughtworks.cconnapp.ui.flow.client

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.thoughtworks.cconn.ConnectionFactory
import com.thoughtworks.cconn.ConnectionState
import com.thoughtworks.cconn.ConnectionType
import com.thoughtworks.cconn.Method
import com.thoughtworks.cconn.NetworkDiscoveryType
import com.thoughtworks.cconn.OnActionListener
import com.thoughtworks.cconn.OnConnectionStateChangeListener
import com.thoughtworks.cconn.OnDataListener
import com.thoughtworks.cconn.definitions.PropKeys
import com.thoughtworks.cconn.utils.DataConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.*
import javax.inject.Inject

data class ClientUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val isDetecting: Boolean = false,
    val detectFlag: String = "FFFE1234",
    val receivedData: String = "",
)

@HiltViewModel
class ClientViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {
    private val _clientUiState = MutableStateFlow(ClientUiState())
    val clientUiState: StateFlow<ClientUiState>
        get() = _clientUiState

    private val detector = ConnectionFactory.createDetector(context, NetworkDiscoveryType.UDP)
    private val connection = ConnectionFactory.createConnection(context, ConnectionType.TCP)
    private val onConnectionStateChangeListener = object : OnConnectionStateChangeListener {
        override fun onConnectionStateChanged(state: ConnectionState, throwable: Throwable?) {
            _clientUiState.update {
                it.copy(connectionState = state)
            }
        }
    }

    init {
        connection.addOnConnectionStateChangedListener(onConnectionStateChangeListener)
    }

    override fun onCleared() {
        connection.removeOnConnectionStateChangedListener(onConnectionStateChangeListener)
        connection.close()
        super.onCleared()
    }

    fun detectAndConnect() {
        _clientUiState.update {
            it.copy(isDetecting = true)
        }

        detector.startDiscover(Properties().apply {
            this[PropKeys.PROP_FLAG] =
                Integer.parseUnsignedInt(_clientUiState.value.detectFlag, FLAG_RADIX)
        }) { props ->
            val serverIp = props[PropKeys.PROP_SERVER_IP]?.toString() ?: ""
            val serverPort =
                props[PropKeys.PROP_SERVER_PORT]?.toString() ?: "0"
            detector.stopDiscover()
            _clientUiState.update {
                it.copy(isDetecting = false)
            }

            connection.start(Properties().apply {
                this[PropKeys.PROP_IP] = serverIp
                this[PropKeys.PROP_PORT] = serverPort
                this[PropKeys.PROP_AUTO_RECONNECT] = true
                this[PropKeys.PROP_MAX_RECONNECT_RETRY_TIME] = MAX_RECONNECT_RETRY_TIME
            })
        }
    }

    fun publish(topic: String, method: Method, data: ByteArray) {
        if (connection.getState() == ConnectionState.CONNECTED) {
            connection.publish(topic, method, data)
        } else {
            Log.e(TAG, "publish failed, not connected")
        }
    }

    fun subscribe(topic: String, method: Method) {
        if (connection.getState() == ConnectionState.CONNECTED) {
            connection.subscribe(topic, method, object : OnDataListener {
                override fun invoke(topic: String, method: Method, data: ByteArray) {
                    _clientUiState.update {
                        it.copy(receivedData = "$topic ${method.name} ${DataConverter.byteArrayToString(data)}\n\n${it.receivedData}")
                    }
                }
            }, object : OnActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "subscribe success")
                }

                override fun onFailure(throwable: Throwable) {
                    Log.e(TAG, "subscribe failed")
                }
            })
        } else {
            Log.e(TAG, "subscribe failed, not connected")
        }
    }

    fun unsubscribe(topic: String, method: Method) {
        if (connection.getState() == ConnectionState.CONNECTED) {
            connection.unsubscribe(topic, method)
        } else {
            Log.e(TAG, "unSubscribe failed, not connected")
        }
    }

    fun updateFlag(flag: String) {
        _clientUiState.update {
            it.copy(detectFlag = flag)
        }
    }

    fun close() {
        detector.stopDiscover()
        connection.close()
        _clientUiState.update {
            it.copy(isDetecting = false)
        }
    }

    companion object {
        private const val TAG = "ClientViewModel"
        private const val MAX_RECONNECT_RETRY_TIME = 8
        private const val FLAG_RADIX = 16
    }
}

```