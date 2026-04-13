package com.willowvibe.agereveal.data.repository

import com.willowvibe.agereveal.data.db.BirthdayDao
import com.willowvibe.agereveal.data.model.SavedBirthday
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for saved birthdays.
 * Responsible for:
 *  - CRUD via [BirthdayDao]
 *  - Keeping [SavedBirthday.nextBirthdayEpochDay] up-to-date on every insert/update
 */
@Singleton
class BirthdayRepository @Inject constructor(
    private val dao: BirthdayDao,
) {

    val allBirthdays: Flow<List<SavedBirthday>> = dao.getAllOrderedByUpcoming()

    val upcomingForWidget: Flow<List<SavedBirthday>> = dao.getUpcomingForWidget()

    suspend fun save(birthday: SavedBirthday): Long {
        val withEpochDay = birthday.copy(nextBirthdayEpochDay = computeNextBirthdayEpochDay(birthday.birthDate))
        return dao.insert(withEpochDay)
    }

    suspend fun update(birthday: SavedBirthday) {
        val withEpochDay = birthday.copy(nextBirthdayEpochDay = computeNextBirthdayEpochDay(birthday.birthDate))
        dao.update(withEpochDay)
    }

    suspend fun delete(birthday: SavedBirthday) = dao.delete(birthday)

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private fun computeNextBirthdayEpochDay(birthDate: LocalDate): Long {
        val today = LocalDate.now()
        var next = birthDate.withYear(today.year)
        if (!next.isAfter(today)) {
            next = next.plusYears(1)
        }
        return next.toEpochDay()
    }
}
