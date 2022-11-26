package com.thoughtworks.cconnapp.ui.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thoughtworks.cconnapp.R
import com.thoughtworks.cconnapp.ui.navigation.Screen

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Button(
            modifier = Modifier
                .padding(top = 100.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .width(200.dp)
                .wrapContentHeight(),
            onClick = {
                navController.navigate(Screen.BusScreen.route)
            }
        ) {
            Text(text = stringResource(id = R.string.bus))
        }
        Button(
            modifier = Modifier
                .padding(top = 30.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .width(200.dp)
                .wrapContentHeight(),
            onClick = {
                navController.navigate(Screen.ClientScreen.route)
            }
        ) {
            Text(text = stringResource(id = R.string.client))
        }
    }
}