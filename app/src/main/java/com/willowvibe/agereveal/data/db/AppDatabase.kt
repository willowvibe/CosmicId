package com.willowvibe.agereveal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.data.model.UnlockedBadge

/**
 * Room database — single table for [SavedBirthday].
 * Increment [version] and provide a migration whenever the schema changes.
 *
 * Schema history:
 *  v1  — initial
 *  v2  — added SavedBirthday.birthTime (nullable TEXT)
 *  v3  — added unlocked_badges table for Milestone Badges System
 */
@Database(
    entities = [SavedBirthday::class, UnlockedBadge::class],
    version = 3,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun birthdayDao(): BirthdayDao
    abstract fun badgeDao(): BadgeDao

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
                    .addMigrations(*Migrations.ALL)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
