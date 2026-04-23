package com.willowvibe.agereveal.data.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime

/**
 * Room type converters for [java.time.LocalDate] and [java.time.LocalTime].
 * Stored as ISO-8601 strings so the DB file remains human-readable.
 */
class RoomConverters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? = time?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }
}
