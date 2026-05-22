package com.willowvibe.agereveal.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
 * Stores all user preferences. Values that need to be read by external processes
 * (widgets, WorkManager workers) are mirrored to SharedPreferences so they can
 * be accessed synchronously.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        // SharedPreferences name visible to widgets and workers
        const val SYNC_PREFS_NAME = "calculator_prefs"

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
        private val IS_PREMIUM_KEY = booleanPreferencesKey("is_premium")
        private val PREMIUM_PURCHASE_TIME_KEY = longPreferencesKey("premium_purchase_time")
        private val TRIAL_DURATION_DAYS_KEY = intPreferencesKey("trial_duration_days")
        private val GRACE_PERIOD_START_KEY = longPreferencesKey("grace_period_start")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val THEME_PACK_KEY = intPreferencesKey("theme_pack")

        // User profile (mirrored to SharedPreferences for widget/worker access)
        private val BIRTH_DATE_KEY = stringPreferencesKey("birth_date")
        private val BIRTH_TIME_KEY = stringPreferencesKey("birth_time")
        private val BIRTH_LOCATION_KEY = stringPreferencesKey("birth_location")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val NOTIFICATION_HOUR_KEY = intPreferencesKey("notification_hour")

        // Daily fortune cache
        private val FORTUNE_DATE_KEY = stringPreferencesKey("fortune_date")
        private val FORTUNE_JSON_KEY = stringPreferencesKey("fortune_json")
    }

    private val dataStore = context.userPrefsDataStore
    private val syncPrefs = context.getSharedPreferences(SYNC_PREFS_NAME, Context.MODE_PRIVATE)

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
        syncPrefs.edit().putInt("target_age", age).apply()
    }

    // ── Time remaining visuals toggle ────────────────────────────────────
    val timeRemainingEnabled: Flow<Boolean> = dataStore.data.map { it[TIME_REMAINING_ENABLED_KEY] ?: true }
    suspend fun setTimeRemainingEnabled(enabled: Boolean) { dataStore.edit { it[TIME_REMAINING_ENABLED_KEY] = enabled } }

    // ── Custom accent color (ARGB) ───────────────────────────────────────
    val accentColor: Flow<Int> = dataStore.data.map { it[ACCENT_COLOR_KEY] ?: 0xFF86EFAC.toInt() }
    suspend fun setAccentColor(color: Int) {
        dataStore.edit { it[ACCENT_COLOR_KEY] = color }
        syncPrefs.edit().putInt("accent_color", color).apply()
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

    // ── Premium status (v2.0) ──────────────────────────────────────────────
    val isPremium: Flow<Boolean> = dataStore.data.map { it[IS_PREMIUM_KEY] ?: false }
    suspend fun setPremium(premium: Boolean) {
        dataStore.edit { it[IS_PREMIUM_KEY] = premium }
    }

    val premiumPurchaseTime: Flow<Long> = dataStore.data.map { it[PREMIUM_PURCHASE_TIME_KEY] ?: 0L }
    suspend fun setPremiumPurchaseTime(timeMillis: Long) {
        dataStore.edit { it[PREMIUM_PURCHASE_TIME_KEY] = timeMillis }
    }

    val trialDurationDays: Flow<Int> = dataStore.data.map { it[TRIAL_DURATION_DAYS_KEY] ?: 0 }
    suspend fun setTrialDurationDays(days: Int) {
        dataStore.edit { it[TRIAL_DURATION_DAYS_KEY] = days }
    }

    // ── Grace period start (v2.0) ──────────────────────────────────────────
    /** Timestamp (epoch millis) when the grace period began; 0L = not in grace period. */
    val gracePeriodStart: Flow<Long> = dataStore.data.map { it[GRACE_PERIOD_START_KEY] ?: 0L }
    suspend fun setGracePeriodStart(timeMillis: Long) {
        dataStore.edit { it[GRACE_PERIOD_START_KEY] = timeMillis }
    }

    // ── Premium theme pack (v2.0) ─────────────────────────────────────────
    val themePack: Flow<Int> = dataStore.data.map { it[THEME_PACK_KEY] ?: 0 }
    suspend fun setThemePack(packId: Int) {
        dataStore.edit { it[THEME_PACK_KEY] = packId }
    }

    // ── Onboarding completed (v2.0) ────────────────────────────────────────
    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_COMPLETED_KEY] ?: false }
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = completed }
    }

    // ── User profile (mirrored to SharedPreferences for widget/worker access) ──
    val birthDate: Flow<String?> = dataStore.data.map { it[BIRTH_DATE_KEY] }
    suspend fun setBirthDate(date: String?) {
        dataStore.edit { if (date != null) it[BIRTH_DATE_KEY] = date else it.remove(BIRTH_DATE_KEY) }
        if (date != null) syncPrefs.edit().putString("birth_date", date).apply()
        else syncPrefs.edit().remove("birth_date").apply()
    }

    val birthTime: Flow<String?> = dataStore.data.map { it[BIRTH_TIME_KEY] }
    suspend fun setBirthTime(time: String?) {
        dataStore.edit { if (time != null) it[BIRTH_TIME_KEY] = time else it.remove(BIRTH_TIME_KEY) }
        if (time != null) syncPrefs.edit().putString("birth_time", time).apply()
        else syncPrefs.edit().remove("birth_time").apply()
    }

    val birthLocation: Flow<String?> = dataStore.data.map { it[BIRTH_LOCATION_KEY] }
    suspend fun setBirthLocation(location: String?) {
        dataStore.edit { if (location != null) it[BIRTH_LOCATION_KEY] = location else it.remove(BIRTH_LOCATION_KEY) }
        if (location != null) syncPrefs.edit().putString("birth_location", location).apply()
        else syncPrefs.edit().remove("birth_location").apply()
    }

    val userName: Flow<String> = dataStore.data.map { it[USER_NAME_KEY] ?: "" }
    suspend fun setUserName(name: String) {
        dataStore.edit { it[USER_NAME_KEY] = name }
        syncPrefs.edit().putString("user_name", name).apply()
    }

    val notificationHour: Flow<Int> = dataStore.data.map { it[NOTIFICATION_HOUR_KEY] ?: 8 }
    suspend fun setNotificationHour(hour: Int) {
        dataStore.edit { it[NOTIFICATION_HOUR_KEY] = hour }
        syncPrefs.edit().putInt("notification_hour", hour).apply()
    }

    // ── Daily fortune cache (mirrored for worker access) ───────────────────
    val fortuneDate: Flow<String?> = dataStore.data.map { it[FORTUNE_DATE_KEY] }
    suspend fun setFortune(date: String, json: String) {
        dataStore.edit {
            it[FORTUNE_DATE_KEY] = date
            it[FORTUNE_JSON_KEY] = json
        }
        syncPrefs.edit().putString("fortune_date", date).putString("fortune_json", json).apply()
    }

    val fortuneJson: Flow<String?> = dataStore.data.map { it[FORTUNE_JSON_KEY] }
}
