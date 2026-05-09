package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RetirementCalculatorTest {

    @Test
    fun `retirement at 60 for 30 year old`() {
        val birth = LocalDate.of(1996, 1, 1)
        val today = LocalDate.of(2026, 1, 1)
        val calc = RetirementCalculator()
        val result = calc.calculate(birth, today, retirementAge = 60)

        assertNotNull(result)
        assertEquals(60, result!!.retirementAge)
        // Career started at 22, so 8 years of work completed out of 38 total
        assert(result.percentOfWorkLifeComplete > 0)
        assert(result.percentOfWorkLifeComplete < 30)
    }

    @Test
    fun `null when already past retirement age`() {
        val birth = LocalDate.of(1950, 1, 1)
        val today = LocalDate.of(2026, 1, 1)
        val calc = RetirementCalculator()
        val result = calc.calculate(birth, today, retirementAge = 60)
        assertNull(result)
    }

    @Test
    fun `work weeks computed correctly`() {
        val birth = LocalDate.of(2000, 1, 1)
        val today = LocalDate.of(2026, 1, 1)
        val calc = RetirementCalculator()
        val result = calc.calculate(birth, today, retirementAge = 60)!!
        assertEquals(result.daysUntilRetirement / 5, result.workWeeksLeft)
    }

    @Test
    fun `percent complete increases with age`() {
        val birth = LocalDate.of(1980, 1, 1)
        val today = LocalDate.of(2026, 1, 1)
        val calc = RetirementCalculator()
        val result = calc.calculate(birth, today, retirementAge = 60)!!
        assert(result.percentOfWorkLifeComplete > 0)
        assert(result.percentOfWorkLifeComplete <= 100)
    }
}
