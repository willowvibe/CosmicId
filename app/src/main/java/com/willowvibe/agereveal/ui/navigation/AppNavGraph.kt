package com.willowvibe.agereveal.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.ui.screen.CalculatorScreen
import com.willowvibe.agereveal.ui.screen.CompatibilityScreen
import com.willowvibe.agereveal.ui.screen.CompareScreen
import com.willowvibe.agereveal.ui.screen.DetailsUnlockScreen
import com.willowvibe.agereveal.ui.screen.LifeTimelineScreen
import com.willowvibe.agereveal.ui.screen.RemindersScreen
import com.willowvibe.agereveal.ui.screen.SettingsScreen
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import com.willowvibe.agereveal.ui.viewmodel.CompatibilityViewModel
import com.willowvibe.agereveal.ui.viewmodel.RemindersViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Calculator : Screen("calculator", "Age", Icons.Default.Calculate)
    data object Details : Screen("details", "Profile", Icons.Default.Star)
    data object Compare : Screen("compare", "Compare", Icons.AutoMirrored.Filled.CompareArrows)
    data object Compatibility : Screen("compatibility", "Match", Icons.Default.Favorite)
    data object Reminders : Screen("reminders", "Bdays", Icons.Default.Cake)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Timeline : Screen("timeline", "Timeline", Icons.Default.Cake)
}

private val bottomNavItems = listOf(
    Screen.Calculator,
    Screen.Details,
    Screen.Compare,
    Screen.Compatibility,
    Screen.Reminders,
    Screen.Settings,
    Screen.Timeline,
)

@Composable
fun AppNavGraph(adManager: AdManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show bottom bar on all screens
    val showBottomBar = true

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = WarmBlack,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = WarmBlack,
                    tonalElevation = 0.dp,
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDest?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    screen.icon,
                                    contentDescription = screen.label,
                                    modifier = Modifier.size(22.dp),
                                )
                            },
                            label = { Text(screen.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WarmInk,
                                selectedTextColor = WarmInk,
                                unselectedIconColor = WarmInkDim,
                                unselectedTextColor = WarmInkDim,
                                indicatorColor = WarmSurface,
                            ),
                        )
                    }
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
                val viewModel: CalculatorViewModel = hiltViewModel(backStackEntry)
                CalculatorScreen(
                    viewModel = viewModel,
                    adManager = adManager,
                    onShareCard = { theme -> viewModel.shareCard(theme) },
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
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                )
            }
            composable(Screen.Details.route) {
                val calcEntry = runCatching { navController.getBackStackEntry(Screen.Calculator.route) }.getOrNull()
                if (calcEntry == null) {
                    navController.navigate(Screen.Calculator.route) { launchSingleTop = true }
                    return@composable
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
                    onShareMilestone = { milestone -> viewModel.shareMilestoneCard(milestone) },
                )
            }
            composable(Screen.Compare.route) {
                CompareScreen(onShowInterstitial = { adManager.maybeShowInterstitial() })
            }
            composable(Screen.Compatibility.route) {
                val navEntry = navController.getBackStackEntry(Screen.Compatibility.route)
                val viewModel: CompatibilityViewModel = hiltViewModel(navEntry)
                CompatibilityScreen(viewModel = viewModel)
            }
            composable(Screen.Reminders.route) {
                val navEntry = navController.getBackStackEntry(Screen.Reminders.route)
                val viewModel: RemindersViewModel = hiltViewModel(navEntry)
                RemindersScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                )
            }
            composable(Screen.Settings.route) {
                val viewModel: RemindersViewModel = hiltViewModel()
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable(Screen.Timeline.route) {
                val calcEntry = runCatching { navController.getBackStackEntry(Screen.Calculator.route) }.getOrNull()
                val viewModel: CalculatorViewModel = if (calcEntry != null) {
                    hiltViewModel(calcEntry)
                } else {
                    hiltViewModel()
                }
                val milestones = viewModel.uiState.value.result?.milestones ?: emptyList()
                LifeTimelineScreen(
                    milestones = milestones,
                    onDismiss = { navController.popBackStack() }
                )
            }
        }
    }
}
