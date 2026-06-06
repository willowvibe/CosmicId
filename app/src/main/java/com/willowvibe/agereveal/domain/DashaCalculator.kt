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
 * **Period hierarchy (Vimshottari sub-periods):**
 *  - **Mahadasha** — major period (length: 7–19 years per planet, total cycle 120y)
 *  - **Antardasha** — sub-period of Mahadasha (length: mahadasha × sub_lord / 120)
 *  - **Pratyantar Dasha** — sub-sub-period of Antardasha (length: antardasha × sub_lord / 120)
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
     * Computes the current Mahadasha, Antardasha, and Pratyantar Dasha for a given
     * birth date-time. Returns a structured [DashaInfo] for the UI to display.
     *
     * @param today Defaults to today; can be overridden for testing.
     */
    fun getDashaDetail(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
        today: LocalDate = LocalDate.now(),
    ): DashaInfo {
        val (mahaIndex, mahaYearsInto) = currentMahadasha(birthDate, birthTime, zoneOffset, today)
        val mahaLord = dashaLords[mahaIndex].first
        val mahaTotalYears = dashaLords[mahaIndex].second

        // Antardasha = sub-period of the Mahadasha.
        val antarSequence = generateSequence(mahaIndex) { (it + 1) % 9 }.take(9).toList()
        var yearsIn = mahaYearsInto
        var antarIndex = 0
        var antarTotalYears = 0.0
        var antarYearsInto = 0.0
        for ((i, lordIndex) in antarSequence.withIndex()) {
            val subYears = mahaTotalYears * (dashaLords[lordIndex].second / totalCycleYears)
            if (yearsIn < subYears) {
                antarIndex = lordIndex
                antarTotalYears = subYears
                antarYearsInto = yearsIn
                break
            }
            yearsIn -= subYears
        }
        val antarLord = dashaLords[antarIndex].first

        // Pratyantar = sub-sub-period of the Antardasha.
        val pratyantSequence = generateSequence(antarIndex) { (it + 1) % 9 }.take(9).toList()
        var yearsIn2 = antarYearsInto
        var pratyantIndex = 0
        var pratyantTotalYears = 0.0
        var pratyantYearsInto = 0.0
        for (lordIndex in pratyantSequence) {
            val subYears = antarTotalYears * (dashaLords[lordIndex].second / totalCycleYears)
            if (yearsIn2 < subYears) {
                pratyantIndex = lordIndex
                pratyantTotalYears = subYears
                pratyantYearsInto = yearsIn2
                break
            }
            yearsIn2 -= subYears
        }
        val pratyantLord = dashaLords[pratyantIndex].first

        return DashaInfo(
            mahadasha = DashaPeriod(
                lord = mahaLord,
                totalYears = mahaTotalYears,
                yearsElapsed = mahaYearsInto,
                yearsRemaining = mahaTotalYears - mahaYearsInto,
            ),
            antardasha = DashaPeriod(
                lord = antarLord,
                totalYears = antarTotalYears,
                yearsElapsed = antarYearsInto,
                yearsRemaining = antarTotalYears - antarYearsInto,
            ),
            pratyantar = DashaPeriod(
                lord = pratyantLord,
                totalYears = pratyantTotalYears,
                yearsElapsed = pratyantYearsInto,
                yearsRemaining = pratyantTotalYears - pratyantYearsInto,
            ),
        )
    }

    /**
     * Returns the current Mahadasha, Antardasha, and Pratyantar as a formatted
     * "Mahadasha · Antardasha · Pratyantar" string. Use [getDashaDetail] for the
     * structured form.
     */
    fun getDashaInfo(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
        today: LocalDate = LocalDate.now(),
    ): String {
        val info = getDashaDetail(birthDate, birthTime, zoneOffset, today)
        return "${info.mahadasha.lord} Mahadasha · ${info.antardasha.lord} Antardasha · ${info.pratyantar.lord} Pratyantar"
    }

    /**
     * Walk the Mahadasha cycle to find the current Mahadasha index and how many
     * years into it we are. Exposed internally so the structured detail method
     * can use it; the original string-based public API also relies on this.
     */
    private fun currentMahadasha(
        birthDate: LocalDate,
        birthTime: LocalTime?,
        zoneOffset: ZoneOffset?,
        today: LocalDate,
    ): Pair<Int, Double> {
        val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)
        val moonLongitude = snapshot.siderealMoonLongitude
        val nakshatraIndex = ((moonLongitude / nakshatraArc).toInt() % 27 + 27) % 27
        val posInNakshatra = moonLongitude % nakshatraArc

        val startLordIndex = nakshatraIndex % 9
        val startLordYears = dashaLords[startLordIndex].second
        val remainingFraction = 1.0 - (posInNakshatra / nakshatraArc)
        val firstDashaRemainingYears = startLordYears * remainingFraction

        val birthDateTime = birthTime?.let { birthDate.atTime(it) } ?: birthDate.atStartOfDay()
        val todayDateTime = today.atStartOfDay()
        val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(birthDateTime.toLocalDate(), todayDateTime.toLocalDate())
        val elapsedYears = elapsedDays / 365.25

        var remainingToWalk = elapsedYears
        var currentMahadashaIndex = startLordIndex
        var yearsIntoMahadasha = 0.0

        if (remainingToWalk <= firstDashaRemainingYears) {
            yearsIntoMahadasha = remainingToWalk
        } else {
            remainingToWalk -= firstDashaRemainingYears
            currentMahadashaIndex = (currentMahadashaIndex + 1) % 9
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
        return currentMahadashaIndex to yearsIntoMahadasha
    }
}

/**
 * Structured Dasha information for a given moment — the current Mahadasha,
 * Antardasha (sub-period), and Pratyantar (sub-sub-period). All three are
 * exposed for the DetailsUnlockScreen "Dasha Detail" card to render a tree.
 */
data class DashaInfo(
    val mahadasha: DashaPeriod,
    val antardasha: DashaPeriod,
    val pratyantar: DashaPeriod,
) {
    /**
     * One-line summary, e.g. "Jupiter Mahadasha · Saturn Antardasha · Mercury Pratyantar".
     */
    fun summary(): String =
        "${mahadasha.lord} Mahadasha · ${antardasha.lord} Antardasha · ${pratyantar.lord} Pratyantar"
}

/**
 * A single Dasha period — the ruling planet, total length, and elapsed/remaining
 * time in years. The same shape is used for Mahadasha, Antardasha, and Pratyantar.
 */
data class DashaPeriod(
    val lord: String,
    val totalYears: Double,
    val yearsElapsed: Double,
    val yearsRemaining: Double,
) {
    /** Display string: e.g. "Jupiter · 16y (8.2 elapsed, 7.8 remaining)". */
    fun displayLabel(): String =
        "$lord · ${"%.0f".format(totalYears)}y " +
            "(${"%.1f".format(yearsElapsed)} elapsed, ${"%.1f".format(yearsRemaining)} remaining)"
}
