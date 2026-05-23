package com.willowvibe.agereveal.ads

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralises AdMob ad unit IDs.
 *
 * v2.0 revamp: Only banner ads remain on the free tier.
 * Rewarded and interstitial ads have been removed in favour of a freemium subscription model.
 *
 * Ad unit IDs:
 *  - Replace with real IDs before publishing. Keep test IDs during development.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        // TODO(playstore): Replace with production banner ad unit ID before release.
        // Test ID (safe to commit — generates no revenue).
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    }
}
