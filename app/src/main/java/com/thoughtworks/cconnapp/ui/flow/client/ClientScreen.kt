package com.thoughtworks.cconnapp.ui.flow.client

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thoughtworks.cconn.ConnectionState
import com.thoughtworks.cconnapp.R
import com.thoughtworks.cconnapp.ui.components.LeftButton
import com.thoughtworks.cconnapp.ui.components.TopBar


@Composable
fun ClientScreen(
    navController: NavController,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val clientUiState = viewModel.clientUiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        TopBar(
            modifier = Modifier
                .align(alignment = Alignment.TopCenter),
            title = stringResource(id = R.string.client),
            connState = clientUiState.value.connectionState,
            leftButton = LeftButton(R.drawable.ic_baseline_arrow_white_24) { navController.popBackStack() },
        )
        Column(
            modifier = Modifier
                .align(alignment = Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            TextField(
                value = clientUiState.value.detectFlag.toString(),
                onValueChange = {
                    var text = it
                    if (it.length > 8) {
                        text = it.substring(0, 8)
                    }
                    viewModel.updateFlag(text)
                },
                label = { Text(stringResource(id = R.string.detect_flag)) }
            )

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Button(
                    enabled = clientUiState.value.connectionState == ConnectionState.DISCONNECTED
                            && !clientUiState.value.isDetecting,
                    onClick = {
                        viewModel.detectAndConnect()
                    }) {
                    Text(text = stringResource(id = R.string.detect_and_connect))
                }
                Spacer(
                    modifier = Modifier
                        .width(8.dp)
                )
                Button(
                    onClick = {
                        viewModel.close()
                    }) {
                    Text(text = stringResource(id = R.string.close))
                }
            }
        }
    }
}