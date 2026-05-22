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
import com.willowvibe.agereveal.domain.DailyFortuneGenerator
import com.willowvibe.agereveal.domain.DashaCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * WorkManager worker that fires a yearly "Cosmic Year Report" notification on the user's
 * own birthday with total days lived, current Vimshottari Dasha period, and a daily fortune
 * preview — giving the user a snapshot of their cosmic year ahead.
 */
@HiltWorker
class YearlyReengagementWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val dashaCalculator: DashaCalculator,
    private val dailyFortuneGenerator: DailyFortuneGenerator,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val birthDateStr = inputData.getString(YearlyReengagementScheduler.KEY_BIRTH_DATE)
            ?: return Result.failure()
        val birthDate = runCatching { LocalDate.parse(birthDateStr) }.getOrNull()
            ?: return Result.failure()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.failure()
            }
        }

        val today = LocalDate.now()
        val totalDays = ChronoUnit.DAYS.between(birthDate, today)
        val dasha = dashaCalculator.getDashaInfo(birthDate, today = today)
        val fortune = dailyFortuneGenerator.generate(birthDate, today)

        val contentText = "You've lived $totalDays days. $dasha — tap for your cosmic year ahead."
        val bigBody = buildString {
            appendLine("You've now lived $totalDays days on this cosmic journey.")
            appendLine()
            appendLine("Current Period: $dasha")
            appendLine()
            appendLine("${fortune.emoji} ${fortune.headline}")
            appendLine(fortune.body)
            appendLine()
            appendLine("Lucky Number: ${fortune.luckyNumber} · Lucky Color: ${fortune.luckyColor}")
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            YearlyReengagementScheduler.CHANNEL_ID,
        )
            .setSmallIcon(R.drawable.ic_cake)
            .setContentTitle("🎉 Happy Birthday! Your Cosmic Year Ahead")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(YearlyReengagementScheduler.NOTIFICATION_ID, notification)

        val scheduler = YearlyReengagementScheduler(applicationContext)
        scheduler.schedule(birthDate)

        return Result.success()
    }
}
