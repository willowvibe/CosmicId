package com.willowvibe.agereveal.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralises all AdMob ad lifecycle management.
 *
 * Usage pattern (as per build plan):
 *  - Banner ad: Managed directly in the Compose screen via AndroidView — NOT here.
 *  - Rewarded ad: Loaded eagerly on startup; always preload the next one immediately after showing.
 *  - Interstitial: Loaded eagerly; cap at 1 per 5 minutes.
 *
 * Ad unit IDs:
 *  - Replace with real IDs before publishing. Keep test IDs during development.
 */
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        // Test ad unit IDs (safe to commit — will not generate real revenue)
        const val BANNER_AD_UNIT_ID      = "ca-app-pub-3940256099942544/6300978111"
        const val REWARDED_AD_UNIT_ID    = "ca-app-pub-3940256099942544/5224354917"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        private const val INTERSTITIAL_COOLDOWN_MS = 5 * 60 * 1_000L  // 5 minutes
    }

    // ---------------------------------------------------------------------------
    // Rewarded ad
    // ---------------------------------------------------------------------------

    private var rewardedAd: RewardedAd? = null

    fun preloadRewardedAd() {
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    // Optionally retry after a delay
                }
            },
        )
    }

    /**
     * Show the rewarded ad. Calls [onRewarded] iff the user earns the reward.
     * Immediately preloads the next ad after show (prevents wait-time on subsequent taps).
     */
    fun showRewardedAd(activity: Activity? = null, onRewarded: () -> Unit) {
        val ad = rewardedAd
        rewardedAd = null

        if (ad == null) {
            // Ad not ready — preload and inform user gracefully (caller decides the UX)
            preloadRewardedAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewardedAd()   // reload immediately for next time
            }
        }

        val hostActivity = activity ?: return
        ad.show(hostActivity) { rewardedAd ->
            if (rewardedAd.amount > 0) onRewarded()
        }
    }

    // ---------------------------------------------------------------------------
    // Interstitial ad
    // ---------------------------------------------------------------------------

    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialShownMs: Long = 0L

    fun preloadInterstitialAd() {
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(e: LoadAdError) {
                    interstitialAd = null
                }
            },
        )
    }

    /**
     * Show the interstitial if the 5-minute cooldown has elapsed.
     * Call this at natural break points (e.g. after 2nd comparison on Compare screen).
     */
    fun maybeShowInterstitial(activity: Activity? = null) {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShownMs < INTERSTITIAL_COOLDOWN_MS) return
        val ad = interstitialAd ?: return
        val hostActivity = activity ?: return

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitialAd()
            }
        }

        ad.show(hostActivity)
        lastInterstitialShownMs = now
    }
}
