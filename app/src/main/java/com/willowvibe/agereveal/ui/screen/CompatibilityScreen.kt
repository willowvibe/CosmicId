package com.willowvibe.agereveal.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DatePicker
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.willowvibe.agereveal.domain.ShareCardGenerator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.R
import com.willowvibe.agereveal.domain.AgeInfo
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.domain.RelationshipType
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmInkMute
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.viewmodel.CompatibilityViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityScreen(
    viewModel: CompatibilityViewModel = hiltViewModel(),
    defaultDateA: java.time.LocalDate? = null,
    defaultNameA: String = "",
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showThemePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    // Pre-fill Person A with "You" tab data on first visit
    LaunchedEffect(Unit) {
        if (uiState.dateA == null && defaultDateA != null) {
            viewModel.onDateASelected(defaultDateA)
        }
        if (uiState.nameA.isBlank() && defaultNameA.isNotBlank()) {
            viewModel.setNameA(defaultNameA)
        }
    }

    if (showThemePicker) {
        ShareThemeSheet(
            onDismiss = { showThemePicker = false },
            onThemeSelected = { theme, _ ->
                viewModel.shareCard(theme)
                showThemePicker = false
            },
            showStoryOption = false,
        )
    }

    // Persist state when navigating away from the screen
    LaunchedEffect(uiState) {
        // State is already preserved by hiltViewModel() within the NavHost scope
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
                Text(
                    "Compatibility",
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmInk,
                )
                Text(
                    "COSMIC MATCH",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmInkDim,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Relationship type selector ────────────────────────────────
                RelationshipTypeSelector(
                    selected = uiState.relationshipType,
                    onSelect = viewModel::setRelationshipType,
                )

                // ── Person A ──────────────────────────────────────────────────
                PersonDateCard(
                    label = "PERSON A",
                    name = uiState.nameA,
                    date = uiState.dateA,
                    onNameChanged = viewModel::setNameA,
                    onDateSelected = { date ->
                        viewModel.onDateASelected(date)
                    },
                )

                // ── VS divider ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = WarmSurfaceSoft)
                    Text(
                        "  vs  ",
                        fontFamily = SerifFamily,
                        fontSize = 18.sp,
                        color = WarmInkDim,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = WarmSurfaceSoft)
                }

                // ── Person B ──────────────────────────────────────────────────
                PersonDateCard(
                    label = "PERSON B",
                    name = uiState.nameB,
                    date = uiState.dateB,
                    onNameChanged = viewModel::setNameB,
                    onDateSelected = { date ->
                        viewModel.onDateBSelected(date)
                    },
                )

                // ── Result ────────────────────────────────────────────────────
                if (uiState.dateA == null || uiState.dateB == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Enter both birthdays\nto reveal your cosmic match",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmInkDim,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                if (uiState.isSameDate) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarmSurface)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✨", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Birthday Twins!",
                                fontFamily = SerifFamily,
                                fontSize = 22.sp,
                                color = WarmAmber,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Both share the same birth date — cosmic alignment at 100%.",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmInkDim,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                if (!uiState.isSameDate) {
                    uiState.result?.let { result ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(500, easing = FastOutSlowInEasing)) +
                                    slideInVertically(tween(500), { it / 3 }),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                AgeComparisonCard(result)
                                CompatibilityResultCard(result)
                                FilledTonalButton(
                                    onClick = { showThemePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Share match card",
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Share Match Card")
                                }
                            }
                        }
                    } ?: EmptyCompatibilityState()
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Relationship type selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun RelationshipTypeSelector(
    selected: RelationshipType,
    onSelect: (RelationshipType) -> Unit,
) {
    val types = RelationshipType.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        types.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) WarmTeal else WarmSurface)
                    .clickable { onSelect(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    type.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) WarmBlack else WarmInkDim,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Person date card
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PersonDateCard(
    label: String,
    name: String,
    date: LocalDate?,
    onNameChanged: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val initialMillis = (date ?: LocalDate.now().minusYears(25))
        .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(
                            Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate(),
                        )
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            label = { Text(label, style = MaterialTheme.typography.labelSmall, color = WarmInkDim) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WarmTeal,
                unfocusedBorderColor = WarmInkDim,
                focusedTextColor = WarmInk,
                unfocusedTextColor = WarmInk,
                focusedLabelColor = WarmInkDim,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                    ?: "Tap to set birthday",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = SerifFamily,
                    fontSize = 16.sp,
                ),
                color = if (date != null) WarmInk else WarmInkMute,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(WarmSurfaceSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = "Pick date",
                    tint = WarmTeal,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Age comparison card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun AgeComparisonCard(result: CompatibilityResult) {
    val ageA = result.personAAge
    val ageB = result.personBAge
    if (ageA == null || ageB == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.age_comparison),
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AgeChip(
                name = result.nameA.ifEmpty { "Person A" },
                age = ageA,
                modifier = Modifier.weight(1f),
            )
            AgeChip(
                name = result.nameB.ifEmpty { "Person B" },
                age = ageB,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            result.ageGapLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmAmber,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AgeChip(name: String, age: AgeInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WarmSurfaceSoft)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${age.years}y ${age.months}m ${age.days}d",
            fontFamily = SerifFamily,
            fontSize = 18.sp,
            color = WarmInk,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compatibility result card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun CompatibilityResultCard(result: CompatibilityResult) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Score hero ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WarmSurface)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${result.overallScore}%",
                fontFamily = SerifFamily,
                fontSize = 64.sp,
                letterSpacing = (-2).sp,
                color = scoreColor(result.overallScore),
            )
            Text(
                result.headline,
                style = MaterialTheme.typography.titleMedium,
                color = WarmInk,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            ScoreBar(
                score = result.overallScore,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
            )
        }

        // ── Breakdown ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ScoreChip(
                label = "WESTERN",
                score = result.westernScore,
                modifier = Modifier.weight(1f),
            )
            ScoreChip(
                label = "CHINESE",
                score = result.chineseScore,
                modifier = Modifier.weight(1f),
            )
        }

        // ── Zodiac pairing detail ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WarmSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "ZODIAC PAIRING",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            ZodiacPairingRow("Western", result.personAWestern, result.personBWestern)
            HorizontalDivider(color = WarmSurfaceSoft)
            ZodiacPairingRow("Element", result.personAElement, result.personBElement)
            HorizontalDivider(color = WarmSurfaceSoft)
            ZodiacPairingRow("Chinese", result.personAChinese, result.personBChinese)
            if (result.chineseRelationshipLabel.isNotEmpty()) {
                HorizontalDivider(color = WarmSurfaceSoft)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Relation",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkMute,
                        modifier = Modifier.width(64.dp),
                    )
                    Text(
                        result.chineseRelationshipLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmAmber,
                    )
                }
            }
        }

        // ── Description ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WarmSurface)
                .padding(16.dp),
        ) {
            Text(
                "ELEMENT READING",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                result.description,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInk,
                lineHeight = 22.sp,
            )
        }
    }
}

@Composable
private fun ZodiacPairingRow(label: String, valueA: String, valueB: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = WarmInkMute,
            modifier = Modifier.width(64.dp),
        )
        Text(
            valueA,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = WarmInk,
            modifier = Modifier.weight(1f),
        )
        Text(
            "·",
            style = MaterialTheme.typography.bodySmall,
            color = WarmInkDim,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Text(
            valueB,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = WarmInk,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun ScoreChip(label: String, score: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = WarmInkDim)
        Spacer(Modifier.height(4.dp))
        Text(
            "$score%",
            fontFamily = SerifFamily,
            fontSize = 28.sp,
            letterSpacing = (-1).sp,
            color = scoreColor(score),
        )
    }
}

@Composable
private fun ScoreBar(score: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(WarmSurfaceSoft)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(score / 100f)
                .height(6.dp)
                .background(scoreColor(score)),
        )
    }
}

@Composable
private fun scoreColor(score: Int) = when {
    score >= 80 -> WarmTeal
    score >= 60 -> WarmAmber
    else -> WarmInkDim
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyCompatibilityState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(WarmAmber.copy(alpha = 0.20f), WarmAmber.copy(alpha = 0.05f)),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("🌙", fontSize = 40.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Discover your cosmic match",
            fontFamily = SerifFamily,
            fontSize = 20.sp,
            color = WarmInkMute,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Enter two birth dates above to reveal your compatibility score, age comparison, and Chinese zodiac harmony.",
            style = MaterialTheme.typography.bodySmall,
            color = WarmInkDim,
            textAlign = TextAlign.Center,
        )
    }
}
