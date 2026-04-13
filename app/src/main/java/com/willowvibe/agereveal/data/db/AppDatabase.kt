package com.willowvibe.agereveal.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.willowvibe.agereveal.data.model.SavedBirthday

/**
 * Room database — single table for [SavedBirthday].
 * Increment [version] and provide a migration whenever the schema changes.
 */
@Database(
    entities = [SavedBirthday::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun birthdayDao(): BirthdayDao

    companion object {
        const val DATABASE_NAME = "age_reveal.db"

        /**
         * Temporary helper used by [BirthdayWidgetProvider] which cannot use Hilt injection.
         * TODO: replace with a proper non-Hilt singleton or EntryPoint injection.
         */
        fun buildInMemory(context: android.content.Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME,
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}
