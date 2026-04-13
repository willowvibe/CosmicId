package com.willowvibe.agereveal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AgeRevealTheme {
                AppNavGraph(adManager = adManager)
            }
        }
    }
}
