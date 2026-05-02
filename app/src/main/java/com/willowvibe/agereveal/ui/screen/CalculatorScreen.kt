package com.willowvibe.agereveal.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.willowvibe.agereveal.ads.AdManager
import com.willowvibe.agereveal.data.model.AgeResult
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
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    adManager: AdManager,
    onShareCard: (ShareCardGenerator.CardTheme) -> Unit,
    onUnlockMore: () -> Unit,
    onOpenSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val ticker by viewModel.tickerSeconds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var showThemePicker by remember { mutableStateOf(false) }

    LaunchedEffect(ticker) { viewModel.onTick() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    if (showThemePicker) {
        ShareThemeSheet(
            onDismiss = { showThemePicker = false },
            onThemeSelected = { theme ->
                onShareCard(theme)
                showThemePicker = false
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(WarmAmber),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "AgeReveal",
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmInk,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmInkDim,
                    )
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(WarmSurface),
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = WarmInkDim,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // ── Person name input ─────────────────────────────────────────
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text("Name", style = MaterialTheme.typography.labelSmall, color = WarmInkDim) },
                    placeholder = {
                        Text(
                            "Enter your name",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmInkMute
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmTeal,
                        unfocusedBorderColor = WarmInkDim,
                        focusedTextColor = WarmInk,
                        unfocusedTextColor = WarmInk,
                        focusedLabelColor = WarmInkDim,
                    ),
                )

                // ── Birth anchor ─────────────────────────────────────────────
                BirthAnchorRow(
                    selectedDate = uiState.birthDate,
                    onDateSelected = viewModel::onBirthDateSelected,
                )

                // ── Precision settings (birth time + location) ───────────────
                PrecisionRow(
                    birthTime = uiState.birthTime,
                    location = uiState.location,
                    onTimeSelected = viewModel::onBirthTimeSelected,
                    onLocationSelected = viewModel::onLocationSelected,
                )

                uiState.result?.let { result ->
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        // Hero stagger entrance — each block fades in with a slight delay
                        StaggeredEnter(delayMillis = 0) { ClockFaceHero(result) }
                        StaggeredEnter(delayMillis = 70) { SecondsStrip(result) }
                        StaggeredEnter(delayMillis = 140) { MiniStatRow(result) }
                        StaggeredEnter(delayMillis = 210) { NextMilestoneChip(result) }
                        StaggeredEnter(delayMillis = 280) {
                            Column {
                                if (!uiState.isUnlocked) {
                                    WatchAdBanner(
                                        isLoading = uiState.isAdLoading,
                                        onWatch = onUnlockMore,
                                    )
                                    Spacer(Modifier.height(14.dp))
                                }
                                AstroTile(result = result, isUnlocked = uiState.isUnlocked, hasLocation = uiState.location != null)
                            }
                        }
                        if (uiState.isUnlocked) {
                            StaggeredEnter(delayMillis = 350) {
                                val shareHaptic = LocalHapticFeedback.current
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(WarmSurface)
                                        .clickable(
                                            role = Role.Button,
                                            onClick = {
                                                shareHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showThemePicker = true
                                            },
                                        )
                                        .semantics { contentDescription = "Share your cosmic profile" }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "Share your cosmic profile",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WarmInk,
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = WarmTeal,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    // Placeholder before a date is chosen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Tap the date above to begin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmInkDim,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── Banner ad ────────────────────────────────────────────────────
            BannerAdView(adUnitId = AdManager.BANNER_AD_UNIT_ID)
        }

        // Snackbar overlay
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Birth anchor row
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthAnchorRow(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val initialMillis = (selectedDate ?: LocalDate.now())
        .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    // Minimum year guard - validate year selection in the confirm button
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    if (showDialog) {
        val haptic = LocalHapticFeedback.current
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        // Validate minimum year (1900) to avoid unreliable astronomical calculations
                        if (selectedDate.year >= 1900) {
                            onDateSelected(selectedDate)
                            showDialog = false
                        }
                    }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable(
                    role = Role.Button,
                    onClick = { showDialog = true },
                )
                .semantics { contentDescription = "Change birth date, currently ${selectedDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)) ?: "not set"}" },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    "BORN",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmInkDim,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selectedDate
                        ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                        ?: "Tap to set your birthday",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SerifFamily,
                        fontSize = 17.sp,
                        letterSpacing = (-0.2).sp,
                    ),
                    color = if (selectedDate != null) WarmInk else WarmInkMute,
                )
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(WarmSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Change birth date",
                    tint = WarmInkMute,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        HorizontalDivider(color = WarmSurfaceSoft, thickness = 1.dp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Clock-face hero
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun ClockFaceHero(result: AgeResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        AgeNumeral(value = result.years.toString(), unit = "years", large = true, modifier = Modifier.weight(1.3f))
        AgeNumeral(
            value = result.months.toString().padStart(2, '0'),
            unit = "months",
            large = false,
            modifier = Modifier.weight(1f)
        )
        AgeNumeral(
            value = result.days.toString().padStart(2, '0'),
            unit = "days",
            large = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AgeNumeral(
    value: String,
    unit: String,
    large: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            fontFamily = SerifFamily,
            fontWeight = if (large) FontWeight.Light else FontWeight.Normal,
            fontSize = if (large) 78.sp else 46.sp,
            lineHeight = if (large) 74.sp else 44.sp,
            letterSpacing = (-2).sp,
            color = WarmInk,
        )
        Text(
            text = unit.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Seconds alive strip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun SecondsStrip(result: AgeResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(WarmAmber),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "SECONDS ALIVE",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            RollingDigits(
                number = result.totalSeconds,
                fontSize = 20.sp,
                color = WarmAmber,
            )
        }
        Text(
            "+1 per\nsecond",
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * Per-digit rolling animation like a digital clock.
 * Only digit characters animate; commas and separators stay static.
 */
@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
internal fun RollingDigits(
    number: Long,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: androidx.compose.ui.graphics.Color,
) {
    val formatted = "%,d".format(number)
    Row {
        formatted.forEach { char ->
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                    },
                    label = "rolling_digit",
                ) { digit ->
                    Text(
                        text = digit.toString(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = fontSize,
                        letterSpacing = (-0.5).sp,
                        color = color,
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize,
                    letterSpacing = (-0.5).sp,
                    color = color,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mini stat chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun MiniStatRow(result: AgeResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MiniStatChip(label = "DAYS", value = "%,d".format(result.totalDays), modifier = Modifier.weight(1f))
        MiniStatChip(label = "HOURS", value = formatCompactNumber(result.totalHours), modifier = Modifier.weight(1f))
        MiniStatChip(
            label = "NEXT BDAY",
            value = "${result.daysToNextBirthday}d",
            accent = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniStatChip(
    label: String,
    value: String,
    accent: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WarmSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = WarmInkDim)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (accent) WarmAmber else WarmInk,
        )
    }
}

private fun formatCompactNumber(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}

// ─────────────────────────────────────────────────────────────────────────────
// Teased details
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TeasedDetails(
    result: AgeResult,
    isUnlocked: Boolean,
    onReveal: () -> Unit,
    onShare: () -> Unit,
) {
    // Hoverable unlock card
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .clickable(
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onReveal()
                },
            )
            .semantics { contentDescription = if (!isUnlocked) "Unlock your Vedic and cosmic profile" else "Share your cosmic profile" }
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    "YOUR VEDIC & COSMIC PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmInkDim,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (!isUnlocked) "Tap to reveal your profile" else "Share your profile card",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmInkMute,
                )
            }
            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(WarmTeal)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Unlock",
                        tint = WarmBlack,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = WarmTeal,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    val blurMod = if (!isUnlocked) Modifier.blur(4.dp) else Modifier

    Column(modifier = blurMod) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TeaseChip("Rashi", result.rashi.ifEmpty { "Meena" }, modifier = Modifier.weight(1f))
            TeaseChip("Nakshatra", result.nakshatra.ifEmpty { "Uttara Bhadrapada" }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TeaseChip("Chinese", result.chineseZodiac.ifEmpty { "Tiger" }, modifier = Modifier.weight(1f))
            TeaseChip(
                "Heartbeats",
                if (result.estimatedHeartbeats > 0) formatHeartbeats(result.estimatedHeartbeats) else "~1.0 B",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TeaseChip(key: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(key, style = MaterialTheme.typography.bodySmall, color = WarmInkMute)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = WarmInk)
    }
}

private fun formatHeartbeats(n: Long): String = when {
    n >= 1_000_000_000 -> "%.2f B".format(n / 1_000_000_000.0)
    n >= 1_000_000 -> "%.1f M".format(n / 1_000_000.0)
    else -> "%,d".format(n)
}

// ─────────────────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────────────────
// Precision settings row (birth time + location combined)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrecisionRow(
    birthTime: LocalTime?,
    location: com.willowvibe.agereveal.data.model.GeoLocation?,
    onTimeSelected: (LocalTime?) -> Unit,
    onLocationSelected: (com.willowvibe.agereveal.data.model.GeoLocation?) -> Unit,
) {
    var showTimeDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = birthTime?.hour ?: 12,
        initialMinute = birthTime?.minute ?: 0,
        is24Hour = false,
    )
    var latText by remember { mutableStateOf(location?.latitude?.toString() ?: "") }
    var lonText by remember { mutableStateOf(location?.longitude?.toString() ?: "") }

    if (showTimeDialog) {
        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            containerColor = WarmSurface,
            titleContentColor = WarmInk,
            title = { Text("Birth time (optional)", color = WarmInk) },
            text = {
                Column {
                    Text(
                        "Set a precise birth time for exact Nakshatra / Rashi / Dasha.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    Spacer(Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                val haptic = LocalHapticFeedback.current
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimeDialog = false
                }) { Text("Set") }
            },
            dismissButton = {
                Row {
                    if (birthTime != null) {
                        TextButton(onClick = {
                            onTimeSelected(null)
                            showTimeDialog = false
                        }) { Text("Clear") }
                    }
                    TextButton(onClick = { showTimeDialog = false }) { Text("Cancel") }
                }
            },
        )
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            containerColor = WarmSurface,
            titleContentColor = WarmInk,
            title = { Text("Birth location (optional)", color = WarmInk) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Enter latitude and longitude for exact Lagna (Ascendant).",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    OutlinedTextField(
                        value = latText,
                        onValueChange = { latText = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                        label = { Text("Latitude (-90 to 90)", color = WarmInkDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmTeal,
                            unfocusedBorderColor = WarmInkDim,
                            focusedTextColor = WarmInk,
                            unfocusedTextColor = WarmInk,
                        ),
                    )
                    OutlinedTextField(
                        value = lonText,
                        onValueChange = { lonText = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                        label = { Text("Longitude (-180 to 180)", color = WarmInkDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmTeal,
                            unfocusedBorderColor = WarmInkDim,
                            focusedTextColor = WarmInk,
                            unfocusedTextColor = WarmInk,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lon = lonText.toDoubleOrNull()
                    if (lat != null && lon != null && lat in -90.0..90.0 && lon in -180.0..180.0) {
                        onLocationSelected(
                            com.willowvibe.agereveal.data.model.GeoLocation(latitude = lat, longitude = lon)
                        )
                    }
                    showLocationDialog = false
                }) { Text("Set") }
            },
            dismissButton = {
                Row {
                    if (location != null) {
                        TextButton(onClick = {
                            onLocationSelected(null)
                            latText = ""
                            lonText = ""
                            showLocationDialog = false
                        }) { Text("Clear") }
                    }
                    TextButton(onClick = { showLocationDialog = false }) { Text("Cancel") }
                }
            },
        )
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── Time chip ──────────────────────────────────────────────
            PrecisionChip(
                label = "TIME",
                value = birthTime?.format(DateTimeFormatter.ofPattern("h:mm a")) ?: "Add",
                isSet = birthTime != null,
                onClick = { showTimeDialog = true },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            // ── Location chip ──────────────────────────────────────────
            PrecisionChip(
                label = "LOCATION",
                value = location?.let { "%.1f°, %.1f°".format(it.latitude, it.longitude) } ?: "Add",
                isSet = location != null,
                onClick = { showLocationDialog = true },
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = WarmSurfaceSoft, thickness = 1.dp)
    }
}

@Composable
internal fun PrecisionChip(
    label: String,
    value: String,
    isSet: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(WarmSurface)
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = "$label: $value" }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = if (isSet) WarmTeal else WarmInkMute,
                fontWeight = if (isSet) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSet) WarmTeal.copy(alpha = 0.15f) else WarmSurfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Change $label",
                tint = if (isSet) WarmTeal else WarmInkMute,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Next milestone countdown chip (shown when the next milestone is within 30 days)
// ─────────────────────────────────────────────────────────────────────────────

private val MILESTONE_TARGETS_CALC = listOf(
    500, 1_000, 2_000, 3_000, 5_000, 7_000, 10_000, 12_500,
    15_000, 20_000, 25_000, 30_000,
)

@Composable
private fun NextMilestoneChip(result: AgeResult) {
    val days = result.totalDays
    val nextTarget = MILESTONE_TARGETS_CALC.firstOrNull { it > days } ?: return
    val daysAway = nextTarget - days
    if (daysAway > 30) return

    val formattedTarget = "%,d".format(nextTarget)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarmAmber.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("✦", color = WarmAmber, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "NEXT MILESTONE",
                style = MaterialTheme.typography.labelSmall,
                color = WarmAmber,
            )
            Text(
                "$formattedTarget days alive — in $daysAway day${if (daysAway == 1L) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = WarmInk,
            )
        }
        Text(
            "$daysAway",
            fontFamily = SerifFamily,
            fontSize = 22.sp,
            color = WarmAmber,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Banner ad view
// ─────────────────────────────────────────────────────────────────────────────

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

/**
 * Wraps [content] in an [AnimatedVisibility] with a staggered fade + slide entrance.
 * Used for the hero reveal on CalculatorScreen so elements appear sequentially.
 */
@Composable
private fun StaggeredEnter(
    delayMillis: Int,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(350, delayMillis = delayMillis, easing = FastOutSlowInEasing)) +
                slideInVertically(tween(350, delayMillis = delayMillis), { it / 5 }),
    ) {
        content()
    }
}
