package com.willowvibe.agereveal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton database instance.
         *
         * Used by components that cannot participate in Hilt injection (e.g. AppWidgetProvider).
         * Hilt's [DatabaseModule] also delegates to this method so both paths share the same instance.
         */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME,
                )
                    // Explicit migration objects must be added before any schema change
                    // See: BUG-001 in BUGS_AND_ISSUES.md
                    .addMigrations(
                        // Migration 1 -> 2 example (commented until needed):
                        // Migration1to2()
                    )
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
