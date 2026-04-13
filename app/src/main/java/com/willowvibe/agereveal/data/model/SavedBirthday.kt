package com.willowvibe.agereveal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Room entity representing a saved birthday (family / friend).
 * Powers the Reminders screen and the home-screen widget.
 */
@Entity(tableName = "saved_birthdays")
data class SavedBirthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Display name of the person */
    val name: String,

    /** Date of birth — stored as ISO-8601 string (e.g. "1996-11-14") via RoomConverters */
    val birthDate: LocalDate,

    /** Emoji or short label, e.g. "🎂" or "Mom" — optional */
    val emoji: String = "🎂",

    /** Whether to fire a local notification 1 day before */
    val notifyEnabled: Boolean = true,

    /** Epoch day of the next birthday after today — kept in sync by the repository */
    val nextBirthdayEpochDay: Long = 0L,
)
