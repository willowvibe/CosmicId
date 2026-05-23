package com.willowvibe.agereveal.billing

import com.android.billingclient.api.BillingClient

/**
 * Pure-logic helpers for Google Play Billing.
 *
 * Extracted from [BillingManager] so they can be unit-tested without
 * an Android runtime or a real [BillingClient].
 */
object BillingUtils {

    /** Converts an ISO 8601 billing period (e.g. "P7D", "P1W", "P1M") to an approximate day count. */
    fun parseBillingPeriodToDays(period: String): Int? {
        val regex = Regex("""P(\d+)([DWMY])""")
        val match = regex.matchEntire(period.uppercase()) ?: return null
        val value = match.groupValues[1].toIntOrNull() ?: return null
        return when (match.groupValues[2]) {
            "D" -> value
            "W" -> value * 7
            "M" -> value * 30
            "Y" -> value * 365
            else -> null
        }
    }

    /** Human-readable message for a [BillingClient.BillingResponseCode]. */
    fun billingErrorMessage(code: Int): String = when (code) {
        BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "Play Store timed out. Please retry."
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Can't reach Play Store. Check your connection."
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing is not available on this device."
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "This subscription is not available."
        BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Billing configuration error."
        BillingClient.BillingResponseCode.ERROR -> "An unexpected billing error occurred."
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "You already own this subscription."
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "Subscription not found."
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "This feature is not supported on your device."
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Play Store disconnected. Reconnecting..."
        BillingClient.BillingResponseCode.USER_CANCELED -> "Purchase cancelled."
        else -> "Billing error (code $code). Please retry."
    }
}
