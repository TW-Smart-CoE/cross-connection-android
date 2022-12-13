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