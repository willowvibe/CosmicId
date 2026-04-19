package com.willowvibe.agereveal.data.repository

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.willowvibe.agereveal.data.db.BirthdayDao
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.widget.BirthdayGlanceWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for saved birthdays.
 * Responsible for:
 *  - CRUD via [BirthdayDao]
 *  - Keeping [SavedBirthday.nextBirthdayEpochDay] up-to-date on every insert/update
 *  - Pushing a widget refresh after every mutation so the home-screen widget never
 *    shows stale data (BUG-008).
 */
@Singleton
class BirthdayRepository @Inject constructor(
    private val dao: BirthdayDao,
    @ApplicationContext private val context: Context,
) {

    val allBirthdays: Flow<List<SavedBirthday>> = dao.getAllOrderedByUpcoming()

    val upcomingForWidget: Flow<List<SavedBirthday>> = dao.getUpcomingForWidget()

    suspend fun save(birthday: SavedBirthday): Long {
        val withEpochDay = birthday.copy(nextBirthdayEpochDay = computeNextBirthdayEpochDay(birthday.birthDate))
        val id = dao.insert(withEpochDay)
        notifyWidget()
        return id
    }

    suspend fun update(birthday: SavedBirthday) {
        val withEpochDay = birthday.copy(nextBirthdayEpochDay = computeNextBirthdayEpochDay(birthday.birthDate))
        dao.update(withEpochDay)
        notifyWidget()
    }

    suspend fun delete(birthday: SavedBirthday) {
        dao.delete(birthday)
        notifyWidget()
    }

    suspend fun deleteAll() {
        dao.deleteAll()
        notifyWidget()
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /** Triggers a re-render of all active BirthdayGlanceWidget instances. */
    private suspend fun notifyWidget() {
        withContext(Dispatchers.Default) {
            runCatching {
                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(BirthdayGlanceWidget::class.java)
                for (glanceId in glanceIds) {
                    BirthdayGlanceWidget().update(context, glanceId)
                }
            }
        }
    }

    private fun computeNextBirthdayEpochDay(birthDate: LocalDate): Long {
        val today = LocalDate.now()
        var next = yearSafeBirthday(birthDate, today.year)
        if (!next.isAfter(today)) next = yearSafeBirthday(birthDate, today.year + 1)
        return next.toEpochDay()
    }

    /**
     * Returns [birthDate] adjusted to [year], safely handling Feb 29 birthdays
     * in non-leap years by mapping to Mar 1 instead of throwing [DateTimeException].
     */
    private fun yearSafeBirthday(birthDate: LocalDate, year: Int): LocalDate = try {
        birthDate.withYear(year)
    } catch (_: DateTimeException) {
        LocalDate.of(year, 3, 1)
    }
}
