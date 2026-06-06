package com.willowvibe.agereveal.domain

import android.icu.util.Calendar
import android.icu.util.ChineseCalendar
import android.util.Log
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converts a Gregorian date to a Chinese lunar calendar string.
 *
 * Uses the built-in [ChineseCalendar] (API 24+) so no external dependency is needed.
 *
 * Output example: "15th day of the 3rd lunar month, Year of the Dragon"
 * With leap month: "1st day of the leap 4th lunar month, Year of the Rabbit"
 */
@Singleton
class LunarCalendarConverter @Inject constructor() {

    /**
     * Format a date as a Chinese lunar string. Returns an empty string if the
     * conversion fails (e.g. the host JVM doesn't ship `android.icu`).
     *
     * BUG-082: Prefer [toLunarResult] when the caller can surface the error.
     * Kept for back-compat with the many call sites that already render the
     * result inline ("Moon birthday: $lunarBirthday").
     */
    fun toLunarString(date: LocalDate): String =
        toLunarResult(date).getOrDefault("")

    /**
     * Format a date as a Chinese lunar string wrapped in [Result].
     *
     * BUG-082: callers that want to react to a conversion failure (e.g. show a
     * "Lunar birthday unavailable in this environment" banner) should consume
     * this directly. The error is logged at WARN before being returned.
     */
    fun toLunarResult(date: LocalDate): Result<String> {
        return try {
            // 1. Create a Gregorian calendar for the input date
            val gregorian = Calendar.getInstance()
            gregorian.set(date.year, date.monthValue - 1, date.dayOfMonth)

            // 2. Convert to Chinese calendar via timestamp
            val chinese = ChineseCalendar()
            chinese.time = gregorian.time

            val lunarMonth = chinese.get(Calendar.MONTH) + 1 // 0-based → 1-based
            val lunarDay = chinese.get(Calendar.DAY_OF_MONTH)
            val isLeap = chinese.get(Calendar.IS_LEAP_MONTH) == 1
            val lunarYear = chinese.get(ChineseCalendar.EXTENDED_YEAR)

            val zodiacAnimal = ZODIAC_ANIMALS[Math.floorMod(lunarYear.toLong(), 12).toInt()]
            val dayStr = ordinalDay(lunarDay)
            val monthOrdinal = ordinalDay(lunarMonth)
            val leapPrefix = if (isLeap) "leap " else ""

            val output = "$dayStr of the $leapPrefix$monthOrdinal lunar month, Year of the $zodiacAnimal"
            Result.success(output)
        } catch (e: Throwable) {
            // BUG-082: explicit log so silent failures don't slip through in
            // instrumentation tests or stripped release builds. We catch
            // Throwable (not just Exception) because the ICU classes can throw
            // UnsatisfiedLinkError on certain JVMs. `safeWarn` keeps the log
            // call from throwing in JVM unit tests where `android.util.Log`
            // is unmocked.
            safeWarn("toLunarResult failed for $date: ${e.message}")
            Result.failure(e)
        }
    }

    private fun ordinalDay(n: Int): String = when {
        n in 11..13 -> "${n}th"
        n % 10 == 1 -> "${n}st"
        n % 10 == 2 -> "${n}nd"
        n % 10 == 3 -> "${n}rd"
        else -> "${n}th"
    }

    companion object {
        private const val TAG = "LunarConverter"
        private val ZODIAC_ANIMALS = listOf(
            "Monkey", "Rooster", "Dog", "Pig", "Rat", "Ox",
            "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat",
        )

        /**
         * Wraps `android.util.Log.w` so it stays a no-op in JVM unit tests
         * (where `Log` is unmocked and throws `RuntimeException`). Production
         * builds hit the real method.
         */
        private fun safeWarn(message: String) {
            runCatching { Log.w(TAG, message) }
        }
    }
}
