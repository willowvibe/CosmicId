package com.willowvibe.agereveal.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel

/**
 * Screen 1 — Main Calculator (always visible, no ad gate).
 *
 * Layout (top → bottom):
 *   TopAppBar "AgeReveal"
 *   [DatePicker card]          ← thumb-friendly
 *   [Primary age display]      ← large, animated reveal
 *   [Stats row]                ← total days, hours, next birthday
 *   [Born on / next birthday day-of-week]
 *   [CTA row: Share Card | Unlock More ▶]
 *   [BannerAd]                 ← anchored to bottom of Scaffold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    adManager: AdManager,
    onShareCard: () -> Unit,
    onUnlockMore: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val ticker by viewModel.tickerSeconds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Recalculate total seconds every tick
    LaunchedEffect(ticker) { viewModel.onTick() }

    // Show errors via Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AgeReveal", style = MaterialTheme.typography.titleLarge) })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // TODO: Replace with real AdMob BannerAdView composable
            BannerAdPlaceholder()
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Date picker
            DatePickerCard(
                selectedDate = uiState.birthDate,
                onDateSelected = viewModel::onBirthDateSelected,
            )

            // Primary age display (animated in after date is set)
            AnimatedVisibility(
                visible = uiState.result != null,
                enter = fadeIn() + slideInVertically { it / 2 },
            ) {
                uiState.result?.let { result ->
                    PrimaryAgeDisplay(result = result)
                    Spacer(modifier = Modifier.height(4.dp))
                    AgeStatsRow(result = result)
                    BornOnCard(result = result)
                    CtaRow(
                        onShare = onShareCard,
                        onUnlock = onUnlockMore,
                        isUnlocked = uiState.isUnlocked,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun DatePickerCard(
    selectedDate: java.time.LocalDate?,
    onDateSelected: (java.time.LocalDate) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Date of Birth", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            // TODO: Replace with DatePickerDialog trigger — large, thumb-friendly
            Text(
                text = selectedDate?.toString() ?: "Tap to select birthday",
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedDate != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrimaryAgeDisplay(result: AgeResult) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "${result.years} yrs  ${result.months} mo  ${result.days} days",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "${"%,d".format(result.totalSeconds)} seconds lived",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AgeStatsRow(result: AgeResult) {
    // TODO: use AgeStatChip components in a FlowRow once the component library is built
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AgeStatItem("Total days lived", "%,d days".format(result.totalDays))
        AgeStatItem("Total hours",      "%,d hrs".format(result.totalHours))
        AgeStatItem("Next birthday in", "${result.daysToNextBirthday} days",
            valueHighlight = true)
    }
}

@Composable
private fun AgeStatItem(label: String, value: String, valueHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (valueHighlight) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BornOnCard(result: AgeResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Born on a ${result.dayOfWeekBorn.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium)
            Text("Next birthday falls on a ${result.dayOfWeekNextBirthday.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CtaRow(onShare: () -> Unit, onUnlock: () -> Unit, isUnlocked: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(modifier = Modifier.weight(1f), onClick = onShare) {
            Text("Share Card")
        }
        if (!isUnlocked) {
            OutlinedButton(modifier = Modifier.weight(1f), onClick = onUnlock) {
                Text("Unlock More ▶")
            }
        }
    }
}

@Composable
private fun BannerAdPlaceholder() {
    // TODO: Replace with real AndroidView wrapping AdView
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("[ Banner Ad — 320×50 ]",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
