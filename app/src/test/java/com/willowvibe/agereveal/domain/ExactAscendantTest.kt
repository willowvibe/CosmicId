package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.data.model.GeoLocation
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ExactAscendantTest {

    private lateinit var astronomy: AstronomicalCalculator
    private lateinit var zodiac: ZodiacCalculator

    @Before
    fun setUp() {
        astronomy = AstronomicalCalculator()
        zodiac = ZodiacCalculator(astronomy)
    }

    @Test
    fun `exact ascendant differs from approximate for non equatorial latitude`() {
        val date = LocalDate.of(1990, 6, 15)
        val time = LocalTime.of(12, 0)
        val zone = java.time.ZoneOffset.ofHoursMinutes(5, 30)

        val approx = zodiac.getApproximateAscendant(date, time, zone)
        val exact = zodiac.getApproximateAscendant(
            date, time, zone,
            GeoLocation(latitude = 19.07, longitude = 72.87), // Mumbai
        )

        // Results should differ because Mumbai is far from the equator
        assertTrue("Exact ($exact) should differ from approximate ($approx)", exact != approx)
    }

    @Test
    fun `exact ascendant returns valid rashi`() {
        val date = LocalDate.of(1990, 6, 15)
        val time = LocalTime.of(12, 0)
        val zone = java.time.ZoneOffset.UTC
        val result = zodiac.getApproximateAscendant(
            date, time, zone,
            GeoLocation(latitude = 51.5, longitude = -0.1), // London
        )
        val knownRashis = listOf(
            "Mesha", "Vrishabha", "Mithuna", "Karka",
            "Simha", "Kanya", "Tula", "Vrishchika",
            "Dhanus", "Makara", "Kumbha", "Meena",
        )
        val base = result.removeSuffix(" ⚠ Cusp")
        assertTrue("Unknown ascendant: $result", knownRashis.any { base.startsWith(it) })
    }

    @Test
    fun `no crash for edge latitudes`() {
        val date = LocalDate.of(2000, 1, 1)
        val time = LocalTime.of(12, 0)

        // North pole
        zodiac.getApproximateAscendant(date, time, null, GeoLocation(90.0, 0.0))
        // South pole
        zodiac.getApproximateAscendant(date, time, null, GeoLocation(-90.0, 0.0))
        // Equator
        zodiac.getApproximateAscendant(date, time, null, GeoLocation(0.0, 0.0))
        // Max longitude
        zodiac.getApproximateAscendant(date, time, null, GeoLocation(0.0, 180.0))
        zodiac.getApproximateAscendant(date, time, null, GeoLocation(0.0, -180.0))
    }
}
