package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Unit tests for [WesternZodiacCalculator] — sign-index math, cusp detection,
 * and Moon-sign behavior.
 */
class WesternZodiacCalculatorTest {

    private lateinit var calculator: WesternZodiacCalculator

    @Before
    fun setUp() {
        calculator = WesternZodiacCalculator(AstronomicalCalculator())
    }

    @Test
    fun `sign names list has 12 entries`() {
        assertEquals(12, calculator.signNames.size)
    }

    @Test
    fun `sign name without emoji`() {
        assertEquals("Aries", calculator.getSignName(0))
        assertEquals("Pisces", calculator.getSignName(11))
    }

    @Test
    fun `J2000 noon gives Capricorn-ish longitude near 280 degrees`() {
        // 2000-01-01 12:00 UT — Sun is at ~280.37° → Capricorn (index 9).
        val index = calculator.getSignIndex(LocalDate.of(2000, 1, 1), LocalTime.of(12, 0), ZoneOffset.UTC)
        assertEquals(9, index)
    }

    @Test
    fun `vernal equinox 2000 returns Aries index`() {
        // 2000-03-20 07:35 UT — vernal equinox; Sun ≈ 0° → Aries (index 0).
        val index = calculator.getSignIndex(LocalDate.of(2000, 3, 20), LocalTime.of(7, 35), ZoneOffset.UTC)
        assertEquals(0, index)
    }

    @Test
    fun `no crash across full year sweep`() {
        val start = LocalDate.of(2000, 1, 1)
        for (i in 0..364) {
            calculator.getZodiac(start.plusDays(i.toLong()))
        }
    }

    @Test
    fun `getZodiac returns plain sign name when not at cusp`() {
        // 2000-06-25 = ~4 days after summer solstice — Sun at ~94° Cancer (well inside).
        val zodiac = calculator.getZodiac(LocalDate.of(2000, 6, 25), LocalTime.of(12, 0), ZoneOffset.UTC)
        assertEquals("Cancer ♋", zodiac)
    }

    @Test
    fun `getZodiac marks cusp when within 1 degree of sign boundary`() {
        // 2000-04-19 15:00 UT — Sun at ~30.0° (right at Aries-Taurus boundary).
        val astronomy = AstronomicalCalculator()
        val jd = astronomy.julianDay(java.time.LocalDateTime.of(2000, 4, 19, 15, 0))
        val sun = astronomy.sunLongitude(jd)
        val posInSign = sun % 30.0
        // We expect the engine to be very close to 0° or 30° at this moment.
        assertTrue(
            "Sun position-in-sign should be near 0 or 30, was $posInSign (sun=$sun)",
            posInSign < 1.0 || posInSign > 29.0,
        )
        val zodiac = calculator.getZodiac(LocalDate.of(2000, 4, 19), LocalTime.of(15, 0), ZoneOffset.UTC)
        assertTrue("Zodiac should contain Cusp marker: $zodiac", zodiac.contains("Cusp"))
    }
}
