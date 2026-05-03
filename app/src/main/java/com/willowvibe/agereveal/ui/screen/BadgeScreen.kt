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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(
    viewModel: BadgeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedBadge by remember { mutableStateOf<BadgeDefinition?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

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
        }
        Spacer(Modifier.height(16.dp))

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

    if (uiState.showConfetti) {
        ConfettiOverlay(onComplete = { viewModel.dismissConfetti() })
    }
}

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
    // Simple particle fade-out using Box alpha animation
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
