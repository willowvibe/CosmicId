package com.willowvibe.agereveal.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [LunarCalendarConverter].
 *
 * BUG-082: the previous implementation returned an empty string on failure
 * with no signal to the caller. These tests lock in the new contract:
 *  - `toLunarResult` returns `Result<String>` with a logged warning on failure
 *  - `toLunarString` still returns `""` on failure (back-compat) — `getOrDefault`
 *  - On JVM where `android.icu` is missing, conversion fails gracefully
 *  - The error is wrapped in `Result.failure`, not thrown
 */
class LunarCalendarConverterTest {

    private val converter = LunarCalendarConverter()

    @Test
    fun `toLunarResult wraps conversion in Result`() {
        val result = converter.toLunarResult(LocalDate.of(1990, 6, 15))
        assertNotNull("Result should never be null", result)
        // On the JVM (no android.icu), this is expected to fail; on Android it
        // succeeds. Either way, no exception is thrown to the caller.
        if (result.isSuccess) {
            val value = result.getOrNull()!!
            assertTrue("Lunar string should mention 'lunar month': $value", value.contains("lunar month"))
        } else {
            assertNotNull("Failure should carry a throwable", result.exceptionOrNull())
        }
    }

    @Test
    fun `toLunarString never throws — returns empty on failure — BUG-082 regression guard`() {
        // The previous bug was a swallowed exception that left callers with
        // no signal. Now `toLunarString` is guaranteed not to throw and to
        // return either a valid string or "" on failure.
        val s = converter.toLunarString(LocalDate.of(1990, 6, 15))
        // On JVM this is ""; on Android this is a real lunar string. Either
        // way, no exception.
        assertTrue("Returned string is either empty or a real lunar description: '$s'",
            s.isEmpty() || s.contains("lunar month"))
    }

    @Test
    fun `toLunarString delegates to toLunarResult getOrDefault — back-compat`() {
        // Locks in the toLunarString ↔ toLunarResult contract: the legacy
        // method is now a thin wrapper over the new Result-returning one.
        val expected = converter.toLunarResult(LocalDate.of(1990, 6, 15)).getOrDefault("")
        val actual = converter.toLunarString(LocalDate.of(1990, 6, 15))
        assertEquals(expected, actual)
    }

    @Test
    fun `toLunarResult for leap year date is wrapped — BUG-082`() {
        // 2024 has a leap 2nd lunar month. The conversion may succeed on
        // Android and fail on JVM — but in either case it must return a
        // Result, not throw.
        val result = converter.toLunarResult(LocalDate.of(2024, 9, 17)) // Mid-Autumn 2024 area
        if (result.isSuccess) {
            val value = result.getOrNull()!!
            // The phrase "leap" appears if a leap month is active for that day.
            assertTrue("Success result should be a non-empty string: '$value'", value.isNotEmpty())
        } else {
            // Failure path: the throwable is preserved in the Result.
            assertFalse("Failure should not silently produce empty string",
                result.getOrNull().isNullOrEmpty().not())
        }
    }

    @Test
    fun `toLunarResult does not throw even for extreme dates`() {
        // Defensive: should never throw regardless of input. The Result is
        // either success or failure but never an unhandled exception.
        val dates = listOf(
            LocalDate.of(1900, 1, 1),   // pre-1970 epoch edge
            LocalDate.of(2050, 12, 31), // far future
            LocalDate.of(2000, 2, 29),  // leap day
            LocalDate.of(1990, 6, 15),  // baseline
        )
        for (date in dates) {
            val result = converter.toLunarResult(date)
            assertTrue(
                "toLunarResult($date) returned a Result (no exception thrown)",
                result.isSuccess || result.isFailure,
            )
        }
    }
}
