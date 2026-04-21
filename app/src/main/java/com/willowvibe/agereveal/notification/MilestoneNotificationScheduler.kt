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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules WorkManager jobs for upcoming life-day milestones (1,000th day, 5,000th, etc.).
 *
 * All milestone jobs share the [ALL_MILESTONES_TAG] tag so they can be bulk-cancelled
 * when the birth date changes. Each job also carries a unique work name based on
 * the birth date's epoch day and target, preventing duplicate scheduling.
 */
@Singleton
class MilestoneNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_ID = "milestone_reminders"
        const val CHANNEL_NAME = "Milestone Reminders"
        const val KEY_TARGET_DAYS = "target_days"
        const val ALL_MILESTONES_TAG = "all_milestones"
        const val NOTIFICATION_ID_BASE = 1_000_000

        private val MILESTONE_TARGETS = listOf(500, 1_000, 2_000, 3_000, 5_000, 7_000, 10_000, 12_500, 15_000, 20_000, 25_000, 30_000)
    }

    init {
        createNotificationChannel()
    }

    /**
     * Cancels any previously scheduled milestone jobs and schedules new ones for all
     * future milestones derived from [birthDate]. Milestones that have already passed
     * are silently skipped.
     */
    fun scheduleUpcomingMilestones(birthDate: LocalDate) {
        val today = LocalDate.now()
        cancelAll()

        MILESTONE_TARGETS.forEach { target ->
            val milestoneDate = birthDate.plusDays(target.toLong())
            if (!milestoneDate.isAfter(today)) return@forEach

            val fireAtMs = milestoneDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val delayMs = fireAtMs - System.currentTimeMillis()
            if (delayMs <= 0) return@forEach

            val uniqueWorkName = "milestone_${birthDate.toEpochDay()}_$target"
            val request = OneTimeWorkRequestBuilder<MilestoneReminderWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(KEY_TARGET_DAYS to target))
                .addTag(ALL_MILESTONES_TAG)
                .addTag(uniqueWorkName)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }
    }

    fun cancelAll() {
        WorkManager.getInstance(context).cancelAllWorkByTag(ALL_MILESTONES_TAG)
    }

    private fun createNotificationChannel() {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Celebrates your life-day milestones (1,000th day, 10,000th day, etc.)."
        }
        manager.createNotificationChannel(channel)
    }
}
