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

/**
 * Schedules a yearly "Cosmic Year Report" notification on the user's own birthday.
 * Fires at 09:00 with total days lived, current Vimshottari Dasha period, and a
 * daily fortune preview — giving the user a snapshot of their cosmic year ahead.
 */
@Singleton
class YearlyReengagementScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "yearly_reengagement"
        const val CHANNEL_NAME = "Cosmic Year Report"
        const val WORK_TAG = "yearly_reengagement"
        const val NOTIFICATION_ID = 2_000_000
        const val KEY_BIRTH_DATE = "birth_date"
        private const val DEFAULT_HOUR = 9
    }

    init {
        createNotificationChannel()
    }

    /** Schedule the yearly notification for the user's birthday. */
    fun schedule(birthDate: LocalDate) {
        val today = LocalDate.now()
        val nextBirthday = computeNextBirthday(birthDate)
        val nowMs = System.currentTimeMillis()
        val birthdayMs = nextBirthday.atTime(DEFAULT_HOUR, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        var delayMs = birthdayMs - nowMs

        // If the 09:00 window on today's birthday has already passed, schedule for next year.
        if (delayMs <= 0) {
            val daysAway = ChronoUnit.DAYS.between(today, nextBirthday)
            delayMs = if (daysAway == 0L) {
                // Today is the birthday but 09:00 has passed — fire immediately
                0L
            } else {
                yearSafeBirthday(birthDate, nextBirthday.year + 1).atTime(DEFAULT_HOUR, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli() - nowMs
            }
        }

        val request = OneTimeWorkRequestBuilder<YearlyReengagementWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .setInputData(workDataOf(KEY_BIRTH_DATE to birthDate.toString()))
            .addTag(WORK_TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    /** Cancel any pending yearly re-engagement notification. */
    fun cancel() {
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
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
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Your annual Cosmic Year Report — total days lived, current Dasha period, and a fortune for the year ahead."
        }
        manager.createNotificationChannel(channel)
    }
}
