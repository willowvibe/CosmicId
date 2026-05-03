package com.willowvibe.agereveal.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.model.BadgeDefinition
import com.willowvibe.agereveal.domain.BadgeDefinitions
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmInkMute
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.viewmodel.BadgeViewModel
import kotlin.math.roundToInt

private enum class ViewMode { GRID, TIMELINE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    viewModel: BadgeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBadge by remember { mutableStateOf<BadgeDefinition?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }

    if (selectedBadge != null) {
        val badge = selectedBadge!!
        val isUnlocked = badge.id in uiState.unlockedIds
        ModalBottomSheet(
            onDismissRequest = { selectedBadge = null },
            sheetState = sheetState,
            containerColor = WarmSurface,
        ) {
            BadgeDetailSheet(
                badge = badge,
                isUnlocked = isUnlocked,
                onShare = {
                    viewModel.shareBadge(badge, ShareCardGenerator.CardTheme.DARK_COSMOS)
                },
                onDismiss = { selectedBadge = null },
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Badges",
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmInk,
                    fontFamily = SerifFamily,
                )
                Text(
                    "${uiState.unlockedIds.size} / ${uiState.allBadges.size} unlocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInkMute,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PillToggle(
                    label = "Grid",
                    active = viewMode == ViewMode.GRID,
                    onClick = { viewMode = ViewMode.GRID },
                )
                PillToggle(
                    label = "Timeline",
                    active = viewMode == ViewMode.TIMELINE,
                    onClick = { viewMode = ViewMode.TIMELINE },
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Progress bar
        val progress = if (uiState.allBadges.isEmpty()) 0f
        else uiState.unlockedIds.size.toFloat() / uiState.allBadges.size
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(WarmSurfaceSoft),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(2.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(WarmTeal),
            )
        }
        Spacer(Modifier.height(16.dp))

        when (viewMode) {
            ViewMode.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.allBadges) { badge ->
                        val isUnlocked = badge.id in uiState.unlockedIds
                        BadgeCard(
                            badge = badge,
                            isUnlocked = isUnlocked,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedBadge = badge
                            },
                        )
                    }
                }
            }
            ViewMode.TIMELINE -> {
                TimelineView(
                    badges = uiState.allBadges,
                    unlockedIds = uiState.unlockedIds,
                )
            }
        }
    }

    if (uiState.showConfetti) {
        ConfettiOverlay(onComplete = { viewModel.dismissConfetti() })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pill toggle
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PillToggle(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .border(
                width = 1.5.dp,
                color = if (active) WarmTeal else WarmSurfaceSoft,
                shape = RoundedCornerShape(99.dp),
            )
            .background(if (active) WarmTeal.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) WarmTeal else WarmInkMute,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Timeline view
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TimelineView(
    badges: List<BadgeDefinition>,
    unlockedIds: Set<String>,
) {
    val scrollState = rememberScrollState()

    // Build events: start node + badge milestones + generic age milestones
    val events = remember(badges, unlockedIds) {
        val list = mutableListOf<TimelineEvent>()

        // Start of journey
        list.add(
            TimelineEvent(
                yearLabel = "Start",
                title = "Your Journey",
                subtitle = "The adventure begins",
                dotColor = WarmAmber,
                isUnlocked = true,
            )
        )

        // Badge events sorted by unlock threshold
        badges.sortedBy { it.unlockSeconds }.forEach { badge ->
            val isUnlocked = badge.id in unlockedIds
            val ageYears = if (badge.unlockSeconds > 0) {
                badge.unlockSeconds / 31_557_600.0
            } else 0.0
            val yearLabel = when {
                badge.unlockSeconds == 0L -> "Birth"
                ageYears >= 1 -> "~${ageYears.roundToInt()} yrs"
                else -> "~${(ageYears * 12).roundToInt()} mo"
            }
            list.add(
                TimelineEvent(
                    yearLabel = yearLabel,
                    title = badge.title,
                    subtitle = if (isUnlocked) "Badge unlocked" else "${badge.unlockSeconds.toDayLabel()}",
                    dotColor = if (isUnlocked) WarmTeal else WarmInkDim,
                    isUnlocked = isUnlocked,
                    emoji = badge.iconEmoji,
                )
            )
        }

        // Generic age milestones
        listOf(1, 10, 18, 21, 30, 40, 50, 60, 70, 80, 90, 100).forEach { years ->
            // Only add if not already covered by a badge threshold
            val alreadyCovered = badges.any {
                val badgeYears = it.unlockSeconds / 31_557_600.0
                badgeYears > 0 && kotlin.math.abs(badgeYears - years) < 0.5
            }
            if (!alreadyCovered) {
                list.add(
                    TimelineEvent(
                        yearLabel = "$years yrs",
                        title = "$years years old",
                        subtitle = "${(years * 365.25).roundToInt().toDayLabel()}",
                        dotColor = WarmInkDim,
                        isUnlocked = false,
                    )
                )
            }
        }

        list.sortedWith(compareBy(
            { it.sortKey },
            { it.title }
        ))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp),
    ) {
        events.forEachIndexed { index, event ->
            val isLast = index == events.lastIndex
            TimelineRow(
                event = event,
                showConnector = !isLast,
            )
        }
    }
}

private data class TimelineEvent(
    val yearLabel: String,
    val title: String,
    val subtitle: String,
    val dotColor: Color,
    val isUnlocked: Boolean,
    val emoji: String? = null,
) {
    val sortKey: Double = when {
        yearLabel == "Start" -> -1.0
        yearLabel == "Birth" -> 0.0
        yearLabel.endsWith(" yrs") -> yearLabel.removeSuffix(" yrs").toDoubleOrNull() ?: 999.0
        yearLabel.endsWith(" mo") -> (yearLabel.removeSuffix(" mo").toDoubleOrNull() ?: 999.0) / 12.0
        else -> 999.0
    }
}

@Composable
private fun TimelineRow(
    event: TimelineEvent,
    showConnector: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Dot + connector column
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(event.dotColor),
            )
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(WarmSurfaceSoft),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (showConnector) 20.dp else 0.dp),
        ) {
            Text(
                event.yearLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                event.emoji?.let {
                    Text(it, fontSize = 14.sp, modifier = Modifier.padding(end = 6.dp))
                }
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (event.isUnlocked) WarmInk else WarmInkMute,
                )
            }
            Text(
                event.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkDim,
            )
        }
    }
}

private fun Long.toDayLabel(): String {
    return when {
        this >= 1_000_000_000 -> "${this / 1_000_000_000}B seconds"
        this >= 1_000_000 -> "${this / 1_000_000}M seconds"
        this >= 1_000 -> "%,d seconds".format(this)
        else -> "$this seconds"
    }
}

private fun Int.toDayLabel(): String {
    return when {
        this >= 1_000_000 -> "${this / 1_000_000}M days"
        this >= 1_000 -> "%,d days".format(this)
        else -> "$this days"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Badge card (grid cell)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BadgeCard(
    badge: BadgeDefinition,
    isUnlocked: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 0.96f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "badge_scale",
    )

    Column(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .then(
                if (isUnlocked) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = WarmTeal,
                        shape = RoundedCornerShape(16.dp),
                    )
                } else Modifier
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isUnlocked) {
            Text(badge.iconEmoji, fontSize = 28.sp)
            Text(
                badge.title,
                style = MaterialTheme.typography.labelSmall,
                color = WarmInk,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            RarityChip(rarity = badge.rarity)
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = WarmInkDim,
                modifier = Modifier.size(20.dp),
            )
            Text(
                "???",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(WarmSurfaceSoft)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    "Locked",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmInkDim,
                    fontSize = 9.sp,
                )
            }
        }
    }
}

@Composable
private fun RarityChip(rarity: com.willowvibe.agereveal.data.model.BadgeRarity) {
    val color = BadgeDefinitions.rarityColor(rarity)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            rarity.name,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontSize = 9.sp,
        )
    }
}

@Composable
private fun BadgeDetailSheet(
    badge: BadgeDefinition,
    isUnlocked: Boolean,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RarityChip(rarity = badge.rarity)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = WarmInkMute)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(badge.iconEmoji, fontSize = 64.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            badge.title,
            style = MaterialTheme.typography.titleLarge,
            color = WarmInk,
            fontFamily = SerifFamily,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            badge.description,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkMute,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        if (isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(WarmTeal)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShare()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Share Badge",
                    color = WarmBlack,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(WarmSurfaceSoft)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Keep living to unlock",
                    color = WarmInkDim,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ConfettiOverlay(onComplete: () -> Unit) {
    val alpha by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 3_000, easing = FastOutSlowInEasing),
        finishedListener = { onComplete() },
        label = "confetti_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(
                Brush.radialGradient(
                    listOf(
                        WarmAmber.copy(alpha = 0.15f),
                        Color.Transparent,
                    ),
                )
            )
            .clickable(enabled = false) {},
    )
}
