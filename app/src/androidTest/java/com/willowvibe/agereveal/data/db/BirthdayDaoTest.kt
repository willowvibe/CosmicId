package com.willowvibe.agereveal.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.willowvibe.agereveal.data.model.SavedBirthday
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class BirthdayDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: BirthdayDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = db.birthdayDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insert_returnsGeneratedId() = runBlocking {
        val birthday = sampleBirthday(name = "Alice")
        val id = dao.insert(birthday)
        assertTrue("Generated ID should be positive", id > 0)
    }

    @Test
    fun insertAndRead_roundTripsAllFields() = runBlocking {
        val birthday = SavedBirthday(
            name = "Alice",
            birthDate = LocalDate.of(1990, 5, 15),
            birthTime = LocalTime.of(14, 30),
            emoji = "🎂",
            notifyEnabled = true,
            nextBirthdayEpochDay = 20_000L,
        )
        dao.insert(birthday)

        val all = dao.getAllOrderedByUpcoming().first()
        assertEquals(1, all.size)
        with(all[0]) {
            assertEquals("Alice", name)
            assertEquals(LocalDate.of(1990, 5, 15), birthDate)
            assertEquals(LocalTime.of(14, 30), birthTime)
            assertEquals("🎂", emoji)
            assertEquals(true, notifyEnabled)
            assertEquals(20_000L, nextBirthdayEpochDay)
        }
    }

    @Test
    fun getUpcomingForWidget_limitsToThreeAndOrders() = runBlocking {
        repeat(5) {
            dao.insert(
                SavedBirthday(
                    name = "Person $it",
                    birthDate = LocalDate.of(1990 + it, 1, 1),
                    nextBirthdayEpochDay = it.toLong(),
                )
            )
        }

        val widgetItems = dao.getUpcomingForWidget().first()
        assertEquals(3, widgetItems.size)
        assertEquals(listOf("Person 0", "Person 1", "Person 2"), widgetItems.map { it.name })
    }

    @Test
    fun update_modifiesExistingRow() = runBlocking {
        val id = dao.insert(sampleBirthday(name = "Bob"))
        val inserted = dao.getAllOrderedByUpcoming().first().first()
        val updated = inserted.copy(name = "Robert", notifyEnabled = false)
        dao.update(updated)

        val all = dao.getAllOrderedByUpcoming().first()
        assertEquals(1, all.size)
        assertEquals("Robert", all[0].name)
        assertEquals(false, all[0].notifyEnabled)
        assertEquals(id, all[0].id)
    }

    @Test
    fun updateNextBirthdayEpochDay_changesOnlyEpochDay() = runBlocking {
        val id = dao.insert(
            sampleBirthday(name = "Charlie", nextBirthdayEpochDay = 1_000L)
        )
        dao.updateNextBirthdayEpochDay(id, 5_000L)

        val all = dao.getAllOrderedByUpcoming().first()
        assertEquals(5_000L, all[0].nextBirthdayEpochDay)
        assertEquals("Charlie", all[0].name)
    }

    @Test
    fun delete_removesRow() = runBlocking {
        val birthday = sampleBirthday(name = "Dave")
        dao.insert(birthday)
        val inserted = dao.getAllOrderedByUpcoming().first().first()
        dao.delete(inserted)

        val all = dao.getAllOrderedByUpcoming().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun deleteAll_clearsTable() = runBlocking {
        dao.insert(sampleBirthday(name = "Eve"))
        dao.insert(sampleBirthday(name = "Frank"))
        dao.deleteAll()

        val all = dao.getAllOrderedByUpcoming().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun getAllOrderedByUpcoming_sortsByEpochDayAscending() = runBlocking {
        dao.insert(sampleBirthday(name = "Later", nextBirthdayEpochDay = 300L))
        dao.insert(sampleBirthday(name = "Sooner", nextBirthdayEpochDay = 100L))
        dao.insert(sampleBirthday(name = "Middle", nextBirthdayEpochDay = 200L))

        val all = dao.getAllOrderedByUpcoming().first()
        assertEquals(listOf("Sooner", "Middle", "Later"), all.map { it.name })
    }

    private fun sampleBirthday(
        name: String,
        nextBirthdayEpochDay: Long = 0L,
    ) = SavedBirthday(
        name = name,
        birthDate = LocalDate.of(1990, 1, 1),
        nextBirthdayEpochDay = nextBirthdayEpochDay,
    )
}
