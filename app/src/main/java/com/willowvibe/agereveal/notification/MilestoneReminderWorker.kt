package com.willowvibe.agereveal.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.willowvibe.agereveal.MainActivity
import com.willowvibe.agereveal.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager worker that fires a milestone day notification (e.g. "You're 10,000 days old!").
 */
@HiltWorker
class MilestoneReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val targetDays = inputData.getInt(MilestoneNotificationScheduler.KEY_TARGET_DAYS, -1)
        if (targetDays < 0) return Result.failure()

        // Android 13+ (API 33): POST_NOTIFICATIONS is a runtime permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
        }

        val formattedDays = "%,d".format(targetDays)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, targetDays,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            MilestoneNotificationScheduler.CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle("🎉 You're $formattedDays days old today!")
            .setContentText("Tap to celebrate this milestone.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(MilestoneNotificationScheduler.NOTIFICATION_ID_BASE + targetDays, notification)

        return Result.success()
    }
}
