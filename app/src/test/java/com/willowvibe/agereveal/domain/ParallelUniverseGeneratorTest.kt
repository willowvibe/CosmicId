package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ParallelUniverseGeneratorTest {

    private val generator = ParallelUniverseGenerator()

    @Test
    fun `generate returns exactly 3 contexts`() {
        val birth = LocalDate.of(1990, 6, 15)
        val today = LocalDate.of(2026, 1, 1)
        val result = generator.generate(birth, today)
        assertEquals(3, result.size)
    }

    @Test
    fun `generate is deterministic for same birth year`() {
        val birth = LocalDate.of(1990, 6, 15)
        val today = LocalDate.of(2026, 1, 1)
        val first = generator.generate(birth, today)
        val second = generator.generate(birth, today)
        assertEquals(first.map { it.era }, second.map { it.era })
        assertEquals(first.map { it.location }, second.map { it.location })
        assertEquals(first.map { it.ageText }, second.map { it.ageText })
    }

    @Test
    fun `age text contains actual age`() {
        val birth = LocalDate.of(1990, 6, 15)
        val today = LocalDate.of(2026, 1, 1)
        val age = today.year - birth.year - 1 // 35 (birthday not yet passed in 2026)
        val result = generator.generate(birth, today)
        result.forEach { ctx ->
            assertTrue("Expected age $age in '${ctx.ageText}'", ctx.ageText.contains(age.toString()))
        }
    }

    @Test
    fun `all contexts have non blank fields`() {
        val birth = LocalDate.of(2000, 1, 1)
        val today = LocalDate.of(2026, 1, 1)
        val result = generator.generate(birth, today)
        result.forEach { ctx ->
            assertTrue(ctx.era.isNotBlank())
            assertTrue(ctx.location.isNotBlank())
            assertTrue(ctx.ageText.isNotBlank())
            assertTrue(ctx.emoji.isNotBlank())
        }
    }

    @Test
    fun `different birth years produce different selections`() {
        val today = LocalDate.of(2026, 1, 1)
        val resultA = generator.generate(LocalDate.of(1990, 1, 1), today)
        val resultB = generator.generate(LocalDate.of(2000, 1, 1), today)
        // Extremely unlikely (1/8^3) that two different seeds pick the exact same 3 eras in same order
        assertTrue(
            "Different birth years should yield different contexts",
            resultA.map { it.era } != resultB.map { it.era },
        )
    }
}
