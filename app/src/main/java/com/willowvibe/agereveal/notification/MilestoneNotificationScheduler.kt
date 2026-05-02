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
 *
 * Per-milestone enablement is tracked in the ViewModel layer via
 * [com.willowvibe.agereveal.data.preferences.UserPreferencesRepository]; the scheduler
 * itself is a passive executor so that it remains testable without DataStore I/O.
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

        val MILESTONE_TARGETS = listOf(
            500, 1_000, 2_000, 3_000, 5_000, 7_000, 10_000, 12_500,
            15_000, 20_000, 25_000, 30_000,
        )

        fun uniqueWorkName(birthDate: LocalDate, target: Int) =
            "milestone_${birthDate.toEpochDay()}_$target"
    }

    init {
        createNotificationChannel()
    }

    /**
     * Cancels any previously scheduled milestone jobs and schedules new ones for all
     * future milestones in [enabledTargets] derived from [birthDate]. Milestones that
     * have already passed are silently skipped.
     *
     * When [enabledTargets] is null, all targets in [MILESTONE_TARGETS] are scheduled
     * (backwards-compatible default).
     */
    fun scheduleUpcomingMilestones(
        birthDate: LocalDate,
        enabledTargets: Set<Int>? = null,
    ) {
        val today = LocalDate.now()
        cancelAll()

        val targets = MILESTONE_TARGETS.filter { enabledTargets == null || it in enabledTargets }
        targets.forEach { target ->
            scheduleSingle(birthDate, target, today)
        }
    }

    /** Schedule a single milestone target if it is in the future. */
    fun scheduleSingle(birthDate: LocalDate, target: Int, today: LocalDate = LocalDate.now()) {
        val milestoneDate = birthDate.plusDays(target.toLong())
        if (!milestoneDate.isAfter(today)) return

        val fireAtMs = milestoneDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delayMs = fireAtMs - System.currentTimeMillis()
        if (delayMs <= 0) return

        val name = uniqueWorkName(birthDate, target)
        val request = OneTimeWorkRequestBuilder<MilestoneReminderWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .setInputData(workDataOf(KEY_TARGET_DAYS to target))
            .addTag(ALL_MILESTONES_TAG)
            .addTag(name)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            name,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    /** Cancel a single milestone's scheduled worker (matches the tag built in [scheduleSingle]). */
    fun cancelSingle(birthDate: LocalDate, target: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag(uniqueWorkName(birthDate, target))
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
