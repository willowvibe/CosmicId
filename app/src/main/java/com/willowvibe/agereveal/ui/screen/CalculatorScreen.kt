package com.willowvibe.agereveal.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.key
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
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
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.ui.components.AgeBody
import com.willowvibe.agereveal.ui.components.AgeCard
import com.willowvibe.agereveal.ui.components.AgeLabel
import com.willowvibe.agereveal.ui.components.AgeValue
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmInkMute
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.domain.PlanetAgeCalculator
import com.willowvibe.agereveal.domain.ProfileDeepLinkGenerator
import com.willowvibe.agereveal.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    onShareCard: (ShareCardGenerator.CardTheme, ShareFormat) -> Unit,
    onOpenDetails: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val ticker by viewModel.tickerSeconds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var showThemePicker by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(ticker) { viewModel.onTick() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }

    if (showThemePicker) {
        ShareThemeSheet(
            onDismiss = { showThemePicker = false },
            onThemeSelected = { theme, format ->
                onShareCard(theme, format)
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Cosmic ID",
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmInk,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // LIVE chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(WarmSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WarmAmber),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmInkDim,
                        )
                    }
                    // Free trial chip
                    uiState.trialDaysRemaining?.let { days ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(WarmTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                "$days day${if (days == 1) "" else "s"} left",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmTeal,
                            )
                        }
                    }
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
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
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
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Hero stagger entrance — each block fades in with a slight delay
                        StaggeredEnter(delayMillis = 0) { ClockFaceHero(result) }
                        StaggeredEnter(delayMillis = 70) { SecondsStrip(result) }
                        StaggeredEnter(delayMillis = 140) { MiniStatRow(result) }

                        // v2.0: Progressive disclosure — rotating highlight cycles through insights
                        StaggeredEnter(delayMillis = 210) {
                            RotatingHighlightCard(
                                result = result,
                                fortune = uiState.dailyFortune,
                                name = uiState.name,
                                celebrityMatches = uiState.celebrityMatches,
                            )
                        }

                        StaggeredEnter(delayMillis = 280) {
                            val exploreHaptic = LocalHapticFeedback.current
                            AgeCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        role = Role.Button,
                                        onClick = {
                                            exploreHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onOpenDetails()
                                        },
                                    )
                                    .semantics { contentDescription = "Explore full cosmic profile" },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AgeBody(
                                        text = "Explore full profile →",
                                        color = WarmTeal,
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Explore",
                                        tint = WarmTeal,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }

                        StaggeredEnter(delayMillis = 300) {
                            val shareHaptic = LocalHapticFeedback.current
                            val copyHaptic = LocalHapticFeedback.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                AgeCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(
                                            role = Role.Button,
                                            onClick = {
                                                shareHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                showThemePicker = true
                                            },
                                        )
                                        .semantics { contentDescription = "Share card image" },
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AgeBody(
                                            text = "Share card",
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
                                AgeCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(
                                            role = Role.Button,
                                            onClick = {
                                                copyHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val link = ProfileDeepLinkGenerator.generate(
                                                    birthDate = result.birthDate,
                                                    name = uiState.name,
                                                    birthTime = uiState.birthTime,
                                                )
                                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(link))
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Profile link copied to clipboard")
                                                }
                                            },
                                        )
                                        .semantics { contentDescription = "Copy profile link" },
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        AgeBody(
                                            text = "Copy link",
                                            color = WarmInk,
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Copy link",
                                            tint = WarmTeal,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
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

            }

            // ── Banner ad ────────────────────────────────────────────────────
            HorizontalDivider(thickness = 1.dp, color = WarmSurfaceSoft)
            Spacer(Modifier.height(4.dp))
            BannerAdView(adUnitId = com.willowvibe.agereveal.ads.AdManager.BANNER_AD_UNIT_ID)
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
                AgeLabel(text = "BORN")
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
    AgeCard {
        Row(
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
                AgeLabel(text = "SECONDS ALIVE")
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
        for (index in formatted.indices) {
            val char = formatted[index]
            key(index) {
                if (char.isDigit()) {
                    AnimatedContent(
                        targetState = char,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn())
                                .togetherWith(
                                    slideOutVertically { height -> -height } + fadeOut()
                                )
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
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(WarmSurface)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(99.dp),
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AgeLabel(text = label)
        Spacer(Modifier.width(6.dp))
        AgeValue(
            text = value,
            accentColor = if (accent) WarmAmber else null,
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
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val context = androidx.compose.ui.platform.LocalContext.current
        val states = remember {
            runCatching {
                context.assets.open("indian_states_coords.json")
                    .bufferedReader()
                    .use { org.json.JSONArray(it.readText()) }
                    .let { array ->
                        List(array.length()) { i ->
                            val obj = array.getJSONObject(i)
                            IndianState(
                                name = obj.getString("name"),
                                lat = obj.getDouble("lat"),
                                lon = obj.getDouble("lon"),
                            )
                        }
                    }
            }.getOrElse { emptyList() }
        }
        var query by remember { mutableStateOf("") }
        val filtered = remember(query, states) {
            if (query.isBlank()) states else states.filter { it.name.contains(query, ignoreCase = true) }
        }

        ModalBottomSheet(
            onDismissRequest = { showLocationDialog = false },
            sheetState = sheetState,
            containerColor = WarmSurface,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(WarmInkDim),
                    )
                }
                Text(
                    "Birth location (optional)",
                    style = MaterialTheme.typography.titleLarge,
                    color = WarmInk,
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = WarmSurfaceSoft, thickness = 1.dp)
                Spacer(Modifier.height(12.dp))
                AgeBody(
                    text = "Select your Indian state for approximate Lagna. State centroids are used for calculation.",
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search state", color = WarmInkDim) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmTeal,
                        unfocusedBorderColor = WarmInkDim,
                        focusedTextColor = WarmInk,
                        unfocusedTextColor = WarmInk,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                val listState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(listState),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    filtered.forEach { state ->
                        val haptic = LocalHapticFeedback.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(WarmSurfaceSoft)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLocationSelected(
                                        com.willowvibe.agereveal.data.model.GeoLocation(
                                            latitude = state.lat,
                                            longitude = state.lon,
                                            label = state.name,
                                            isApproximate = true,
                                        )
                                    )
                                    showLocationDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                state.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmInk,
                            )
                            Text(
                                "${"%.1f".format(state.lat)}°, ${"%.1f".format(state.lon)}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmInkDim,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (location != null) {
                    TextButton(
                        onClick = {
                            onLocationSelected(null)
                            showLocationDialog = false
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text("Clear location", color = WarmInkMute)
                    }
                }
            }
        }
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
                value = location?.let {
                    val base = it.label.ifEmpty { "%.1f°, %.1f°".format(it.latitude, it.longitude) }
                    if (it.isApproximate) "$base *" else base
                } ?: "Add",
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
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
            )
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
            AgeLabel(text = label)
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
private fun TimeRemainingCard(timeRemaining: com.willowvibe.agereveal.domain.TimeRemainingCalculator.TimeRemaining) {
    AgeCard {
        AgeLabel(text = "TIME REMAINING", accentColor = WarmAmber)
        Spacer(Modifier.height(6.dp))
        AgeBody(
            text = "You have ${timeRemaining.weekends} weekends left until you're ${timeRemaining.targetAge}",
            color = WarmInk,
        )
        AgeBody(
            text = "That's ${timeRemaining.fridays} Fridays, ${timeRemaining.paychecks} paychecks, ${timeRemaining.fullMoons} full moons",
        )
    }
}

@Composable
private fun RetirementCard(retirement: com.willowvibe.agereveal.domain.RetirementCalculator.RetirementResult) {
    AgeCard {
        AgeLabel(text = "WORK LIFE", accentColor = WarmTeal)
        Spacer(Modifier.height(6.dp))
        AgeBody(
            text = "${retirement.workWeeksLeft} work weeks left until retirement at ${retirement.retirementAge}",
            color = WarmInk,
        )
        AgeBody(
            text = "${retirement.percentOfWorkLifeComplete}% of your work life is complete · ${retirement.daysUntilRetirement} days to go",
        )
    }
}

@Composable
private fun DailyFortuneCard(
    fortune: com.willowvibe.agereveal.domain.DailyFortuneGenerator.Fortune,
    onShare: () -> Unit,
) {
    AgeCard(modifier = Modifier.clickable { onShare() }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AgeLabel(text = "DAILY COSMIC FORTUNE")
            Text(
                fortune.emoji,
                fontSize = 20.sp,
            )
        }
        Spacer(Modifier.height(6.dp))
        AgeBody(
            text = fortune.headline,
            color = WarmInk,
        )
        AgeBody(
            text = fortune.body,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AgeValue(
                text = "Lucky #${fortune.luckyNumber}",
                accentColor = WarmTeal,
            )
            AgeValue(
                text = fortune.luckyColor,
                accentColor = WarmTeal,
            )
            AgeBody(text = fortune.moonPhase)
        }
    }
}

private data class IndianState(val name: String, val lat: Double, val lon: Double)

// ─────────────────────────────────────────────────────────────────────────────
// Rotating highlight card — cycles through fortune / milestone / planet age / celebrity
// ─────────────────────────────────────────────────────────────────────────────

private enum class HighlightType { MILESTONE, FORTUNE, PLANET_AGE, CELEBRITY }

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
private fun RotatingHighlightCard(
    result: AgeResult,
    fortune: com.willowvibe.agereveal.domain.DailyFortuneGenerator.Fortune?,
    name: String,
    celebrityMatches: List<com.willowvibe.agereveal.data.model.CelebrityMatch>,
) {
    val hasFortune = fortune != null
    val hasCelebrities = celebrityMatches.isNotEmpty()
    val highlights = remember(hasFortune, hasCelebrities) {
        buildList {
            add(HighlightType.MILESTONE)
            if (hasFortune) add(HighlightType.FORTUNE)
            add(HighlightType.PLANET_AGE)
            if (hasCelebrities) add(HighlightType.CELEBRITY)
        }
    }
    var index by remember { mutableIntStateOf(0) }
    // Clamp index when list shrinks (e.g. fortune goes null)
    if (index >= highlights.size) index = 0
    val current = highlights[index]

    LaunchedEffect(highlights.size) {
        while (true) {
            delay(4_000L)
            index = (index + 1) % highlights.size
        }
    }

    AnimatedContent(
        targetState = current,
        transitionSpec = {
            (fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 })
                .togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it / 4 })
        },
        label = "rotating_highlight",
    ) { highlight ->
        when (highlight) {
            HighlightType.MILESTONE -> MilestoneHighlight(result)
            HighlightType.FORTUNE -> FortuneHighlight(fortune)
            HighlightType.PLANET_AGE -> PlanetAgeHighlight(result, name)
            HighlightType.CELEBRITY -> CelebrityHighlight(celebrityMatches)
        }
    }
}

@Composable
private fun MilestoneHighlight(result: AgeResult) {
    val days = result.totalDays
    val nextTarget = MILESTONE_TARGETS_CALC.firstOrNull { it > days } ?: return
    val daysAway = nextTarget - days
    val formattedTarget = "%,d".format(nextTarget)
    AgeCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✦", color = WarmAmber, fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgeLabel(text = "NEXT MILESTONE", accentColor = WarmAmber)
                AgeBody(
                    text = "$formattedTarget days alive — in $daysAway day${if (daysAway == 1L) "" else "s"}",
                    color = WarmInk,
                )
            }
            AgeValue(text = "$daysAway", accentColor = WarmAmber)
        }
    }
}

@Composable
private fun FortuneHighlight(
    fortune: com.willowvibe.agereveal.domain.DailyFortuneGenerator.Fortune?,
) {
    if (fortune == null) return
    AgeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AgeLabel(text = "DAILY COSMIC FORTUNE")
            Text(fortune.emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.height(6.dp))
        AgeBody(text = fortune.headline, color = WarmInk)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AgeValue(text = "Lucky #${fortune.luckyNumber}", accentColor = WarmTeal)
            AgeValue(text = fortune.luckyColor, accentColor = WarmTeal)
        }
    }
}

@Composable
private fun PlanetAgeHighlight(result: AgeResult, name: String) {
    val calculator = remember { PlanetAgeCalculator() }
    val planetAge = remember(result.totalSeconds) {
        calculator.calculatePlanetAges(result.years + result.months / 12.0)
            .firstOrNull { it.planet == com.willowvibe.agereveal.domain.Planet.MARS }
    }
    if (planetAge == null) return
    val displayName = name.ifEmpty { "You" }
    val formatted = calculator.formatPlanetAge(planetAge.ageYears)
    AgeCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(com.willowvibe.agereveal.domain.Planet.MARS.emoji, fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgeLabel(text = "PLANET AGE")
                AgeBody(
                    text = "On Mars, $displayName is only $formatted ${if (planetAge.ageYears > 1.0) "years" else "year"} old",
                    color = WarmInk,
                )
            }
        }
    }
}

@Composable
private fun CelebrityHighlight(
    matches: List<com.willowvibe.agereveal.data.model.CelebrityMatch>,
) {
    if (matches.isEmpty()) {
        CelebrityHighlightPlaceholder()
        return
    }
    val match = matches.first()
    val categoryLabel = match.category.lowercase().replaceFirstChar { it.uppercase() }
    AgeCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgeLabel(text = "CELEBRITY MATCH")
                AgeBody(
                    text = "${match.name} · $categoryLabel",
                    color = WarmInk,
                )
                AgeBody(
                    text = "Born ${match.birthDate.year}",
                    color = WarmInkMute,
                )
            }
        }
    }
}

@Composable
private fun CelebrityHighlightPlaceholder() {
    AgeCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⭐", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                AgeLabel(text = "CELEBRITY MATCH")
                AgeBody(
                    text = "Coming soon — discover who shares your birthday",
                    color = WarmInkMute,
                )
            }
        }
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
            onRelease = { it.destroy() },
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
