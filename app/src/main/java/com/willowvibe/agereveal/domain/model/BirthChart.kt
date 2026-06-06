package com.willowvibe.agereveal.domain.model

import com.willowvibe.agereveal.domain.AstronomicalCalculator
import com.willowvibe.agereveal.domain.Aspect
import com.willowvibe.agereveal.domain.AspectCalculator
import com.willowvibe.agereveal.domain.DashaInfo
import com.willowvibe.agereveal.domain.DashaCalculator
import com.willowvibe.agereveal.domain.DivisionalChartCalculator
import com.willowvibe.agereveal.domain.EphemerisSnapshot
import com.willowvibe.agereveal.domain.MoonPhase
import com.willowvibe.agereveal.domain.MoonPhaseCalculator
import com.willowvibe.agereveal.domain.NakshatraCalculator
import com.willowvibe.agereveal.domain.NakshatraData
import com.willowvibe.agereveal.domain.NakshatraMetadata
import com.willowvibe.agereveal.domain.NavamsaChart
import com.willowvibe.agereveal.domain.ZodiacCalculator
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
 * @property tropicalAscendant Tropical rising sign (Western Lagna) — separate from
 *           the sidereal Vedic Lagna. Null when no birth time/location is available.
 * @property tithi Lunar day (1-30)
 * @property nakshatra Nakshatra (lunar mansion)
 * @property nakshatraPada Nakshatra pada (quarter)
 * @property nakshatraMetadata Rich nakshatra metadata (lord, deity, gana, symbol, emoji)
 * @property chineseZodiac Chinese zodiac animal
 * @property chineseStemBranch Chinese stem-branch (Heavenly Stem + Earthly Branch)
 * @property planetPositions Map of planet to its zodiac sign
 * @property planetLongitudes Map of planet to its sidereal ecliptic longitude
 *           in degrees (0-360). Used by [com.willowvibe.agereveal.domain.SynastryCalculator]
 *           for chart-to-chart aspect computation (BUG-087).
 * @property isRetrograde Map of planet to retrograde status
 * @property dashaInfo Vimshottari Dasha (Mahadasha + Antardasha) — one-line summary
 * @property dashaDetail Structured Dasha with Pratyantar sub-sub-period (Phase 6.5)
 * @property navamsaChart Navamsa (D-9) divisional chart (Phase 6.5)
 * @property planetaryAspects Conjunctions, sextiles, squares, trines, oppositions (Phase 6.5)
 * @property baZiInfo Four Pillars (Year + Month pillars)
 * @property lunarBirthday Lunar calendar date
 * @property sunLongitude Sun's ecliptic longitude (degrees, 0-360)
 * @property moonLongitude Moon's ecliptic longitude (degrees, 0-360)
 * @property birthMoonPhase The phase of the Moon at the moment of birth — e.g. "Waxing Crescent" with illumination. Phase 6.5+.
 */
@ConsistentCopyVisibility
data class BirthChart internal constructor(
    val snapshot: EphemerisSnapshot,
    val location: GeoLocation?,
    val westernSign: String,
    val westernMoonSign: String,
    val rashi: String,
    val rashiLord: String,
    val ascendant: String,
    val tropicalAscendant: String?,
    val tithi: String,
    val nakshatra: String,
    val nakshatraPada: String,
    val nakshatraMetadata: NakshatraData?,
    val chineseZodiac: String,
    val chineseStemBranch: String,
    val planetPositions: Map<CelestialBody, String>,
    val planetLongitudes: Map<CelestialBody, Double>,
    val isRetrograde: Map<CelestialBody, Boolean>,
    val dashaInfo: String,
    val dashaDetail: DashaInfo?,
    val navamsaChart: NavamsaChart?,
    val planetaryAspects: List<Aspect>,
    val baZiInfo: String,
    val lunarBirthday: String,
    val sunLongitude: Double,
    val moonLongitude: Double,
    val birthMoonPhase: MoonPhase?,
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
            nakshatraCalculator: NakshatraCalculator = NakshatraCalculator(AstronomicalCalculator(), NakshatraMetadata()),
            dashaCalculator: DashaCalculator = DashaCalculator(AstronomicalCalculator()),
            baZiCalculator: BaZiCalculator = BaZiCalculator(zodiacCalculator),
            lunarConverter: LunarCalendarConverter = LunarCalendarConverter(),
            divisionalChartCalculator: DivisionalChartCalculator = DivisionalChartCalculator(),
            aspectCalculator: AspectCalculator = AspectCalculator(AstronomicalCalculator()),
            moonPhaseCalculator: MoonPhaseCalculator = MoonPhaseCalculator(),
        ): BirthChart {
            val astronomy = AstronomicalCalculator()
            val snapshot = astronomy.snapshot(birthDate, birthTime, zoneOffset)

            // Phase 6.5: moon phase at birth — derived from snapshot Sun/Moon longitudes
            // (always available, no extra computation needed). Named "birth moon phase"
            // to distinguish from today's moon phase used by DailyFortuneGenerator.
            val birthMoonPhase = moonPhaseCalculator.calculate(
                snapshot.tropicalSunLongitude,
                snapshot.tropicalMoonLongitude,
            )

            // Compute all derived values
            val westernSign = zodiacCalculator.getWesternZodiac(birthDate, birthTime, zoneOffset)
            val westernMoonSign = zodiacCalculator.getWesternMoonSign(birthDate, birthTime, zoneOffset)
            val rashi = zodiacCalculator.getRashi(birthDate, birthTime, zoneOffset)
            val rashiLord = zodiacCalculator.getRashiLord(birthDate, birthTime, zoneOffset)
            val ascendant = zodiacCalculator.getApproximateAscendant(birthDate, birthTime, zoneOffset, location)
            val tropicalAscendant = if (location != null) {
                // Phase 6.5: tropical rising sign (Western Lagna) — only computable
                // with a precise birth location; falls back to "—" otherwise.
                zodiacCalculator.getApproximateAscendant(birthDate, birthTime, zoneOffset, location)
            } else null
            val tithi = zodiacCalculator.getTithi(birthDate, birthTime, zoneOffset)
            val nakshatraDetails = nakshatraCalculator.getNakshatraDetails(birthDate, birthTime, zoneOffset)
            val nakshatra = nakshatraDetails.name
            val nakshatraPada = "${nakshatraDetails.name} — ${nakshatraDetails.padaName()}"
            val chineseZodiac = zodiacCalculator.getChineseZodiac(birthDate)
            val chineseStemBranch = zodiacCalculator.getChineseStemBranch(birthDate)
            val dashaInfo = dashaCalculator.getDashaInfo(birthDate, birthTime, zoneOffset)
            val dashaDetail = dashaCalculator.getDashaDetail(birthDate, birthTime, zoneOffset)
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
            val planetLongitudes = mutableMapOf<CelestialBody, Double>()

            // Define which planets to compute (Mercury through Pluto)
            val allPlanets = CelestialBody.all

            for (celestialBody in allPlanets) {
                // The Sun and Moon (and lunar nodes) don't have heliocentric orbital
                // elements, so we don't compute a sign from AstronomicalCalculator.Planet.
                // Sun's sign is the Western sign; Moon's is the Moon sign.
                when (celestialBody) {
                    CelestialBody.SUN -> {
                        planetPositions[celestialBody] = westernSign
                        planetLongitudes[celestialBody] = snapshot.siderealSunLongitude
                    }
                    CelestialBody.MOON -> {
                        planetPositions[celestialBody] = westernMoonSign
                        planetLongitudes[celestialBody] = snapshot.siderealMoonLongitude
                    }
                    CelestialBody.RAHU, CelestialBody.KETU -> {
                        // Nodes aren't computed here — would need a dedicated calculation.
                        // Left out of the planetPositions map; UI can omit or label as "—".
                    }
                    else -> {
                        val planet = when (celestialBody) {
                            CelestialBody.MERCURY -> AstronomicalCalculator.Planet.MERCURY
                            CelestialBody.VENUS -> AstronomicalCalculator.Planet.VENUS
                            CelestialBody.MARS -> AstronomicalCalculator.Planet.MARS
                            CelestialBody.JUPITER -> AstronomicalCalculator.Planet.JUPITER
                            CelestialBody.SATURN -> AstronomicalCalculator.Planet.SATURN
                            CelestialBody.URANUS -> AstronomicalCalculator.Planet.URANUS
                            CelestialBody.NEPTUNE -> AstronomicalCalculator.Planet.NEPTUNE
                            CelestialBody.PLUTO -> AstronomicalCalculator.Planet.PLUTO
                            else -> error("Unhandled body: $celestialBody")
                        }

                        val longitude = astronomy.planetLongitude(jd, planet)
                        val signIndex = ((longitude / 30.0).toInt() % 12 + 12) % 12
                        val signName = zodiacCalculator.getWesternSignName(signIndex)

                        planetPositions[celestialBody] = signName
                        isRetrograde[celestialBody] = astronomy.isRetrograde(planet, jd)
                        planetLongitudes[celestialBody] = ((longitude - snapshot.ayanamsa) % 360.0 + 360.0) % 360.0
                    }
                }
            }

            // Phase 6.5: Divisional chart (Navamsa D-9) + planetary aspects.
            val navamsaChart = divisionalChartCalculator.getNavamsaChart(planetLongitudes)
            val planetaryAspects = aspectCalculator.computeAspects(jd, planetLongitudes)

            return BirthChart(
                snapshot = snapshot,
                location = location,
                westernSign = westernSign,
                westernMoonSign = westernMoonSign,
                rashi = rashi,
                rashiLord = rashiLord,
                ascendant = ascendant,
                tropicalAscendant = tropicalAscendant,
                tithi = tithi,
                nakshatra = nakshatra,
                nakshatraPada = nakshatraPada,
                nakshatraMetadata = nakshatraDetails.data,
                chineseZodiac = chineseZodiac,
                chineseStemBranch = chineseStemBranch,
                planetPositions = planetPositions,
                planetLongitudes = planetLongitudes,
                isRetrograde = isRetrograde,
                dashaInfo = dashaInfo,
                dashaDetail = dashaDetail,
                navamsaChart = navamsaChart,
                planetaryAspects = planetaryAspects,
                baZiInfo = baZiInfo,
                lunarBirthday = lunarBirthday,
                sunLongitude = snapshot.tropicalSunLongitude,
                moonLongitude = snapshot.tropicalMoonLongitude,
                birthMoonPhase = birthMoonPhase,
            )
        }
    }
}
