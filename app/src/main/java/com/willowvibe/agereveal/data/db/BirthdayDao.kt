package com.willowvibe.agereveal.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.willowvibe.agereveal.data.model.SavedBirthday
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for [SavedBirthday].
 * All reads return [Flow] so that Room auto-updates the UI on DB changes.
 */
@Dao
interface BirthdayDao {

    /** Stream all saved birthdays, ordered by the next upcoming birthday (soonest first). */
    @Query("SELECT * FROM saved_birthdays ORDER BY nextBirthdayEpochDay ASC")
    fun getAllOrderedByUpcoming(): Flow<List<SavedBirthday>>

    /** Get the single next upcoming birthday (used by the home-screen widget). */
    @Query("SELECT * FROM saved_birthdays ORDER BY nextBirthdayEpochDay ASC LIMIT 3")
    fun getUpcomingForWidget(): Flow<List<SavedBirthday>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(birthday: SavedBirthday): Long

    @Update
    suspend fun update(birthday: SavedBirthday)

    @Delete
    suspend fun delete(birthday: SavedBirthday)

    @Query("UPDATE saved_birthdays SET nextBirthdayEpochDay = :epochDay WHERE id = :id")
    suspend fun updateNextBirthdayEpochDay(id: Long, epochDay: Long)

    @Query("DELETE FROM saved_birthdays")
    suspend fun deleteAll()
}
