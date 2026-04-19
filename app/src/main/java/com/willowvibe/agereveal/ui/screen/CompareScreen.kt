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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmInkMute
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.viewmodel.CompareViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel = hiltViewModel(),
    onShowInterstitial: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.comparisonCount) {
        if (uiState.comparisonCount > 0 && uiState.comparisonCount % 3 == 0) onShowInterstitial()
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Compare", style = MaterialTheme.typography.titleLarge, color = WarmInk)
                Text("2 PEOPLE", style = MaterialTheme.typography.labelSmall, color = WarmInkDim)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // ── Person A ─────────────────────────────────────────────────
                PersonCard(
                    label = "Person A",
                    name = uiState.labelA.takeIf { it != "Person A" } ?: "",
                    onNameChanged = viewModel::onPersonANameChanged,
                    selectedDate = uiState.dateA,
                    onDateSelected = { viewModel.onPersonADateSelected(it, uiState.labelA) },
                    isOlder = uiState.olderLabel == uiState.labelA && !uiState.isSameAge,
                )

                // ── "vs" divider ─────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = WarmSurfaceSoft)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "vs",
                        fontFamily = SerifFamily,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
                        color = WarmInkMute,
                    )
                    Spacer(Modifier.width(10.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = WarmSurfaceSoft)
                }

                // ── Person B ─────────────────────────────────────────────────
                PersonCard(
                    label = "Person B",
                    name = uiState.labelB.takeIf { it != "Person B" } ?: "",
                    onNameChanged = viewModel::onPersonBNameChanged,
                    selectedDate = uiState.dateB,
                    onDateSelected = { viewModel.onPersonBDateSelected(it, uiState.labelB) },
                    isOlder = uiState.olderLabel == uiState.labelB && !uiState.isSameAge,
                )

                // ── Compare button ───────────────────────────────────────────
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = viewModel::compare,
                    enabled = uiState.dateA != null && uiState.dateB != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmTeal,
                        contentColor = WarmBlack,
                        disabledContainerColor = WarmSurfaceSoft,
                        disabledContentColor = WarmInkDim,
                    ),
                ) {
                    Text("Compare", fontWeight = FontWeight.SemiBold)
                }

                // ── Result card ──────────────────────────────────────────────
                if (uiState.isSameAge || uiState.olderLabel.isNotEmpty()) {
                    CompareResultCard(
                        olderLabel = uiState.olderLabel,
                        isSameAge = uiState.isSameAge,
                        years = uiState.differenceYears,
                        months = uiState.differenceMonths,
                        days = uiState.differenceDays,
                        dateA = uiState.dateA,
                        dateB = uiState.dateB,
                        labelA = uiState.labelA,
                        labelB = uiState.labelB,
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Person card
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonCard(
    label: String,
    name: String,
    onNameChanged: (String) -> Unit,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    isOlder: Boolean,
) {
    var showDialog by remember { mutableStateOf(false) }
    val initialMillis = (selectedDate ?: LocalDate.now())
        .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate())
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    val borderColor = if (isOlder) WarmTeal.copy(alpha = 0.5f) else Color.Transparent
    val displayName = name.ifBlank { label }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .then(
                if (isOlder) Modifier.padding(1.dp).clip(RoundedCornerShape(13.dp)).background(WarmSurface)
                else Modifier
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isOlder) WarmTeal else WarmSurfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                fontFamily = SerifFamily,
                fontSize = 18.sp,
                color = if (isOlder) WarmBlack else WarmInk,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                placeholder = {
                    Text(label, style = MaterialTheme.typography.bodySmall, color = WarmInkDim)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = WarmInk),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmTeal,
                    unfocusedBorderColor = WarmSurfaceSoft,
                    cursorColor = WarmTeal,
                ),
            )
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = { showDialog = true },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(
                    text = selectedDate
                        ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
                        ?: "Set date",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedDate != null) WarmInkMute else WarmTeal,
                )
            }
        }

        if (isOlder) {
            Text(
                "OLDER",
                style = MaterialTheme.typography.labelSmall,
                color = WarmTeal,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compare result card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CompareResultCard(
    olderLabel: String,
    isSameAge: Boolean,
    years: Int,
    months: Int,
    days: Int,
    dateA: LocalDate?,
    dateB: LocalDate?,
    labelA: String,
    labelB: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        if (isSameAge) {
            Text(
                "SAME BIRTHDAY",
                style = MaterialTheme.typography.labelSmall,
                color = WarmAmber,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Both share the exact same birth date.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkMute,
            )
        } else {
            Text(
                "${olderLabel.uppercase()} IS OLDER BY",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (years > 0) {
                    Text(
                        years.toString(),
                        fontFamily = SerifFamily,
                        fontSize = 38.sp,
                        letterSpacing = (-1).sp,
                        color = WarmInk
                    )
                    Text(
                        "y",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInkMute,
                        modifier = Modifier.padding(start = 4.dp, end = 10.dp)
                    )
                }
                if (months > 0) {
                    Text(
                        months.toString(),
                        fontFamily = SerifFamily,
                        fontSize = 38.sp,
                        letterSpacing = (-1).sp,
                        color = WarmInk
                    )
                    Text(
                        "m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInkMute,
                        modifier = Modifier.padding(start = 4.dp, end = 10.dp)
                    )
                }
                Text(
                    days.toString(),
                    fontFamily = SerifFamily,
                    fontSize = 38.sp,
                    letterSpacing = (-1).sp,
                    color = WarmInk
                )
                Text(
                    "d",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkMute,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Bias bar — proportion of combined age lived
            if (dateA != null && dateB != null) {
                val today = LocalDate.now()
                val daysA = java.time.temporal.ChronoUnit.DAYS.between(dateA, today).coerceAtLeast(1)
                val daysB = java.time.temporal.ChronoUnit.DAYS.between(dateB, today).coerceAtLeast(1)
                val olderIsA = olderLabel == labelA
                val olderDays = if (olderIsA) daysA else daysB
                val youngerDays = if (olderIsA) daysB else daysA
                val olderPct = olderDays.toFloat() / (olderDays + youngerDays).toFloat()
                val olderPctDisplay = (olderPct * 100).toInt()

                Spacer(Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(WarmSurfaceSoft),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(olderPct.coerceIn(0.1f, 0.9f))
                            .height(6.dp)
                            .background(WarmAmber),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "${labelA.uppercase()} · ${if (olderLabel == labelA) olderPctDisplay else (100 - olderPctDisplay)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmInkDim,
                    )
                    Text(
                        "${labelB.uppercase()} · ${if (olderLabel == labelB) olderPctDisplay else (100 - olderPctDisplay)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmInkDim,
                    )
                }
            }
        }
    }
}
