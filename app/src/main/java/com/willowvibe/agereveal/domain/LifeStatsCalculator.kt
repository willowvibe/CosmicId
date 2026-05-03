package com.willowvibe.agereveal.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year

/**
 * Computes fun aggregated life statistics for shareable cards.
 * Pure Kotlin — no Android framework imports.
 */
class LifeStatsCalculator {

    data class LifeStat(
        val label: String,
        val value: String,
        val emoji: String,
    )

    fun calculateAll(
        birthDate: LocalDate,
        today: LocalDate = LocalDate.now(),
        totalDays: Long,
        totalSeconds: Long,
    ): List<LifeStat> {
        return listOf(
            LifeStat(
                label = "Full moons",
                value = "%,.0f".format(totalDays / 29.53),
                emoji = "🌕",
            ),
            LifeStat(
                label = "Fridays the 13th",
                value = "%,d".format(countFridaysThe13th(birthDate, today)),
                emoji = "🗓️",
            ),
            LifeStat(
                label = "Leap years",
                value = "%,d".format(countLeapYears(birthDate, today)),
                emoji = "📅",
            ),
            LifeStat(
                label = "Heartbeats",
                value = formatLargeNumber(totalSeconds * 1.2),
                emoji = "♥️",
            ),
            LifeStat(
                label = "Breaths taken",
                value = formatLargeNumber(totalSeconds * 0.267),
                emoji = "🫁",
            ),
            LifeStat(
                label = "Meals eaten",
                value = "%,d".format(totalDays * 3),
                emoji = "🍽️",
            ),
            LifeStat(
                label = "Words spoken",
                value = formatLargeNumber(totalDays * 7_000.0),
                emoji = "🗣️",
            ),
            LifeStat(
                label = "Steps walked",
                value = formatLargeNumber(totalDays * 5_000.0),
                emoji = "👟",
            ),
        )
    }

    private fun countFridaysThe13th(start: LocalDate, end: LocalDate): Int {
        var count = 0
        var date = start
        while (!date.isAfter(end)) {
            if (date.dayOfMonth == 13 && date.dayOfWeek == DayOfWeek.FRIDAY) {
                count++
            }
            date = date.plusDays(1)
        }
        return count
    }

    private fun countLeapYears(start: LocalDate, end: LocalDate): Int {
        return (start.year..end.year).count { Year.isLeap(it.toLong()) }
    }

    private fun formatLargeNumber(n: Double): String = when {
        n >= 1_000_000_000 -> "%,.2f B".format(n / 1_000_000_000.0)
        n >= 1_000_000 -> "%,.1f M".format(n / 1_000_000.0)
        n >= 1_000 -> "%,.0f K".format(n / 1_000.0)
        else -> "%,.0f".format(n)
    }
}
