package com.willowvibe.agereveal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.domain.AgeInfo
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.domain.RelationshipType
import com.willowvibe.agereveal.ui.screen.AgeComparisonCard
import com.willowvibe.agereveal.ui.screen.AstroTile
import com.willowvibe.agereveal.ui.screen.CompatibilityResultCard
import com.willowvibe.agereveal.ui.screen.MilestoneRow
import com.willowvibe.agereveal.ui.screen.PersonDateCard
import com.willowvibe.agereveal.ui.screen.RelationshipTypeSelector
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Multi-component integration tests that exercise flows
 * as a user would experience them.
 *
 * Covers:
 *  - Unlocked profile with milestones
 *  - Compatibility input result share card flow
 *  - Milestone notification toggle flow
 */
class EndToEndFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val birthDate = LocalDate.of(1990, 6, 15)

    private val ageResult = AgeResult(
        birthDate = birthDate,
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
        dashaDetail = com.willowvibe.agereveal.domain.DashaInfo(
            mahadasha = com.willowvibe.agereveal.domain.DashaPeriod(
                lord = "Jupiter", totalYears = 16.0, yearsElapsed = 8.0, yearsRemaining = 8.0,
            ),
            antardasha = com.willowvibe.agereveal.domain.DashaPeriod(
                lord = "Saturn", totalYears = 2.53, yearsElapsed = 1.0, yearsRemaining = 1.53,
            ),
            pratyantar = com.willowvibe.agereveal.domain.DashaPeriod(
                lord = "Mercury", totalYears = 0.36, yearsElapsed = 0.1, yearsRemaining = 0.26,
            ),
        ),
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

    // Flow 1: Unlocked profile with milestones
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun unlockedProfileFlow_showsFullAstro_andMilestones() {
        composeTestRule.setContent {
            AgeRevealTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AstroTile(result = ageResult, isUnlocked = true, hasLocation = true)
                    Spacer(Modifier.height(16.dp))
                    MilestoneRow(
                        milestone = pastMilestone,
                        isUnlocked = true,
                        onShare = {},
                    )
                }
            }
        }
        // Unlocked: full astrology visible
        composeTestRule.onNodeWithText("Gemini ♊ · Mithuna (मिथुन)", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("PLANETARY POSITIONS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jupiter Mahadasha", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("BA ZI (FOUR PILLARS)").assertIsDisplayed()
        // Milestone share button visible when unlocked
        composeTestRule.onNodeWithText("10,000th day").assertIsDisplayed()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Flow 2: Compatibility — enter two people → see cosmic match result
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun compatibilityFlow_entersNamesAndDates_showsResult() {
        val compatResult = CompatibilityResult(
            personAWestern = "Aries ♈",
            personBWestern = "Leo ♌",
            personAElement = "Fire",
            personBElement = "Fire",
            personAChinese = "🐉 Dragon",
            personBChinese = "🐒 Monkey",
            westernScore = 88,
            chineseScore = 82,
            overallScore = 85,
            headline = "A blazing cosmic match!",
            description = "Double Fire creates an energetic and passionate bond.",
            nameA = "Alex",
            nameB = "Jordan",
            relationshipType = RelationshipType.Romantic,
            personAAge = AgeInfo(28, 3, 15),
            personBAge = AgeInfo(26, 1, 5),
            ageGapLabel = "Alex is ~2 years older than Jordan",
            chineseRelationshipLabel = "Triple harmony",
        )

        composeTestRule.setContent {
            AgeRevealTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    RelationshipTypeSelector(
                        selected = RelationshipType.Romantic,
                        onSelect = {},
                    )
                    Spacer(Modifier.height(16.dp))
                    PersonDateCard(
                        label = "PERSON A",
                        name = "Alex",
                        date = LocalDate.of(1997, 1, 10),
                        onNameChanged = {},
                        onDateSelected = {},
                    )
                    Spacer(Modifier.height(16.dp))
                    PersonDateCard(
                        label = "PERSON B",
                        name = "Jordan",
                        date = LocalDate.of(1999, 3, 20),
                        onNameChanged = {},
                        onDateSelected = {},
                    )
                    Spacer(Modifier.height(16.dp))
                    AgeComparisonCard(result = compatResult)
                    Spacer(Modifier.height(16.dp))
                    CompatibilityResultCard(result = compatResult)
                }
            }
        }

        // Both names visible
        composeTestRule.onNodeWithText("Alex").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jordan").assertIsDisplayed()

        // Age comparison
        composeTestRule.onNodeWithText("28y 3m 15d").assertIsDisplayed()
        composeTestRule.onNodeWithText("26y 1m 5d").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alex is ~2 years older than Jordan").assertIsDisplayed()

        // Result card
        composeTestRule.onNodeWithText("85%").assertIsDisplayed()
        composeTestRule.onNodeWithText("A blazing cosmic match!").assertIsDisplayed()
        composeTestRule.onNodeWithText("88%").assertIsDisplayed() // Western score
        composeTestRule.onNodeWithText("82%").assertIsDisplayed() // Chinese score
        composeTestRule.onNodeWithText("ZODIAC PAIRING").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aries ♈").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leo ♌").assertIsDisplayed()
        composeTestRule.onNodeWithText("ELEMENT READING").assertIsDisplayed()
    }

    @Test
    fun compatibilityFlow_relationshipTypeSelection_changesActiveType() {
        var currentType = RelationshipType.Romantic
        composeTestRule.setContent {
            AgeRevealTheme {
                RelationshipTypeSelector(
                    selected = currentType,
                    onSelect = { currentType = it },
                )
            }
        }
        // Initially Romantic is selected
        composeTestRule.onNodeWithText("Romantic").assertIsDisplayed()
        // Click Friendship
        composeTestRule.onNodeWithText("Friendship").performClick()
        assert(currentType == RelationshipType.Friendship)
    }

    @Test
    fun compatibilityFlow_personDateCard_nameInputUpdates() {
        var name = ""
        composeTestRule.setContent {
            AgeRevealTheme {
                PersonDateCard(
                    label = "PERSON A",
                    name = name,
                    date = null,
                    onNameChanged = { name = it },
                    onDateSelected = {},
                )
            }
        }
        composeTestRule.onNodeWithText("PERSON A").performTextInput("Sam")
        assert(name == "Sam")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Flow 3: Milestone notification toggle flow
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun milestoneNotificationFlow_togglesOnAndOff() {
        var notificationEnabled = true
        val milestone = Milestone(
            targetDays = 15_000,
            date = LocalDate.now().plusDays(100),
            isPast = false,
            daysAway = 100,
        )

        composeTestRule.setContent {
            AgeRevealTheme {
                MilestoneRow(
                    milestone = milestone,
                    isUnlocked = true,
                    onShare = {},
                    onToggleNotification = { _, enabled ->
                        notificationEnabled = enabled
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("15,000th day").assertIsDisplayed()
        val toggleNode = composeTestRule.onNodeWithContentDescription("Toggle milestone notification")
        toggleNode.assertIsDisplayed()
        toggleNode.performClick()
        assert(!notificationEnabled)
    }
}
