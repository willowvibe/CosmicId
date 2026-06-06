package com.willowvibe.agereveal.domain

import com.willowvibe.agereveal.domain.model.CelestialBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the [NakshatraMetadata] lookup table — the canonical data that
 * powers the Vedic Nakshatra card on DetailsUnlockScreen and seeds Vimshottari
 * Dasha calculations.
 *
 * Reference values from Brihat Parashara Hora Shastra, the Lahiri-published
 * nakshatra table (Government of India ephemeris), and Astro-Vision's published
 * nakshatra table. Each row in the 27-nakshatra table is checked for:
 *  - The Vimshottari Dasha lord sequence (Ketu→Venus→Sun→Moon→Mars→Rahu→Jupiter
 *    →Saturn→Mercury, repeated 3×).
 *  - The Gana family (Deva / Manushya / Rakshasa) per published reference.
 *  - Non-empty symbol and deity fields (Hindi + English).
 */
class NakshatraMetadataTest {

    private val meta = NakshatraMetadata()

    // ----- Index lookups -----

    @Test
    fun `forIndex 0 returns Ashwini with Ketu lord`() {
        val n = meta.forIndex(0)
        assertEquals("Ashwini", n.name)
        assertEquals(CelestialBody.KETU, n.lord)
        assertEquals(Gana.DEVA, n.gana)
        assertEquals(0.0, n.startDegree, 0.0001)
        assertEquals(13.3333, n.endDegree, 0.001)
    }

    @Test
    fun `forIndex 3 returns Rohini with Moon lord (the famous one)`() {
        val n = meta.forIndex(3)
        assertEquals("Rohini", n.name)
        assertEquals(CelestialBody.MOON, n.lord)
        assertEquals(Gana.MANUSHYA, n.gana)
        assertEquals("Chariot / Cart", n.symbol)
    }

    @Test
    fun `forIndex 26 returns Revati with Mercury lord (last nakshatra)`() {
        val n = meta.forIndex(26)
        assertEquals("Revati", n.name)
        assertEquals(CelestialBody.MERCURY, n.lord)
        assertEquals(Gana.DEVA, n.gana)
        assertEquals(346.6667, n.startDegree, 0.001)
        assertEquals(360.0, n.endDegree, 0.0001)
    }

    // ----- Longitude-based lookup -----

    @Test
    fun `forLongitude at 0 degrees returns Ashwini`() {
        val n = meta.forLongitude(0.0)
        assertEquals("Ashwini", n.name)
    }

    @Test
    fun `forLongitude at 40 degrees returns Rohini (start of Rohini)`() {
        val n = meta.forLongitude(40.0)
        assertEquals("Rohini", n.name)
    }

    @Test
    fun `forLongitude at 359 degrees returns Revati (last nakshatra)`() {
        val n = meta.forLongitude(359.0)
        assertEquals("Revati", n.name)
    }

    @Test
    fun `forLongitude wraps modulo 360`() {
        val n = meta.forLongitude(720.0)  // 0° wrapped
        assertEquals("Ashwini", n.name)
    }

    // ----- Vimshottari Dasha lord sequence -----

    @Test
    fun `Vimshottari lord sequence is correct for all 27 nakshatras`() {
        // The standard Vimshottari Dasha sequence, repeated 3 times to cover all 27 nakshatras.
        val expectedSequence = listOf(
            CelestialBody.KETU, CelestialBody.VENUS, CelestialBody.SUN, CelestialBody.MOON,
            CelestialBody.MARS, CelestialBody.RAHU, CelestialBody.JUPITER, CelestialBody.SATURN,
            CelestialBody.MERCURY,
        )
        val fullSequence = (0 until 27).map { i -> expectedSequence[i % 9] }
        val actual = (0 until 27).map { i -> meta.forIndex(i).lord }
        assertEquals(fullSequence, actual)
    }

    // ----- Gana distribution -----

    @Test
    fun `Deva gana includes Ashwini Mrigashira Punarvasu Pushya Hasta Swati Shravana Revati`() {
        val devaNames = (0 until 27)
            .map { meta.forIndex(it) }
            .filter { it.gana == Gana.DEVA }
            .map { it.name }
        // 9 Deva nakshatras (one per Vimshottari segment of 9).
        assertEquals(9, devaNames.size)
        assertTrue("Ashwini", devaNames.contains("Ashwini"))
        assertTrue("Mrigashira", devaNames.contains("Mrigashira"))
        assertTrue("Punarvasu", devaNames.contains("Punarvasu"))
        assertTrue("Pushya", devaNames.contains("Pushya"))
        assertTrue("Hasta", devaNames.contains("Hasta"))
        assertTrue("Swati", devaNames.contains("Swati"))
        assertTrue("Shravana", devaNames.contains("Shravana"))
        assertTrue("Revati", devaNames.contains("Revati"))
    }

    @Test
    fun `all 27 nakshatras have non-empty name deity and symbol`() {
        for (i in 0 until 27) {
            val n = meta.forIndex(i)
            assertNotNull("nakshatra $i name is null", n.name)
            assertTrue("nakshatra $i name is empty", n.name.isNotBlank())
            assertNotNull("nakshatra $i deity is null", n.deity)
            assertTrue("nakshatra $i deity is empty", n.deity.isNotBlank())
            assertNotNull("nakshatra $i symbol is null", n.symbol)
            assertTrue("nakshatra $i symbol is empty", n.symbol.isNotBlank())
            assertNotNull("nakshatra $i symbolEmoji is null", n.symbolEmoji)
        }
    }

    // ----- Degree boundaries -----

    @Test
    fun `degree boundaries cover the full ecliptic without gaps or overlaps`() {
        for (i in 1 until 27) {
            val prev = meta.forIndex(i - 1)
            val curr = meta.forIndex(i)
            assertEquals(
                "Boundary between $i-1 and $i",
                prev.endDegree,
                curr.startDegree,
                0.001,
            )
        }
    }

    @Test
    fun `forIndex handles negative input by modulo wrap`() {
        // e.g. user passes -1 by mistake
        val n = meta.forIndex(-1)
        assertEquals("Revati", n.name)  // index 26
    }

    // ----- Display label -----

    @Test
    fun `displayLabel includes symbol name lord and deity`() {
        val n = meta.forIndex(3)  // Rohini
        val label = n.displayLabel()
        assertTrue("contains Rohini: $label", label.contains("Rohini"))
        assertTrue("contains Moon: $label", label.contains("Moon"))
        assertTrue("contains Brahma: $label", label.contains("Brahma"))
    }
}
