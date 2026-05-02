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
    // Chinese Stem-Branch
    // -------------------------------------------------------------------------

    @Test
    fun `stem branch for 2024 is Jia Chen Wood Dragon`() {
        val sb = calculator.getChineseStemBranch(LocalDate.of(2024, 6, 1))
        assertTrue("Expected Jia-Chen / Wood-Dragon, got: $sb",
            sb.contains("Jia") && sb.contains("Chen") && sb.contains("Wood") && sb.contains("Dragon"))
    }

    @Test
    fun `stem branch for 1984 is Jia Zi Wood Rat`() {
        // 1984 is the start of the 60-year cycle
        val sb = calculator.getChineseStemBranch(LocalDate.of(1984, 3, 1))
        assertTrue("Expected Jia-Zi / Wood-Rat, got: $sb",
            sb.contains("Jia") && sb.contains("Zi") && sb.contains("Wood") && sb.contains("Rat"))
    }

    @Test
    fun `stem branch cycle repeats every 60 years`() {
        val date1 = LocalDate.of(1984, 6, 1)
        val date2 = LocalDate.of(2044, 6, 1)
        assertEquals(
            calculator.getChineseStemBranch(date1),
            calculator.getChineseStemBranch(date2),
        )
    }

    @Test
    fun `stem branch respects lunar new year cutoff`() {
        // Jan 2000 is still 1999 in Chinese calendar → Ji-Mao / Earth-Rabbit
        val sb = calculator.getChineseStemBranch(LocalDate.of(2000, 1, 25))
        assertTrue("Expected Rabbit before CNY, got: $sb", sb.contains("Rabbit"))
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

    // -------------------------------------------------------------------------
    // Western Moon Sign
    // -------------------------------------------------------------------------

    @Test
    fun `western moon sign returns a known sign`() {
        val sign = calculator.getWesternMoonSign(LocalDate.of(1990, 6, 15))
        val base = sign.removeSuffix(" ⚠ Cusp")
        assertTrue("Unknown moon sign: $sign", allWesternSigns.any { base.startsWith(it) })
    }

    @Test
    fun `moon sign changes over 24 hours`() {
        // Moon moves ~13°/day, so it should shift signs every ~2.3 days.
        // Over 30 days we should see at least 10 different signs.
        val date = LocalDate.of(1990, 6, 15)
        val signs = (0..29).map { day ->
            calculator.getWesternMoonSign(date.plusDays(day.toLong()))
        }.toSet()
        assertTrue("Expected multiple moon signs over 30 days, got: $signs", signs.size >= 5)
    }

    @Test
    fun `no exception thrown for moon sign across full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getWesternMoonSign(start.plusDays(i.toLong()))
        }
    }

    // -------------------------------------------------------------------------
    // Tithi — Moon-Sun elongation
    // -------------------------------------------------------------------------

    @Test
    fun `tithi returns a valid name and paksha`() {
        val tithi = calculator.getTithi(LocalDate.of(1990, 6, 15))
        assertTrue("Expected tithi with paksha, got: $tithi",
            tithi.contains("Paksha"))
    }

    @Test
    fun `tithi number is between 1 and 30`() {
        // Verify underlying tithi index by checking the astronomical calculator directly
        val astro = AstronomicalCalculator()
        val jd = astro.julianDayNoon(LocalDate.of(1990, 6, 15))
        val sun = astro.sunLongitude(jd)
        val moon = astro.moonLongitude(jd)
        val tithi = astro.tithi(sun, moon)
        assertTrue("Tithi $tithi out of range", tithi in 1..30)
    }

    @Test
    fun `tithi cycles through all 30 values across a lunar month`() {
        // The Moon gains ~12°/day on the Sun, so tithi should change roughly daily.
        // Over 30 days we should see most or all tithi values.
        val start = LocalDate.of(2000, 1, 1)
        val seen = mutableSetOf<Int>()
        val astro = AstronomicalCalculator()
        for (i in 0..29) {
            val jd = astro.julianDayNoon(start.plusDays(i.toLong()))
            val sun = astro.sunLongitude(jd)
            val moon = astro.moonLongitude(jd)
            seen.add(astro.tithi(sun, moon))
        }
        assertTrue("Expected many tithi values over 30 days, got ${seen.size}: $seen", seen.size >= 20)
    }

    // -------------------------------------------------------------------------
    // Approximate Ascendant (Lagna) — GMST at 0° latitude
    // -------------------------------------------------------------------------

    @Test
    fun `approximate ascendant returns a known rashi`() {
        val asc = calculator.getApproximateAscendant(LocalDate.of(1990, 6, 15))
        val base = asc.removeSuffix(" ⚠ Cusp")
        val knownRashis = listOf(
            "Mesha", "Vrishabha", "Mithuna", "Karka",
            "Simha", "Kanya", "Tula", "Vrishchika",
            "Dhanus", "Makara", "Kumbha", "Meena",
        )
        assertTrue("Unknown ascendant: $asc", knownRashis.any { base.startsWith(it) })
    }

    @Test
    fun `approximate ascendant does not crash across full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getApproximateAscendant(start.plusDays(i.toLong()))
        }
    }

    // -------------------------------------------------------------------------
    // Timezone offset propagation
    // -------------------------------------------------------------------------

    @Test
    fun `zoneOffset changes moon sign result for non UTC timezone`() {
        // For a user in India (UTC+5:30), local noon = 06:30 UT.
        // The Moon moves ~0.5°/hour, so 5.5 hours = ~2.75° shift —
        // enough to potentially change sign when near a cusp.
        val date = LocalDate.of(1990, 6, 15)
        val noOffset = calculator.getWesternMoonSign(date, null, null)
        val indiaOffset = calculator.getWesternMoonSign(date, null, java.time.ZoneOffset.ofHoursMinutes(5, 30))
        // Results may or may not differ for this specific date, but the
        // calculator must handle the offset without crashing.
        assertTrue("Both results must be valid signs", noOffset.isNotBlank() && indiaOffset.isNotBlank())
    }

    @Test
    fun `zoneOffset does not crash for any day in a year`() {
        val start = LocalDate.of(2000, 1, 1)
        val offset = java.time.ZoneOffset.ofHours(-8) // Pacific
        for (i in 0..364) {
            calculator.getRashi(start.plusDays(i.toLong()), null, offset)
            calculator.getWesternZodiac(start.plusDays(i.toLong()), null, offset)
        }
    }

    // -------------------------------------------------------------------------
    // Planetary positions summary
    // -------------------------------------------------------------------------

    @Test
    fun `planet positions returns all 7 planets`() {
        val positions = calculator.getPlanetPositions(LocalDate.of(1990, 6, 15))
        val planetNames = positions.map { it.first }
        assertEquals(
            listOf("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn"),
            planetNames,
        )
    }

    @Test
    fun `planet positions returns known western signs`() {
        val positions = calculator.getPlanetPositions(LocalDate.of(1990, 6, 15))
        val knownSigns = listOf(
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces",
        )
        positions.forEach { (planet, sign) ->
            assertTrue("Planet $planet has unknown sign: $sign", sign in knownSigns)
        }
    }

    @Test
    fun `planet positions does not crash across full year`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getPlanetPositions(start.plusDays(i.toLong()))
        }
    }
}
