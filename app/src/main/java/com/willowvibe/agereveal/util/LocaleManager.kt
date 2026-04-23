package com.willowvibe.agereveal.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Per-app language helper.
 *
 * Uses AndroidX AppCompatDelegate's per-app locales API. On Android 13+ this
 * integrates with the system language picker; on older versions it is applied
 * automatically at activity recreate time.
 *
 * [languageTag] examples:
 *  - "system" → follow system locale (empty list)
 *  - "en"     → force English
 *  - "hi"     → force Hindi
 */
object LocaleManager {

    fun apply(languageTag: String) {
        val locales = when (languageTag) {
            "system", "" -> LocaleListCompat.getEmptyLocaleList()
            else -> LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
