package com.willowvibe.agereveal.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Screen 1 — Main Calculator (always visible, no ad gate).
 *
 * Layout (top → bottom):
 *   TopAppBar "AgeReveal"
 *   [DatePicker card]          ← thumb-friendly, opens Material3 DatePickerDialog
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
            BannerAdView(adUnitId = AdManager.BANNER_AD_UNIT_ID)
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
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PrimaryAgeDisplay(result = result)
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
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerCard(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    // Initialise picker at selected date, or today
    val initialMillis = (selectedDate ?: LocalDate.now())
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        onDateSelected(date)
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Date of Birth",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDate
                        ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                        ?: "Tap to select birthday",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedDate != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "Open date picker",
                tint = MaterialTheme.colorScheme.primary,
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
private fun CtaRow(onShare: () -> Unit, onUnlock: () -> Unit, isUnlocked: Boolean, hasResult: Boolean = true) {
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
private fun BannerAdView(adUnitId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
