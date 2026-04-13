package com.willowvibe.agereveal.data.db

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Room type converters for [java.time.LocalDate].
 * Stored as ISO-8601 string ("YYYY-MM-DD") so it is human-readable in the DB file.
 */
class RoomConverters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }
}
