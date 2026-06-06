package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Planetary longitude + sign-position computations for the 7 classical
 * geocentric bodies (Sun, Moon, Mercury, Moon, Venus, Mars, Jupiter,
 * Saturn) using [AstronomicalCalculator].
 *
 * Outer planets (Uranus, Neptune, Pluto) are computed by
 * [AstronomicalCalculator] but are not surfaced in the legacy
 * [getPlanetLongitudes] / [getPlanetPositions] API — they have their own
 * longitudes via [AstronomicalCalculator.Planet].
 */
@Singleton
class PlanetaryCalculator @Inject constructor(
    private val astronomy: AstronomicalCalculator,
    private val western: WesternZodiacCalculator,
) {

    /**
     * Raw geocentric tropical ecliptic longitudes for the classical 7
     * planets. Returns (planet name, longitude in degrees [0, 360)) pairs.
     * Useful for dignity calculations that need exact degree positions.
     */
    fun getPlanetLongitudes(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): List<Pair<String, Double>> {
        val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atTime(12, 0)
        val utDateTime = zoneOffset?.let {
            localDateTime.atOffset(it).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
        } ?: localDateTime
        val jd = astronomy.julianDay(utDateTime)

        val sun = astronomy.sunLongitude(jd)
        val moon = astronomy.moonLongitude(jd)

        return listOf(
            "Sun" to sun,
            "Moon" to moon,
            "Mercury" to astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.MERCURY),
            "Venus" to astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.VENUS),
            "Mars" to astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.MARS),
            "Jupiter" to astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.JUPITER),
            "Saturn" to astronomy.planetLongitude(jd, AstronomicalCalculator.Planet.SATURN),
        )
    }

    /**
     * Planet positions summary — geocentric tropical zodiac sign for each
     * classical planet. Returns a list of (planet name, sign) pairs.
     */
    fun getPlanetPositions(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
        zoneOffset: ZoneOffset? = null,
    ): List<Pair<String, String>> {
        val planets = getPlanetLongitudes(birthDate, birthTime, zoneOffset)
        return planets.map { (name, longitude) ->
            val index = ((longitude / 30.0).toInt() % 12 + 12) % 12
            name to western.getSignName(index)
        }
    }
}
