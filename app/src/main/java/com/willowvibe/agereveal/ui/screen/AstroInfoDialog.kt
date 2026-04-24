package com.willowvibe.agereveal.ui.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────────────────
// Astrology Explanations Data
// ─────────────────────────────────────────────────────────────────────────────

data class AstrologyTerm(
    val title: String,
    val description: String,
)

val astrologyTerms = listOf(
    AstrologyTerm(
        title = "Western Zodiac",
        description = "The Western zodiac is a system of 12 astrological signs, each representing a 30-degree sector of the ecliptic circle. Your zodiac sign is determined by the position of the sun at the time of your birth. Each sign is associated with distinct personality traits and characteristics."
    ),
    AstrologyTerm(
        title = "Rashi (Vedic Zodiac)",
        description = "In Vedic astrology, the Rashi refers to the moon sign - the zodiac sign in which the moon was located at the time of your birth. There are 12 Rashis, each governed by a specific planet. The Rashi plays a crucial role in predicting life events and determining auspicious timings."
    ),
    AstrologyTerm(
        title = "Nakshatra",
        description = "A Nakshatra is a lunar mansion in Vedic astrology. There are 27 Nakshatras, each spanning 13 degrees and 20 minutes of the moon's orbit. Your Nakshatra reveals deeper insights about your personality, life purpose, and spiritual journey. It's used to determine the most auspicious times for various activities."
    ),
    AstrologyTerm(
        title = "Nakshatra Pada",
        description = "Each nakshatra is divided into four equal quarters called padas (3°20′ each). Your pada refines the nakshatra's influence and determines your navamsa (D-9 chart) placement, offering deeper insight into your inner nature and life path."
    ),
    AstrologyTerm(
        title = "Moon Sign",
        description = "In Western astrology, your Moon sign represents your emotional nature, instincts, and subconscious reactions. It is determined by the position of the Moon at the time of your birth and offers insight into how you process feelings and seek security."
    ),
    AstrologyTerm(
        title = "Chinese Zodiac",
        description = "The Chinese zodiac is a 12-year cycle, with each year represented by a specific animal. Your Chinese zodiac sign is determined by your birth year. The animals are: Rat, Ox, Tiger, Rabbit, Dragon, Snake, Horse, Goat, Monkey, Rooster, Dog, and Pig. Each animal is associated with particular characteristics and fortunes."
    ),
    AstrologyTerm(
        title = "Moon Phase",
        description = "The moon phase at your birth represents the current lunar cycle position when you were born. The moon moves through 8 primary phases in approximately 29.5 days. Each phase carries unique energetic qualities that influence personality traits and emotional patterns."
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// Astrology Explanation Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AstrologyExplanationDialog(
    term: AstrologyTerm,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
        title = {
            Text(
                text = term.title,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = term.description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Utility: Get term by name
// ─────────────────────────────────────────────────────────────────────────────

fun getAstrologyTerm(termName: String): AstrologyTerm? {
    return astrologyTerms.find { it.title == termName }
}
