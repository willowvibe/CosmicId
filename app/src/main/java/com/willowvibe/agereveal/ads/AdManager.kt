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
import java.lang.ref.WeakReference
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
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        private const val INTERSTITIAL_COOLDOWN_MS = 5 * 60 * 1_000L  // 5 minutes
    }

    /**
     * Weak reference to the host Activity — prevents memory leaks.
     * Must be updated every time the Activity resumes via [attachActivity].
     */
    private var activityRef: WeakReference<Activity> = WeakReference(null)

    /** Call from [MainActivity.onResume] (and onStart) to keep a valid Activity reference. */
    fun attachActivity(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    /** Call from [MainActivity.onDestroy] to drop the reference early. */
    fun detachActivity() {
        activityRef = WeakReference(null)
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
                }
            },
        )
    }

    /**
     * Show the rewarded ad. Calls [onRewarded] iff the user earns the reward.
     * If no ad is ready, preloads a new one and calls [onNotReady] so the UI can inform the user.
     * Immediately preloads the next ad after show (prevents wait-time on subsequent taps).
     */
    fun showRewardedAd(onRewarded: () -> Unit, onNotReady: (() -> Unit)? = null) {
        val ad = rewardedAd
        val activity = activityRef.get()

        if (ad == null || activity == null) {
            preloadRewardedAd()
            onNotReady?.invoke()
            return
        }

        rewardedAd = null

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadRewardedAd()
            }
        }

        ad.show(activity) { rewardItem ->
            if (rewardItem.amount > 0) onRewarded()
        }
    }

    // ---------------------------------------------------------------------------
    // Interstitial ad
    // ---------------------------------------------------------------------------

    private var interstitialAd: InterstitialAd? = null

    @Volatile
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
    fun maybeShowInterstitial() {
        val now = System.currentTimeMillis()
        if (now - lastInterstitialShownMs < INTERSTITIAL_COOLDOWN_MS) return

        val ad = interstitialAd ?: return
        val activity = activityRef.get() ?: return

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preloadInterstitialAd()
            }
        }

        ad.show(activity)
        lastInterstitialShownMs = now
    }

    /**
     * Check if a rewarded ad is available for display.
     * Returns true if an ad is loaded and ready to show.
     */
    fun isRewardedAdAvailable(): Boolean = rewardedAd != null
}
