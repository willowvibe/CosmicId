package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.ui.viewmodel.CompareViewModel

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

    // Trigger interstitial after the 2nd comparison (as per build plan)
    LaunchedEffect(uiState.comparisonCount) {
        if (uiState.comparisonCount == 2) onShowInterstitial()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compare Ages") })
        },
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
            if (uiState.olderLabel.isNotEmpty()) {
                CompareResultCard(
                    olderLabel = uiState.olderLabel,
                    years = uiState.differenceYears,
                    months = uiState.differenceMonths,
                    days = uiState.differenceDays,
                )
            }
        }
    }
}

@Composable
private fun PersonDateCard(
    modifier: Modifier = Modifier,
    label: String,
    selectedDate: java.time.LocalDate?,
    onDateSelected: (java.time.LocalDate) -> Unit,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            // TODO: Replace with DatePickerDialog trigger
            Text(
                text = selectedDate?.toString() ?: "Tap to set",
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
    years: Int, months: Int, days: Int,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
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
