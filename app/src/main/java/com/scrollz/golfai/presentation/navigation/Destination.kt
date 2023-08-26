package com.scrollz.golfai.presentation.navigation

sealed class Destination(val route: String) {
    data object Main: Destination(route = "main")
    data object Report: Destination(route = "report")
}
