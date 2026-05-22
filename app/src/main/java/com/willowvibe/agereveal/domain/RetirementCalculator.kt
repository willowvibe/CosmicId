package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Computes days until retirement and related working-life stats.
 * Configurable retirement age (default 60).
 *
 * Pure Kotlin — no Android framework imports.
 */
class RetirementCalculator {

    data class RetirementResult(
        val daysUntilRetirement: Long,
        val workWeeksLeft: Long,
        val workDaysLeft: Long,
        val workHoursLeft: Long,
        val percentOfWorkLifeComplete: Int,
        val retirementAge: Int,
    )

    /**
     * Calculate retirement stats.
     * @param retirementAge Target retirement age in years
     * @return null if already past retirement age
     */
    fun calculate(
        birthDate: LocalDate,
        today: LocalDate = LocalDate.now(),
        retirementAge: Int = 60,
    ): RetirementResult? {
        val retirementDate = birthDate.plusYears(retirementAge.toLong())
        if (retirementDate.isBefore(today)) return null

        val daysUntil = ChronoUnit.DAYS.between(today, retirementDate)
        val workWeeksLeft = daysUntil / 5 // Assume 5-day work week
        val workDaysLeft = workWeeksLeft * 5
        val workHoursLeft = workDaysLeft * 8 // Assume 8-hour work day

        // Total potential work days from age 22 (typical career start) to retirement
        val careerStart = birthDate.plusYears(22)
        val totalWorkDays = ChronoUnit.DAYS.between(careerStart, retirementDate).toDouble()
        val workDaysCompleted = if (careerStart.isBefore(today)) {
            ChronoUnit.DAYS.between(careerStart, today).toDouble()
        } else 0.0

        val percentComplete = if (totalWorkDays > 0) {
            ((workDaysCompleted / totalWorkDays) * 100).toInt().coerceIn(0, 100)
        } else 0

        return RetirementResult(
            daysUntilRetirement = daysUntil,
            workWeeksLeft = workWeeksLeft,
            workDaysLeft = workDaysLeft,
            workHoursLeft = workHoursLeft,
            percentOfWorkLifeComplete = percentComplete,
            retirementAge = retirementAge,
        )
    }
}
