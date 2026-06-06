package com.willowvibe.agereveal.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.domain.Aspect
import com.willowvibe.agereveal.domain.AspectType
import com.willowvibe.agereveal.domain.DashaInfo
import com.willowvibe.agereveal.domain.DashaPeriod
import com.willowvibe.agereveal.domain.Gana
import com.willowvibe.agereveal.domain.NakshatraData
import com.willowvibe.agereveal.domain.NavamsaChart
import com.willowvibe.agereveal.domain.SignPosition
import com.willowvibe.agereveal.domain.model.CelestialBody
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalTime

/**
 * Instrumented Compose tests for the 4 new Vedic tab composables in
 * [DetailsUnlockScreen]. Uses createComposeRule() with a synthetic AgeResult
 * — no Hilt, no ViewModel. Mirrors the testable-overload pattern established
 * by OnboardingScreenUiTest (Phase 6.5).
 *
 * The 4 cards (Nakshatra, Dasha, Navamsa, Aspects) are declared `internal` on
 * DetailsUnlockScreen.kt so this test can call them directly.
 */
@RunWith(AndroidJUnit4::class)
class VedicTabUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun sampleResult(
        withNakshatra: Boolean = true,
        withDasha: Boolean = true,
        withNavamsa: Boolean = true,
        withAspects: Boolean = true,
    ): AgeResult {
        val nakshatra = if (withNakshatra) NakshatraData(
            index = 3, // Rohini
            name = "Rohini",
            nameHangul = "रोहिणी",
            lord = CelestialBody.MOON,
            deity = "Brahma / Prajapati",
            deityHangul = "ब्रह्मा",
            gana = Gana.MANUSHYA,
            ganaHangul = "Manushya",
            symbol = "Ox cart",
            symbolEmoji = "🐂",
            startDegree = 40.0,
            endDegree = 53.3333,
        ) else null

        val dasha = if (withDasha) DashaInfo(
            mahadasha = DashaPeriod("Moon", 10.0, 4.0, 6.0),
            antardasha = DashaPeriod("Mars", 1.33, 0.5, 0.83),
            pratyantar = DashaPeriod("Saturn", 0.11, 0.05, 0.06),
        ) else null

        val navamsa = if (withNavamsa) NavamsaChart(
            positions = mapOf(
                CelestialBody.SUN to SignPosition(
                    rashiIndex = 0,
                    rashiName = "Mesha",
                    degreeInSign = 15.0,
                ),
                CelestialBody.MOON to SignPosition(
                    rashiIndex = 3,
                    rashiName = "Karka",
                    degreeInSign = 8.0,
                ),
            ),
            rashiOccupancy = mapOf(
                0 to listOf(CelestialBody.SUN),
                3 to listOf(CelestialBody.MOON, CelestialBody.MERCURY),
            ),
        ) else null

        val aspects = if (withAspects) listOf(
            Aspect(CelestialBody.SUN, CelestialBody.MOON, AspectType.TRINE, 120.0, 1.2, true),
            Aspect(CelestialBody.MARS, CelestialBody.SATURN, AspectType.SQUARE, 90.0, 2.1, false),
        ) else emptyList()

        return AgeResult(
            birthDate = LocalDate.of(2000, 1, 1),
            birthTime = LocalTime.of(12, 0),
            years = 26, months = 5, days = 6,
            totalDays = 9670L, totalHours = 232080L, totalMinutes = 13924800L, totalSeconds = 835488000L,
            nextBirthdayDate = LocalDate.of(2026, 1, 1),
            daysToNextBirthday = 200L,
            dayOfWeekBorn = "SATURDAY", dayOfWeekNextBirthday = "THURSDAY",
            milestones = emptyList<Milestone>(),
            westernZodiac = "Capricorn", westernMoonSign = "Cancer",
            rashi = "Sagittarius", rashiLord = "Jupiter",
            approximateAscendant = "Vrishchika", tithi = "Saptami",
            nakshatra = "Rohini — Mrigashira", nakshatraPada = "Pada 3",
            chineseZodiac = "Dragon", chineseStemBranch = "Metal-Dragon",
            planetPositions = emptyList(),
            planetDignities = emptyList(),
            dashaDetail = dasha,
            baZiInfo = "Jia-Chen (Wood-Dragon)",
            lunarBirthday = "11th Month, Day 6",
            estimatedHeartbeats = 1002979200L,
            globalPercentile = "Top 0.1%", sharedBirthDateEstimate = "~280,000 people",
            parallelUniverses = emptyList(),
            nakshatraMetadata = nakshatra,
            navamsaChart = navamsa,
            planetaryAspects = aspects,
            tropicalAscendant = "Virgo",
        )
    }

    @Test
    fun nakshatraCard_rendersLordAndDeity() {
        val result = sampleResult()
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    AgeResultNakshatraCard(
                        metadata = result.nakshatraMetadata!!,
                        name = result.nakshatra,
                        padaName = result.nakshatraPada,
                        tithi = result.tithi,
                        isApprox = false,
                    )
                }
            }
        }
        composeTestRule.onNodeWithText("NAKSHATRA").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lord: Moon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Deity: Brahma / Prajapati").assertIsDisplayed()
    }

    @Test
    fun dashaTreeCard_rendersThreeRows() {
        val result = sampleResult()
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    AgeResultDashaTreeCard(detail = result.dashaDetail!!)
                }
            }
        }
        composeTestRule.onNodeWithText("MAHADASHA").assertIsDisplayed()
        composeTestRule.onNodeWithText("ANTARDASHA").assertIsDisplayed()
        composeTestRule.onNodeWithText("PRATYANTAR").assertIsDisplayed()
    }

    @Test
    fun navamsaSnapshotCard_rendersHeader() {
        val result = sampleResult()
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    NavamsaSnapshotCard(chart = result.navamsaChart!!)
                }
            }
        }
        composeTestRule.onNodeWithText("NAVARMSA (D-9)").assertIsDisplayed()
    }

    @Test
    fun planetaryAspectsCard_rendersHarmoniousAndTenseSections() {
        val result = sampleResult()
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    PlanetaryAspectsCard(aspects = result.planetaryAspects)
                }
            }
        }
        composeTestRule.onNodeWithText("HARMONIOUS").assertIsDisplayed()
        composeTestRule.onNodeWithText("TENSE").assertIsDisplayed()
    }

    @Test
    fun emptyAspects_rendersNoAspectsInOrb() {
        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    PlanetaryAspectsCard(aspects = emptyList())
                }
            }
        }
        composeTestRule.onNodeWithText("No major aspects in orb").assertIsDisplayed()
    }
}
