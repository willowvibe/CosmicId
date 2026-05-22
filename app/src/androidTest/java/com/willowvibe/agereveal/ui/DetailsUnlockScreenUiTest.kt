package com.willowvibe.agereveal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.ui.screen.AstroTile
import com.willowvibe.agereveal.ui.screen.HeartbeatRow
import com.willowvibe.agereveal.ui.screen.LifeProgressBar
import com.willowvibe.agereveal.ui.screen.MilestoneRow
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * UI tests for DetailsUnlockScreen sub-components.
 *
 * Covers:
 *  - MilestoneRow rendering, share button, notification toggle
 *  - HeartbeatRow display
 *  - LifeProgressBar percentage
 *  - AstroTile in profile context
 */
class DetailsUnlockScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleResult = AgeResult(
        birthDate = LocalDate.of(1990, 6, 15),
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

    private val pastMilestone = Milestone(
        targetDays = 10_000,
        date = LocalDate.of(2017, 9, 1),
        isPast = true,
        daysAway = -2_500,
    )

    private val futureMilestone = Milestone(
        targetDays = 15_000,
        date = LocalDate.of(2031, 5, 10),
        isPast = false,
        daysAway = 2_500,
    )

    @Test
    fun milestoneRow_pastMilestone_showsShareButton() {
        composeTestRule.setContent {
            AgeRevealTheme {
                MilestoneRow(
                    milestone = pastMilestone,
                    isUnlocked = true,
                    onShare = {},
                )
            }
        }
        composeTestRule.onNodeWithText("10,000th day").assertIsDisplayed()
        composeTestRule.onNodeWithText("✓").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share milestone").assertIsDisplayed()
    }

    @Test
    fun milestoneRow_futureMilestone_showsNotificationToggle() {
        composeTestRule.setContent {
            AgeRevealTheme {
                MilestoneRow(
                    milestone = futureMilestone,
                    isUnlocked = true,
                    onShare = {},
                )
            }
        }
        composeTestRule.onNodeWithText("15,000th day").assertIsDisplayed()
        composeTestRule.onNodeWithText("IN 2,500D").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Toggle milestone notification").assertIsDisplayed()
    }

    @Test
    fun milestoneRow_lockedState_hidesShareAndToggle() {
        composeTestRule.setContent {
            AgeRevealTheme {
                MilestoneRow(
                    milestone = pastMilestone,
                    isUnlocked = false,
                    onShare = {},
                )
            }
        }
        composeTestRule.onNodeWithText("10,000th day").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share milestone").assertDoesNotExist()
    }

    @Test
    fun milestoneRow_todayMilestone_showsTodayBadge() {
        val todayMilestone = Milestone(
            targetDays = 12_500,
            date = LocalDate.now(),
            isPast = false,
            daysAway = 0,
        )
        composeTestRule.setContent {
            AgeRevealTheme {
                MilestoneRow(
                    milestone = todayMilestone,
                    isUnlocked = true,
                    onShare = {},
                )
            }
        }
        composeTestRule.onNodeWithText("TODAY ✦").assertIsDisplayed()
    }

    @Test
    fun heartbeatRow_showsFormattedHeartbeats() {
        composeTestRule.setContent {
            AgeRevealTheme {
                HeartbeatRow(heartbeats = 1_296_000_000)
            }
        }
        composeTestRule.onNodeWithText("1.30 B heartbeats and counting", substring = true).assertIsDisplayed()
    }

    @Test
    fun lifeProgressBar_showsPercentageAndYears() {
        composeTestRule.setContent {
            AgeRevealTheme {
                LifeProgressBar(totalDays = 12_500)
            }
        }
        composeTestRule.onNodeWithText("LIFE LIVED").assertIsDisplayed()
        // 12500 / 29200 ≈ 42%
        composeTestRule.onNodeWithText("42%", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("34 yrs of 80", substring = true).assertIsDisplayed()
    }

    @Test
    fun astroTile_inProfileContext_showsUnlockedContent() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AstroTile(result = sampleResult, isUnlocked = true, hasLocation = true)
            }
        }
        composeTestRule.onNodeWithText("WESTERN · VEDIC · CHINESE").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gemini ♊ · Mithuna (मिथुन)", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("PLANETARY POSITIONS").assertIsDisplayed()
    }
}
