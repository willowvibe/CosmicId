package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.ui.components.AgeBody
import com.willowvibe.agereveal.ui.components.AgeCard
import com.willowvibe.agereveal.ui.components.AgeLabel
import com.willowvibe.agereveal.ui.components.AgeValue
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import java.time.LocalDate

@Composable
fun LifeTimelineScreen(
    milestones: List<Milestone>,
    onDismiss: () -> Unit,
    onShare: (Milestone) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Life Timeline",
                style = MaterialTheme.typography.titleLarge,
                color = WarmInk,
                fontWeight = FontWeight.SemiBold,
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WarmSurface)
                    .clickable { onDismiss() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Back",
                    tint = WarmInk,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(milestones) { milestone ->
                MilestoneRow(milestone, onShare = { onShare(milestone) })
            }
        }
    }
}

@Composable
private fun MilestoneRow(milestone: Milestone, onShare: () -> Unit = {}) {
    val isPast = milestone.isPast
    val isToday = milestone.date == LocalDate.now()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurface)
            .clickable { onShare() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (isPast) WarmTeal.copy(alpha = 0.2f)
                    else if (isToday) Color(0xFFFFD700).copy(alpha = 0.3f)
                    else WarmSurfaceSoft
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isPast) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = "Achieved",
                    tint = WarmTeal,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text(
                    "${milestone.daysAway}d",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isToday) Color(0xFFFFD700) else WarmInkDim,
                    maxLines = 1,
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = when {
                    isToday -> "Today's Milestone!"
                    else -> "%,d Days Alive".format(milestone.targetDays)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInk,
                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
            )
            AgeBody(
                text = milestone.date.toString(),
                color = WarmInkDim,
            )
        }

        if (isPast) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Reached",
                tint = WarmTeal,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
