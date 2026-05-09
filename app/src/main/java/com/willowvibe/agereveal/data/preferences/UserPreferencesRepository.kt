package com.willowvibe.agereveal.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPrefsDataStore by preferencesDataStore(name = "user_prefs")

/**
 * Central DataStore-backed preferences.
 *
 * Stores:
 *  - Theme mode (system/light/dark)
 *  - Language override ("system" / "en" / "hi")
 *  - In-app review prompted flag
 *  - Share count (used as heuristic for review prompt timing)
 *  - Global birthday notifications enabled flag
 *  - Per-milestone enabled flags (e.g. "milestone_10000" -> true)
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        // Theme
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2

        private val THEME_KEY = intPreferencesKey("theme_mode")
        private val LANGUAGE_KEY = stringPreferencesKey("language_tag")
        private val REVIEW_PROMPTED_KEY = booleanPreferencesKey("review_prompted")
        private val SHARE_COUNT_KEY = intPreferencesKey("share_count")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val TARGET_AGE_KEY = intPreferencesKey("target_age")
        private val TIME_REMAINING_ENABLED_KEY = booleanPreferencesKey("time_remaining_enabled")
        private val ACCENT_COLOR_KEY = intPreferencesKey("accent_color")
        private val RETIREMENT_AGE_KEY = intPreferencesKey("retirement_age")
        private val RETIREMENT_ENABLED_KEY = booleanPreferencesKey("retirement_enabled")
    }

    private val dataStore = context.userPrefsDataStore

    // ── Theme ─────────────────────────────────────────────────────────────
    val themeMode: Flow<Int> = dataStore.data.map { it[THEME_KEY] ?: THEME_SYSTEM }
    suspend fun setThemeMode(mode: Int) { dataStore.edit { it[THEME_KEY] = mode } }

    // ── Language ─────────────────────────────────────────────────────────
    /** "system" (default) | "en" | "hi" */
    val languageTag: Flow<String> = dataStore.data.map { it[LANGUAGE_KEY] ?: "system" }
    suspend fun setLanguageTag(tag: String) { dataStore.edit { it[LANGUAGE_KEY] = tag } }

    // ── Review ───────────────────────────────────────────────────────────
    val reviewPrompted: Flow<Boolean> = dataStore.data.map { it[REVIEW_PROMPTED_KEY] ?: false }
    suspend fun markReviewPrompted() { dataStore.edit { it[REVIEW_PROMPTED_KEY] = true } }

    val shareCount: Flow<Int> = dataStore.data.map { it[SHARE_COUNT_KEY] ?: 0 }
    suspend fun incrementShareCount(): Int {
        var next = 0
        dataStore.edit { prefs ->
            next = (prefs[SHARE_COUNT_KEY] ?: 0) + 1
            prefs[SHARE_COUNT_KEY] = next
        }
        return next
    }

    // ── Global notifications toggle ──────────────────────────────────────
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFICATIONS_ENABLED_KEY] ?: true }
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    // ── Per-milestone toggles (key: "milestone_<days>") ──────────────────
    fun milestoneEnabled(targetDays: Int): Flow<Boolean> {
        val key = booleanPreferencesKey("milestone_$targetDays")
        return dataStore.data.map { it[key] ?: true }
    }

    suspend fun setMilestoneEnabled(targetDays: Int, enabled: Boolean) {
        val key = booleanPreferencesKey("milestone_$targetDays")
        dataStore.edit { it[key] = enabled }
    }

    // ── Lifespan target age for progress widget ──────────────────────────
    val targetAge: Flow<Int> = dataStore.data.map { it[TARGET_AGE_KEY] ?: 80 }
    suspend fun setTargetAge(age: Int) {
        dataStore.edit { it[TARGET_AGE_KEY] = age }
        // Mirror to SharedPreferences so the widget can read synchronously
        context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
            .edit().putInt("target_age", age).apply()
    }

    // ── Time remaining visuals toggle ────────────────────────────────────
    val timeRemainingEnabled: Flow<Boolean> = dataStore.data.map { it[TIME_REMAINING_ENABLED_KEY] ?: true }
    suspend fun setTimeRemainingEnabled(enabled: Boolean) { dataStore.edit { it[TIME_REMAINING_ENABLED_KEY] = enabled } }

    // ── Custom accent color (ARGB) ───────────────────────────────────────
    val accentColor: Flow<Int> = dataStore.data.map { it[ACCENT_COLOR_KEY] ?: 0xFF86EFAC.toInt() }
    suspend fun setAccentColor(color: Int) {
        dataStore.edit { it[ACCENT_COLOR_KEY] = color }
        // Mirror to SharedPreferences so ShareCardGenerator can read synchronously
        context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
            .edit().putInt("accent_color", color).apply()
    }

    // ── Retirement calculator settings ───────────────────────────────────
    val retirementAge: Flow<Int> = dataStore.data.map { it[RETIREMENT_AGE_KEY] ?: 60 }
    suspend fun setRetirementAge(age: Int) {
        dataStore.edit { it[RETIREMENT_AGE_KEY] = age }
    }

    val retirementEnabled: Flow<Boolean> = dataStore.data.map { it[RETIREMENT_ENABLED_KEY] ?: true }
    suspend fun setRetirementEnabled(enabled: Boolean) {
        dataStore.edit { it[RETIREMENT_ENABLED_KEY] = enabled }
    }
}
