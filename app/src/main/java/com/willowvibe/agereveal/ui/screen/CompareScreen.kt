package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.ui.viewmodel.CompareViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Screen 3 — Compare two people.
 *
 * Two date pickers side by side (Person A / Person B).
 * Below: "X is older by Y years Z months W days."
 * After the 2nd comparison → trigger interstitial ad (handled via [onShowInterstitial]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel = hiltViewModel(),
    onShowInterstitial: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger interstitial after the 2nd comparison (as per build plan)
    LaunchedEffect(uiState.comparisonCount) {
        if (uiState.comparisonCount == 2) onShowInterstitial()
    }

    // Show validation errors via Snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compare Ages") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Two person date pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PersonDateCard(
                    modifier = Modifier.weight(1f),
                    label = "Person A",
                    selectedDate = uiState.dateA,
                    onDateSelected = { viewModel.onPersonADateSelected(it) },
                )
                PersonDateCard(
                    modifier = Modifier.weight(1f),
                    label = "Person B",
                    selectedDate = uiState.dateB,
                    onDateSelected = { viewModel.onPersonBDateSelected(it) },
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = viewModel::compare,
                enabled = uiState.dateA != null && uiState.dateB != null,
            ) {
                Text("Compare")
            }

            // Result
            if (uiState.isSameAge || uiState.olderLabel.isNotEmpty()) {
                CompareResultCard(
                    olderLabel = uiState.olderLabel,
                    isSameAge = uiState.isSameAge,
                    years = uiState.differenceYears,
                    months = uiState.differenceMonths,
                    days = uiState.differenceDays,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonDateCard(
    modifier: Modifier = Modifier,
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

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
        modifier = modifier.clickable { showDialog = true },
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "Select date for $label",
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = selectedDate
                    ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                    ?: "Tap to set",
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedDate != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompareResultCard(
    olderLabel: String,
    isSameAge: Boolean,
    years: Int, months: Int, days: Int,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isSameAge) {
                Text("Same birthday!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Both people share the exact same birth date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("$olderLabel is older by",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    buildString {
                        if (years > 0) append("$years yr${if (years > 1) "s" else ""}  ")
                        if (months > 0) append("$months mo  ")
                        append("$days day${if (days != 1) "s" else ""}")
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
