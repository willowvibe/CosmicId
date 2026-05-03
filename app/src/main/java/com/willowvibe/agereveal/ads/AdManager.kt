package com.willowvibe.agereveal.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
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
        private const val PREFS_NAME = "ad_preferences"
        private const val KEY_LAST_INTERSTITIAL_SHOWN = "last_interstitial_shown_ms"

        // Test ad unit IDs (safe to commit — will not generate real revenue)
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

        private const val INTERSTITIAL_COOLDOWN_MS = 5 * 60 * 1_000L  // 5 minutes

        // Retry configuration for ad loading failures
        private const val MAX_REWARDED_AD_RETRIES = 3
        private const val MAX_INTERSTITIAL_AD_RETRIES = 3
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Strong reference to the host Activity during ad display — prevents premature GC.
     * Cleared immediately after ad dismissal.
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

    private var rewardedAdRetryCount = 0

    fun preloadRewardedAd() {
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAdRetryCount = 0
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (rewardedAdRetryCount < MAX_REWARDED_AD_RETRIES) {
                        val attempt = rewardedAdRetryCount
                        rewardedAdRetryCount++
                        val delayMs = 1000L * (1L shl attempt)
                        Handler(Looper.getMainLooper()).postDelayed({ preloadRewardedAd() }, delayMs)
                    } else {
                        rewardedAd = null
                        rewardedAdRetryCount = 0
                    }
                }
            },
        )
    }

    /**
     * Show the rewarded ad. Calls [onRewarded] iff the user earns the reward.
     * If no ad is ready, preloads a new one and calls [onNotReady] so the UI can inform the user.
     * Immediately preloads the next ad after show (prevents wait-time on subsequent taps).
     *
     * Note: The Activity reference is held for the duration of the ad display to prevent GC.
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

        // Hold activity reference during ad display to prevent GC
        // The Activity is passed directly to ad.show() so it remains strongly referenced
        ad.show(activity) { rewardItem ->
            if (rewardItem.amount > 0) onRewarded()
        }
    }

    // ---------------------------------------------------------------------------
    // Interstitial ad
    // ---------------------------------------------------------------------------

    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var lastInterstitialShownMs: Long = prefs.getLong(KEY_LAST_INTERSTITIAL_SHOWN, 0L)

    private var interstitialAdRetryCount = 0

    fun preloadInterstitialAd() {
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialAdRetryCount = 0
                }

                override fun onAdFailedToLoad(e: LoadAdError) {
                    if (interstitialAdRetryCount < MAX_INTERSTITIAL_AD_RETRIES) {
                        val attempt = interstitialAdRetryCount
                        interstitialAdRetryCount++
                        val delayMs = 1000L * (1L shl attempt)
                        Handler(Looper.getMainLooper()).postDelayed({ preloadInterstitialAd() }, delayMs)
                    } else {
                        interstitialAd = null
                        interstitialAdRetryCount = 0
                    }
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
        // Persist for robustness across app kills
        prefs.edit().putLong(KEY_LAST_INTERSTITIAL_SHOWN, now).apply()
    }

    /**
     * Check if a rewarded ad is available for display.
     * Returns true if an ad is loaded and ready to show.
     */
    fun isRewardedAdAvailable(): Boolean = rewardedAd != null
}
