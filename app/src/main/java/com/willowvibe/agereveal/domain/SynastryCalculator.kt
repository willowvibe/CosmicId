package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.BirthChart
import com.willowvibe.agereveal.domain.model.CelestialBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Synastry — the branch of astrology that compares two birth charts to
 * evaluate relationship dynamics. Whereas [AspectCalculator] looks at aspects
 * *within* a single chart (e.g. "my Sun trine my Moon"), this calculator
 * looks at aspects *between* two charts (e.g. "my Sun trine your Moon").
 *
 * BUG-087: powers the "Inter-chart aspects" panel in
 * [com.willowvibe.agereveal.ui.screen.CompatibilityScreen]. Complements
 * [ZodiacCompatibilityCalculator] (sign-based scoring) and
 * [VedicCompatibilityCalculator] (Guna Milan) with real angular geometry.
 *
 * **Composite score** weights conjunction/trine (harmonious) above square/
 * opposition (tense) so the resulting 0-100 score tracks "how smoothly the
 * charts interlock" rather than "how many aspects exist." SexTiles are
 * counted as mildly harmonious (opportunity, not destiny).
 *
 * Bodies considered: Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn,
 * Uranus, Neptune, Pluto. Rahu/Ketu are excluded (lunar-node cross-aspects
 * would be a separate Vedic-only feature).
 */
@Singleton
class SynastryCalculator @Inject constructor() {

    /**
     * Compute inter-chart aspects between [chartA] and [chartB], plus a
     * weighted compatibility score in 0..100.
     *
     * The two charts may have been computed at different times and locations;
     * the only thing that matters here is the per-body sidereal longitudes.
     *
     * Self-aspect guard: when the same [BirthChart] instance is passed as
     * both A and B (e.g. a UI bug, a unit test, or a feature that compares a
     * person to themselves), the result is the empty set. The guard is
     * reference-equality (`===`) because two distinct people born at the
     * same instant legitimately should be reported as having every
     * conjunction in common.
     */
    fun calculate(chartA: BirthChart, chartB: BirthChart): Synastry {
        val selfComparison = chartA === chartB
        val longitudesA = chartA.planetLongitudes
        val longitudesB = chartB.planetLongitudes
        val bodiesA = longitudesA.keys.filter { it in SYN_ASTRY_BODIES }
        val bodiesB = longitudesB.keys.filter { it in SYN_ASTRY_BODIES }

        val aspects = mutableListOf<SynastryAspect>()

        for (b1 in bodiesA) {
            val lon1 = longitudesA[b1] ?: continue
            for (b2 in bodiesB) {
                if (selfComparison) continue
                val lon2 = longitudesB[b2] ?: continue
                val angle = signedAngle(lon1, lon2)
                val exactAngle = kotlin.math.abs(angle)
                ASPECT_DEFS.forEach { def ->
                    val orb = abs(exactAngle - def.exactDegree)
                    if (orb <= def.orb) {
                        aspects.add(
                            SynastryAspect(
                                personAPlanet = b1,
                                personBPlanet = b2,
                                type = def.type,
                                exactDegree = def.exactDegree,
                                orb = orb,
                            )
                        )
                    }
                }
            }
        }

        val sorted = aspects.sortedBy { it.orb }
        val score = computeScore(sorted)
        return Synastry(aspects = sorted, score = score)
    }

    /**
     * 0..100 weighted score. Each aspect contributes its weight, modulated by
     * orb tightness (tighter orb = stronger contribution). Cap at 100.
     */
    private fun computeScore(aspects: List<SynastryAspect>): Int {
        if (aspects.isEmpty()) return 0
        var raw = 0.0
        for (a in aspects) {
            // The closer to exact, the higher the contribution. Within orb,
            // contribution = weight * (1 - orb / maxOrb).
            val maxOrb = ASPECT_DEFS.first { it.type == a.type }.orb
            val tightness = 1.0 - (a.orb / maxOrb)
            raw += a.type.synastryWeight * tightness
        }
        // Calibrated so a "rich" synastry (5+ aspects, mixed harmonious/tense)
        // lands in the 60-90 range. The constants were tuned against known
        // couples in the VedicCompatibilityCalculatorTest reference set.
        val normalized = (raw / REFERENCE_SUM) * 100.0
        return normalized.coerceIn(0.0, 100.0).toInt()
    }

    private fun signedAngle(from: Double, to: Double): Double {
        var diff = (to - from) % 360.0
        if (diff > 180.0) diff -= 360.0
        if (diff <= -180.0) diff += 360.0
        return diff
    }

    companion object {
        /** Bodies considered for cross-chart aspects. */
        val SYN_ASTRY_BODIES: Set<CelestialBody> = setOf(
            CelestialBody.SUN,
            CelestialBody.MOON,
            CelestialBody.MERCURY,
            CelestialBody.VENUS,
            CelestialBody.MARS,
            CelestialBody.JUPITER,
            CelestialBody.SATURN,
            CelestialBody.URANUS,
            CelestialBody.NEPTUNE,
            CelestialBody.PLUTO,
        )

        /**
         * Reference sum used to normalize the score. Tuned so a typical
         * "5-aspect, mostly harmonious" synastry lands near 70/100. Adjust
         * with care — changing this shifts the meaning of the score.
         */
        private const val REFERENCE_SUM = 7.0

        private val ASPECT_DEFS: List<AspectDef> = listOf(
            AspectDef(AspectType.CONJUNCTION, 0.0, 8.0),
            AspectDef(AspectType.SEXTILE, 60.0, 6.0),
            AspectDef(AspectType.SQUARE, 90.0, 7.0),
            AspectDef(AspectType.TRINE, 120.0, 8.0),
            AspectDef(AspectType.OPPOSITION, 180.0, 7.0),
        )
    }
}

/** Result of a synastry calculation. */
data class Synastry(
    val aspects: List<SynastryAspect>,
    val score: Int,
) {
    /**
     * Verdict bucket based on [score]. 0-19 Cold, 20-39 Mixed, 40-59 Warm,
     * 60-79 Strong, 80-100 Intense. Used for the UI headline ("Strong
     * connection — 74/100").
     */
    fun verdict(): String = when {
        score >= 80 -> "Intense"
        score >= 60 -> "Strong"
        score >= 40 -> "Warm"
        score >= 20 -> "Mixed"
        else -> "Cold"
    }

    /**
     * Group aspects by type for the UI accordion ("Harmonious" vs "Tense"
     * sections). Trines + Sextiles + Conjunctions go in Harmonious; Squares
     * + Oppositions go in Tense. Conjunctions are blended (read as "the
     * energy merges") so they count as mildly harmonious.
     */
    fun grouped(): Pair<List<SynastryAspect>, List<SynastryAspect>> {
        val harmonious = aspects.filter {
            it.type == AspectType.CONJUNCTION ||
                it.type == AspectType.SEXTILE ||
                it.type == AspectType.TRINE
        }
        val tense = aspects.filter {
            it.type == AspectType.SQUARE || it.type == AspectType.OPPOSITION
        }
        return harmonious to tense
    }
}

/** A single cross-chart aspect row. */
data class SynastryAspect(
    val personAPlanet: CelestialBody,
    val personBPlanet: CelestialBody,
    val type: AspectType,
    val exactDegree: Double,
    val orb: Double,
) {
    fun displayLabel(personAName: String = "A", personBName: String = "B"): String =
        "$personAName ${personAPlanet.displayName} ${type.symbol} " +
            "$personBName ${personBPlanet.displayName} " +
            "(${type.displayName}, ${"%.1f".format(orb)}°)"
}

private data class AspectDef(
    val type: AspectType,
    val exactDegree: Double,
    val orb: Double,
)

/**
 * Per-aspect weight used by [SynastryCalculator.computeScore]. Trines are
 * the strongest harmoniser, oppositions the strongest tension.
 */
private val AspectType.synastryWeight: Double
    get() = when (this) {
        AspectType.CONJUNCTION -> 1.2
        AspectType.SEXTILE -> 0.8
        AspectType.SQUARE -> -0.6
        AspectType.TRINE -> 1.5
        AspectType.OPPOSITION -> -0.4
    }
