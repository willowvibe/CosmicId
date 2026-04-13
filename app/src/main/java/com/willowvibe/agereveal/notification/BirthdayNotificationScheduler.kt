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
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

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
        const val CHANNEL_ID   = "birthday_reminders"
        const val CHANNEL_NAME = "Birthday Reminders"
        const val KEY_NAME     = "person_name"
        const val KEY_ID       = "birthday_id"

        fun workTag(id: Long) = "birthday_$id"
    }

    init {
        createNotificationChannel()
    }

    fun scheduleFor(id: Long, name: String, birthDate: LocalDate) {
        val nextBirthday = computeNextBirthday(birthDate)
        val reminderDateTime = nextBirthday.minusDays(1).atTime(9, 0)
        val delayMs = reminderDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() - System.currentTimeMillis()

        if (delayMs <= 0) return  // birthday is today or tomorrow at 09:00 already passed

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
        var next = birthDate.withYear(today.year)
        if (!next.isAfter(today)) next = next.plusYears(1)
        return next
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
