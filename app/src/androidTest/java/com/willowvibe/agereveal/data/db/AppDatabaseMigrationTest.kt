package com.willowvibe.agereveal.data.db

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppDatabaseMigrationTest {

    private val testDbName = "migration-test.db"

    @Test
    fun migration_1_2_addsBirthTimeColumnAndPreservesData() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbPath = context.getDatabasePath(testDbName).absolutePath

        // Clean up any prior test artifact
        File(dbPath).delete()

        // 1. Create a v1 database manually
        val v1Db = SQLiteDatabase.openOrCreateDatabase(dbPath, null)
        v1Db.version = 1
        v1Db.execSQL(
            """
            CREATE TABLE saved_birthdays (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                birthDate TEXT NOT NULL,
                emoji TEXT NOT NULL,
                notifyEnabled INTEGER NOT NULL,
                nextBirthdayEpochDay INTEGER NOT NULL
            )
            """.trimIndent()
        )
        v1Db.execSQL(
            """
            INSERT INTO saved_birthdays (name, birthDate, emoji, notifyEnabled, nextBirthdayEpochDay)
            VALUES ('Alice', '1990-05-15', '🎂', 1, 20000)
            """.trimIndent()
        )
        v1Db.close()

        // 2. Open with Room at v2, applying the migration
        val roomDb = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            testDbName,
        ).addMigrations(Migrations.MIGRATION_1_2)
            .build()

        // 3. Verify migrated data
        runBlocking {
            val all = roomDb.birthdayDao().getAllOrderedByUpcoming().first()
            assertEquals(1, all.size)
            val alice = all[0]
            assertEquals("Alice", alice.name)
            assertEquals(java.time.LocalDate.of(1990, 5, 15), alice.birthDate)
            assertNull(alice.birthTime)
            assertEquals("🎂", alice.emoji)
            assertEquals(true, alice.notifyEnabled)
            assertEquals(20_000L, alice.nextBirthdayEpochDay)
        }

        roomDb.close()
    }

    @Test
    fun migration_1_2_allowsNewRowWithBirthTime() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dbPath = context.getDatabasePath(testDbName + "_new").absolutePath
        File(dbPath).delete()

        // 1. Create v1 database
        val v1Db = SQLiteDatabase.openOrCreateDatabase(dbPath, null)
        v1Db.version = 1
        v1Db.execSQL(
            """
            CREATE TABLE saved_birthdays (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                birthDate TEXT NOT NULL,
                emoji TEXT NOT NULL,
                notifyEnabled INTEGER NOT NULL,
                nextBirthdayEpochDay INTEGER NOT NULL
            )
            """.trimIndent()
        )
        v1Db.close()

        // 2. Migrate to v2
        val roomDb = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            testDbName + "_new",
        ).addMigrations(Migrations.MIGRATION_1_2)
            .build()

        // 3. Insert a new row with birthTime after migration
        runBlocking {
            val id = roomDb.birthdayDao().insert(
                com.willowvibe.agereveal.data.model.SavedBirthday(
                    name = "Bob",
                    birthDate = java.time.LocalDate.of(1985, 3, 10),
                    birthTime = java.time.LocalTime.of(9, 30),
                    nextBirthdayEpochDay = 15_000L,
                )
            )
            assertTrue(id > 0)

            val all = roomDb.birthdayDao().getAllOrderedByUpcoming().first()
            assertEquals(1, all.size)
            assertEquals(java.time.LocalTime.of(9, 30), all[0].birthTime)
        }

        roomDb.close()
    }
}
