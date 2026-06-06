package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Unit tests for [PlanetaryCalculator] — planet longitudes and sign
 * positions for the 7 classical geocentric bodies.
 */
class PlanetaryCalculatorTest {

    private lateinit var calculator: PlanetaryCalculator
    private lateinit var astronomy: AstronomicalCalculator

    @Before
    fun setUp() {
        astronomy = AstronomicalCalculator()
        calculator = PlanetaryCalculator(astronomy, WesternZodiacCalculator(astronomy))
    }

    @Test
    fun `getPlanetLongitudes returns 7 classical planets`() {
        val planets = calculator.getPlanetLongitudes(LocalDate.of(2000, 1, 1))
        assertEquals(7, planets.size)
        val names = planets.map { it.first }.toSet()
        assertEquals(
            setOf("Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn"),
            names,
        )
    }

    @Test
    fun `all planet longitudes are in 0 to 360 range`() {
        val planets = calculator.getPlanetLongitudes(LocalDate.of(2000, 1, 1))
        for ((name, longitude) in planets) {
            assertTrue("$name longitude $longitude should be in [0, 360)", longitude in 0.0..360.0)
        }
    }

    @Test
    fun `getPlanetPositions has 7 entries with valid sign names`() {
        val positions = calculator.getPlanetPositions(LocalDate.of(2000, 1, 1))
        assertEquals(7, positions.size)
        val validSigns = setOf(
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces",
        )
        for ((name, sign) in positions) {
            assertTrue("$name sign '$sign' should be a western sign", sign in validSigns)
        }
    }

    @Test
    fun `J2000 Sun is in Capricorn`() {
        // Sun at ~280.37° → Capricorn (index 9, 270°–300°)
        val positions = calculator.getPlanetPositions(LocalDate.of(2000, 1, 1), LocalTime.of(12, 0), ZoneOffset.UTC)
        val sun = positions.first { it.first == "Sun" }
        assertEquals("Capricorn", sun.second)
    }
}
