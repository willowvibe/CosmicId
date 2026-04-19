package com.willowvibe.agereveal.domain

import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor
import kotlin.math.sin

/**
 * Lightweight ephemeris used for sidereal Rashi (Sun sign) and Nakshatra (lunar mansion).
 *
 * Formulas are low-precision variants from Meeus, "Astronomical Algorithms".
 * Positions are computed at 12:00 UT of the birth date — adequate for the Sun (≈1°/day
 * motion) but the Moon (≈13°/day) can be off by up to half a nakshatra without a birth
 * time. The Lahiri (Chitrapaksha) ayanamsa is applied to convert tropical longitudes to
 * sidereal.
 */
@Singleton
class AstronomicalCalculator @Inject constructor() {

    /** Julian Day Number at 12:00 UT on the given civil date. */
    fun julianDayNoon(date: LocalDate): Double {
        var y = date.year
        var m = date.monthValue
        if (m <= 2) {
            y -= 1; m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716)) +
                floor(30.6001 * (m + 1)) +
                date.dayOfMonth + b - 1524.5 + 0.5
    }

    /** Sun's tropical ecliptic longitude in degrees, normalised to [0, 360). */
    fun sunLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = norm360(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = Math.toRadians(norm360(357.52911 + 35999.05029 * t - 0.0001537 * t * t))
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(m) +
                (0.019993 - 0.000101 * t) * sin(2 * m) +
                0.000289 * sin(3 * m)
        return norm360(l0 + c)
    }

    /** Moon's tropical ecliptic longitude in degrees, normalised to [0, 360). */
    fun moonLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val lp = norm360(218.3164477 + 481267.88123421 * t)
        val d = Math.toRadians(norm360(297.8501921 + 445267.1114034 * t))
        val ms = Math.toRadians(norm360(357.5291092 + 35999.0502909 * t))
        val mp = Math.toRadians(norm360(134.9633964 + 477198.8675055 * t))
        val correction =
            6.289 * sin(mp) +
                    -1.274 * sin(mp - 2 * d) +
                    0.658 * sin(2 * d) +
                    -0.186 * sin(ms) +
                    -0.059 * sin(2 * mp - 2 * d) +
                    -0.057 * sin(mp - 2 * d + ms) +
                    0.053 * sin(mp + 2 * d) +
                    0.046 * sin(2 * d - ms) +
                    0.041 * sin(mp - ms) +
                    -0.035 * sin(d) +
                    -0.031 * sin(mp + ms)
        return norm360(lp + correction)
    }

    /** Lahiri ayanamsa in degrees. 23°51'11" at J2000 + ~50.29"/year of precession. */
    fun lahiriAyanamsa(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        return 23.85306 + t * (5029.0 / 3600.0)
    }

    fun siderealSunLongitude(birthDate: LocalDate, birthTime: LocalTime? = null): Double {
        val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atTime(12, 0)
        val jd = julianDay(localDateTime)
        return norm360(sunLongitude(jd) - lahiriAyanamsa(jd))
    }

    fun siderealMoonLongitude(birthDate: LocalDate, birthTime: LocalTime? = null): Double {
        val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atTime(12, 0)
        val jd = julianDay(localDateTime)
        return norm360(moonLongitude(jd) - lahiriAyanamsa(jd))
    }

    /** Julian Day Number at the given date-time (for precise calculations with birth time). */
    fun julianDay(dateTime: java.time.LocalDateTime): Double {
        val date = dateTime.toLocalDate()
        var y = date.year
        var m = date.monthValue
        if (m <= 2) {
            y -= 1; m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        val dayFraction = (dateTime.hour + dateTime.minute / 60.0 + dateTime.second / 3600.0) / 24.0
        return floor(365.25 * (y + 4716)) +
                floor(30.6001 * (m + 1)) +
                date.dayOfMonth + b - 1524.5 + dayFraction
    }

    private fun norm360(x: Double): Double = ((x % 360.0) + 360.0) % 360.0
}
