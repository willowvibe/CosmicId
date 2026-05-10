package com.willowvibe.agereveal.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willowvibe.agereveal.ui.theme.WarmTeal
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.willowvibe.agereveal.domain.ProfileDeepLinkGenerator
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.ui.screen.ShareFormat
import com.willowvibe.agereveal.ui.screen.CalculatorScreen
import com.willowvibe.agereveal.ui.screen.CompatibilityScreen
import com.willowvibe.agereveal.ui.screen.DetailsUnlockScreen
import com.willowvibe.agereveal.ui.screen.LifeTimelineScreen
import com.willowvibe.agereveal.ui.screen.RemindersScreen
import com.willowvibe.agereveal.ui.screen.OnboardingScreen
import com.willowvibe.agereveal.ui.screen.SettingsScreen
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import com.willowvibe.agereveal.ui.viewmodel.CompatibilityViewModel
import com.willowvibe.agereveal.ui.viewmodel.MainViewModel
import com.willowvibe.agereveal.ui.viewmodel.RemindersViewModel
import com.willowvibe.agereveal.ui.viewmodel.SettingsViewModel

sealed class Screen(val route: String, val label: String, @DrawableRes val icon: Int) {
    data object Onboarding : Screen("onboarding", "", R.drawable.ic_tab_you)
    data object Calculator : Screen("calculator", "My Cosmos", R.drawable.ic_tab_you)
    data object Details : Screen("details", "Profile", R.drawable.ic_tab_badges)
    data object Compatibility : Screen("compatibility", "Match", R.drawable.ic_tab_match)
    data object Reminders : Screen("reminders", "Bdays", R.drawable.ic_tab_bdays)
    data object Settings : Screen("settings", "Settings", R.drawable.ic_tab_you)
    data object Timeline : Screen("timeline", "Timeline", R.drawable.ic_tab_timeline)
}

private val bottomNavItems = listOf(
    Screen.Calculator,
    Screen.Compatibility,
    Screen.Reminders,
    Screen.Timeline,
)

@Composable
fun AppNavGraph(
    deepLinkProfile: ProfileDeepLinkGenerator.ParsedProfile? = null,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val mainViewModel: MainViewModel = hiltViewModel()
    val onboardingCompleted by mainViewModel.hasCompletedOnboarding.collectAsState()
    val startDestination = remember(onboardingCompleted) {
        if (onboardingCompleted) Screen.Calculator.route else Screen.Onboarding.route
    }

    // Hide bottom bar on onboarding
    val showBottomBar = currentDest?.route != Screen.Onboarding.route

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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(3.dp)
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(WarmTeal),
                                        )
                                        Spacer(Modifier.height(2.dp))
                                    }
                                    Icon(
                                        painter = painterResource(id = screen.icon),
                                        contentDescription = screen.label,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            },
                            label = { Text(screen.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = WarmInk,
                                selectedTextColor = WarmInk,
                                unselectedIconColor = WarmInkDim,
                                unselectedTextColor = WarmInkDim,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        mainViewModel.completeOnboarding()
                        navController.navigate(Screen.Calculator.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(Screen.Calculator.route) { backStackEntry ->
                val viewModel: CalculatorViewModel = hiltViewModel(backStackEntry)
                LaunchedEffect(deepLinkProfile) {
                    deepLinkProfile?.let { profile ->
                        viewModel.onBirthDateSelected(profile.birthDate)
                        viewModel.onNameChanged(profile.name)
                        profile.birthTime?.let { viewModel.onBirthTimeSelected(it) }
                    }
                }
                CalculatorScreen(
                    viewModel = viewModel,
                    onShareCard = { theme, format ->
                        when (format) {
                            ShareFormat.SQUARE -> viewModel.shareCard(theme, context as? android.app.Activity)
                            ShareFormat.STORY -> viewModel.shareStoryCard(theme, context as? android.app.Activity)
                            ShareFormat.GREEN_SCREEN -> viewModel.shareTransparentOverlay(context as? android.app.Activity)
                        }
                    },
                    onOpenDetails = { navController.navigate(Screen.Details.route) },
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
                    onShareMilestone = { milestone -> viewModel.shareMilestoneCard(milestone, activity = context as? android.app.Activity) },
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
                    onShare = { milestone -> viewModel.shareMilestoneCard(milestone, activity = context as? android.app.Activity) },
                )
            }
        }
    }
}
