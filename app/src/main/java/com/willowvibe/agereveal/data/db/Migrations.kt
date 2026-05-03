package com.willowvibe.agereveal.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema migrations.
 *
 * Never use `fallbackToDestructiveMigration()` — it wipes user data (BUG-001).
 * Each schema bump requires an explicit [Migration] below.
 */
object Migrations {

    /**
     * v1 → v2 : adds nullable `birthTime` TEXT column to `saved_birthdays`
     * for precise Nakshatra/Rashi calculations. Existing rows keep birthTime=NULL,
     * which the UI labels as "Approximate".
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE saved_birthdays ADD COLUMN birthTime TEXT DEFAULT NULL")
        }
    }

    /**
     * v2 → v3 : creates `unlocked_badges` table for the Milestone Badges System.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE unlocked_badges (
                    badgeId TEXT PRIMARY KEY NOT NULL,
                    unlockedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
