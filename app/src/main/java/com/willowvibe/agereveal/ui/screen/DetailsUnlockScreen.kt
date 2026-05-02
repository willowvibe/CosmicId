package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
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
    onWatchAd: () -> Unit,
    onShareMilestone: (Milestone) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

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
                    "Enter a birth date on the Age tab first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkDim,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // ── Big astro tile ───────────────────────────────────────────
                AstroTile(result = result, isUnlocked = uiState.isUnlocked, hasLocation = uiState.location != null)

                // ── Watch-ad gate (only when not unlocked) ───────────────────
                if (!uiState.isUnlocked) {
                    WatchAdBanner(isLoading = uiState.isAdLoading, onWatch = onWatchAd)
                }

                // ── Milestone timeline ───────────────────────────────────────
                if (result.milestones.isNotEmpty()) {
                    MilestoneTimeline(
                        milestones = result.milestones,
                        totalDays = result.totalDays,
                        isUnlocked = uiState.isUnlocked,
                        onShare = onShareMilestone,
                        onToggleNotification = { target, enabled ->
                            viewModel.setMilestoneEnabled(target, enabled)
                        },
                    )
                }

                // ── Heartbeat counter ────────────────────────────────────────
                if (uiState.isUnlocked && result.estimatedHeartbeats > 0) {
                    HeartbeatRow(result.estimatedHeartbeats)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Astro tile
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun AstroTile(result: AgeResult, isUnlocked: Boolean, hasLocation: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface),
    ) {
        // Radial glow in the top-right corner
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(WarmTeal.copy(alpha = 0.20f), WarmTeal.copy(alpha = 0f)),
                    ),
                ),
        )

        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "WESTERN · VEDIC · CHINESE",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
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
            } else {
                // Placeholder when not yet unlocked
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
    Column(modifier = modifier) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = WarmInk,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DashaRow(info: String) {
    Column {
        Text(
            "DASHA",
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            info,
            style = MaterialTheme.typography.bodySmall,
            color = WarmAmber,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BaZiRow(info: String) {
    Column {
        Text(
            "BA ZI (FOUR PILLARS)",
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            info,
            style = MaterialTheme.typography.bodySmall,
            color = WarmInk,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Planet positions table
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlanetPositionTable(positions: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "PLANETARY POSITIONS",
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Spacer(Modifier.height(4.dp))
        positions.forEach { (planet, sign) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    planet,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInkMute,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    sign,
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInk,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Watch-ad inline banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun WatchAdBanner(isLoading: Boolean, onWatch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                "Unlock full profile",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInk,
                fontWeight = FontWeight.SemiBold
            )
            Text("Watch a 15s ad", style = MaterialTheme.typography.bodySmall, color = WarmInkMute)
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WarmTeal, strokeWidth = 2.dp)
        } else {
            val haptic = LocalHapticFeedback.current
            val isDark = MaterialTheme.colorScheme.background == WarmBlack
            Box {
                if (isDark) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        WarmTeal.copy(alpha = 0.18f),
                                        WarmTeal.copy(alpha = 0f),
                                    ),
                                )
                            ),
                    )
                }
                FilledTonalButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onWatch()
                }) {
                    Text("Watch & Reveal", style = MaterialTheme.typography.labelMedium)
                }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .padding(14.dp),
    ) {
        Text(
            "LIFE TIMELINE",
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
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
            Text(
                "LIFE LIVED",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            Text(
                "${(progress * 100).toInt()}% · ~$years yrs of 80",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkMute,
            )
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
            Text(
                "%,dth day".format(milestone.targetDays),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (milestone.isPast || isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = if (milestone.isPast || isToday) WarmInk else WarmInkMute,
            )
            Text(
                milestone.date.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                color = WarmInkDim,
            )
        }
        Text(
            statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            fontWeight = if (milestone.isPast || isToday) FontWeight.SemiBold else FontWeight.Normal,
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
// Heartbeat counter
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun HeartbeatRow(heartbeats: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text("♥", color = WarmAmber, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            "${formatHeartbeatsLong(heartbeats)} heartbeats and counting",
            style = MaterialTheme.typography.bodySmall,
            color = WarmInkDim,
        )
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
