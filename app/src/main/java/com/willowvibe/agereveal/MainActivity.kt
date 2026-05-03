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
import androidx.lifecycle.lifecycleScope
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import com.willowvibe.agereveal.ui.navigation.AppNavGraph
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import com.willowvibe.agereveal.util.LocaleManager
import com.willowvibe.agereveal.widget.SecondsCounterUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single-activity entry point.
 * Hosts the full Compose NavGraph — all screens are composable destinations.
 *
 * Extends [ComponentActivity] (sufficient for Compose) — per-app locales work via
 * AppCompatDelegate's static application-level API; the activity type does not matter.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var userPrefs: UserPreferencesRepository

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or denied — notifications work accordingly */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply persisted locale before any Compose content inflates
        lifecycleScope.launch {
            runCatching { LocaleManager.apply(userPrefs.languageTag.first()) }
        }

        requestNotificationPermissionIfNeeded()

        // Schedule periodic widget refresh for the seconds counter
        SecondsCounterUpdateWorker.schedule(this)

        setContent {
            AgeRevealTheme {
                AppNavGraph(adManager = adManager)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adManager.attachActivity(this)
    }

    override fun onStop() {
        super.onStop()
        adManager.detachActivity()
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
