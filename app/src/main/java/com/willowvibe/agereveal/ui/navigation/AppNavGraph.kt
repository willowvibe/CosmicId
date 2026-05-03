package com.willowvibe.agereveal.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import com.willowvibe.agereveal.R
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.app.Activity
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.ui.screen.ShareFormat
import com.willowvibe.agereveal.ui.screen.BadgeScreen
import com.willowvibe.agereveal.ui.screen.CalculatorScreen
import com.willowvibe.agereveal.ui.screen.CompatibilityScreen
// CompareScreen removed — functionality merged into Match tab
import com.willowvibe.agereveal.ui.screen.DetailsUnlockScreen
import com.willowvibe.agereveal.ui.screen.LifeTimelineScreen
import com.willowvibe.agereveal.ui.screen.RemindersScreen
import com.willowvibe.agereveal.ui.screen.SettingsScreen
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.viewmodel.BadgeViewModel
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import com.willowvibe.agereveal.ui.viewmodel.CompatibilityViewModel
import com.willowvibe.agereveal.ui.viewmodel.RemindersViewModel
import com.willowvibe.agereveal.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, @DrawableRes val icon: Int) {
    data object Calculator : Screen("calculator", "You", R.drawable.ic_tab_you)
    data object Details : Screen("details", "Profile", R.drawable.ic_tab_badges)
    data object Compatibility : Screen("compatibility", "Match", R.drawable.ic_tab_match)
    data object Reminders : Screen("reminders", "Bdays", R.drawable.ic_tab_bdays)
    data object Badges : Screen("badges", "Badges", R.drawable.ic_tab_badges)
    data object Settings : Screen("settings", "Settings", R.drawable.ic_tab_you)
    data object Timeline : Screen("timeline", "Timeline", R.drawable.ic_tab_timeline)
}

private val bottomNavItems = listOf(
    Screen.Calculator,
    Screen.Compatibility,
    Screen.Reminders,
    Screen.Badges,
    Screen.Timeline,
)

@Composable
fun AppNavGraph(adManager: AdManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

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
                                    painter = painterResource(id = screen.icon),
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
                    onShareCard = { theme, format ->
                        when (format) {
                            ShareFormat.SQUARE -> viewModel.shareCard(theme, activity)
                            ShareFormat.STORY -> viewModel.shareStoryCard(theme, activity)
                            ShareFormat.GREEN_SCREEN -> viewModel.shareTransparentOverlay(activity)
                            ShareFormat.ASCII_ART -> viewModel.shareAsciiArt()
                        }
                    },
                    onUnlockMore = {
                        adManager.showRewardedAd(
                            onRewarded = {
                                viewModel.onRewardedAdEarned()
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
                    onShareMilestone = { milestone -> viewModel.shareMilestoneCard(milestone, activity = activity) },
                )
            }
            composable(Screen.Compatibility.route) { backStackEntry ->
                val viewModel: CompatibilityViewModel = hiltViewModel(backStackEntry)
                val calcEntry = remember(navController) {
                    runCatching { navController.getBackStackEntry(Screen.Calculator.route) }.getOrNull()
                }
                val calcVm = calcEntry?.let { hiltViewModel<CalculatorViewModel>(it) }
                val defaultDateA = remember(calcVm) { calcVm?.uiState?.value?.birthDate }
                val defaultNameA = remember(calcVm) { calcVm?.uiState?.value?.name ?: "" }
                CompatibilityScreen(
                    viewModel = viewModel,
                    defaultDateA = defaultDateA,
                    defaultNameA = defaultNameA,
                )
            }
            composable(Screen.Reminders.route) { backStackEntry ->
                val viewModel: RemindersViewModel = hiltViewModel(backStackEntry)
                RemindersScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                )
            }
            composable(Screen.Badges.route) { backStackEntry ->
                val viewModel: BadgeViewModel = hiltViewModel(backStackEntry)
                BadgeScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    settingsViewModel = settingsViewModel
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
                    onDismiss = { navController.popBackStack() },
                    onShare = { milestone -> viewModel.shareMilestoneCard(milestone, activity = activity) },
                )
            }
        }
    }
}
