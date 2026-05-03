package com.willowvibe.agereveal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Rarity tier for milestone badges.
 */
enum class BadgeRarity {
    Common,
    Uncommon,
    Rare,
    Epic,
    Legendary,
    Special,
}

/**
 * Immutable definition of a milestone badge.
 * These are seeded in [BadgeDefinitions] and never persisted individually —
 * only the user's unlock record ([UnlockedBadge]) is stored in Room.
 */
data class BadgeDefinition(
    val id: String,
    val title: String,
    val description: String,
    val unlockSeconds: Long,
    val iconEmoji: String,
    val rarity: BadgeRarity,
)

/**
 * Room entity tracking which badges the user has unlocked.
 *
 * The [badgeId] maps to a [BadgeDefinition.id] from the seeded list.
 * [unlockedAt] is a Unix epoch millis timestamp.
 */
@Entity(tableName = "unlocked_badges")
data class UnlockedBadge(
    @PrimaryKey
    val badgeId: String,
    val unlockedAt: Long = System.currentTimeMillis(),
)
