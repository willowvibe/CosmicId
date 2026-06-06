package com.willowvibe.agereveal.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.willowvibe.agereveal.domain.AstronomicalCalculator
import com.willowvibe.agereveal.domain.BaZiCalculator
import com.willowvibe.agereveal.domain.SajuKoreanCalculator
import com.willowvibe.agereveal.domain.ZodiacCalculator
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Compose tests for the Korean Saju 십신 (Ten Gods) card.
 *
 * `SajuTenGodsCard` is `internal` (not `private`) on [DetailsUnlockScreen] —
 * private file-level symbols are NOT visible to androidTest (different
 * compilation unit), so we use `internal` to make the card testable. This
 * matches the pattern set by the Vedic Phase E cards
 * (e.g. AgeResultNakshatraCard) in the same file.
 */
@RunWith(AndroidJUnit4::class)
class SajuTenGodsCardUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var chart: SajuKoreanCalculator.SajuChart

    @Before
    fun setUp() {
        val baZi = BaZiCalculator(ZodiacCalculator(AstronomicalCalculator()))
        val kor = SajuKoreanCalculator(baZi)
        chart = kor.computeChart(
            date = java.time.LocalDate.of(1993, 12, 11),
            hour = 2,
            minute = 45,
            zoneOffsetHours = 5.5,
            gender = BaZiCalculator.Gender.MALE,
        )
        assertNotNull(chart)
    }

    @Test
    fun rendersCardTitle() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    SajuTenGodsCard(chart = chart)
                }
            }
        }
        composeTestRule.onNodeWithText("십신", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsAllFourPillarsAsRows() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    SajuTenGodsCard(chart = chart)
                }
            }
        }
        composeTestRule.onNodeWithText("년주", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("월주", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("일주", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("시주", substring = true).assertIsDisplayed()
    }
}
