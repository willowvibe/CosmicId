package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.BirthChart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Unit tests for [SynastryCalculator] — verifies cross-chart aspects and the
 * composite 0..100 score.
 *
 * BUG-087: closes the "Compatibility uses no synastry" gap. The two-person
 * screen previously computed only sign-based scoring; this adds real angular
 * geometry between the two charts.
 */
class SynastryCalculatorTest {

    private val calculator = SynastryCalculator()

    private fun chart(
        date: LocalDate,
        time: LocalTime = LocalTime.of(12, 0),
    ): BirthChart = BirthChart.compute(
        birthDate = date,
        birthTime = time,
        zoneOffset = ZoneOffset.UTC,
    )

    @Test
    fun `identical chart object passed twice produces no self-aspects`() {
        val a = chart(LocalDate.of(1990, 6, 15))
        // When the SAME chart object is passed as both A and B, the
        // `chartA === chartB` short-circuit prevents self-aspects (we don't
        // want a chart to be "compatible" with itself).
        val syn = calculator.calculate(a, a)
        assertNotNull(syn)
        assertEquals(0, syn.aspects.size)
        assertEquals(0, syn.score)
    }

    @Test
    fun `two charts from same birth moment have all same-body aspects in conjunction`() {
        // Two charts built from the same birth data have identical longitudes,
        // so for every body B the pair (A.B, B.B) is a perfect conjunction.
        // Cross-body pairs (e.g. A.Moon vs B.Jupiter) reflect the natural
        // separation of Moon and Jupiter in that chart, not a test failure.
        val a = chart(LocalDate.of(1990, 6, 15))
        val b = chart(LocalDate.of(1990, 6, 15))
        val syn = calculator.calculate(a, b)
        // All bodies in SYN_ASTRY_BODIES are present in both charts, so there
        // should be at least 10 same-body conjunction entries (Sun, Moon,
        // Mercury, Venus, Mars, Jupiter, Saturn, Uranus, Neptune, Pluto).
        val sameBodyConjunctions = syn.aspects.filter {
            it.personAPlanet == it.personBPlanet && it.type == AspectType.CONJUNCTION
        }
        assertTrue(
            "Should have same-body conjunctions for every synastry body, got ${sameBodyConjunctions.size}",
            sameBodyConjunctions.size >= SynastryCalculator.SYN_ASTRY_BODIES.size,
        )
        // Every same-body aspect must be a conjunction with near-zero orb.
        for (asp in sameBodyConjunctions) {
            assertTrue(
                "Same-body aspect orb ${asp.orb} should be < 0.1°",
                asp.orb < 0.1,
            )
        }
    }

    @Test
    fun `score is in 0 to 100 range`() {
        val a = chart(LocalDate.of(1990, 6, 15))
        val b = chart(LocalDate.of(1990, 12, 25)) // half a year later
        val syn = calculator.calculate(a, b)
        assertTrue("Score ${syn.score} should be in 0..100", syn.score in 0..100)
    }

    @Test
    fun `verdict buckets follow the documented thresholds`() {
        val synEmpty = Synastry(aspects = emptyList(), score = 0)
        val synCold = Synastry(aspects = emptyList(), score = 15)
        val synMixed = Synastry(aspects = emptyList(), score = 30)
        val synWarm = Synastry(aspects = emptyList(), score = 50)
        val synStrong = Synastry(aspects = emptyList(), score = 70)
        val synIntense = Synastry(aspects = emptyList(), score = 90)
        assertEquals("Cold", synEmpty.verdict())
        assertEquals("Cold", synCold.verdict())
        assertEquals("Mixed", synMixed.verdict())
        assertEquals("Warm", synWarm.verdict())
        assertEquals("Strong", synStrong.verdict())
        assertEquals("Intense", synIntense.verdict())
    }

    @Test
    fun `grouped splits same-body conjunctions into harmonious bucket`() {
        // With two same-input charts, same-body aspects are all conjunctions
        // and cross-body aspects depend on the chart's natural separations.
        // The harmonious bucket should at minimum contain every same-body
        // conjunction.
        val a = chart(LocalDate.of(1990, 6, 15))
        val b = chart(LocalDate.of(1990, 6, 15))
        val syn = calculator.calculate(a, b)
        val (harmonious, _) = syn.grouped()
        val sameBodyConjunctionsInHarmonious = harmonious.count {
            it.personAPlanet == it.personBPlanet && it.type == AspectType.CONJUNCTION
        }
        assertTrue(
            "All same-body conjunctions should be in the harmonious bucket",
            sameBodyConjunctionsInHarmonious >= SynastryCalculator.SYN_ASTRY_BODIES.size,
        )
    }

    @Test
    fun `cross chart aspect labels include both planet names`() {
        val a = chart(LocalDate.of(1990, 6, 15))
        val b = chart(LocalDate.of(1990, 6, 15))
        val syn = calculator.calculate(a, b)
        assertTrue("Should have at least one aspect", syn.aspects.isNotEmpty())
        val label = syn.aspects.first().displayLabel("Alice", "Bob")
        assertTrue("Label should mention Alice: '$label'", label.contains("Alice"))
        assertTrue("Label should mention Bob: '$label'", label.contains("Bob"))
    }

    @Test
    fun `chart with planetLongitudes feeds synastry`() {
        // BUG-087 wiring check: BirthChart.compute() must populate
        // planetLongitudes so SynastryCalculator has data to work with.
        val a = chart(LocalDate.of(1990, 6, 15))
        assertTrue(
            "BirthChart.planetLongitudes should be populated for synastry",
            a.planetLongitudes.isNotEmpty(),
        )
        // Sun and Moon should always be present
        assertTrue("Sun longitude should be in planetLongitudes", a.planetLongitudes.containsKey(com.willowvibe.agereveal.domain.model.CelestialBody.SUN))
        assertTrue("Moon longitude should be in planetLongitudes", a.planetLongitudes.containsKey(com.willowvibe.agereveal.domain.model.CelestialBody.MOON))
    }

    @Test
    fun `half-year pair produces a non-trivial score`() {
        // Two charts 6 months apart: most fast planets have moved 30+° each,
        // so we'd expect a mix of aspects (trines/sextiles from outer planets,
        // some oppositions from inner). Score should not be 0.
        val a = chart(LocalDate.of(1990, 6, 15))
        val b = chart(LocalDate.of(1990, 12, 15))
        val syn = calculator.calculate(a, b)
        assertTrue("Half-year pair should produce at least one aspect", syn.aspects.isNotEmpty())
        assertTrue("Score ${syn.score} should be > 0 for a half-year pair", syn.score > 0)
    }
}
