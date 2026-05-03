package com.willowvibe.agereveal.domain

import androidx.compose.ui.graphics.Color
import com.willowvibe.agereveal.data.model.BadgeDefinition
import com.willowvibe.agereveal.data.model.BadgeRarity

/**
 * Seeded milestone badge definitions.
 *
 * These are never persisted; only the user's unlock record is stored.
 * Thresholds are in **seconds alive**.
 */
object BadgeDefinitions {

    val ALL: List<BadgeDefinition> = listOf(
        BadgeDefinition(
            id = "1M_SEC",
            title = "1 Million Seconds",
            description = "You have lived one million seconds.",
            unlockSeconds = 1_000_000L,
            iconEmoji = "🔔",
            rarity = BadgeRarity.Common,
        ),
        BadgeDefinition(
            id = "10M_SEC",
            title = "10 Million Seconds",
            description = "Ten million ticks of the cosmic clock.",
            unlockSeconds = 10_000_000L,
            iconEmoji = "⏳",
            rarity = BadgeRarity.Common,
        ),
        BadgeDefinition(
            id = "100M_SEC",
            title = "100 Million Seconds",
            description = "Over three years of existence.",
            unlockSeconds = 100_000_000L,
            iconEmoji = "🌟",
            rarity = BadgeRarity.Uncommon,
        ),
        BadgeDefinition(
            id = "1B_SEC",
            title = "Billion Seconds Club",
            description = "One billion seconds lived. Welcome to the club.",
            unlockSeconds = 1_000_000_000L,
            iconEmoji = "🏆",
            rarity = BadgeRarity.Rare,
        ),
        BadgeDefinition(
            id = "5K_DAYS",
            title = "5K Days Society",
            description = "Five thousand days of memories.",
            unlockSeconds = 5_000L * 86_400L,
            iconEmoji = "📖",
            rarity = BadgeRarity.Common,
        ),
        BadgeDefinition(
            id = "10K_DAYS",
            title = "10K Days Society",
            description = "Ten thousand sunrises witnessed.",
            unlockSeconds = 10_000L * 86_400L,
            iconEmoji = "🌅",
            rarity = BadgeRarity.Uncommon,
        ),
        BadgeDefinition(
            id = "25K_DAYS",
            title = "Silver Jubilee (Days)",
            description = "25,000 days — a silver milestone in days.",
            unlockSeconds = 25_000L * 86_400L,
            iconEmoji = "🥈",
            rarity = BadgeRarity.Rare,
        ),
        BadgeDefinition(
            id = "30K_DAYS",
            title = "30K Legend",
            description = "Thirty thousand days of life.",
            unlockSeconds = 30_000L * 86_400L,
            iconEmoji = "💎",
            rarity = BadgeRarity.Epic,
        ),
        BadgeDefinition(
            id = "QUARTER_CENT",
            title = "Quarter-Century Captain",
            description = "25 years of navigating existence.",
            unlockSeconds = 25L * 365L * 86_400L,
            iconEmoji = "⚓",
            rarity = BadgeRarity.Uncommon,
        ),
        BadgeDefinition(
            id = "HALF_CENT",
            title = "Half-Century Hero",
            description = "Fifty years of stories.",
            unlockSeconds = 50L * 365L * 86_400L,
            iconEmoji = "🎖️",
            rarity = BadgeRarity.Rare,
        ),
        BadgeDefinition(
            id = "FULL_CENT",
            title = "Century Survivor",
            description = "One hundred years. Truly legendary.",
            unlockSeconds = 100L * 365L * 86_400L,
            iconEmoji = "🏁",
            rarity = BadgeRarity.Legendary,
        ),
        BadgeDefinition(
            id = "LEAP_BABY",
            title = "Leap Baby",
            description = "Born on the rarest day of the calendar.",
            unlockSeconds = 0L,
            iconEmoji = "🎊",
            rarity = BadgeRarity.Special,
        ),
        BadgeDefinition(
            id = "MILLENNIUM",
            title = "Millennium Baby",
            description = "Born in the year 2000.",
            unlockSeconds = 0L,
            iconEmoji = "🌎",
            rarity = BadgeRarity.Special,
        ),
    )

    /** Return badges whose threshold is met by [secondsAlive]. */
    fun forSeconds(secondsAlive: Long): List<BadgeDefinition> =
        ALL.filter { it.unlockSeconds <= secondsAlive }

    /** Color associated with each rarity tier. */
    fun rarityColor(rarity: BadgeRarity): Color = when (rarity) {
        BadgeRarity.Common -> Color(0xFFA89B86)
        BadgeRarity.Uncommon -> Color(0xFF3D7A6E)
        BadgeRarity.Rare -> Color(0xFFDEB84A)
        BadgeRarity.Epic -> Color(0xFF9D4EDD)
        BadgeRarity.Legendary -> Color(0xFFFF6B35)
        BadgeRarity.Special -> Color(0xFF00F5FF)
    }
}
