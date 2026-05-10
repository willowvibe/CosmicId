package com.willowvibe.agereveal.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Minimum-viable analytics wrapper for Cosmic ID beta.
 *
 * Logs key funnel and conversion events to Firebase Analytics.
 * Gracefully handles missing Firebase configuration (tests / CI) by
 * silently dropping events when [analytics] is unavailable.
 *
 * Event names follow Firebase recommendations (snake_case, max 40 chars).
 * Parameter names follow Firebase recommendations (snake_case, max 40 chars).
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val analytics: FirebaseAnalytics? = runCatching {
        FirebaseAnalytics.getInstance(context)
    }.getOrNull()

    /** Log a custom event with optional string parameters. */
    fun logEvent(name: String, params: Map<String, String> = emptyMap()) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) -> putString(key, value) }
        }
        analytics?.logEvent(name, bundle)
    }

    /** Log a screen view (auto-collected by Firebase; use this for manual screen tracking). */
    fun logScreenView(screenName: String, screenClass: String) {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        })
    }

    // ---------------------------------------------------------------------------
    // Onboarding funnel
    // ---------------------------------------------------------------------------

    fun logOnboardingStep1Complete() = logEvent("onboarding_step_1_complete")
    fun logOnboardingStep2Complete() = logEvent("onboarding_step_2_complete")
    fun logOnboardingStep3Complete() = logEvent("onboarding_step_3_complete")
    fun logOnboardingComplete() = logEvent("onboarding_complete")

    // ---------------------------------------------------------------------------
    // Paywall funnel
    // ---------------------------------------------------------------------------

    fun logPaywallShown() = logEvent("paywall_shown")
    fun logPaywallSubscribeTap(sku: String) = logEvent("paywall_subscribe_tap", mapOf("sku" to sku))
    fun logPaywallDismiss() = logEvent("paywall_dismiss")

    // ---------------------------------------------------------------------------
    // Share events
    // ---------------------------------------------------------------------------

    fun logShareInitiated(format: String) = logEvent("share_initiated", mapOf("format" to format))

    // ---------------------------------------------------------------------------
    // Deep-link events
    // ---------------------------------------------------------------------------

    fun logDeepLinkReceived() = logEvent("deep_link_received")
    fun logDeepLinkProfileViewed() = logEvent("deep_link_profile_viewed")

    // ---------------------------------------------------------------------------
    // Premium conversion
    // ---------------------------------------------------------------------------

    fun logPurchaseComplete(sku: String) = logEvent("purchase_complete", mapOf("sku" to sku))
    fun logTrialStarted(sku: String) = logEvent("trial_started", mapOf("sku" to sku))
    fun logTrialConverted(sku: String) = logEvent("trial_converted", mapOf("sku" to sku))

    // ---------------------------------------------------------------------------
    // Engagement
    // ---------------------------------------------------------------------------

    fun logCalculatorResult() = logEvent("calculator_result")
    fun logCompatibilityChecked() = logEvent("compatibility_checked")
    fun logBirthdayAdded() = logEvent("birthday_added")
    fun logSettingsChanged(setting: String, value: String) =
        logEvent("settings_changed", mapOf("setting" to setting, "value" to value))
}
