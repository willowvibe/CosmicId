package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CelebrityMatchCalculatorTest {

    private val json = """
        [
          {"name":"Amitabh Bachchan","dob":"1942-10-11","category":"Bollywood"},
          {"name":"Shah Rukh Khan","dob":"1965-11-02","category":"Bollywood"},
          {"name":"Virat Kohli","dob":"1988-11-05","category":"Cricket"},
          {"name":"Test Person","dob":"1990-10-11","category":"Global"}
        ]
    """.trimIndent()

    @Test
    fun `parseMatches returns celebrities with same month and day`() {
        val date = LocalDate.of(1990, 10, 11)
        val matches = CelebrityMatchCalculator.parseMatches(json, date, limit = 10)

        assertTrue("Expected at least one match", matches.isNotEmpty())
        val amitabh = matches.find { it.name == "Amitabh Bachchan" }
        assertTrue("Amitabh Bachchan should match Oct 11", amitabh != null)
        assertEquals("Bollywood", amitabh?.category)

        val testPerson = matches.find { it.name == "Test Person" }
        assertTrue("Test Person should match exact year", testPerson != null)
    }

    @Test
    fun `parseMatches respects limit`() {
        val date = LocalDate.of(2000, 10, 11)
        val matches = CelebrityMatchCalculator.parseMatches(json, date, limit = 1)
        assertTrue("Expected at least one match", matches.isNotEmpty())
        assertEquals("Should respect limit of 1", 1, matches.size)
    }

    @Test
    fun `parseMatches returns empty for unmatched date`() {
        val date = LocalDate.of(2000, 2, 29)
        val matches = CelebrityMatchCalculator.parseMatches(json, date, limit = 3)
        assertTrue("Expected no matches for Feb 29", matches.isEmpty())
    }

    @Test
    fun `parseMatches sorts exact year matches first`() {
        val date = LocalDate.of(1990, 10, 11)
        val matches = CelebrityMatchCalculator.parseMatches(json, date, limit = 10)
        assertTrue("Expected multiple matches", matches.size >= 2)
        // Exact year match (1990) should come before non-exact year (1942)
        assertEquals("Test Person", matches[0].name)
        assertEquals("Amitabh Bachchan", matches[1].name)
    }
}
