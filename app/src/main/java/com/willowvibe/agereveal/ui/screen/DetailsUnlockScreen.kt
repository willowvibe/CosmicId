package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.willowvibe.agereveal.ui.components.AgeBody
import com.willowvibe.agereveal.ui.components.AgeCard
import com.willowvibe.agereveal.ui.components.AgeLabel
import com.willowvibe.agereveal.ui.components.AgeValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.domain.AstronomicalCalculator
import com.willowvibe.agereveal.domain.GenerationCalculator
import com.willowvibe.agereveal.domain.LifeStatsCalculator
import com.willowvibe.agereveal.domain.MoonPhaseCalculator
import com.willowvibe.agereveal.domain.PlanetAgeCalculator
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmInkMute
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import java.time.format.DateTimeFormatter

@Composable
fun DetailsUnlockScreen(
    viewModel: CalculatorViewModel,
    onShareMilestone: (Milestone) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

    // ── Phase 5 Gen Z feature computations ──────────────────────────────────
    val generationCalculator = remember { GenerationCalculator() }
    val moonPhaseCalculator = remember { MoonPhaseCalculator() }
    val planetAgeCalculator = remember { PlanetAgeCalculator() }
    val astroCalc = remember { AstronomicalCalculator() }

    val generation = remember(result?.birthDate) {
        result?.birthDate?.year?.let { generationCalculator.getGeneration(it) }
    }
    val birthMoonPhase = remember(result?.birthDate) {
        result?.birthDate?.let { date ->
            val snap = astroCalc.snapshot(date, result?.birthTime)
            moonPhaseCalculator.calculate(snap.tropicalSunLongitude, snap.tropicalMoonLongitude)
        }
    }
    val currentMoonPhase = remember {
        val now = java.time.LocalDate.now()
        val snap = astroCalc.snapshot(now)
        moonPhaseCalculator.calculate(snap.tropicalSunLongitude, snap.tropicalMoonLongitude)
    }
    val planetAges = remember(result) {
        result?.let { planetAgeCalculator.calculatePlanetAges(it.years.toDouble()) } ?: emptyList()
    }
    val lifeStatsCalculator = remember { LifeStatsCalculator() }
    val lifeStats = remember(result) {
        result?.let { r ->
            lifeStatsCalculator.calculateAll(
                birthDate = r.birthDate,
                today = java.time.LocalDate.now(),
                totalDays = r.totalDays,
                totalSeconds = r.totalSeconds,
            )
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(
                "Your profile",
                style = MaterialTheme.typography.titleLarge,
                color = WarmInk,
            )
            if (result != null) {
                Text(
                    "Born under a ${moonPhaseHint(result.birthDate.monthValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInkMute,
                )
            }
        }

        if (result == null) {
            // No birth date entered yet
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Enter a birth date on the My Cosmos tab first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkDim,
                )
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { 4 })
            val scope = rememberCoroutineScope()
            val tabTitles = listOf("Overview", "Western", "Vedic", "Chinese")

            Column(modifier = Modifier.weight(1f)) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = WarmBlack,
                    contentColor = WarmTeal,
                    divider = {},
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (pagerState.currentPage == index) WarmTeal else WarmInkDim,
                                )
                            },
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        when (page) {
                            0 -> OverviewTab(
                                result = result,
                                generation = generation,
                                birthMoonPhase = birthMoonPhase,
                                currentMoonPhase = currentMoonPhase,
                                planetAges = planetAges,
                                lifeStats = lifeStats,
                                viewModel = viewModel,
                                onShareMilestone = onShareMilestone,
                                hasLocation = uiState.location != null,
                            )
                            1 -> WesternTab(result = result)
                            2 -> VedicTab(result = result, hasLocation = uiState.location != null)
                            3 -> ChineseTab(result = result)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(
    result: AgeResult,
    generation: com.willowvibe.agereveal.domain.Generation?,
    birthMoonPhase: com.willowvibe.agereveal.domain.MoonPhase?,
    currentMoonPhase: com.willowvibe.agereveal.domain.MoonPhase,
    planetAges: List<com.willowvibe.agereveal.domain.PlanetAge>,
    lifeStats: List<LifeStatsCalculator.LifeStat>,
    viewModel: CalculatorViewModel,
    onShareMilestone: (Milestone) -> Unit,
    hasLocation: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AstroTile(
            result = result,
            isUnlocked = true,
            hasLocation = hasLocation,
            showDeepDetails = false,
        )

        if (generation != null) {
            GenerationBadgeChip(
                generation = generation,
                totalSeconds = result.totalSeconds,
            )
        }

        if (result.globalPercentile.isNotEmpty()) {
            PercentileCard(
                percentileText = result.globalPercentile,
                sharedEstimate = result.sharedBirthDateEstimate,
                onShare = { viewModel.sharePercentileCard() },
            )
        }

        if (birthMoonPhase != null) {
            MoonPhaseCard(
                birthPhase = birthMoonPhase,
                currentPhase = currentMoonPhase,
            )
        }

        if (planetAges.isNotEmpty()) {
            PlanetAgesRow(planetAges = planetAges)
        }

        if (result.planetPositions.isNotEmpty()) {
            PlanetPositionTable(result.planetPositions)
        }

        if (result.milestones.isNotEmpty()) {
            MilestoneTimeline(
                milestones = result.milestones,
                totalDays = result.totalDays,
                isUnlocked = true,
                onShare = onShareMilestone,
                onToggleNotification = { target, enabled ->
                    viewModel.setMilestoneEnabled(target, enabled)
                },
            )
        }

        if (result.estimatedHeartbeats > 0) {
            HeartbeatRow(result.estimatedHeartbeats)
        }

        if (lifeStats.isNotEmpty()) {
            LifeStatsSection(
                stats = lifeStats,
                onShare = { stat ->
                    viewModel.shareLifeStatCard(stat.label, stat.value, stat.emoji)
                },
            )
        }
    }
}

@Composable
private fun WesternTab(result: AgeResult) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (result.westernZodiac.isNotEmpty()) {
            AgeCard {
                AgeLabel(text = "WESTERN ZODIAC")
                Spacer(Modifier.height(6.dp))
                Text(
                    result.westernZodiac,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp,
                    color = WarmInk,
                )
            }
        }

        if (result.westernMoonSign.isNotEmpty()) {
            AgeCard {
                AgeLabel(text = "WESTERN MOON SIGN")
                Spacer(Modifier.height(6.dp))
                AgeValue(text = result.westernMoonSign)
            }
        }
    }
}

@Composable
private fun VedicTab(result: AgeResult, hasLocation: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (result.planetDignities.isNotEmpty()) {
            PlanetDignityCard(dignities = result.planetDignities)
        }
        if (result.rashi.isNotEmpty()) {
            AgeCard {
                AgeLabel(text = "RASHI (SIDEREAL SUN SIGN)")
                Spacer(Modifier.height(6.dp))
                Text(
                    result.rashi,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp,
                    color = WarmInk,
                )
                if (result.rashiLord.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    AgeBody(text = "Lord: ${result.rashiLord}")
                }
            }
        }

        if (result.nakshatra.isNotEmpty()) {
            AgeCard {
                AgeLabel(text = "NAKSHATRA")
                Spacer(Modifier.height(6.dp))
                AgeValue(text = result.nakshatra)
                if (result.nakshatraPada.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    AgeBody(text = "Pada: ${result.nakshatraPada}")
                }
                if (result.tithi.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    AgeBody(text = "Tithi: ${result.tithi}")
                }
            }
        }

        if (result.approximateAscendant.isNotEmpty()) {
            AgeCard {
                val label = if (hasLocation) "LAGNA (ASCENDANT)" else "LAGNA (APPROXIMATE)"
                AgeLabel(text = label)
                Spacer(Modifier.height(6.dp))
                AgeValue(text = result.approximateAscendant)
            }
        }

        if (result.dashaInfo.isNotEmpty()) {
            AgeCard {
                DashaRow(result.dashaInfo)
            }
        }
    }
}

@Composable
private fun ChineseTab(result: AgeResult) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (result.chineseZodiac.isNotEmpty() || result.chineseStemBranch.isNotEmpty()) {
            AgeCard {
                AgeLabel(text = "CHINESE ZODIAC")
                Spacer(Modifier.height(6.dp))
                val hero = result.chineseStemBranch.ifEmpty { result.chineseZodiac }
                Text(
                    hero,
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp,
                    color = WarmInk,
                )
                if (result.chineseZodiac.isNotEmpty() && result.chineseStemBranch.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    AgeBody(text = "Animal: ${result.chineseZodiac}")
                }
            }
        }

        if (result.baZiInfo.isNotEmpty()) {
            AgeCard {
                BaZiRow(result.baZiInfo)
            }
        }

        if (result.lunarBirthday.isNotEmpty()) {
            AgeCard {
                AgeLabel(text = "LUNAR BIRTHDAY")
                Spacer(Modifier.height(6.dp))
                AgeValue(text = result.lunarBirthday)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Astro tile
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun AstroTile(
    result: AgeResult,
    isUnlocked: Boolean,
    hasLocation: Boolean = false,
    showDeepDetails: Boolean = true,
) {
    AgeCard {
        // Radial glow in the top-right corner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(140.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(WarmTeal.copy(alpha = 0.20f), WarmTeal.copy(alpha = 0f)),
                    ),
                ),
        )

        Column {
            AgeLabel(text = "WESTERN · VEDIC · CHINESE", modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(6.dp))
            if (isUnlocked && result.westernZodiac.isNotEmpty()) {
                // ── Hero: primary signs ────────────────────────────────────
                Text(
                    "${result.westernZodiac} · ${result.rashi}",
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp,
                    color = WarmInk,
                )

                // Single approximate badge instead of repeating everywhere
                if (result.birthTime == null) {
                    Spacer(Modifier.height(6.dp))
                    ApproximateBadge()
                }

                // ── Compact astrology grid ─────────────────────────────────
                Spacer(Modifier.height(10.dp))
                val items = buildList {
                    if (result.westernMoonSign.isNotEmpty()) add("Moon" to result.westernMoonSign)
                    if (result.rashiLord.isNotEmpty()) add("Lord" to result.rashiLord)
                    if (result.chineseStemBranch.isNotEmpty()) add("Chinese" to result.chineseStemBranch.split(" / ").last())
                    else if (result.chineseZodiac.isNotEmpty()) add("Chinese" to result.chineseZodiac)
                    if (result.approximateAscendant.isNotEmpty()) {
                        val lagnaLabel = if (hasLocation) "Lagna" else "Lagna (approx)"
                        add(lagnaLabel to result.approximateAscendant)
                    }
                    if (result.tithi.isNotEmpty()) add("Tithi" to result.tithi)
                    if (result.nakshatra.isNotEmpty()) add("Nakshatra" to result.nakshatra)
                    if (result.nakshatraPada.isNotEmpty()) add("Pada" to result.nakshatraPada)
                    if (result.lunarBirthday.isNotEmpty()) add("Lunar" to result.lunarBirthday)
                }
                AstroGrid(items)

                if (showDeepDetails) {
                    // ── Dasha ──────────────────────────────────────────────────
                    if (result.dashaInfo.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        DashaRow(result.dashaInfo)
                    }

                    // ── Ba Zi ──────────────────────────────────────────────────
                    if (result.baZiInfo.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        BaZiRow(result.baZiInfo)
                    }

                    // ── Planet positions ───────────────────────────────────────
                    if (result.planetPositions.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        PlanetPositionTable(result.planetPositions)
                    }
                }
            } else {
                // Placeholder when not yet unlocked — blurred teaser
                Column(
                    modifier = Modifier
                        .blur(2.dp)
                        .alpha(0.7f),
                ) {
                    Text(
                        "Pisces · Meena",
                        fontFamily = SerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        letterSpacing = (-0.5).sp,
                        color = WarmInkDim,
                        fontStyle = FontStyle.Italic,
                    )
                    Text(
                        "Watch an ad to reveal your signs",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun ApproximateBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(WarmAmber.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            "Approximate — add birth time for exact results",
            style = MaterialTheme.typography.labelSmall,
            color = WarmAmber,
        )
    }
}

/**
 * Compact 2-column grid for astrology label-value pairs.
 * Falls back to a single column if there is only 1 item.
 */
@Composable
private fun AstroGrid(items: List<Pair<String, String>>) {
    if (items.isEmpty()) return
    if (items.size == 1) {
        AstroGridItem(label = items[0].first, value = items[0].second)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AstroGridItem(
                    label = pair[0].first,
                    value = pair[0].second,
                    modifier = Modifier.weight(1f),
                )
                if (pair.size > 1) {
                    AstroGridItem(
                        label = pair[1].first,
                        value = pair[1].second,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AstroGridItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            stateDescription = "$label: $value"
        },
    ) {
        AgeLabel(text = label)
        Spacer(Modifier.height(2.dp))
        AgeBody(
            text = value,
            color = WarmInk,
        )
    }
}

@Composable
private fun DashaRow(info: String) {
    Column {
        AgeLabel(text = "DASHA")
        Spacer(Modifier.height(2.dp))
        AgeValue(
            text = info,
            accentColor = WarmAmber,
        )
    }
}

@Composable
private fun BaZiRow(info: String) {
    Column {
        AgeLabel(text = "BA ZI (FOUR PILLARS)")
        Spacer(Modifier.height(2.dp))
        AgeBody(
            text = info,
            color = WarmInk,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Planet positions table
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlanetDignityCard(dignities: List<com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.PlanetaryDignity>) {
    val colorForDignity: (com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.Dignity) -> androidx.compose.ui.graphics.Color = {
        when (it) {
            com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.Dignity.EXALTED -> WarmTeal
            com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.Dignity.MOOLATRIKONA -> WarmTeal.copy(alpha = 0.75f)
            com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.Dignity.OWN_HOUSE -> WarmInk
            com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.Dignity.NEUTRAL -> WarmInkDim
            com.willowvibe.agereveal.domain.PlanetaryDignityCalculator.Dignity.DEBILITATED -> androidx.compose.ui.graphics.Color(0xFFE57373)
        }
    }
    AgeCard {
        AgeLabel(text = "PLANETARY DIGNITIES")
        Spacer(Modifier.height(4.dp))
        dignities.forEach { dignity ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgeBody(text = dignity.planetName)
                Text(
                    text = "${dignity.dignity.label}${dignity.proximityHint}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = colorForDignity(dignity.dignity),
                )
            }
            if (dignity !== dignities.last()) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun PlanetPositionTable(positions: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AgeLabel(text = "PLANETARY POSITIONS")
        Spacer(Modifier.height(4.dp))
        positions.forEach { (planet, sign) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgeBody(text = planet)
                AgeBody(
                    text = sign,
                    color = WarmInk,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Milestone timeline
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MilestoneTimeline(
    milestones: List<Milestone>,
    totalDays: Long,
    isUnlocked: Boolean,
    onShare: (Milestone) -> Unit,
    onToggleNotification: (Int, Boolean) -> Unit = { _, _ -> },
) {
    AgeCard {
        AgeLabel(text = "LIFE TIMELINE")
        Spacer(Modifier.height(10.dp))

        // Life progress bar
        LifeProgressBar(totalDays = totalDays)

        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(WarmSurfaceSoft),
        )
        Spacer(Modifier.height(10.dp))

        milestones.forEachIndexed { index, milestone ->
            MilestoneRow(
                milestone = milestone,
                isUnlocked = isUnlocked,
                onShare = { onShare(milestone) },
                onToggleNotification = onToggleNotification,
            )
            if (index < milestones.size - 1) {
                // Connecting vertical line between dots (aligned with the dot at start = 12.dp padding + 4dp offset)
                Box(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .size(width = 1.dp, height = 10.dp)
                        .background(WarmSurfaceSoft),
                )
            }
        }
    }
}

@Composable
internal fun LifeProgressBar(totalDays: Long) {
    val lifeExpectancyDays = 29_200L // ~80 years
    val progress = (totalDays.toFloat() / lifeExpectancyDays).coerceIn(0f, 1f)
    val years = totalDays / 365

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AgeLabel(text = "LIFE LIVED")
            AgeLabel(text = "${(progress * 100).toInt()}% · ~$years yrs of 80")
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(WarmSurfaceSoft),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(WarmTeal),
            )
        }
    }
}

@Composable
internal fun MilestoneRow(
    milestone: Milestone,
    isUnlocked: Boolean,
    onShare: () -> Unit,
    onToggleNotification: (Int, Boolean) -> Unit = { _, _ -> },
) {
    val isToday = milestone.daysAway == 0L
    val dotColor = when {
        isToday -> WarmAmber
        milestone.isPast -> WarmTeal
        else -> WarmSurfaceSoft
    }
    val statusLabel = when {
        isToday -> "TODAY ✦"
        milestone.isPast -> "✓"
        else -> "IN ${milestone.daysAway}D"
    }
    val statusColor = when {
        isToday -> WarmAmber
        milestone.isPast -> WarmTeal
        else -> WarmInkDim
    }
    // Per-milestone notification toggle — collect from DataStore via Calculator VM prefs.
    // Because this composable is stateless w.r.t. DataStore, we store the toggle state
    // locally and call back up to the ViewModel on change.
    var notifyEnabled by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            AgeBody(
                text = "%,dth day".format(milestone.targetDays),
                color = if (milestone.isPast || isToday) WarmInk else WarmInkMute,
            )
            Text(
                milestone.date.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                color = WarmInkDim,
            )
        }
        AgeLabel(
            text = statusLabel,
            accentColor = statusColor,
        )
        if (isUnlocked && milestone.isPast) {
            IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share milestone",
                    tint = WarmTeal,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        if (isUnlocked && !milestone.isPast && !isToday) {
            IconButton(
                onClick = {
                    notifyEnabled = !notifyEnabled
                    onToggleNotification(milestone.targetDays, notifyEnabled)
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = if (notifyEnabled) Icons.Default.NotificationsActive
                    else Icons.Default.NotificationsOff,
                    contentDescription = "Toggle milestone notification",
                    tint = if (notifyEnabled) WarmTeal else WarmInkDim,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Generation badge chip (Gen Z flex)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun GenerationBadgeChip(generation: com.willowvibe.agereveal.domain.Generation, totalSeconds: Long) {
    val secLabel = remember(totalSeconds) {
        when {
            totalSeconds >= 1_000_000_000 -> "%.1fB sec".format(totalSeconds / 1_000_000_000.0)
            totalSeconds >= 1_000_000 -> "%.1fM sec".format(totalSeconds / 1_000_000.0)
            else -> "%,d sec".format(totalSeconds)
        }
    }
    AgeCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                generation.emoji,
                fontSize = 24.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgeBody(
                    text = "Certified ${generation.shortName}",
                    color = WarmInk,
                )
                AgeLabel(text = "$secLabel · ${generation.startYear}–${generation.endYear}")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Moon phase card (birth + current)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun MoonPhaseCard(
    birthPhase: com.willowvibe.agereveal.domain.MoonPhase,
    currentPhase: com.willowvibe.agereveal.domain.MoonPhase,
) {
    AgeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Birth moon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AgeLabel(text = "BIRTH MOON")
                Spacer(Modifier.height(8.dp))
                MoonPhaseVisual(
                    illuminationFraction = birthPhase.illuminationFraction.toFloat(),
                    waxing = birthPhase.waxing,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.height(6.dp))
                AgeBody(
                    text = birthPhase.name,
                    color = WarmInk,
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .width(1.dp)
                    .background(WarmSurfaceSoft),
            )

            // Current moon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AgeLabel(text = "MOON TONIGHT")
                Spacer(Modifier.height(8.dp))
                MoonPhaseVisual(
                    illuminationFraction = currentPhase.illuminationFraction.toFloat(),
                    waxing = currentPhase.waxing,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.height(6.dp))
                AgeBody(
                    text = currentPhase.name,
                    color = WarmInk,
                )
            }
        }
    }
}

@Composable
private fun MoonPhaseVisual(
    illuminationFraction: Float,
    waxing: Boolean,
    modifier: Modifier = Modifier,
) {
    val lightColor = WarmAmber
    val darkColor = WarmInkMute.copy(alpha = 0.18f)
    Box(
        modifier = modifier
            .drawWithContent {
                val radius = size.minDimension / 2
                val centerX = size.width / 2
                val centerY = size.height / 2

                // Dark background circle
                drawCircle(color = darkColor, radius = radius, center = Offset(centerX, centerY))

                when {
                    illuminationFraction >= 0.99f -> {
                        drawCircle(color = lightColor, radius = radius, center = Offset(centerX, centerY))
                    }
                    illuminationFraction <= 0.01f -> { /* new moon — already dark */ }
                    else -> {
                        val lit = illuminationFraction
                        val term = kotlin.math.abs(1f - 2f * lit) * radius
                        val outer = androidx.compose.ui.geometry.Rect(
                            centerX - radius, centerY - radius,
                            centerX + radius, centerY + radius,
                        )
                        if (lit <= 0.5f) {
                            // Crescent: small sliver of light
                            val termOval = androidx.compose.ui.geometry.Rect(
                                centerX - term, centerY - radius,
                                centerX + term, centerY + radius,
                            )
                            if (waxing) {
                                val path = Path().apply {
                                    arcTo(outer, -90f, 180f, false)
                                    arcTo(termOval, 90f, -180f, false)
                                    close()
                                }
                                drawPath(path, lightColor)
                            } else {
                                val path = Path().apply {
                                    arcTo(outer, 90f, 180f, false)
                                    arcTo(termOval, -90f, -180f, false)
                                    close()
                                }
                                drawPath(path, lightColor)
                            }
                        } else {
                            // Gibbous: mostly lit, small dark sliver
                            drawCircle(color = lightColor, radius = radius, center = Offset(centerX, centerY))
                            val termOval = androidx.compose.ui.geometry.Rect(
                                centerX - term, centerY - radius,
                                centerX + term, centerY + radius,
                            )
                            if (waxing) {
                                val path = Path().apply {
                                    arcTo(outer, 90f, 180f, false)
                                    arcTo(termOval, -90f, -180f, false)
                                    close()
                                }
                                drawPath(path, darkColor)
                            } else {
                                val path = Path().apply {
                                    arcTo(outer, -90f, 180f, false)
                                    arcTo(termOval, 90f, -180f, false)
                                    close()
                                }
                                drawPath(path, darkColor)
                            }
                        }
                    }
                }
            },
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Planet ages horizontal row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun PlanetAgesRow(planetAges: List<com.willowvibe.agereveal.domain.PlanetAge>) {
    val calc = remember { PlanetAgeCalculator() }
    AgeCard {
        AgeLabel(text = "PLANET AGES")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            planetAges.forEach { planetAge ->
                val formatted = calc.formatPlanetAge(planetAge.ageYears)
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarmSurfaceSoft)
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        planetAge.planet.emoji,
                        fontSize = 20.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    AgeBody(
                        text = formatted,
                        color = WarmInk,
                    )
                    AgeLabel(text = planetAge.planet.displayName)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Heartbeat counter
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun HeartbeatRow(heartbeats: Long) {
    AgeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("♥", color = WarmAmber, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            AgeBody(
                text = "${formatHeartbeatsLong(heartbeats)} heartbeats and counting",
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Life stats dashboard
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun LifeStatsSection(
    stats: List<LifeStatsCalculator.LifeStat>,
    onShare: (LifeStatsCalculator.LifeStat) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    AgeCard {
        AgeLabel(text = "LIFE STATS")
        Spacer(Modifier.height(12.dp))
        stats.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                pair.forEach { stat ->
                    LifeStatCard(
                        stat = stat,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onShare(stat)
                        },
                    )
                }
                if (pair.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LifeStatCard(
    stat: LifeStatsCalculator.LifeStat,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurfaceSoft)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stat.emoji, fontSize = 24.sp)
        Spacer(Modifier.height(4.dp))
        AgeValue(text = stat.value)
        AgeLabel(text = stat.label)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Parallel Universe Birth card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ParallelUniverseCard(
    universes: List<com.willowvibe.agereveal.domain.ParallelUniverseGenerator.UniverseContext>,
    onShare: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    AgeCard {
        AgeLabel(text = "PARALLEL UNIVERSE BIRTH")
        Spacer(Modifier.height(12.dp))
        universes.forEachIndexed { index, universe ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(universe.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    AgeBody(
                        text = universe.era,
                        color = WarmInk,
                    )
                    AgeLabel(text = universe.ageText)
                }
            }
            if (index < universes.size - 1) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(WarmSurfaceSoft),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WarmTeal.copy(alpha = 0.10f))
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShare()
                }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = "Share parallel universe",
                tint = WarmTeal,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Share",
                style = MaterialTheme.typography.labelMedium,
                color = WarmTeal,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Global age percentile card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun PercentileCard(
    percentileText: String,
    sharedEstimate: String,
    onShare: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val bigNumber = percentileText.substringAfter("Older than ").substringBefore("%")
    AgeCard {
        AgeLabel(text = "GLOBAL PERCENTILE")
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "🌍",
                fontSize = 28.sp,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgeValue(text = "$bigNumber%")
                AgeBody(
                    text = "of humans alive today are younger than you",
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        AgeLabel(text = sharedEstimate)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WarmTeal.copy(alpha = 0.10f))
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShare()
                }
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = "Share percentile",
                tint = WarmTeal,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Share stat",
                style = MaterialTheme.typography.labelMedium,
                color = WarmTeal,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun formatHeartbeatsLong(n: Long): String = when {
    n >= 1_000_000_000 -> "%,.2f B".format(n / 1_000_000_000.0)
    n >= 1_000_000 -> "%,.1f M".format(n / 1_000_000.0)
    else -> "%,d".format(n)
}

internal fun moonPhaseHint(month: Int): String = when (month) {
    1, 2 -> "waxing crescent moon"
    3, 4 -> "full moon season"
    5, 6 -> "waning gibbous moon"
    7, 8 -> "new moon season"
    9, 10 -> "waxing gibbous moon"
    else -> "waning crescent moon"
}
