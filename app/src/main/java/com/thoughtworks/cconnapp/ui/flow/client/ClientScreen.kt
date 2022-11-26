package com.thoughtworks.cconnapp.ui.flow.client

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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


    }
}