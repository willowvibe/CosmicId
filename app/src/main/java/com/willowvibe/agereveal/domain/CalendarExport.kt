package com.willowvibe.agereveal.domain

import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
        activity.startActivity(intent)
    }
}
