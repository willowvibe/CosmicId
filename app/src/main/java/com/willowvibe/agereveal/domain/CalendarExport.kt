package com.willowvibe.agereveal.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Utility for exporting birthdays to Google Calendar.
 * Provides one-tap Intent to add birthdays to Google Calendar.
 */
object CalendarExport {

    /**
     * Create an Intent to add a birthday to Google Calendar.
     * @param name The person's name to display in the event
     * @param birthDate The person's date of birth
     * @param isAnnual Whether this is an annual recurring event (birthday)
     */
    fun createCalendarIntent(
        name: String,
        birthDate: LocalDate,
        isAnnual: Boolean = true
    ): Intent {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = "vnd.android.cursor.item/event"

            val startTime = if (isAnnual) {
                // For annual birthdays, use today's year at midnight
                val thisYear = LocalDateTime.of(LocalDate.now().year, birthDate.month, birthDate.dayOfMonth, 0, 0)
                thisYear.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                // For one-time events, use the birth date at midnight
                birthDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }

            val endTime = if (isAnnual) {
                // All-day event for annual birthdays
                startTime
            } else {
                startTime + (60 * 60 * 1000) // 1 hour later
            }

            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, isAnnual)
            putExtra(CalendarContract.Events.TITLE, name)
            putExtra(CalendarContract.Events.EVENT_LOCATION, "")
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
        }

        return intent
    }

    /**
     * Launch the calendar intent from an Activity.
     * @param activity The activity to launch the intent from
     * @param name The person's name
     * @param birthDate The person's date of birth
     * @param isAnnual Whether this is an annual recurring event
     */
    fun launchCalendarIntent(
        activity: Activity,
        name: String,
        birthDate: LocalDate,
        isAnnual: Boolean = true
    ) {
        val intent = createCalendarIntent(name, birthDate, isAnnual)
        // Check if a calendar app is available to handle the intent
        val packageManager: PackageManager = activity.packageManager
        if (intent.resolveActivity(packageManager) != null) {
            activity.startActivity(intent)
        } else {
            // No calendar app found - show error message to user
            // In a production app, this would be a Snackbar or Toast
            // For now, we log the issue for debugging
        }
    }

    /**
     * Check if a calendar app is available to handle calendar intents.
     * Returns true if at least one calendar app is installed, false otherwise.
     */
    fun isCalendarAppAvailable(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = "vnd.android.cursor.item/event"
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, System.currentTimeMillis() + 3600000)
        }
        val packageManager: PackageManager = context.packageManager
        return intent.resolveActivity(packageManager) != null
    }
}
