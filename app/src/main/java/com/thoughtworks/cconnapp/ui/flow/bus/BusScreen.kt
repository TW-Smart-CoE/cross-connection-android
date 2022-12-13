package com.thoughtworks.cconnapp.ui.flow.bus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thoughtworks.cconnapp.R
import com.thoughtworks.cconnapp.ui.components.LeftButton
import com.thoughtworks.cconnapp.ui.components.TopBar

@Composable
fun BusScreen(
    navController: NavController,
    viewModel: BusViewModel = hiltViewModel()
) {
    val busUiState = viewModel.busUiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        TopBar(
            modifier = Modifier
                .align(alignment = Alignment.TopCenter),
            title = stringResource(id = R.string.bus),
            connState = null,
            leftButton = LeftButton(R.drawable.ic_baseline_arrow_white_24) { navController.popBackStack() },
        )
        Column(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .padding(top = 64.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(8.dp),
                text = stringResource(id = R.string.tcp_server),
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .width(108.dp),
                    singleLine = true,
                    value = busUiState.value.registerFlag,
                    onValueChange = {
                        var text = it
                        if (it.length > 8) {
                            text = it.substring(0, 8)
                        }
                        viewModel.updateFlag(text)
                    },
                    label = { Text(stringResource(id = R.string.register_flag)) }
                )
                Spacer(modifier = Modifier.width(5.dp))
                TextField(
                    modifier = Modifier
                        .width(140.dp),
                    singleLine = true,
                    value = busUiState.value.recvBufferSize,
                    onValueChange = {
                        viewModel.updateRecvBufferSize(it)
                    },
                    label = { Text(stringResource(id = R.string.recv_buffer_size)) }
                )
            }

            Row(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .width(160.dp),
                    singleLine = true,
                    value = busUiState.value.serverIp,
                    onValueChange = {
                        var text = it
                        if (it.length > 15) {
                            text = it.substring(0, 15)
                        }
                        viewModel.updateServerIp(text)
                    },
                    label = { Text(stringResource(id = R.string.server_ip)) }
                )
                Spacer(modifier = Modifier.width(5.dp))
                TextField(
                    modifier = Modifier
                        .width(100.dp),
                    singleLine = true,
                    value = busUiState.value.serverPort,
                    onValueChange = {
                        viewModel.updateServerPort(it)
                    },
                    label = { Text(stringResource(id = R.string.server_port)) }
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
            ) {
                TextField(
                    modifier = Modifier
                        .width(100.dp),
                    singleLine = true,
                    value = busUiState.value.broadcastPort,
                    onValueChange = {
                        viewModel.updateBroadcastPort(it)
                    },
                    label = { Text(stringResource(id = R.string.broadcast_port)) }
                )
                Spacer(modifier = Modifier.width(5.dp))
                TextField(
                    modifier = Modifier
                        .width(100.dp),
                    singleLine = true,
                    value = busUiState.value.broadcastInterval,
                    onValueChange = {
                        viewModel.updateBroadcastInterval(it)
                    },
                    label = { Text(stringResource(id = R.string.broadcast_interval)) }
                )
            }
            Button(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                enabled = !busUiState.value.tcpServerStarted,
                onClick = {
                    viewModel.startTcpServer()
                }
            ) {
                Text(text = stringResource(id = R.string.start))
            }
        }
    }
}