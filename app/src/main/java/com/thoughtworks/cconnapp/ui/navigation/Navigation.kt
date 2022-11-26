package com.thoughtworks.cconnapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thoughtworks.cconnapp.ui.flow.MainScreen
import com.thoughtworks.cconnapp.ui.flow.bus.BusScreen
import com.thoughtworks.cconnapp.ui.flow.client.ClientScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController = navController)
        }
        composable(route = Screen.BusScreen.route) {
            BusScreen(navController = navController)
        }
        composable(route = Screen.ClientScreen.route) {
            ClientScreen(navController = navController)
        }
    }
}