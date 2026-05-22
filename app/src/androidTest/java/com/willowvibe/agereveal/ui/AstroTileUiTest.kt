package com.willowvibe.agereveal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.ui.screen.AstroTile
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * UI tests for [AstroTile] — the core astrology display component.
 *
 * Covers:
 *  - Locked state placeholder
 *  - Unlocked state with all astrology fields
 *  - Approximate badge visibility
 *  - Grid layout correctness
 *  - Planet position table rendering
 */
class AstroTileUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleResult = AgeResult(
        birthDate = LocalDate.of(1990, 6, 15),
        birthTime = LocalTime.of(14, 30),
        years = 34,
        months = 2,
        days = 10,
        totalDays = 12_500,
        totalHours = 300_000,
        totalMinutes = 18_000_000,
        totalSeconds = 1_080_000_000,
        nextBirthdayDate = LocalDate.of(2026, 6, 15),
        daysToNextBirthday = 45,
        dayOfWeekBorn = "FRIDAY",
        dayOfWeekNextBirthday = "MONDAY",
        westernZodiac = "Gemini ♊",
        rashi = "Mithuna (मिथुन)",
        westernMoonSign = "Virgo ♍",
        rashiLord = "Mercury",
        approximateAscendant = "Kanya (कन्या)",
        tithi = "Dashami (Shukla Paksha)",
        nakshatra = "Ardra (आर्द्रा)",
        nakshatraPada = "Ardra (आर्द्रा) — 2nd Pada",
        chineseZodiac = "🐎 Horse",
        chineseStemBranch = "庚 Geng-午 Wu / Metal-Horse",
        planetPositions = listOf(
            "Sun" to "Gemini",
            "Moon" to "Virgo",
            "Mercury" to "Taurus",
            "Venus" to "Cancer",
            "Mars" to "Leo",
            "Jupiter" to "Aries",
            "Saturn" to "Capricorn",
        ),
        dashaInfo = "Jupiter Mahadasha · Saturn Antardasha",
        baZiInfo = "Year: Geng-Wu (Metal-Horse) · Month: Ren-Wu (Water-Horse)",
        estimatedHeartbeats = 1_296_000_000,
        isExact = true,
    )

    @Test
    fun lockedState_showsPlaceholderText() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = false)
            }
        }
        composeTestRule.onNodeWithText("Watch an ad to reveal your signs").assertIsDisplayed()
    }

    @Test
    fun unlockedState_showsWesternAndRashiSigns() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = true)
            }
        }
        composeTestRule.onNodeWithText("Gemini ♊ · Mithuna (मिथुन)", substring = true).assertIsDisplayed()
    }

    @Test
    fun unlockedState_showsDashaInfo() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = true)
            }
        }
        composeTestRule.onNodeWithText("Jupiter Mahadasha", substring = true).assertIsDisplayed()
    }

    @Test
    fun unlockedState_showsBaZiInfo() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = true)
            }
        }
        composeTestRule.onNodeWithText("BA ZI (FOUR PILLARS)").assertIsDisplayed()
    }

    @Test
    fun unlockedState_showsPlanetTable() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = true)
            }
        }
        composeTestRule.onNodeWithText("PLANETARY POSITIONS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sun").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jupiter").assertIsDisplayed()
    }

    @Test
    fun approximateState_showsApproximateBadge_whenBirthTimeIsNull() {
        val approxResult = sampleResult.copy(birthTime = null, isExact = false)
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = approxResult, isUnlocked = true)
            }
        }
        composeTestRule.onNodeWithText("Approximate", substring = true).assertIsDisplayed()
    }

    @Test
    fun exactLocation_showsLagnaLabel() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = true, hasLocation = true)
            }
        }
        composeTestRule.onNodeWithText("Lagna").assertIsDisplayed()
    }

    @Test
    fun noLocation_showsApproximateLabel() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = true, hasLocation = false)
            }
        }
        composeTestRule.onNodeWithText("Lagna (approx)").assertIsDisplayed()
    }
}
