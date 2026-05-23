package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ProfileDeepLinkGeneratorTest {

    @Test
    fun `encode and decode roundtrip with all fields`() {
        val date = LocalDate.of(1995, 8, 24)
        val name = "Rahul"
        val time = LocalTime.of(14, 30)
        val link = ProfileDeepLinkGenerator.generate(date, name, time)
        val encoded = link.substringAfterLast("/")
        val parsed = ProfileDeepLinkGenerator.parse(encoded)
        assertNotNull(parsed)
        assertEquals(date, parsed!!.birthDate)
        assertEquals(name, parsed.name)
        assertEquals(time, parsed.birthTime)
    }

    @Test
    fun `encode and decode without birthTime`() {
        val date = LocalDate.of(2000, 1, 1)
        val link = ProfileDeepLinkGenerator.generate(date, "Alice")
        val encoded = link.substringAfterLast("/")
        val parsed = ProfileDeepLinkGenerator.parse(encoded)
        assertNotNull(parsed)
        assertEquals(date, parsed!!.birthDate)
        assertEquals("Alice", parsed.name)
        assertNull(parsed.birthTime)
    }

    @Test
    fun `encode and decode with empty name`() {
        val date = LocalDate.of(1990, 6, 15)
        val link = ProfileDeepLinkGenerator.generate(date)
        val encoded = link.substringAfterLast("/")
        val parsed = ProfileDeepLinkGenerator.parse(encoded)
        assertNotNull(parsed)
        assertEquals(date, parsed!!.birthDate)
        assertEquals("", parsed.name)
        assertNull(parsed.birthTime)
    }

    @Test
    fun `name with quotes is escaped correctly`() {
        val name = "She said \"hello\""
        val date = LocalDate.of(1990, 1, 1)
        val link = ProfileDeepLinkGenerator.generate(date, name)
        val encoded = link.substringAfterLast("/")
        val parsed = ProfileDeepLinkGenerator.parse(encoded)
        assertNotNull(parsed)
        assertEquals(name, parsed!!.name)
    }

    @Test
    fun `name with backslash is escaped correctly`() {
        val name = "C:\\Users\\Alice"
        val date = LocalDate.of(1990, 1, 1)
        val link = ProfileDeepLinkGenerator.generate(date, name)
        val encoded = link.substringAfterLast("/")
        val parsed = ProfileDeepLinkGenerator.parse(encoded)
        assertNotNull(parsed)
        assertEquals(name, parsed!!.name)
    }

    @Test
    fun `generateShareUrl uses https scheme`() {
        val url = ProfileDeepLinkGenerator.generateShareUrl(
            LocalDate.of(2000, 1, 1), "Test"
        )
        assertEquals(true, url.startsWith("https://"))
        assertEquals(true, url.contains("willowvibe.com"))
    }

    @Test
    fun `parse returns null for invalid base64`() {
        val parsed = ProfileDeepLinkGenerator.parse("!!!invalid!!!")
        assertNull(parsed)
    }

    @Test
    fun `parse returns null for missing date key`() {
        val payload = """{"n":"Alice"}"""
        val encoded = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(payload.toByteArray(Charsets.UTF_8))
        val parsed = ProfileDeepLinkGenerator.parse(encoded)
        assertNull(parsed)
    }

    @Test
    fun `parse handles midnight birthTime`() {
        val date = LocalDate.of(1990, 1, 1)
        val time = LocalTime.MIDNIGHT
        val link = ProfileDeepLinkGenerator.generate(date, "Test", time)
        val encoded = link.substringAfterLast("/")
        val parsed = ProfileDeepLinkGenerator.parse(encoded)
        assertNotNull(parsed)
        assertEquals(time, parsed!!.birthTime)
    }
}
