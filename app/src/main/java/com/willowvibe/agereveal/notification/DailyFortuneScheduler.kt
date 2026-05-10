package com.willowvibe.agereveal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily cosmic fortune push notification via WorkManager.
 *
 * Uses a [OneTimeWorkRequest] with exact daily rescheduling (the worker
 * re-enqueues itself after firing). This guarantees precise timing better
 * than [androidx.work.PeriodicWorkRequest]'s flex window.
 *
 * Preferences (hour & enabled flag) are stored in a dedicated
 * SharedPreferences file so the scheduler can read them synchronously.
 */
@Singleton
class DailyFortuneScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "daily_fortune"
        const val CHANNEL_NAME = "Daily Cosmic Fortune"
        const val WORK_TAG = "daily_fortune"
        const val NOTIFICATION_ID = 3_000_000
        private const val PREFS_NAME = "fortune_settings"
        private const val KEY_HOUR = "fortune_hour"
        private const val KEY_ENABLED = "fortune_enabled"
        const val DEFAULT_HOUR = 8
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _fortuneHour = MutableStateFlow(prefs.getInt(KEY_HOUR, DEFAULT_HOUR))
    val fortuneHour: StateFlow<Int> = _fortuneHour.asStateFlow()

    private val _fortuneEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, true))
    val fortuneEnabled: StateFlow<Boolean> = _fortuneEnabled.asStateFlow()

    init {
        createNotificationChannel()
    }

    /** Persist the desired notification hour (0–23) and reschedule if active. */
    fun setFortuneHour(hour: Int) {
        prefs.edit().putInt(KEY_HOUR, hour.coerceIn(0, 23)).apply()
        _fortuneHour.value = hour
    }

    /** Persist the master toggle and reschedule (or cancel) accordingly. */
    fun setFortuneEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        _fortuneEnabled.value = enabled
    }

    /**
     * Schedule the next daily fortune notification.
     *
     * @param birthDate The user's birth date. If null, the scheduler reads from
     *                  [calculator_prefs] SharedPreferences. If still null, any
     *                  pending work is cancelled.
     * @param hour The hour of day (0–23) to fire; defaults to the stored preference.
     */
    fun schedule(birthDate: LocalDate? = null, hour: Int = fortuneHour.value) {
        val effectiveBirthDate = birthDate ?: runCatching {
            val str = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
                .getString("birth_date", null)
            str?.let { LocalDate.parse(it) }
        }.getOrNull()

        if (effectiveBirthDate == null || !fortuneEnabled.value) {
            cancel()
            return
        }

        val nowMs = System.currentTimeMillis()
        val nextFireMs = computeNextFireTime(hour)
        var delayMs = nextFireMs - nowMs
        if (delayMs < 0) delayMs = 0

        val request = OneTimeWorkRequestBuilder<DailyFortuneWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /** Cancel any pending daily fortune work. */
    fun cancel() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }

    private fun computeNextFireTime(hour: Int): Long {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var fireAt = now
            .withHour(hour.coerceIn(0, 23))
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        if (!fireAt.isAfter(now)) {
            fireAt = fireAt.plusDays(1)
        }
        return fireAt.toInstant().toEpochMilli()
    }

    private fun createNotificationChannel() {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Your daily cosmic fortune delivered as a push notification."
        }
        manager.createNotificationChannel(channel)
    }
}
