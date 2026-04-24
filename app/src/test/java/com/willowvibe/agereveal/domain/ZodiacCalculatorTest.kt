package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ZodiacCalculatorTest {

    private lateinit var calculator: ZodiacCalculator

    private val allWesternSigns = listOf(
        "Aries", "Taurus", "Gemini", "Cancer",
        "Leo", "Virgo", "Libra", "Scorpio",
        "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    @Before
    fun setUp() {
        calculator = ZodiacCalculator(AstronomicalCalculator())
    }

    // -------------------------------------------------------------------------
    // Western zodiac — Sun longitude based
    // -------------------------------------------------------------------------

    @Test
    fun `western zodiac returns a known sign`() {
        val sign = calculator.getWesternZodiac(LocalDate.of(1990, 6, 15))
        val base = sign.removeSuffix(" ⚠ Cusp")
        assertTrue("Unknown sign: $sign", allWesternSigns.any { base.startsWith(it) })
    }

    @Test
    fun `aries around march equinox 2020`() {
        // Vernal equinox 2020 was ~20 Mar 03:50 UTC → Sun enters Aries
        val sign = calculator.getWesternZodiac(LocalDate.of(2020, 3, 21))
        assertTrue("Expected Aries around equinox, got: $sign", sign.contains("Aries"))
    }

    @Test
    fun `libra around september equinox 2020`() {
        // Autumnal equinox 2020 was ~22 Sep 13:30 UTC → Sun enters Libra
        val sign = calculator.getWesternZodiac(LocalDate.of(2020, 9, 23))
        assertTrue("Expected Libra around equinox, got: $sign", sign.contains("Libra"))
    }

    @Test
    fun `cusp flag appears within 1 degree of boundary`() {
        // Search a full year for any cusp markers — they should be rare but present.
        val start = LocalDate.of(2000, 1, 1)
        var cuspCount = 0
        for (i in 0..364) {
            val result = calculator.getWesternZodiac(start.plusDays(i.toLong()))
            if (result.endsWith(" ⚠ Cusp")) cuspCount++
        }
        // With 12 boundaries and Sun at ~1°/day, we expect ~24 cusp days per year.
        assertTrue("Expected some cusp days, got $cuspCount", cuspCount >= 10)
    }

    @Test
    fun `no exception thrown for any day in a full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getWesternZodiac(start.plusDays(i.toLong()))
        }
    }

    // -------------------------------------------------------------------------
    // Chinese zodiac — Lunar New Year boundary (BUG-004 regression)
    // -------------------------------------------------------------------------

    @Test
    fun `born before CNY 2000 is Rabbit not Dragon`() {
        // CNY 2000 = Feb 5 — Jan 25 is still the Year of the Rabbit (1999)
        val sign = calculator.getChineseZodiac(LocalDate.of(2000, 1, 25))
        assertTrue("Expected Rabbit, got: $sign", sign.contains("Rabbit"))
    }

    @Test
    fun `born on CNY 2000 is Dragon`() {
        val sign = calculator.getChineseZodiac(LocalDate.of(2000, 2, 5))
        assertTrue("Expected Dragon, got: $sign", sign.contains("Dragon"))
    }

    @Test
    fun `born after CNY 2000 is Dragon`() {
        val sign = calculator.getChineseZodiac(LocalDate.of(2000, 3, 1))
        assertTrue("Expected Dragon, got: $sign", sign.contains("Dragon"))
    }

    @Test
    fun `born before CNY 2019 is Dog not Pig`() {
        // CNY 2019 = Feb 5 — Jan 1 2019 should still be Dog (2018)
        val sign = calculator.getChineseZodiac(LocalDate.of(2019, 1, 1))
        assertTrue("Expected Dog, got: $sign", sign.contains("Dog"))
    }

    @Test
    fun `born after CNY 2019 is Pig`() {
        val sign = calculator.getChineseZodiac(LocalDate.of(2019, 2, 6))
        assertTrue("Expected Pig, got: $sign", sign.contains("Pig"))
    }

    @Test
    fun `year 1900 march is Rat`() {
        // 1900 is year 0 in the cycle — Rat
        val sign = calculator.getChineseZodiac(LocalDate.of(1900, 3, 1))
        assertTrue("Expected Rat, got: $sign", sign.contains("Rat"))
    }

    @Test
    fun `chinese zodiac cycle repeats every 12 years`() {
        val date1 = LocalDate.of(2000, 6, 1)
        val date2 = LocalDate.of(2012, 6, 1)
        assertEquals(
            calculator.getChineseZodiac(date1),
            calculator.getChineseZodiac(date2),
        )
    }

    // -------------------------------------------------------------------------
    // Rashi cusp detection (BUG-005)
    // -------------------------------------------------------------------------

    @Test
    fun `getRashi returns a non-empty string`() {
        val rashi = calculator.getRashi(LocalDate.of(1990, 6, 15))
        assertTrue(rashi.isNotBlank())
    }

    @Test
    fun `getRashi cusp marker is present or absent — no crash`() {
        // Exercise many dates to ensure no crash; cusp detection must not throw
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            val d = start.plusDays(i.toLong())
            calculator.getRashi(d) // must not throw
        }
    }

    // -------------------------------------------------------------------------
    // Rashi Lord
    // -------------------------------------------------------------------------

    @Test
    fun `rashi lord returns a known planet`() {
        val lord = calculator.getRashiLord(LocalDate.of(1990, 6, 15))
        val knownLords = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")
        assertTrue("Unknown lord: $lord", lord in knownLords)
    }

    @Test
    fun `rashi lord matches rashi index`() {
        // Mesha (Aries) lord = Mars
        val meshaLord = calculator.getRashiLord(LocalDate.of(2020, 4, 15))
        assertEquals("Mars", meshaLord)
    }

    @Test
    fun `no exception thrown for rashi lord across full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getRashiLord(start.plusDays(i.toLong()))
        }
    }
}
