package com.willowvibe.agereveal.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Computes global age percentile and shared-birth-date estimates using
 * simplified UN World Population Prospects 2024 data.
 *
 * Pure Kotlin — no Android framework imports.
 */
@Singleton
class AgePercentileCalculator @Inject constructor() {

    data class PercentileResult(
        val percentileText: String,
        val sharedBirthDateEstimate: String,
    )

    /**
     * Returns a human-readable percentile string and a rough estimate of how many
     * people alive share the exact same birth date.
     *
     * @param years Age in completed years
     */
    fun calculate(years: Int): PercentileResult {
        val pct = computePercentile(years)
        val pctText = "Older than ${pct.toInt()}% of the global population"
        val shared = "~${formatEstimate(ESTIMATED_SHARED_BIRTH_DATE)} people alive share your exact birth date"
        return PercentileResult(pctText, shared)
    }

    /**
     * Computes the cumulative percentage of the global population that is
     * strictly younger than [years] old, using linear interpolation between
     * UN WPP 2024 quintile data points.
     */
    internal fun computePercentile(years: Int): Double {
        if (years <= 0) return 0.0
        if (years >= MAX_AGE) return distribution.last().second

        // Find the bracket
        var i = 0
        while (i < distribution.size - 1 && distribution[i + 1].first <= years) {
            i++
        }

        val (ageLow, pctLow) = distribution[i]
        val (ageHigh, pctHigh) = distribution[i + 1]

        // Linear interpolation
        val fraction = (years - ageLow).toDouble() / (ageHigh - ageLow)
        return pctLow + fraction * (pctHigh - pctLow)
    }

    companion object {
        /** World population ≈ 8.1B / 365.25 days ≈ 22.1M per calendar day. */
        private const val ESTIMATED_SHARED_BIRTH_DATE = 22_000_000L

        private const val MAX_AGE = 100

        /**
         * UN WPP 2024 approximate cumulative distribution.
         * Each pair is (age, % of global population younger than this age).
         */
        private val distribution = listOf(
            0 to 0.0,
            1 to 1.7,
            5 to 8.2,
            10 to 15.8,
            15 to 23.2,
            20 to 30.3,
            25 to 37.1,
            30 to 43.4,
            35 to 49.2,
            40 to 54.5,
            45 to 59.6,
            50 to 64.3,
            55 to 68.7,
            60 to 72.9,
            65 to 76.9,
            70 to 80.8,
            75 to 84.4,
            80 to 87.7,
            85 to 90.6,
            90 to 93.2,
            95 to 95.3,
            100 to 97.0,
        )

        private fun formatEstimate(n: Long): String = when {
            n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000.0)
            n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
            n >= 1_000 -> "%.0fK".format(n / 1_000.0)
            else -> n.toString()
        }
    }
}
