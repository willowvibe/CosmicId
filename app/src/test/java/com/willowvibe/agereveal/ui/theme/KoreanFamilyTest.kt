package com.willowvibe.agereveal.ui.theme

import androidx.compose.ui.text.font.FontFamily
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Lightweight smoke test for the typography setup. We don't render text
 * here (that needs a Compose runtime) — we just verify the families are
 * wired up and distinct.
 */
class KoreanFamilyTest {

    @Test
    fun `KoreanFamily is wired up and non-null`() {
        assertNotNull("KoreanFamily should be defined", KoreanFamily)
    }

    @Test
    fun `KoreanFamily is distinct from InterFamily`() {
        // We want Korean Hangul to NOT use Inter — Inter is a Latin font and
        // has no Hangul glyphs. The system sans-serif is the correct
        // rendering path. If a future design pass bundles Noto Sans KR,
        // KoreanFamily will become a real FontFamily(R.font...) which is
        // also distinct from InterFamily.
        assertNotEquals(
            "KoreanFamily should NOT alias InterFamily (Inter has no Hangul glyphs)",
            InterFamily, KoreanFamily,
        )
    }

    @Test
    fun `InterFamily and SerifFamily are distinct font families`() {
        assertNotEquals(InterFamily, SerifFamily)
    }
}
