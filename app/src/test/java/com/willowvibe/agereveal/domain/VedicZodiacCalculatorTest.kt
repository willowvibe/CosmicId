package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.data.model.GeoLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Unit tests for [VedicZodiacCalculator] — Rashi, Rashi Lord, Tithi, and
 * Lagna cusp behavior.
 */
class VedicZodiacCalculatorTest {

    private lateinit var calculator: VedicZodiacCalculator
    private lateinit var astronomy: AstronomicalCalculator

    @Before
    fun setUp() {
        astronomy = AstronomicalCalculator()
        calculator = VedicZodiacCalculator(astronomy)
    }

    @Test
    fun `rashi names has 12 entries`() {
        assertEquals(12, calculator.rashiNames.size)
    }

    @Test
    fun `rashi lord table has 12 entries`() {
        assertEquals(12, calculator.rashiLords.size)
    }

    @Test
    fun `tithi names has 30 entries`() {
        assertEquals(30, calculator.tithiNames.size)
    }

    @Test
    fun `J2000 sidereal Sun longitude produces a rashi`() {
        // 2000-01-01 12:00 UT — sidereal Sun ≈ 256.5° → Dhanus (Sagittarius, index 8)
        val rashi = calculator.getRashi(LocalDate.of(2000, 1, 1), LocalTime.of(12, 0), ZoneOffset.UTC)
        assertTrue("Expected Dhanus-ish rashi, got: $rashi", rashi.startsWith("Dhanus"))
    }

    @Test
    fun `rashi lord matches the rashi`() {
        val rashi = calculator.getRashi(LocalDate.of(2000, 1, 1), LocalTime.of(12, 0), ZoneOffset.UTC)
        val lord = calculator.getRashiLord(LocalDate.of(2000, 1, 1), LocalTime.of(12, 0), ZoneOffset.UTC)
        // Dhanus lord = Jupiter
        assertTrue("Dhanus lord should be Jupiter, got: $lord", lord == "Jupiter")
    }

    @Test
    fun `tithi output contains Paksha`() {
        val tithi = calculator.getTithi(LocalDate.of(2000, 1, 6), LocalTime.of(12, 0), ZoneOffset.UTC)
        assertTrue("Tithi should contain 'Paksha': $tithi", tithi.contains("Paksha"))
    }

    @Test
    fun `no crash for tithi sweep`() {
        for (i in 0..29) {
            calculator.getTithi(LocalDate.of(2000, 1, 1).plusDays(i.toLong()))
        }
    }

    @Test
    fun `ascendant without location is approximate`() {
        // Without location, we fall back to the equatorial approximation.
        val asc = calculator.getApproximateAscendant(LocalDate.of(1990, 6, 15), LocalTime.of(12, 0), ZoneOffset.UTC)
        assertTrue("Ascendant should be a non-empty rashi name: '$asc'", asc.isNotBlank())
    }

    @Test
    fun `getTropicalAscendantSign returns Western sign name for J2000 epoch`() {
        val western = WesternZodiacCalculator(astronomy)
        val zodiac = ZodiacCalculator(astronomy)

        // 2000-01-01 12:00 UTC, Greenwich
        val birthDate = LocalDate.of(2000, 1, 1)
        val birthTime = LocalTime.of(12, 0)
        val zoneOffset = ZoneOffset.UTC
        val location = GeoLocation(latitude = 51.4779, longitude = -0.0015) // Greenwich

        val tropical = calculator.getTropicalAscendantSign(birthDate, birthTime, zoneOffset, location)
        val tropicalIdx = ((astronomy.exactAscendantLongitude(
            astronomy.julianDay(birthDate.atTime(birthTime).atOffset(zoneOffset).toLocalDateTime()),
            location.latitude, location.longitude
        ) / 30.0).toInt() % 12 + 12) % 12
        val expected = western.getSignName(tropicalIdx)

        assertEquals(expected, tropical)
        // Sanity: tropical and sidereal differ by ~24° (Lahiri ayanamsa)
        assertNotEquals(zodiac.getApproximateAscendant(birthDate, birthTime, zoneOffset, location), tropical)
    }
}
