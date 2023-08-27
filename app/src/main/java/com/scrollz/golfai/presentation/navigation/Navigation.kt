package com.scrollz.golfai.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scrollz.golfai.presentation.mainScreen.MainViewModel
import com.scrollz.golfai.presentation.mainScreen.components.MainScreen
import com.scrollz.golfai.presentation.reportScreen.ReportViewModel
import com.scrollz.golfai.presentation.reportScreen.components.ReportScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = Destination.Main.route
        ) {
            composable(
                route = Destination.Main.route
            ) {
                val mainViewModel = hiltViewModel<MainViewModel>()
                val mainState by mainViewModel.state.collectAsStateWithLifecycle()
                MainScreen(
                    state = mainState,
                    onEvent = mainViewModel::onEvent,
                    onReportClick = { reportID ->
                        navController.navigate(Destination.Report.route + "/$reportID") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Destination.Report.route + "/{reportID}",
                arguments = listOf(
                    navArgument(
                        name = "reportID"
                    ) {
                        type = NavType.IntType
                    }
                )
            ) {
                val reportViewModel = hiltViewModel<ReportViewModel>()
                val reportState by reportViewModel.state.collectAsStateWithLifecycle()
                ReportScreen(
                    state = reportState,
                    onEvent = reportViewModel::onEvent,
                    navigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
