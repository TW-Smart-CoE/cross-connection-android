package com.thoughtworks.cconnapp.ui.flow.client

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.thoughtworks.cconn.ConnectionFactory
import com.thoughtworks.cconn.ConnectionState
import com.thoughtworks.cconn.ConnectionType
import com.thoughtworks.cconn.Method
import com.thoughtworks.cconn.NetworkDiscoveryType
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
    val detectFlag: String = "FFFEC1E5",
    val isDetecting: Boolean = false,
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
        detectAndConnect()
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

        detector.startDiscover(Properties()) { props ->
            val serverIp = props[PropKeys.PROP_UDP_DETECTOR_ON_FOUND_SERVICE_IP]?.toString() ?: ""
            val serverPort =
                props[PropKeys.PROP_UDP_DETECTOR_ON_FOUND_SERVICE_PORT]?.toString() ?: "0"
            detector.stopDiscover()
            _clientUiState.update {
                it.copy(isDetecting = false)
            }

            connection.init(Properties().apply {
                this[PropKeys.PROP_UDP_DETECTOR_FLAG] =
                    Integer.parseUnsignedInt(_clientUiState.value.detectFlag, FLAG_RADIX)
                this[PropKeys.PROP_IP] = serverIp
                this[PropKeys.PROP_PORT] = serverPort
                this[PropKeys.PROP_AUTO_CONNECT] = true
                this[PropKeys.PROP_MAX_RECONNECT_RETRY_TIME] = MAX_RECONNECT_RETRY_TIME
            })
        }
    }

    fun publish(topic: String, method: Method, data: String) {
        if (connection.getState() == ConnectionState.CONNECTED) {
            connection.publish(topic, method, DataConverter.stringToByteArray(data))
        } else {
            Log.e(TAG, "publish failed, not connected")
        }
    }

    fun subscribe(topic: String, method: Method) {
        if (connection.getState() == ConnectionState.CONNECTED) {
            connection.subscribe(topic, method, object : OnDataListener {
                override fun invoke(topic: String, method: Method, data: ByteArray) {
                    println(DataConverter.byteArrayToString(data))
                }
            })
        } else {
            Log.e(TAG, "subscribe failed, not connected")
        }
    }

    fun updateFlag(s: String) {
        _clientUiState.update {
            it.copy(detectFlag = s)
        }
    }

    fun close() {
        connection.close()
    }

    companion object {
        private const val TAG = "ClientViewModel"
        private const val MAX_RECONNECT_RETRY_TIME = 8
        private const val FLAG_RADIX = 16
    }
}