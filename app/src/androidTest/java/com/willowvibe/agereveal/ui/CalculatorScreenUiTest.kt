package com.willowvibe.agereveal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.ui.screen.ClockFaceHero
import com.willowvibe.agereveal.ui.screen.MiniStatRow
import com.willowvibe.agereveal.ui.screen.PrecisionChip
import com.willowvibe.agereveal.ui.screen.SecondsStrip
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * UI tests for CalculatorScreen sub-components.
 *
 * Covers:
 *  - PrecisionChip states (Add vs set values)
 *  - ClockFaceHero age display
 *  - Seconds strip
 *  - Mini stat chips
 */
class CalculatorScreenUiTest {

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
    )

    @Test
    fun precisionRow_timeChip_showsAdd_whenNoTimeSet() {
        composeTestRule.setContent {
            AgeRevealTheme {
                PrecisionChip(
                    label = "TIME",
                    value = "Add",
                    isSet = false,
                    onClick = {},
                )
            }
        }
        composeTestRule.onNodeWithText("TIME").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
    }

    @Test
    fun precisionRow_timeChip_showsTime_whenSet() {
        composeTestRule.setContent {
            AgeRevealTheme {
                PrecisionChip(
                    label = "TIME",
                    value = "2:30 PM",
                    isSet = true,
                    onClick = {},
                )
            }
        }
        composeTestRule.onNodeWithText("2:30 PM").assertIsDisplayed()
    }

    @Test
    fun precisionRow_locationChip_showsCoordinates_whenSet() {
        composeTestRule.setContent {
            AgeRevealTheme {
                PrecisionChip(
                    label = "LOCATION",
                    value = "19.1°, 72.9°",
                    isSet = true,
                    onClick = {},
                )
            }
        }
        composeTestRule.onNodeWithText("19.1°, 72.9°").assertIsDisplayed()
    }

    @Test
    fun clockFaceHero_rendersAgeNumerals() {
        composeTestRule.setContent {
            AgeRevealTheme {
                ClockFaceHero(result = sampleResult)
            }
        }
        composeTestRule.onNodeWithText("34").assertIsDisplayed()
        composeTestRule.onNodeWithText("YEARS").assertIsDisplayed()
        composeTestRule.onNodeWithText("02").assertIsDisplayed()
        composeTestRule.onNodeWithText("MONTHS").assertIsDisplayed()
    }

    @Test
    fun secondsStrip_showsTotalSeconds() {
        composeTestRule.setContent {
            AgeRevealTheme {
                SecondsStrip(result = sampleResult)
            }
        }
        composeTestRule.onNodeWithText("SECONDS ALIVE").assertIsDisplayed()
    }

    @Test
    fun miniStatRow_showsDaysHoursNextBirthday() {
        composeTestRule.setContent {
            AgeRevealTheme {
                MiniStatRow(result = sampleResult)
            }
        }
        composeTestRule.onNodeWithText("DAYS").assertIsDisplayed()
        composeTestRule.onNodeWithText("HOURS").assertIsDisplayed()
        composeTestRule.onNodeWithText("NEXT BDAY").assertIsDisplayed()
    }
}
