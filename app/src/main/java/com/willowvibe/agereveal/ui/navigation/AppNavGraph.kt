package com.willowvibe.agereveal.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.ui.screen.CalculatorScreen
import com.willowvibe.agereveal.ui.screen.CompareScreen
import com.willowvibe.agereveal.ui.screen.DetailsUnlockScreen
import com.willowvibe.agereveal.ui.screen.RemindersScreen
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Calculator : Screen("calculator", "Age", Icons.Default.Calculate)
    data object Details    : Screen("details",    "Unlock", Icons.Default.LockOpen)
    data object Compare    : Screen("compare",    "Compare", Icons.Default.CompareArrows)
    data object Reminders  : Screen("reminders",  "Birthdays", Icons.Default.Cake)
}

private val bottomNavItems = listOf(
    Screen.Calculator,
    Screen.Details,
    Screen.Compare,
    Screen.Reminders,
)

@Composable
fun AppNavGraph(adManager: AdManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calculator.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Calculator.route) { backStackEntry ->
                // Use the same CalculatorViewModel instance for both Calculator + Details screens
                // by scoping it to the nav graph's back-stack entry.
                val viewModel: CalculatorViewModel = hiltViewModel(backStackEntry)

                CalculatorScreen(
                    viewModel = viewModel,
                    adManager = adManager,
                    onShareCard = { viewModel.shareCard() },
                    onUnlockMore = {
                        adManager.showRewardedAd(
                            onRewarded = {
                                viewModel.onRewardedAdEarned()
                                navController.navigate(Screen.Details.route)
                            },
                            onNotReady = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Ad not ready yet — try again in a moment")
                                }
                            },
                        )
                    },
                )
            }
            composable(Screen.Details.route) {
                // Route the Details screen to the Calculator's back-stack entry so they share state
                val calcEntry = remember(navController) {
                    navController.getBackStackEntry(Screen.Calculator.route)
                }
                val viewModel: CalculatorViewModel = hiltViewModel(calcEntry)

                DetailsUnlockScreen(
                    viewModel = viewModel,
                    onWatchAd = {
                        adManager.showRewardedAd(
                            onRewarded = { viewModel.onRewardedAdEarned() },
                            onNotReady = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Ad not ready yet — try again in a moment")
                                }
                            },
                        )
                    },
                )
            }
            composable(Screen.Compare.route) {
                CompareScreen(
                    onShowInterstitial = { adManager.maybeShowInterstitial() },
                )
            }
            composable(Screen.Reminders.route) {
                RemindersScreen(
                    onAddBirthday = { /* handled internally by the FAB inside RemindersScreen */ },
                )
            }
        }
    }
}
