package com.willowvibe.agereveal.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.willowvibe.agereveal.MainActivity
import com.willowvibe.agereveal.R
import com.willowvibe.agereveal.domain.DailyFortuneGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

/**
 * WorkManager worker that generates and displays the daily cosmic fortune push notification.
 *
 * Reads the user's birth date from [calculator_prefs] SharedPreferences, generates a
 * deterministic fortune via [DailyFortuneGenerator], and shows a heads-up notification.
 * After firing, it reschedules itself for the next day via [DailyFortuneScheduler].
 */
@HiltWorker
class DailyFortuneWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dailyFortuneGenerator: DailyFortuneGenerator,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val birthDateStr = applicationContext
            .getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
            .getString("birth_date", null)
            ?: return Result.failure()

        val birthDate = runCatching { LocalDate.parse(birthDateStr) }.getOrNull()
            ?: return Result.failure()

        // Android 13+ (API 33): POST_NOTIFICATIONS is a runtime permission.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
        }

        val fortune = dailyFortuneGenerator.generate(birthDate)

        val intent = android.content.Intent(applicationContext, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            applicationContext,
            DailyFortuneScheduler.NOTIFICATION_ID,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            DailyFortuneScheduler.CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_fortune)
            .setContentTitle("${fortune.emoji} ${fortune.headline}")
            .setContentText(fortune.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fortune.body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(DailyFortuneScheduler.NOTIFICATION_ID, notification)

        // Reschedule for tomorrow
        val scheduler = DailyFortuneScheduler(applicationContext)
        scheduler.schedule(birthDate)

        return Result.success()
    }
}
