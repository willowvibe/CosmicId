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
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.ui.navigation.AppNavGraph
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity entry point.
 * Hosts the full Compose NavGraph — all screens are composable destinations.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var adManager: AdManager

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* granted or denied — notifications work accordingly */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

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
