package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Computes darkly motivational "time remaining" stats until a target age.
 * Pure Kotlin — no Android framework imports.
 */
class TimeRemainingCalculator {

    data class TimeRemaining(
        val weekends: Long,
        val fridays: Long,
        val paychecks: Long,
        val fullMoons: Long,
        val targetAge: Int,
    )

    fun calculate(
        birthDate: LocalDate,
        today: LocalDate = LocalDate.now(),
        targetAge: Int,
    ): TimeRemaining? {
        val targetDate = birthDate.plusYears(targetAge.toLong())
        if (targetDate.isBefore(today)) return null // already past target age

        val daysRemaining = ChronoUnit.DAYS.between(today, targetDate)
        val weekends = daysRemaining / 7
        val fridays = countDaysOfWeek(birthDate, targetDate, today, java.time.DayOfWeek.FRIDAY)
        val paychecks = fridays // assume monthly paychecks ≈ fridays is a rough proxy; better: months remaining
        val fullMoons = (daysRemaining / 29.53).toLong()

        return TimeRemaining(
            weekends = weekends,
            fridays = fridays,
            paychecks = ChronoUnit.MONTHS.between(today, targetDate).coerceAtLeast(0),
            fullMoons = fullMoons,
            targetAge = targetAge,
        )
    }

    private fun countDaysOfWeek(
        birthDate: LocalDate,
        targetDate: LocalDate,
        today: LocalDate,
        dayOfWeek: java.time.DayOfWeek,
    ): Long {
        var count = 0L
        var date = today
        while (date.isBefore(targetDate)) {
            if (date.dayOfWeek == dayOfWeek) count++
            date = date.plusDays(1)
        }
        return count
    }
}
