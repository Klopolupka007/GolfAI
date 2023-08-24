package com.scrollz.golfai.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scrollz.golfai.presentation.mainScreen.components.MainScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        NavHost(
            navController = navController,
            startDestination = Destination.Main.route
        ) {
            composable(
                route = Destination.Main.route
            ) {
                MainScreen()
            }
        }
    }
}
