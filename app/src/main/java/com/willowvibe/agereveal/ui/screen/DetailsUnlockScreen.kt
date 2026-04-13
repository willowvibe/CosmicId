package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel

/**
 * Screen 2 — Unlockable Details (rewarded ad gate).
 * Shows after the user taps "Unlock More ▶" on CalculatorScreen and watches the rewarded ad.
 *
 * Displays: Zodiac · Rashi · Nakshatra · Chinese zodiac · Heartbeats · Milestone days.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsUnlockScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    onWatchAd: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val result = uiState.result

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Detailed Profile") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!uiState.isUnlocked || result == null) {
                UnlockGate(isLoading = uiState.isAdLoading, onWatch = onWatchAd)
            } else {
                UnlockedDetails(result = result)
            }
        }
    }
}

@Composable
private fun UnlockGate(isLoading: Boolean, onWatch: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Default.Lock, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Text("Unlock Your Vedic Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Text(
                "Watch a short 15-second video to reveal your Rashi, Nakshatra, Chinese zodiac, milestone days, and estimated heartbeats.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onWatch) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Text("  Watch & Unlock  ")
                }
            }
        }
    }
}

@Composable
private fun UnlockedDetails(result: AgeResult) {
    // Zodiac group
    SectionCard(title = "Astrological Signs") {
        DetailRow("Western Zodiac", result.westernZodiac)
        DetailRow("Rashi (Vedic)", result.rashi)
        DetailRow("Nakshatra", result.nakshatra)
        DetailRow("Chinese Zodiac", result.chineseZodiac)
    }

    // Life stats group
    SectionCard(title = "Life Statistics") {
        DetailRow("Estimated Heartbeats", "%,d".format(result.estimatedHeartbeats))
    }

    // Milestones group
    if (result.milestones.isNotEmpty()) {
        SectionCard(title = "Milestone Days") {
            result.milestones.forEach { milestone ->
                val label = "%,d".format(milestone.targetDays) + "th day"
                val value = buildString {
                    append(milestone.date.toString())
                    if (milestone.isPast) append("  ✓ passed")
                    else append("  (in ${milestone.daysAway}d)")
                }
                DetailRow(label, value, highlight = !milestone.isPast)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface)
    }
}
