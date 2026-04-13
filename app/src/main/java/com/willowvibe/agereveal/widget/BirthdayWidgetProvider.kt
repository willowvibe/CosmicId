package com.willowvibe.agereveal.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.willowvibe.agereveal.R
import com.willowvibe.agereveal.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 2×2 home screen widget: shows the next upcoming birthday and the count of days.
 * Uses traditional AppWidgetProvider (RemoteViews) — compatible with all Android versions.
 *
 * Future: migrate to Glance (Jetpack Compose Widgets) after API 26 baseline is stable.
 *
 * Widget is updated every hour (3_600_000 ms) via updatePeriodMillis in widget_info.xml.
 * The widget itself fetches data directly from Room on a coroutine to keep the
 * receiver callback quick (BroadcastReceiver has a tight timeout budget).
 */
class BirthdayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.buildInMemory(context)   // TODO: use DI / single instance
            val dao = db.birthdayDao()
            val upcoming = dao.getUpcomingForWidget().first()

            appWidgetIds.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_layout)

                if (upcoming.isEmpty()) {
                    views.setTextViewText(R.id.widget_days_count, "—")
                    views.setTextViewText(R.id.widget_person_name, "Add birthdays →")
                    views.setTextViewText(R.id.widget_next_info, "")
                } else {
                    val next = upcoming.first()
                    val today = LocalDate.now()
                    val daysLeft = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(next.nextBirthdayEpochDay))

                    views.setTextViewText(R.id.widget_days_count, daysLeft.toString())
                    views.setTextViewText(R.id.widget_person_name, "${next.emoji} ${next.name}'s birthday")

                    val nextLine = if (upcoming.size > 1) {
                        val second = upcoming[1]
                        val daysToSecond = ChronoUnit.DAYS.between(
                            today, LocalDate.ofEpochDay(second.nextBirthdayEpochDay)
                        )
                        "Next: ${second.name} in ${daysToSecond}d"
                    } else ""
                    views.setTextViewText(R.id.widget_next_info, nextLine)
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
