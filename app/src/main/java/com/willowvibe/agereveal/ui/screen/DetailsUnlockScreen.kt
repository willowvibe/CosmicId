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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    viewModel: CalculatorViewModel = hiltViewModel(),
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
                AstroTile(result = result, isUnlocked = uiState.isUnlocked)

                // ── Watch-ad gate (only when not unlocked) ───────────────────
                if (!uiState.isUnlocked) {
                    WatchAdBanner(isLoading = uiState.isAdLoading, onWatch = onWatchAd)
                }

                // ── Milestone timeline ───────────────────────────────────────
                if (result.milestones.isNotEmpty()) {
                    MilestoneTimeline(
                        milestones = result.milestones,
                        isUnlocked = uiState.isUnlocked,
                        onShare = onShareMilestone,
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
private fun AstroTile(result: AgeResult, isUnlocked: Boolean) {
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
                Text(
                    "${result.westernZodiac} · ${result.rashi}",
                    fontFamily = SerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.5).sp,
                    color = WarmInk,
                )
                if (result.chineseZodiac.isNotEmpty()) {
                    val chineseParts = result.chineseZodiac.split(" ", limit = 2)
                    val chineseLabel = if (chineseParts.size >= 2)
                        "Year of the ${chineseParts[1]} ${chineseParts[0]}"
                    else result.chineseZodiac
                    Text(
                        chineseLabel,
                        fontFamily = SerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        letterSpacing = (-0.3).sp,
                        color = WarmInk,
                    )
                }
                if (result.nakshatra.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Text("Nakshatra ", style = MaterialTheme.typography.bodySmall, color = WarmInkMute)
                        Text(result.nakshatra, style = MaterialTheme.typography.bodySmall, color = WarmInk)
                    }
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

// ─────────────────────────────────────────────────────────────────────────────
// Watch-ad inline banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WatchAdBanner(isLoading: Boolean, onWatch: () -> Unit) {
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
            Text("Unlock full profile", style = MaterialTheme.typography.bodyMedium, color = WarmInk, fontWeight = FontWeight.SemiBold)
            Text("Watch a 15s ad", style = MaterialTheme.typography.bodySmall, color = WarmInkMute)
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WarmTeal, strokeWidth = 2.dp)
        } else {
            FilledTonalButton(onClick = onWatch) {
                Text("Watch & Reveal", style = MaterialTheme.typography.labelMedium)
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
    isUnlocked: Boolean,
    onShare: (Milestone) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .padding(14.dp),
    ) {
        Text(
            "MILESTONE DAYS",
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Spacer(Modifier.height(10.dp))

        milestones.forEach { milestone ->
            MilestoneRow(
                milestone = milestone,
                isUnlocked = isUnlocked,
                onShare = { onShare(milestone) },
            )
        }
    }
}

@Composable
private fun MilestoneRow(
    milestone: Milestone,
    isUnlocked: Boolean,
    onShare: () -> Unit,
) {
    val dotColor = when {
        milestone.daysAway == 0L -> WarmAmber
        milestone.isPast         -> WarmInkDim
        else                     -> WarmTeal
    }
    val statusLabel = when {
        milestone.daysAway == 0L -> "TODAY"
        milestone.isPast         -> "PASSED"
        else                     -> "IN ${milestone.daysAway}D"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = WarmInk,
            )
            Text(
                milestone.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                color = WarmInkDim,
            )
        }
        Text(
            statusLabel,
            style = MaterialTheme.typography.labelSmall,
            color = dotColor,
        )
        if (isUnlocked) {
            IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Share milestone", tint = WarmTeal, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Heartbeat counter
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeartbeatRow(heartbeats: Long) {
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
    n >= 1_000_000     -> "%,.1f M".format(n / 1_000_000.0)
    else               -> "%,d".format(n)
}

private fun moonPhaseHint(month: Int): String = when (month) {
    1, 2  -> "waxing crescent moon"
    3, 4  -> "full moon season"
    5, 6  -> "waning gibbous moon"
    7, 8  -> "new moon season"
    9, 10 -> "waxing gibbous moon"
    else  -> "waning crescent moon"
}
