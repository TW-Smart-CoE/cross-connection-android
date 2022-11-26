package com.thoughtworks.cconnapp.ui.navigation

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object BusScreen : Screen("bus_screen")
    object ClientScreen : Screen("client_screen")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
