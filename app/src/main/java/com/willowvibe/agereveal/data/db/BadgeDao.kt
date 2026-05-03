package com.willowvibe.agereveal.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.willowvibe.agereveal.data.model.UnlockedBadge
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for [UnlockedBadge].
 * All reads return [Flow] so Room auto-updates the UI on DB changes.
 */
@Dao
interface BadgeDao {

    /** Stream all unlocked badges. */
    @Query("SELECT * FROM unlocked_badges ORDER BY unlockedAt DESC")
    fun getAllUnlocked(): Flow<List<UnlockedBadge>>

    /** Check whether a specific badge has been unlocked. */
    @Query("SELECT COUNT(*) > 0 FROM unlocked_badges WHERE badgeId = :badgeId")
    suspend fun isUnlocked(badgeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(unlocked: UnlockedBadge)

    @Query("DELETE FROM unlocked_badges")
    suspend fun deleteAll()
}
