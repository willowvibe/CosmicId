package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class BaZiCalculatorTest {

    private lateinit var calculator: BaZiCalculator

    @Before
    fun setUp() {
        val zodiac = ZodiacCalculator(AstronomicalCalculator())
        calculator = BaZiCalculator(zodiac)
    }

    // -------------------------------------------------------------------------
    // Year pillar
    // -------------------------------------------------------------------------

    @Test
    fun `year pillar for 2024 contains Jia and Chen`() {
        val pillar = calculator.getYearPillar(LocalDate.of(2024, 6, 1))
        assertTrue("Expected Jia-Chen in: $pillar",
            pillar.contains("Jia") && pillar.contains("Chen"))
    }

    @Test
    fun `year pillar for 1984 contains Jia and Zi`() {
        // 1984 is Jia-Zi, start of the 60-year cycle
        val pillar = calculator.getYearPillar(LocalDate.of(1984, 3, 1))
        assertTrue("Expected Jia-Zi in: $pillar",
            pillar.contains("Jia") && pillar.contains("Zi"))
    }

    @Test
    fun `year pillar respects lunar new year cutoff`() {
        // Jan 2000 is still 1999 in Chinese calendar → Ji-Mao / Earth-Rabbit
        val pillar = calculator.getYearPillar(LocalDate.of(2000, 1, 25))
        assertTrue("Expected Rabbit before CNY, got: $pillar", pillar.contains("Rabbit"))
    }

    // -------------------------------------------------------------------------
    // Month pillar
    // -------------------------------------------------------------------------

    @Test
    fun `month pillar returns non empty string`() {
        val pillar = calculator.getMonthPillar(LocalDate.of(1990, 6, 15))
        assertTrue(pillar.isNotBlank())
    }

    @Test
    fun `month pillar contains dash`() {
        val pillar = calculator.getMonthPillar(LocalDate.of(1990, 6, 15))
        assertTrue("Expected stem-branch format with dash: $pillar", pillar.contains("-"))
    }

    @Test
    fun `month pillar for Jia year starts with Bing stem`() {
        // 2024 is Jia-Chen year. First month (寅 Tiger) should be Bing (丙).
        // Feb 4 - Mar 5 = 寅 month
        val pillar = calculator.getMonthPillar(LocalDate.of(2024, 2, 15))
        assertTrue("Expected Bing stem in Jia year first month, got: $pillar",
            pillar.startsWith("Bing"))
    }

    @Test
    fun `month pillar for Wu year starts with Jia stem`() {
        // 2018 is Wu-Xu year (after CNY Feb 16). First month (寅 Tiger) should be Jia (甲).
        val pillar = calculator.getMonthPillar(LocalDate.of(2018, 2, 20))
        assertTrue("Expected Jia stem in Wu year first month, got: $pillar",
            pillar.startsWith("Jia"))
    }

    @Test
    fun `different months produce different pillars`() {
        val p1 = calculator.getMonthPillar(LocalDate.of(2024, 2, 15)) // ~寅
        val p2 = calculator.getMonthPillar(LocalDate.of(2024, 5, 15)) // ~巳
        assertTrue("Different months should differ: $p1 vs $p2", p1 != p2)
    }

    // -------------------------------------------------------------------------
    // Ba Zi summary
    // -------------------------------------------------------------------------

    @Test
    fun `ba zi summary contains year and month`() {
        val summary = calculator.getBaZiSummary(LocalDate.of(1990, 6, 15))
        assertTrue("Expected 'Year:' in summary: $summary", summary.contains("Year:"))
        assertTrue("Expected 'Month:' in summary: $summary", summary.contains("Month:"))
    }

    // -------------------------------------------------------------------------
    // No crash across full year
    // -------------------------------------------------------------------------

    @Test
    fun `no exception for any day in a full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getBaZiSummary(start.plusDays(i.toLong()))
        }
    }
}
