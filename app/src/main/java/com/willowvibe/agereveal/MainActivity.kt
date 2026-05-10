package com.willowvibe.agereveal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.willowvibe.agereveal.analytics.AnalyticsManager
import com.willowvibe.agereveal.billing.BillingManager
import com.willowvibe.agereveal.domain.ProfileDeepLinkGenerator
import com.willowvibe.agereveal.notification.DailyFortuneScheduler
import com.willowvibe.agereveal.ui.navigation.AppNavGraph
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import com.willowvibe.agereveal.widget.SecondsCounterUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity entry point.
 * Hosts the full Compose NavGraph — all screens are composable destinations.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var billingManager: BillingManager
    @Inject lateinit var dailyFortuneScheduler: DailyFortuneScheduler
    @Inject lateinit var analytics: AnalyticsManager

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or denied — notifications work accordingly */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        billingManager.startConnection()
        requestNotificationPermissionIfNeeded()

        // Schedule periodic widget refresh for the seconds counter
        SecondsCounterUpdateWorker.schedule(this)

        // Ensure daily fortune push is scheduled if the user has set a birth date
        dailyFortuneScheduler.schedule()

        val deepLinkProfile = intent?.data?.let { ProfileDeepLinkGenerator.parse(it) }
        if (deepLinkProfile != null) {
            analytics.logDeepLinkReceived()
        }

        setContent {
            AgeRevealTheme {
                AppNavGraph(deepLinkProfile = deepLinkProfile)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
