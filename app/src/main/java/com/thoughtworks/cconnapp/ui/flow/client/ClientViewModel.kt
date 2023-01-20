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
            this[PropKeys.PROP_BROADCAST_PORT] = 12000
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