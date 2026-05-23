package com.willowvibe.agereveal.domain.model

import com.willowvibe.agereveal.domain.AstronomicalCalculator
import com.willowvibe.agereveal.domain.EphemerisSnapshot
import com.willowvibe.agereveal.domain.ZodiacCalculator
import com.willowvibe.agereveal.domain.NakshatraCalculator
import com.willowvibe.agereveal.domain.DashaCalculator
import com.willowvibe.agereveal.domain.BaZiCalculator
import com.willowvibe.agereveal.domain.LunarCalendarConverter
import com.willowvibe.agereveal.data.model.GeoLocation
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Comprehensive birth chart model that captures all computed astrological data
 * for a given birth moment.
 *
 * This model serves as a "computed once" container that decouples age math from
 * astrology, allowing all astrological calculations to be performed once and
 * reused across the application without recomputation.
 *
 * @property snapshot The ephemeris snapshot (Sun/Moon positions, ayanamsa, tithi)
 * @property location Optional geographic location for exact calculations
 * @property westernSign Western zodiac sign (tropical, from Sun longitude)
 * @property westernMoonSign Western moon sign (tropical, from Moon longitude)
 * @property rashi Vedic rashi (sidereal Sun sign)
 * @property rashiLord Lord of the rashi
 * @property ascendant Exact or approximate ascendant (Lagna)
 * @property tithi Lunar day (1-30)
 * @property nakshatra Nakshatra (lunar mansion)
 * @property nakshatraPada Nakshatra pada (quarter)
 * @property chineseZodiac Chinese zodiac animal
 * @property chineseStemBranch Chinese stem-branch (Heavenly Stem + Earthly Branch)
 * @property planetPositions Map of planet to its zodiac sign
 * @property isRetrograde Map of planet to retrograde status
 * @property dashaInfo Vimshottari Dasha (Mahadasha + Antardasha)
 * @property baZiInfo Four Pillars (Year + Month pillars)
 * @property lunarBirthday Lunar calendar date
 * @property sunLongitude Sun's ecliptic longitude (degrees, 0-360)
 * @property moonLongitude Moon's ecliptic longitude (degrees, 0-360)
 */
data class BirthChart internal constructor(
    val snapshot: EphemerisSnapshot,
    val location: GeoLocation?,
    val westernSign: String,
    val westernMoonSign: String,
    val rashi: String,
    val rashiLord: String,
    val ascendant: String,
    val tithi: String,
    val nakshatra: String,
    val nakshatraPada: String,
    val chineseZodiac: String,
    val chineseStemBranch: String,
    val planetPositions: Map<CelestialBody, String>,
    val isRetrograde: Map<CelestialBody, Boolean>,
    val dashaInfo: String,
    val baZiInfo: String,
    val lunarBirthday: String,
    val sunLongitude: Double,
    val moonLongitude: Double,
) {

    /**
     * Get the Vedic ayanamsa (precession correction) used in this chart.
     */
    val ayanamsa: Double get() = snapshot.ayanamsa

    /**
     * Get the sidereal Sun longitude.
     */
    val siderealSunLongitude: Double get() = snapshot.siderealSunLongitude

    /**
     * Get the sidereal Moon longitude.
     */
    val siderealMoonLongitude: Double get() = snapshot.siderealMoonLongitude

    /**
     * Get the Julian Day for this birth chart.
     */
    val jd: Double get() = snapshot.jd

    /**
     * Check if this is an exact chart (has birth time) or approximate.
     */
    val isExact: Boolean get() = location != null || (snapshot.jd % 1.0) != 0.0

    /**
     * Generate a summary string for the birth chart.
     */
    fun toSummary(): String {
        return "Sun: $westernSign (${rashi}) | Moon: $westernMoonSign | " +
                "Ascendant: $ascendant | Dasha: $dashaInfo"
    }

    companion object {

        /**
         * Compute a complete birth chart for a given birth date-time and location.
         *
         * This is the main entry point for creating a BirthChart. All calculations
         * are performed once and cached in the returned model.
         *
         * @param birthDate Birth date
         * @param birthTime Optional birth time for precise calculations
         * @param zoneOffset Optional timezone offset
         * @param location Optional geographic location for exact ascendant
         * @param zodiacCalculator Zodiac calculator instance
         * @param nakshatraCalculator Nakshatra calculator instance
         * @param dashaCalculator Dasha calculator instance
         * @param baZiCalculator BaZi calculator instance
         * @param lunarConverter Lunar calendar converter instance
         * @return A complete [BirthChart] for the birth moment
         */
        fun compute(
            birthDate: LocalDate,
            birthTime: LocalTime? = null,
            zoneOffset: ZoneOffset? = null,
            location: GeoLocation? = null,
            zodiacCalculator: ZodiacCalculator = ZodiacCalculator(AstronomicalCalculator()),
            nakshatraCalculator: NakshatraCalculator = NakshatraCalculator(AstronomicalCalculator()),
            dashaCalculator: DashaCalculator = DashaCalculator(AstronomicalCalculator()),
            baZiCalculator: BaZiCalculator = BaZiCalculator(zodiacCalculator),
            lunarConverter: LunarCalendarConverter = LunarCalendarConverter(),
        ): BirthChart {
            val astronomy = AstronomicalCalculator()
            val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)

            // Compute all derived values
            val westernSign = zodiacCalculator.getWesternZodiac(birthDate, birthTime, zoneOffset)
            val westernMoonSign = zodiacCalculator.getWesternMoonSign(birthDate, birthTime, zoneOffset)
            val rashi = zodiacCalculator.getRashi(birthDate, birthTime, zoneOffset)
            val rashiLord = zodiacCalculator.getRashiLord(birthDate, birthTime, zoneOffset)
            val ascendant = zodiacCalculator.getApproximateAscendant(birthDate, birthTime, zoneOffset, location)
            val tithi = zodiacCalculator.getTithi(birthDate, birthTime, zoneOffset)
            val nakshatra = nakshatraCalculator.getNakshatra(birthDate, birthTime, zoneOffset)
            val nakshatraPada = nakshatraCalculator.getNakshatraWithPada(birthDate, birthTime, zoneOffset)
            val chineseZodiac = zodiacCalculator.getChineseZodiac(birthDate)
            val chineseStemBranch = zodiacCalculator.getChineseStemBranch(birthDate)
            val dashaInfo = dashaCalculator.getDashaInfo(birthDate, birthTime, zoneOffset)
            val baZiInfo = baZiCalculator.getBaZiSummary(birthDate)
            val lunarBirthday = lunarConverter.toLunarString(birthDate)

            // Planet positions and retrograde status
            val localDateTime = birthTime?.let { bt -> birthDate.atTime(bt) } ?: birthDate.atStartOfDay()
            val utDateTime = zoneOffset?.let {
                localDateTime.atOffset(it).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime()
            } ?: localDateTime
            val jd = astronomy.julianDay(utDateTime)

            val planetPositions = mutableMapOf<CelestialBody, String>()
            val isRetrograde = mutableMapOf<CelestialBody, Boolean>()

            // Define which planets to compute (Mercury through Pluto)
            val allPlanets = CelestialBody.all

            for (celestialBody in allPlanets) {
                val planet = when (celestialBody) {
                    CelestialBody.MERCURY -> AstronomicalCalculator.Planet.MERCURY
                    CelestialBody.VENUS -> AstronomicalCalculator.Planet.VENUS
                    CelestialBody.MARS -> AstronomicalCalculator.Planet.MARS
                    CelestialBody.JUPITER -> AstronomicalCalculator.Planet.JUPITER
                    CelestialBody.SATURN -> AstronomicalCalculator.Planet.SATURN
                    CelestialBody.URANUS -> AstronomicalCalculator.Planet.URANUS
                    CelestialBody.NEPTUNE -> AstronomicalCalculator.Planet.NEPTUNE
                    CelestialBody.PLUTO -> AstronomicalCalculator.Planet.PLUTO
                }

                val longitude = astronomy.planetLongitude(jd, planet)
                val signIndex = ((longitude / 30.0).toInt() % 12 + 12) % 12
                val signName = zodiacCalculator.getWesternSignName(signIndex)

                planetPositions[celestialBody] = signName
                isRetrograde[celestialBody] = astronomy.isRetrograde(planet, jd)
            }

            return BirthChart(
                snapshot = snapshot,
                location = location,
                westernSign = westernSign,
                westernMoonSign = westernMoonSign,
                rashi = rashi,
                rashiLord = rashiLord,
                ascendant = ascendant,
                tithi = tithi,
                nakshatra = nakshatra,
                nakshatraPada = nakshatraPada,
                chineseZodiac = chineseZodiac,
                chineseStemBranch = chineseStemBranch,
                planetPositions = planetPositions,
                isRetrograde = isRetrograde,
                dashaInfo = dashaInfo,
                baZiInfo = baZiInfo,
                lunarBirthday = lunarBirthday,
                sunLongitude = snapshot.tropicalSunLongitude,
                moonLongitude = snapshot.tropicalMoonLongitude,
            )
        }
    }
}
