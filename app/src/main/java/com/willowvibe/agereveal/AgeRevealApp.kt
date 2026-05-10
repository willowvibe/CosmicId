package com.willowvibe.agereveal

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — entry point for Hilt DI.
 * Banner ads are initialised lazily by the Compose UI layer.
 */
@HiltAndroidApp
class AgeRevealApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
