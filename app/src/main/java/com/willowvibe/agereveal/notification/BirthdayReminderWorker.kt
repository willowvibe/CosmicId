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
 * WorkManager worker that fires the birthday reminder notification.
 * Runs in the background even if the app is closed.
 */
@HiltWorker
class BirthdayReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val name = inputData.getString(BirthdayNotificationScheduler.KEY_NAME) ?: return Result.failure()
        val id   = inputData.getLong(BirthdayNotificationScheduler.KEY_ID, -1L)

        if (id < 0) return Result.failure()

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

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            BirthdayNotificationScheduler.CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle("🎂 $name's birthday is tomorrow!")
            .setContentText("Tap to see their age and share a card.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(id.toInt(), notification)

        return Result.success()
    }
}
