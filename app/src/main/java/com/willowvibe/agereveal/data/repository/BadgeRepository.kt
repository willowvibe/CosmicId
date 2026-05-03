package com.willowvibe.agereveal.data.repository

import com.willowvibe.agereveal.data.db.BadgeDao
import com.willowvibe.agereveal.data.model.BadgeDefinition
import com.willowvibe.agereveal.data.model.UnlockedBadge
import com.willowvibe.agereveal.domain.BadgeDefinitions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for milestone badges.
 *
 * Responsible for:
 *  - Reading the seeded [BadgeDefinitions.ALL] list
 *  - Persisting unlocks via [BadgeDao]
 *  - Computing which badges are newly reached given a birth date
 */
@Singleton
class BadgeRepository @Inject constructor(
    private val dao: BadgeDao,
) {

    val unlockedBadges: Flow<List<UnlockedBadge>> = dao.getAllUnlocked()

    /** Unlock a single badge by id (no-op if already unlocked). */
    suspend fun unlock(badgeId: String) {
        withContext(Dispatchers.IO) {
            dao.insert(UnlockedBadge(badgeId = badgeId))
        }
    }

    /**
     * Check the current age in seconds against all badge thresholds and
     * unlock any newly reached ones.
     *
     * @return The list of [BadgeDefinition] that were *newly* unlocked this call.
     */
    suspend fun checkAndUnlock(
        birthDate: LocalDate,
        birthTime: LocalTime? = null,
    ): List<BadgeDefinition> = withContext(Dispatchers.IO) {
        val totalSeconds = computeTotalSeconds(birthDate, birthTime)
        val alreadyUnlocked = unlockedBadges.first().map { it.badgeId }.toSet()

        val newlyReached = BadgeDefinitions.ALL
            .filter { it.unlockSeconds <= totalSeconds && it.id !in alreadyUnlocked }

        newlyReached.forEach { badge ->
            dao.insert(UnlockedBadge(badgeId = badge.id))
        }

        newlyReached
    }

    /** Check whether a specific badge has been unlocked. */
    suspend fun hasUnlocked(badgeId: String): Boolean =
        withContext(Dispatchers.IO) { dao.isUnlocked(badgeId) }

    /** Reset all unlocks (debug / testing only). */
    suspend fun resetAll() {
        withContext(Dispatchers.IO) { dao.deleteAll() }
    }

    private fun computeTotalSeconds(birthDate: LocalDate, birthTime: LocalTime?): Long {
        val birthDateTime = birthTime?.let { birthDate.atTime(it) } ?: birthDate.atStartOfDay()
        return ChronoUnit.SECONDS.between(birthDateTime, LocalDateTime.now())
    }
}
