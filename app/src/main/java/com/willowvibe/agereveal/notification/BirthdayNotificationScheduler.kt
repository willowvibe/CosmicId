package com.willowvibe.agereveal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Schedules and cancels [BirthdayReminderWorker] jobs via WorkManager.
 *
 * One WorkManager job per saved birthday, identified by a unique tag = "birthday_<id>".
 * The job is scheduled to fire 1 day before the next birthday at 09:00 local time.
 */
@Singleton
class BirthdayNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "birthday_reminders"
        const val CHANNEL_NAME = "Birthday Reminders"
        const val KEY_NAME = "person_name"
        const val KEY_ID = "birthday_id"
        private const val PREFS_NAME = "reminder_settings"
        private const val KEY_HOUR = "notification_hour"
        const val DEFAULT_HOUR = 9

        fun workTag(id: Long) = "birthday_$id"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _notificationHour = MutableStateFlow(prefs.getInt(KEY_HOUR, DEFAULT_HOUR))
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()

    fun setNotificationHour(hour: Int) {
        prefs.edit().putInt(KEY_HOUR, hour).apply()
        _notificationHour.value = hour
    }

    init {
        createNotificationChannel()
    }

    fun scheduleFor(id: Long, name: String, birthDate: LocalDate) {
        val today = LocalDate.now()
        val nextBirthday = computeNextBirthday(birthDate)
        val nowMs = System.currentTimeMillis()
        val hour = prefs.getInt(KEY_HOUR, DEFAULT_HOUR)
        val reminderMs = nextBirthday.minusDays(1).atTime(hour, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        var delayMs = reminderMs - nowMs

        // The standard 09:00-the-day-before window has already passed. If the birthday is
        // imminent (today or tomorrow), fire immediately so the user isn't told "in 1 year".
        // Otherwise schedule against the next occurrence.
        if (delayMs <= 0) {
            val daysAway = ChronoUnit.DAYS.between(today, nextBirthday)
            delayMs = if (daysAway <= 1) {
                0L
            } else {
                yearSafeBirthday(birthDate, nextBirthday.year + 1).minusDays(1).atTime(hour, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli() - nowMs
            }
        }

        // Check SCHEDULE_EXACT_ALARM permission on Android 12+
        // On Android 12+, exact alarms require the SCHEDULE_EXACT_ALARM permission
        // WorkManager handles this internally, but we log the status for debugging
        val canScheduleExactAlarms = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Android 11 and below allow exact alarms by default
        }

        val request = OneTimeWorkRequestBuilder<BirthdayReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(KEY_NAME to name, KEY_ID to id))
            .addTag(workTag(id))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workTag(id),
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel(id: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag(workTag(id))
    }

    private fun computeNextBirthday(birthDate: LocalDate): LocalDate {
        val today = LocalDate.now()
        var next = yearSafeBirthday(birthDate, today.year)
        if (!next.isAfter(today)) next = yearSafeBirthday(birthDate, today.year + 1)
        return next
    }

    private fun yearSafeBirthday(birthDate: LocalDate, year: Int): LocalDate {
        if (birthDate.monthValue == 2 && birthDate.dayOfMonth == 29 && !java.time.Year.isLeap(year.toLong())) {
            return LocalDate.of(year, 3, 1)
        }
        return birthDate.withYear(year)
    }

    private fun createNotificationChannel() {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminds you 1 day before a saved birthday."
        }
        manager.createNotificationChannel(channel)
    }
}
