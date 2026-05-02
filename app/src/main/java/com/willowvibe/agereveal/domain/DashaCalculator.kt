package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

/**
 * Vimshottari Dasha approximation based on the Moon's nakshatra at birth.
 *
 * The 120-year cycle is divided among 9 planets (grahas). The starting Dasha lord
 * and the proportion remaining at birth are determined by the Moon's position within
 * its nakshatra. The result identifies the current Mahadasha (major period) and
 * Antardasha (sub-period) as of today.
 *
 * Accuracy note: without a precise birth time, the Moon can be off by up to half a
 * nakshatra, which may shift the starting Dasha lord. The result should be shown
 * with an "Approximate" label when birth time is absent.
 */
@Singleton
class DashaCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
) {

    private val nakshatraArc = 360.0 / 27.0 // 13°20'

    /** Dasha lords in canonical order and their full periods in years. */
    private val dashaLords = listOf(
        "Ketu" to 7.0,
        "Venus" to 20.0,
        "Sun" to 6.0,
        "Moon" to 10.0,
        "Mars" to 7.0,
        "Rahu" to 18.0,
        "Jupiter" to 16.0,
        "Saturn" to 19.0,
        "Mercury" to 17.0,
    )

    private val totalCycleYears = dashaLords.sumOf { it.second }

    /**
     * Computes the current Mahadasha and Antardasha for a given birth date-time.
     *
     * @param today Defaults to today; can be overridden for testing.
     * @return A formatted string like "Jupiter Mahadasha · Saturn Antardasha".
     */
    fun getDashaInfo(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
        today: LocalDate = LocalDate.now(),
    ): String {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val moonLongitude = snapshot.siderealMoonLongitude
        val nakshatraIndex = ((moonLongitude / nakshatraArc).toInt() % 27 + 27) % 27
        val posInNakshatra = moonLongitude % nakshatraArc

        // Lord index cycles every 9 nakshatras: 0,1,2,3,4,5,6,7,8, 0,1,2...
        val startLordIndex = nakshatraIndex % 9
        val startLordYears = dashaLords[startLordIndex].second
        val remainingFraction = 1.0 - (posInNakshatra / nakshatraArc)
        val firstDashaRemainingYears = startLordYears * remainingFraction

        // Days from birth to today (approximate; leap days are fine for Dasha-level precision)
        val birthDateTime = birthTime?.let { birthDate.atTime(it) } ?: birthDate.atStartOfDay()
        val todayDateTime = today.atStartOfDay()
        val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(birthDateTime.toLocalDate(), todayDateTime.toLocalDate())
        val elapsedYears = elapsedDays / 365.25

        // Walk the Mahadasha cycle to find the current lord and how many years into it we are
        var remainingToWalk = elapsedYears
        var currentMahadashaIndex = startLordIndex
        var yearsIntoMahadasha = 0.0

        // First, consume the remainder of the birth Dasha
        if (remainingToWalk <= firstDashaRemainingYears) {
            yearsIntoMahadasha = remainingToWalk
            remainingToWalk = 0.0
        } else {
            remainingToWalk -= firstDashaRemainingYears
            currentMahadashaIndex = (currentMahadashaIndex + 1) % 9
            // Continue through full cycles
            while (remainingToWalk > 0) {
                val lordYears = dashaLords[currentMahadashaIndex].second
                if (remainingToWalk < lordYears) {
                    yearsIntoMahadasha = remainingToWalk
                    remainingToWalk = 0.0
                } else {
                    remainingToWalk -= lordYears
                    currentMahadashaIndex = (currentMahadashaIndex + 1) % 9
                }
            }
        }

        val mahadashaLord = dashaLords[currentMahadashaIndex].first
        val mahadashaTotalYears = dashaLords[currentMahadashaIndex].second

        // Within the Mahadasha, compute the Antardasha (sub-period)
        // Each sub-period = mahadashaYears * (subLordYears / 120)
        val antardashaSequence = generateSequence(currentMahadashaIndex) { (it + 1) % 9 }.take(9).toList()
        var yearsIntoAntardasha = yearsIntoMahadasha
        var antardashaLord = ""
        for (lordIndex in antardashaSequence) {
            val subYears = mahadashaTotalYears * (dashaLords[lordIndex].second / totalCycleYears)
            if (yearsIntoAntardasha < subYears) {
                antardashaLord = dashaLords[lordIndex].first
                break
            }
            yearsIntoAntardasha -= subYears
        }

        return if (antardashaLord.isNotEmpty()) {
            "$mahadashaLord Mahadasha · $antardashaLord Antardasha"
        } else {
            "$mahadashaLord Mahadasha"
        }
    }
}
