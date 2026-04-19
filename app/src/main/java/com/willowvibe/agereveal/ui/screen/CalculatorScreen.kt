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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
            .background(WarmBlack),
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
                // ── Birth anchor ─────────────────────────────────────────────
                BirthAnchorRow(
                    selectedDate = uiState.birthDate,
                    onDateSelected = viewModel::onBirthDateSelected,
                )

                uiState.result?.let { result ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(400, easing = FastOutSlowInEasing)) +
                                slideInVertically(tween(400), { it / 4 }),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            // ── Clock-face trio ───────────────────────────────
                            ClockFaceHero(result)

                            // ── Seconds alive strip ───────────────────────────
                            SecondsStrip(result)

                            // ── Mini stat chips ───────────────────────────────
                            MiniStatRow(result)

                            // ── Teased cosmic profile ─────────────────────────
                            TeasedDetails(
                                result = result,
                                isUnlocked = uiState.isUnlocked,
                                onReveal = onUnlockMore,
                                onShare = { showThemePicker = true },
                            )
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

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
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
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(WarmSurface),
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
private fun ClockFaceHero(result: AgeResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Start,
    ) {
        AgeNumeral(value = result.years.toString(), unit = "years", large = true, modifier = Modifier.weight(1.3f))
        AgeNumeral(value = result.months.toString().padStart(2, '0'), unit = "months", large = false, modifier = Modifier.weight(1f))
        AgeNumeral(value = result.days.toString().padStart(2, '0'), unit = "days", large = false, modifier = Modifier.weight(1f))
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
            fontWeight = FontWeight.Normal,
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
private fun SecondsStrip(result: AgeResult) {
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
            Text(
                text = "%,d".format(result.totalSeconds),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp,
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

// ─────────────────────────────────────────────────────────────────────────────
// Mini stat chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MiniStatRow(result: AgeResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MiniStatChip(label = "DAYS", value = "%,d".format(result.totalDays), modifier = Modifier.weight(1f))
        MiniStatChip(label = "HOURS", value = formatCompactNumber(result.totalHours), modifier = Modifier.weight(1f))
        MiniStatChip(label = "NEXT BDAY", value = "${result.daysToNextBirthday}d", accent = true, modifier = Modifier.weight(1f))
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
    n >= 1_000     -> "%.1fK".format(n / 1_000.0)
    else           -> n.toString()
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "YOUR VEDIC & COSMIC PROFILE",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInkDim,
            )
            if (!isUnlocked) {
                Text(
                    "Reveal →",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = WarmTeal,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onReveal() },
                )
            } else {
                Text(
                    "Share ↗",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = WarmTeal,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onShare() },
                )
            }
        }

        Spacer(Modifier.height(10.dp))

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
                TeaseChip("Heartbeats", if (result.estimatedHeartbeats > 0) formatHeartbeats(result.estimatedHeartbeats) else "~1.0 B", modifier = Modifier.weight(1f))
            }
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
    n >= 1_000_000     -> "%.1f M".format(n / 1_000_000.0)
    else               -> "%,d".format(n)
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
