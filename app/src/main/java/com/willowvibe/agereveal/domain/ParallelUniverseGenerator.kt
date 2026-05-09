package com.willowvibe.agereveal.domain

import java.time.LocalDate
import javax.inject.Inject

/**
 * Generates "Parallel Universe Birth" fun facts — alternate historical contexts
 * showing what the user's age would mean in different eras and cultures.
 *
 * Pure Kotlin — no Android framework imports.
 */
class ParallelUniverseGenerator @Inject constructor() {

    data class UniverseContext(
        val era: String,
        val location: String,
        val ageText: String,
        val emoji: String,
    )

    private val contexts = listOf(
        ContextTemplate(
            era = "Ancient Rome",
            location = "Roman Empire",
            emoji = "🏛️",
            description = "You'd be {age} years old during the reign of Augustus. A full Roman citizen!",
            yearRange = -27..14,
        ),
        ContextTemplate(
            era = "1920s India",
            location = "British Raj",
            emoji = "🇮🇳",
            description = "At {age}, you'd be marching toward Independence. The Quit India movement is brewing.",
            yearRange = 1920..1929,
        ),
        ContextTemplate(
            era = "1980s Tokyo",
            location = "Japan",
            emoji = "🇯🇵",
            description = "{age} years old during the bubble economy. Everyone's buying stocks and disco dancing.",
            yearRange = 1980..1989,
        ),
        ContextTemplate(
            era = "Renaissance Florence",
            location = "Italy",
            emoji = "🎨",
            description = "A {age}-year-old patron of the arts. Michelangelo is sculpting David right now.",
            yearRange = 1490..1500,
        ),
        ContextTemplate(
            era = "1960s USA",
            location = "America",
            emoji = "🚀",
            description = "At {age}, you're watching the Moon landing live on TV. The Space Age is here.",
            yearRange = 1960..1969,
        ),
        ContextTemplate(
            era = "Ancient Egypt",
            location = "Kingdom of Egypt",
            emoji = "🐪",
            description = "{age} years old — you'd have already survived the annual Nile flood many times over.",
            yearRange = -2600..-2500,
        ),
        ContextTemplate(
            era = "1990s Internet",
            location = "Silicon Valley",
            emoji = "💾",
            description = "A {age}-year-old coding HTML in Notepad. The dot-com boom is about to explode.",
            yearRange = 1995..1999,
        ),
        ContextTemplate(
            era = "Viking Age",
            location = "Scandinavia",
            emoji = "⚔️",
            description = "At {age}, you're a seasoned raider. Your longship has seen many shores.",
            yearRange = 850..900,
        ),
    )

    fun generate(birthDate: LocalDate, today: LocalDate = LocalDate.now()): List<UniverseContext> {
        val age = today.year - birthDate.year
        // Deterministic selection based on birth year — same user always gets same contexts
        val seed = birthDate.year.absoluteValue
        val selected = contexts.shuffled(java.util.Random(seed.toLong())).take(3)

        return selected.map { template ->
            UniverseContext(
                era = template.era,
                location = template.location,
                ageText = template.description.replace("{age}", age.toString()),
                emoji = template.emoji,
            )
        }
    }

    private data class ContextTemplate(
        val era: String,
        val location: String,
        val emoji: String,
        val description: String,
        val yearRange: IntRange,
    )

    private val Int.absoluteValue: Int get() = if (this < 0) -this else this
}
