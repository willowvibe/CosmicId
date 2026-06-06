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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.R
import com.willowvibe.agereveal.domain.SajuKoreanCalculator
import com.willowvibe.agereveal.ui.theme.KoreanFamily
import kotlinx.coroutines.launch
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.domain.Aspect
import com.willowvibe.agereveal.domain.AspectType
import com.willowvibe.agereveal.domain.AstronomicalCalculator
import com.willowvibe.agereveal.domain.DashaInfo
import com.willowvibe.agereveal.domain.DashaPeriod
import com.willowvibe.agereveal.domain.GenerationCalculator
import com.willowvibe.agereveal.domain.LifeStatsCalculator
import com.willowvibe.agereveal.domain.MoonPhaseCalculator
import com.willowvibe.agereveal.domain.NakshatraData
import com.willowvibe.agereveal.domain.NavamsaChart
import com.willowvibe.agereveal.domain.PlanetAgeCalculator
import com.willowvibe.agereveal.domain.model.CelestialBody
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
                    color = WarmInkMute,
                )
            }
        } else {
            val pagerState = rememberPagerState(pageCount = { 4 })
            val scope = rememberCoroutineScope()
            val tabTitles = listOf("Overview", "Western", "Vedic", "Korean Saju")

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
                                    color = if (pagerState.currentPage == index) WarmTeal else WarmInkMute,
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
                            2 -> VedicTab(
                                result = result,
                                hasLocation = uiState.location != null,
                                hasBirthTime = uiState.birthTime != null,
                            )
                            3 -> KoreanSajuTab(viewModel = viewModel, hasBirthTime = uiState.birthTime != null)
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
private fun VedicTab(result: AgeResult, hasLocation: Boolean, hasBirthTime: Boolean) {
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

        if (result.nakshatraMetadata != null) {
            AgeResultNakshatraCard(
                metadata = result.nakshatraMetadata,
                name = result.nakshatra,
                padaName = result.nakshatraPada,
                tithi = result.tithi,
                isApprox = !hasBirthTime,
            )
        }

        if (result.approximateAscendant.isNotEmpty()) {
            AgeCard {
                val label = if (hasLocation) "LAGNA (ASCENDANT)" else "LAGNA (APPROXIMATE)"
                AgeLabel(text = label)
                Spacer(Modifier.height(6.dp))
                if (hasLocation && !result.tropicalAscendant.isNullOrEmpty()) {
                    // Two-row layout: tropical (Western) + sidereal (Vedic)
                    AgeBody(text = "Tropical: ${result.tropicalAscendant}")
                    Spacer(Modifier.height(4.dp))
                    AgeValue(text = "Sidereal: ${result.approximateAscendant}")
                } else {
                    // Approximate-only (no location)
                    AgeValue(text = result.approximateAscendant)
                }
            }
        }

        if (result.dashaDetail != null) {
            AgeResultDashaTreeCard(detail = result.dashaDetail)
        }

        if (result.navamsaChart != null) {
            NavamsaSnapshotCard(chart = result.navamsaChart)
        }

        if (result.planetaryAspects.isNotEmpty()) {
            PlanetaryAspectsCard(aspects = result.planetaryAspects)
        }
    }
}

@Composable
private fun ChineseTab(result: AgeResult) {
    // Deprecated in v2.1 — Korean Saju is now the East-Asian pillar. Kept as
    // a thin fallback for callers that may still pass `result`; renders the
    // legacy Chinese ba-zi summary so nothing in the world breaks.
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

@Composable
private fun KoreanSajuTab(
    viewModel: CalculatorViewModel,
    hasBirthTime: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    val chart = uiState.sajuChart
    val yongshin = uiState.sajuYongshin
    val unlocked = uiState.isKoreanSajuUnlocked

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Header (always visible)
        AgeCard {
            AgeLabel(
                text = stringResource(R.string.saju_tab_title).uppercase(),
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            AgeBody(
                text = stringResource(R.string.saju_tab_subtitle),
                color = WarmInkMute,
            )
        }

        if (chart == null) {
            // Chart hasn't computed yet (e.g. user hasn't entered a birth date)
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmSurfaceSoft)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.saju_loading),
                    fontFamily = KoreanFamily,
                    color = WarmInkMute,
                )
            }
            return
        }

        // 1) Day Master hero (always visible — basic info, not premium-gated)
        SajuDayMasterCard(chart = chart)

        // 2) Four Pillars card (always visible)
        SajuFourPillarsCard(chart = chart, hasBirthTime = hasBirthTime)

        if (!unlocked) {
            // Premium-gate: 오행 / 용신 / 대운 are behind the korean_saju_unlock IAP
            SajuUnlockTeaserCard()
            return
        }

        // 3) 오행 (Five Element) balance — premium content
        SajuOHaengBalanceCard(chart = chart)

        // 4) 용신 (Yongshin) suggestion — premium content
        if (yongshin != null) {
            SajuYongshinCard(yongshin = yongshin)
        }

        // 5) 대운 (Daeun) 10-year luck cycle — premium content, requires birth time
        if (hasBirthTime && chart.daeun.isNotEmpty()) {
            SajuDaeunTimelineCard(chart = chart)
        } else if (!hasBirthTime) {
            SajuDaeunRequiresTimeCard()
        }

        // 6) Disclaimer
        Text(
            stringResource(R.string.saju_disclaimer),
            style = MaterialTheme.typography.bodySmall,
            color = WarmInkMute,
            textAlign = TextAlign.Start,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Korean Saju tab — sub-composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SajuDayMasterCard(chart: SajuKoreanCalculator.SajuChart) {
    val elementEn = chart.dayMasterElement
    val swatch = elementSwatch(elementEn)
    val elementHangul = SajuKoreanCalculator.ELEMENT_HANGUL[elementEn] ?: elementEn
    val elementHanja = SajuKoreanCalculator.ELEMENT_HANJA[elementEn] ?: elementEn
    val stage = chart.dayMasterTwelveStage.takeIf { it.isNotEmpty() }

    AgeCard {
        AgeLabel(text = stringResource(R.string.saju_day_master))
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(swatch.copy(alpha = 0.20f))
                    .border(1.dp, swatch, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    chart.dayMaster.hangul,
                    fontFamily = KoreanFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = swatch,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "${chart.dayMaster.hangul}(${chart.dayMaster.hanja}) ${elementHangul}(${elementHanja})",
                    fontFamily = KoreanFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = WarmInk,
                )
                Text(
                    "${elementHangul} 일간 · ${elementEn}",
                    fontFamily = KoreanFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInkMute,
                )
            }
        }
        if (stage != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(WarmSurfaceSoft),
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgeLabel(text = "12운성 (月令)")
                Text(
                    stage,
                    fontFamily = KoreanFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmTeal,
                )
            }
        }
    }
}

@Composable
private fun SajuFourPillarsCard(
    chart: SajuKoreanCalculator.SajuChart,
    hasBirthTime: Boolean,
) {
    AgeCard {
        AgeLabel(
            text = stringResource(R.string.saju_year_pillar) + " · " +
                stringResource(R.string.saju_month_pillar) + " · " +
                stringResource(R.string.saju_day_pillar) + (if (hasBirthTime) " · " +
                stringResource(R.string.saju_hour_pillar) else ""),
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SajuPillarColumn(
                label = stringResource(R.string.saju_year_pillar),
                pillar = chart.year,
            )
            SajuPillarColumn(
                label = stringResource(R.string.saju_month_pillar),
                pillar = chart.month,
            )
            SajuPillarColumn(
                label = stringResource(R.string.saju_day_pillar),
                pillar = chart.day,
                highlight = true,
            )
            if (hasBirthTime && chart.hour != null) {
                SajuPillarColumn(
                    label = stringResource(R.string.saju_hour_pillar),
                    pillar = chart.hour,
                )
            }
        }
    }
}

@Composable
private fun SajuPillarColumn(
    label: String,
    pillar: SajuKoreanCalculator.KoreanPillar,
    highlight: Boolean = false,
) {
    val swatch = elementSwatch(pillar.element)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlight) swatch.copy(alpha = 0.10f) else WarmSurfaceSoft)
            .border(
                width = if (highlight) 1.dp else 0.dp,
                color = if (highlight) swatch.copy(alpha = 0.5f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            pillar.stem.hangul,
            fontFamily = KoreanFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = swatch,
        )
        Text(
            pillar.stem.hanja,
            fontFamily = SerifFamily,
            fontSize = 11.sp,
            color = WarmInkMute,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            pillar.branch.hangul,
            fontFamily = KoreanFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = WarmInk,
        )
        Text(
            pillar.branch.hanja,
            fontFamily = SerifFamily,
            fontSize = 11.sp,
            color = WarmInkMute,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            label.substringBefore(" "),
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
    }
}

@Composable
private fun SajuOHaengBalanceCard(chart: SajuKoreanCalculator.SajuChart) {
    val balance = chart.oHaengBalance
    val maxCount = (balance.total.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val elements = listOf("Wood", "Fire", "Earth", "Metal", "Water")
    AgeCard {
        AgeLabel(text = stringResource(R.string.saju_o_haeng_title))
        Spacer(Modifier.height(4.dp))
        AgeBody(
            text = stringResource(R.string.saju_o_haeng_subtitle),
            color = WarmInkMute,
        )
        Spacer(Modifier.height(12.dp))
        elements.forEach { element ->
            val count = balance.total[element] ?: 0
            val swatch = elementSwatch(element)
            val fraction = count.toFloat() / maxCount.toFloat()
            val hangul = SajuKoreanCalculator.ELEMENT_HANGUL[element] ?: element
            val hanja = SajuKoreanCalculator.ELEMENT_HANJA[element] ?: element
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "$hangul($hanja) — $element",
                        fontFamily = KoreanFamily,
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmInk,
                    )
                    Text(
                        "$count",
                        fontFamily = KoreanFamily,
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmInkMute,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(WarmSurfaceSoft),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(swatch),
                    )
                }
            }
        }

        balance.dominant?.let { dom ->
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(WarmSurfaceSoft),
            )
            Spacer(Modifier.height(8.dp))
            val domHangul = SajuKoreanCalculator.ELEMENT_HANGUL[dom] ?: dom
            AgeBody(
                text = stringResource(R.string.saju_o_haeng_dominant_format, domHangul, balance.total[dom] ?: 0),
                color = WarmInk,
            )
        }
    }
}

@Composable
private fun SajuYongshinCard(yongshin: SajuKoreanCalculator.YongshinCard) {
    val swatch = elementSwatch(yongshin.favourableElementEn)
    AgeCard {
        AgeLabel(text = stringResource(R.string.saju_yongshin_title))
        Spacer(Modifier.height(4.dp))
        AgeBody(
            text = stringResource(R.string.saju_yongshin_subtitle),
            color = WarmInkMute,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            yongshin.shortSummary,
            fontFamily = KoreanFamily,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInk,
        )
        Spacer(Modifier.height(12.dp))

        // Favourable element hero swatch
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(swatch.copy(alpha = 0.25f))
                    .border(1.dp, swatch, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    yongshin.favourableElementHangul,
                    fontFamily = KoreanFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = swatch,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(
                        R.string.saju_yongshin_favourable_format,
                        "${yongshin.favourableElementHangul}(${yongshin.favourableElementHanja})",
                    ),
                    fontFamily = KoreanFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInk,
                )
                Text(
                    stringResource(
                        R.string.saju_yongshin_unfavourable_format,
                        SajuKoreanCalculator.ELEMENT_HANGUL[yongshin.unfavourableElementEn] ?: yongshin.unfavourableElementEn,
                    ),
                    fontFamily = KoreanFamily,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInkMute,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(WarmSurfaceSoft),
        )
        Spacer(Modifier.height(8.dp))

        // 4 sub-rows: colour, direction, season, status
        YongshinSubRow(
            label = stringResource(R.string.saju_yongshin_color_format, "").trimEnd(':'),
            value = yongshin.favourableColorName,
            swatch = swatch,
        )
        YongshinSubRow(
            label = stringResource(R.string.saju_yongshin_direction_format, "").trimEnd(':'),
            value = yongshin.favourableDirection,
        )
        YongshinSubRow(
            label = stringResource(R.string.saju_yongshin_season_format, "").trimEnd(':'),
            value = yongshin.favourableSeason,
        )
        YongshinSubRow(
            label = stringResource(R.string.saju_yongshin_reasoning_label),
            value = yongshin.status,
        )
    }
}

@Composable
private fun YongshinSubRow(
    label: String,
    value: String,
    swatch: Color? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AgeBody(text = label, color = WarmInkMute)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (swatch != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(swatch),
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                value,
                fontFamily = KoreanFamily,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInk,
            )
        }
    }
}

@Composable
private fun SajuDaeunTimelineCard(chart: SajuKoreanCalculator.SajuChart) {
    val periods = chart.daeun
    AgeCard {
        AgeLabel(text = stringResource(R.string.saju_daeun_title))
        Spacer(Modifier.height(4.dp))
        AgeBody(
            text = stringResource(R.string.saju_daeun_subtitle),
            color = WarmInkMute,
        )
        Spacer(Modifier.height(10.dp))
        // Horizontally scrollable pill list — Daeun can have 7-9 periods
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            periods.forEach { period ->
                val swatch = elementSwatch(period.pillar.element)
                Column(
                    modifier = Modifier
                        .width(86.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(WarmSurfaceSoft)
                        .border(1.dp, swatch.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "${period.startAge}–${period.endAge}세",
                        fontFamily = KoreanFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmInkMute,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        period.pillar.stem.hangul + period.pillar.branch.hangul,
                        fontFamily = KoreanFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = swatch,
                    )
                    Text(
                        period.pillar.displayHanja,
                        fontFamily = SerifFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmInkDim,
                    )
                }
            }
        }
    }
}

@Composable
private fun SajuDaeunRequiresTimeCard() {
    AgeCard {
        AgeLabel(text = stringResource(R.string.saju_daeun_title))
        Spacer(Modifier.height(8.dp))
        AgeBody(
            text = stringResource(R.string.saju_daeun_requires_birth_time),
            color = WarmInkMute,
        )
    }
}

@Composable
private fun SajuUnlockTeaserCard() {
    AgeCard {
        AgeLabel(text = stringResource(R.string.saju_unlock_title))
        Spacer(Modifier.height(4.dp))
        AgeBody(
            text = stringResource(R.string.saju_unlock_subtitle),
            color = WarmInkMute,
        )
        Spacer(Modifier.height(12.dp))
        // Show a blurred preview of the 오행 chart to make the teaser feel tangible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(WarmSurfaceSoft)
                .blur(4.dp)
                .alpha(0.6f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf("목", "화", "토", "금", "수").forEach {
                    Text(
                        it,
                        fontFamily = KoreanFamily,
                        fontSize = 22.sp,
                        color = WarmInkMute,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WarmTeal.copy(alpha = 0.12f))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.saju_unlock_cta),
                fontFamily = KoreanFamily,
                fontWeight = FontWeight.SemiBold,
                color = WarmTeal,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Element swatch — used by the pillar columns, 오행 bars, 용신 hero
// ─────────────────────────────────────────────────────────────────────────────

private fun elementSwatch(elementEn: String): Color = when (elementEn) {
    "Wood" -> Color(0xFF6FA86B)   // muted leaf green
    "Fire" -> Color(0xFFD9694E)   // warm coral
    "Earth" -> Color(0xFFB8924A)  // ochre
    "Metal" -> Color(0xFFB8B8B8)  // pearl grey
    "Water" -> Color(0xFF5586A8)  // ocean blue
    else -> WarmTeal
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
            AgeLabel(text = "WESTERN · VEDIC · KOREAN SAJU", modifier = Modifier.semantics { heading() })
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
                    if (result.chineseStemBranch.isNotEmpty()) add("Saju" to result.chineseStemBranch.split(" / ").last())
                    else if (result.chineseZodiac.isNotEmpty()) add("Saju" to result.chineseZodiac)
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
        else -> WarmInkMute
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

// ─────────────────────────────────────────────────────────────────────────────
// Vedic tab — Phase 6.5 enrichment sub-composables
// ─────────────────────────────────────────────────────────────────────────────

private enum class DashaSize { M, A, P }

@Composable
internal fun AgeResultNakshatraCard(
    metadata: NakshatraData,
    name: String,
    padaName: String,
    tithi: String,
    isApprox: Boolean,
) {
    AgeCard {
        AgeLabel(text = if (isApprox) "NAKSHATRA (APPROXIMATE)" else "NAKSHATRA")
        Spacer(Modifier.height(6.dp))
        AgeValue(text = if (name.isNotEmpty()) name else metadata.name)
        Spacer(Modifier.height(2.dp))
        AgeBody(text = "${metadata.symbolEmoji} ${metadata.symbol}", color = WarmInkDim)
        if (padaName.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            AgeBody(text = padaName)
        }
        if (tithi.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            AgeBody(text = "Tithi: $tithi")
        }
        Spacer(Modifier.height(8.dp))
        AgeBody(text = "Lord: ${metadata.lord.displayName}")
        AgeBody(text = "Deity: ${metadata.deity}")
        AgeBody(text = "Gana: ${metadata.ganaHangul.split(" ")[0]}")
    }
}

@Composable
internal fun AgeResultDashaTreeCard(detail: DashaInfo) {
    AgeCard {
        AgeLabel(text = "DASHA")
        Spacer(Modifier.height(8.dp))
        DashaTreeRow(period = detail.mahadasha, size = DashaSize.M)
        Spacer(Modifier.height(6.dp))
        DashaTreeRow(period = detail.antardasha, size = DashaSize.A)
        Spacer(Modifier.height(4.dp))
        DashaTreeRow(period = detail.pratyantar, size = DashaSize.P)
    }
}

@Composable
private fun DashaTreeRow(period: DashaPeriod, size: DashaSize) {
    val (fontFamily, fontSize, fontWeight) = when (size) {
        DashaSize.M -> Triple(SerifFamily, 22.sp, FontWeight.Bold)
        DashaSize.A -> Triple(SerifFamily, 18.sp, FontWeight.Normal)
        DashaSize.P -> Triple(FontFamily.Default, 14.sp, FontWeight.Normal)
    }
    val sizeLabel = when (size) {
        DashaSize.M -> "MAHADASHA"
        DashaSize.A -> "ANTARDASHA"
        DashaSize.P -> "PRATYANTAR"
    }
    Column {
        AgeBody(text = sizeLabel, color = WarmInkMute)
        Text(
            text = "${period.lord} · ${"%.1f".format(period.yearsRemaining)}y remaining",
            fontFamily = fontFamily,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = WarmInk,
        )
    }
}

@Composable
internal fun NavamsaSnapshotCard(chart: NavamsaChart) {
    AgeCard {
        AgeLabel(text = "NAVARMSA (D-9)")
        Spacer(Modifier.height(6.dp))

        // D-9 ascendant: we don't compute it explicitly in NavamsaChart, but
        // the Lagna ascendant maps to its own rashi — for now show the most
        // populated rashi as the "headline" D-9 rashi.
        val topOccupant = chart.rashiOccupancy.maxByOrNull { it.value.size }
        if (topOccupant != null) {
            val rashiIndex = topOccupant.key
            val bodies = topOccupant.value
            val rashiName = bodies.firstOrNull()?.let { _ -> "Rashi $rashiIndex" } ?: "—"
            Spacer(Modifier.height(4.dp))
            AgeBody(text = "Most populated rashi: $rashiName")
        }

        Spacer(Modifier.height(8.dp))
        AgeBody(text = "PLANETARY DISTRIBUTION", color = WarmInkMute)
        Spacer(Modifier.height(4.dp))

        val topRows = chart.rashiOccupancy.entries
            .sortedByDescending { it.value.size }
            .take(5)
        if (topRows.isEmpty()) {
            AgeBody(text = "—")
        } else {
            topRows.forEach { (rashiIndex, bodies) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Rashi $rashiIndex",
                        fontFamily = SerifFamily,
                        fontSize = 14.sp,
                        color = WarmInk,
                        modifier = Modifier.width(96.dp),
                    )
                    Text(
                        text = bodies.joinToString(", ") { it.displayName },
                        fontSize = 14.sp,
                        color = WarmInkDim,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PlanetaryAspectsCard(aspects: List<Aspect>) {
    val (harmonious, tense) = groupAspectsByTone(aspects)
    AgeCard {
        AgeLabel(text = "PLANETARY ASPECTS")
        Spacer(Modifier.height(8.dp))

        if (harmonious.isEmpty() && tense.isEmpty()) {
            AgeBody(text = "No major aspects in orb", color = WarmInkMute)
        } else {
            if (harmonious.isNotEmpty()) {
                AgeBody(text = "HARMONIOUS", color = WarmInkMute)
                Spacer(Modifier.height(4.dp))
                harmonious.take(5).forEach { aspect ->
                    AspectRow(aspect = aspect)
                }
                Spacer(Modifier.height(8.dp))
            }
            if (tense.isNotEmpty()) {
                AgeBody(text = "TENSE", color = WarmInkMute)
                Spacer(Modifier.height(4.dp))
                tense.take(5).forEach { aspect ->
                    AspectRow(aspect = aspect)
                }
            }
        }
    }
}

@Composable
private fun AspectRow(aspect: Aspect) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${aspect.planet1.displayName} ${aspect.type.symbol} ${aspect.planet2.displayName}",
            fontSize = 14.sp,
            color = WarmInk,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${"%.1f".format(aspect.orb)}° ${if (aspect.applying) "→" else "←"}",
            fontSize = 13.sp,
            color = WarmInkMute,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Vedic tab — display helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun groupAspectsByTone(aspects: List<Aspect>): Pair<List<Aspect>, List<Aspect>> {
    val harmonious = mutableListOf<Aspect>()
    val tense = mutableListOf<Aspect>()
    for (aspect in aspects) {
        when (aspect.type) {
            AspectType.TRINE, AspectType.SEXTILE -> harmonious.add(aspect)
            AspectType.SQUARE, AspectType.OPPOSITION -> tense.add(aspect)
            AspectType.CONJUNCTION -> {
                if (aspect.orb <= 4.0) harmonious.add(aspect) else tense.add(aspect)
            }
        }
    }
    harmonious.sortBy { it.orb }
    tense.sortBy { it.orb }
    return harmonious to tense
}
