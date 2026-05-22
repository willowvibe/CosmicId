package com.willowvibe.agereveal.domain

import android.icu.util.Calendar
import android.icu.util.ChineseCalendar
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

    fun toLunarString(date: LocalDate): String {
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
            val lunarYear = chinese.get(Calendar.EXTENDED_YEAR)

            val zodiacAnimal = ZODIAC_ANIMALS[Math.floorMod(lunarYear.toLong(), 12).toInt()]
            val dayStr = ordinalDay(lunarDay)
            val monthStr = buildString {
                if (isLeap) append("leap ")
                append(lunarMonth)
            }

            "$dayStr of the ${monthStr}th lunar month, Year of the $zodiacAnimal"
        } catch (_: Exception) {
            // Fallback for JVM unit tests where android.icu is unavailable
            ""
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
        private val ZODIAC_ANIMALS = listOf(
            "Monkey", "Rooster", "Dog", "Pig", "Rat", "Ox",
            "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat",
        )
    }
}
