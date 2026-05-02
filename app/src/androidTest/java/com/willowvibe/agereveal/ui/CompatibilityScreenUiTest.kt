package com.willowvibe.agereveal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.willowvibe.agereveal.domain.AgeInfo
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.domain.RelationshipType
import com.willowvibe.agereveal.ui.screen.AgeComparisonCard
import com.willowvibe.agereveal.ui.screen.CompatibilityResultCard
import com.willowvibe.agereveal.ui.screen.PersonDateCard
import com.willowvibe.agereveal.ui.screen.RelationshipTypeSelector
import com.willowvibe.agereveal.ui.theme.AgeRevealTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * UI tests for CompatibilityScreen sub-components.
 *
 * Covers:
 *  - RelationshipTypeSelector (all 4 types)
 *  - PersonDateCard name input and date display
 *  - AgeComparisonCard rendering
 *  - CompatibilityResultCard score, breakdown, zodiac pairing
 *  - Empty / same-date / result states
 */
class CompatibilityScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCompatibilityResult = CompatibilityResult(
        personAWestern = "Gemini ♊",
        personBWestern = "Virgo ♍",
        personAElement = "Air",
        personBElement = "Earth",
        personAChinese = "🐎 Horse",
        personBChinese = "🐍 Snake",
        westernScore = 72,
        chineseScore = 65,
        overallScore = 68,
        headline = "A balanced cosmic connection",
        description = "Air and Earth can create a stable and grounding partnership when both partners respect their differences.",
        nameA = "Alice",
        nameB = "Bob",
        relationshipType = RelationshipType.Romantic,
        personAAge = AgeInfo(34, 2, 10),
        personBAge = AgeInfo(30, 5, 20),
        ageGapLabel = "Alice is ~4 years older than Bob",
        chineseRelationshipLabel = "Complementary signs",
    )

    @Test
    fun relationshipTypeSelector_showsAllTypes() {
        composeTestRule.setContent {
            AgeRevealTheme {
                RelationshipTypeSelector(
                    selected = RelationshipType.Romantic,
                    onSelect = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Romantic").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sibling").assertIsDisplayed()
        composeTestRule.onNodeWithText("Friendship").assertIsDisplayed()
        composeTestRule.onNodeWithText("Regular").assertIsDisplayed()
    }

    @Test
    fun relationshipTypeSelector_selectsDifferentType() {
        var selectedType: RelationshipType? = null
        composeTestRule.setContent {
            AgeRevealTheme {
                RelationshipTypeSelector(
                    selected = RelationshipType.Romantic,
                    onSelect = { selectedType = it },
                )
            }
        }
        composeTestRule.onNodeWithText("Friendship").performClick()
        assert(selectedType == RelationshipType.Friendship)
    }

    @Test
    fun personDateCard_showsLabel_andEmptyDate() {
        composeTestRule.setContent {
            AgeRevealTheme {
                PersonDateCard(
                    label = "PERSON A",
                    name = "",
                    date = null,
                    onNameChanged = {},
                    onDateSelected = {},
                )
            }
        }
        composeTestRule.onNodeWithText("PERSON A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap to set birthday").assertIsDisplayed()
    }

    @Test
    fun personDateCard_showsNameAndDate_whenSet() {
        composeTestRule.setContent {
            AgeRevealTheme {
                PersonDateCard(
                    label = "PERSON B",
                    name = "Bob",
                    date = LocalDate.of(1995, 3, 20),
                    onNameChanged = {},
                    onDateSelected = {},
                )
            }
        }
        composeTestRule.onNodeWithText("PERSON B").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
    }

    @Test
    fun personDateCard_nameInput_triggersCallback() {
        var enteredName = ""
        composeTestRule.setContent {
            AgeRevealTheme {
                PersonDateCard(
                    label = "PERSON A",
                    name = enteredName,
                    date = null,
                    onNameChanged = { enteredName = it },
                    onDateSelected = {},
                )
            }
        }
        composeTestRule.onNodeWithText("PERSON A").performTextInput("Alice")
        assert(enteredName == "Alice")
    }

    @Test
    fun ageComparisonCard_shoresBothAges_andGapLabel() {
        composeTestRule.setContent {
            AgeRevealTheme {
                AgeComparisonCard(result = sampleCompatibilityResult)
            }
        }
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
        composeTestRule.onNodeWithText("34y 2m 10d").assertIsDisplayed()
        composeTestRule.onNodeWithText("30y 5m 20d").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alice is ~4 years older than Bob").assertIsDisplayed()
    }

    @Test
    fun compatibilityResultCard_showsScoreHeadlineAndBreakdown() {
        composeTestRule.setContent {
            AgeRevealTheme {
                CompatibilityResultCard(result = sampleCompatibilityResult)
            }
        }
        composeTestRule.onNodeWithText("68%").assertIsDisplayed()
        composeTestRule.onNodeWithText("A balanced cosmic connection").assertIsDisplayed()
        composeTestRule.onNodeWithText("WESTERN").assertIsDisplayed()
        composeTestRule.onNodeWithText("72%").assertIsDisplayed()
        composeTestRule.onNodeWithText("CHINESE").assertIsDisplayed()
        composeTestRule.onNodeWithText("65%").assertIsDisplayed()
    }

    @Test
    fun compatibilityResultCard_showsZodiacPairing() {
        composeTestRule.setContent {
            AgeRevealTheme {
                CompatibilityResultCard(result = sampleCompatibilityResult)
            }
        }
        composeTestRule.onNodeWithText("ZODIAC PAIRING").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gemini ♊").assertIsDisplayed()
        composeTestRule.onNodeWithText("Virgo ♍").assertIsDisplayed()
        composeTestRule.onNodeWithText("Air").assertIsDisplayed()
        composeTestRule.onNodeWithText("Earth").assertIsDisplayed()
    }

    @Test
    fun compatibilityResultCard_showsElementReading() {
        composeTestRule.setContent {
            AgeRevealTheme {
                CompatibilityResultCard(result = sampleCompatibilityResult)
            }
        }
        composeTestRule.onNodeWithText("ELEMENT READING").assertIsDisplayed()
        composeTestRule.onNodeWithText("Air and Earth can create a stable", substring = true).assertIsDisplayed()
    }
}
