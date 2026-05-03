package com.willowvibe.agereveal.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that refreshes all active [SecondsCounterGlanceWidget] instances.
 *
 * Scheduled every 15 minutes so the seconds display does not drift too far
 * between system-driven widget redraws.
 */
@HiltWorker
class SecondsCounterUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(SecondsCounterGlanceWidget::class.java)
        for (id in glanceIds) {
            SecondsCounterGlanceWidget().update(applicationContext, id)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "seconds_counter_widget_update"

        /** Enqueue the 15-minute periodic refresh if not already scheduled. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SecondsCounterUpdateWorker>(
                15, TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
