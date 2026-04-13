package com.willowvibe.agereveal

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.willowvibe.agereveal.ads.AdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class — entry point for Hilt DI and AdMob SDK initialisation.
 */
@HiltAndroidApp
class AgeRevealApp : Application() {

    @Inject lateinit var adManager: AdManager

    override fun onCreate() {
        super.onCreate()

        // Initialise AdMob SDK (must run before any ad is loaded)
        MobileAds.initialize(this) {
            // Preload both ad formats eagerly so they are ready when user first interacts
            adManager.preloadRewardedAd()
            adManager.preloadInterstitialAd()
        }
    }
}
