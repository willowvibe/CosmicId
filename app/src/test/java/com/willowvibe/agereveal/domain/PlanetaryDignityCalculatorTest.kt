package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlanetaryDignityCalculatorTest {

    private lateinit var calc: PlanetaryDignityCalculator

    @Before
    fun setUp() {
        calc = PlanetaryDignityCalculator()
    }

    @Test
    fun `Sun in Leo at 25 degrees is Own House`() {
        val result = calc.computeDignities(listOf("Sun" to 145.0)) // 145° = Leo 25°
        assertEquals(1, result.size)
        assertEquals("Sun", result[0].planetName)
        assertEquals(PlanetaryDignityCalculator.Dignity.OWN_HOUSE, result[0].dignity)
    }

    @Test
    fun `Sun in Leo at 10 degrees is Moolatrikona`() {
        val result = calc.computeDignities(listOf("Sun" to 130.0)) // 130° = Leo 10°
        assertEquals(PlanetaryDignityCalculator.Dignity.MOOLATRIKONA, result[0].dignity)
    }

    @Test
    fun `Moon in Taurus at 3 degrees is Exalted near exact`() {
        val result = calc.computeDignities(listOf("Moon" to 33.0)) // 33° = Taurus 3°
        assertEquals(PlanetaryDignityCalculator.Dignity.EXALTED, result[0].dignity)
        assertEquals(" near exact", result[0].proximityHint)
    }

    @Test
    fun `Saturn in Libra at 20 degrees is Exalted near exact`() {
        val result = calc.computeDignities(listOf("Saturn" to 200.0)) // 200° = Libra 20°
        assertEquals(PlanetaryDignityCalculator.Dignity.EXALTED, result[0].dignity)
        assertEquals(" near exact", result[0].proximityHint)
    }

    @Test
    fun `Mars in Cancer is Debilitated`() {
        val result = calc.computeDignities(listOf("Mars" to 95.0)) // 95° = Cancer (sign 4)
        assertEquals(PlanetaryDignityCalculator.Dignity.DEBILITATED, result[0].dignity)
    }

    @Test
    fun `Jupiter in Sagittarius 5 degrees is Moolatrikona`() {
        val result = calc.computeDignities(listOf("Jupiter" to 245.0)) // 245° = Sagittarius 5°
        assertEquals(PlanetaryDignityCalculator.Dignity.MOOLATRIKONA, result[0].dignity)
    }

    @Test
    fun `Jupiter in Sagittarius 15 degrees is Own House`() {
        val result = calc.computeDignities(listOf("Jupiter" to 255.0)) // 255° = Sagittarius 15°
        assertEquals(PlanetaryDignityCalculator.Dignity.OWN_HOUSE, result[0].dignity)
    }

    @Test
    fun `Venus in Pisces 27 degrees is Exalted`() {
        val result = calc.computeDignities(listOf("Venus" to 357.0)) // 357° = Pisces 27°
        assertEquals(PlanetaryDignityCalculator.Dignity.EXALTED, result[0].dignity)
    }

    @Test
    fun `Mercury in Aquarius is Neutral`() {
        val result = calc.computeDignities(listOf("Mercury" to 310.0)) // 310° = Aquarius
        assertEquals(PlanetaryDignityCalculator.Dignity.NEUTRAL, result[0].dignity)
    }

    @Test
    fun `unknown planet returns Neutral`() {
        val result = calc.computeDignities(listOf("Pluto" to 100.0))
        assertEquals(PlanetaryDignityCalculator.Dignity.NEUTRAL, result[0].dignity)
    }

    @Test
    fun `displayLabel formats correctly`() {
        val d = PlanetaryDignityCalculator.PlanetaryDignity(
            planetName = "Mars",
            dignity = PlanetaryDignityCalculator.Dignity.EXALTED,
            proximityHint = " near exact",
        )
        assertEquals("Mars — Exalted near exact", d.displayLabel())
    }

    @Test
    fun `debilitation trumps exaltation when both signs match impossible`() {
        // This is a sanity check: exaltation and debilitation are 180° apart,
        // so a planet can never be in both at once.
        // We verify Saturn in Aries (debilitation) is flagged correctly.
        val result = calc.computeDignities(listOf("Saturn" to 5.0)) // 5° = Aries
        assertEquals(PlanetaryDignityCalculator.Dignity.DEBILITATED, result[0].dignity)
    }
}
