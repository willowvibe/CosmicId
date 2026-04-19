package com.willowvibe.agereveal.ui.screen

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmAmberDeep
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmInkMute
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.viewmodel.RemindersViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val birthdays by viewModel.birthdays.collectAsState()
    val notificationHour by viewModel.notificationHour.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (showAddSheet) {
        AddBirthdaySheet(
            sheetState = sheetState,
            onDismiss = { showAddSheet = false },
            onSave = { name, date, emoji ->
                viewModel.addBirthday(name, date, emoji)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
            },
        )
    }

    if (showSettingsSheet) {
        NotificationSettingsSheet(
            sheetState = settingsSheetState,
            currentHour = notificationHour,
            onDismiss = { showSettingsSheet = false },
            onHourSelected = { hour ->
                viewModel.setNotificationHour(hour)
                scope.launch { settingsSheetState.hide() }.invokeOnCompletion { showSettingsSheet = false }
            },
        )
    }

    val today = LocalDate.now()
    val sorted = birthdays.sortedBy {
        ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(it.nextBirthdayEpochDay))
    }
    val nextUp = sorted.firstOrNull()
    val later = if (sorted.size > 1) sorted.drop(1) else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack),
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Birthdays", style = MaterialTheme.typography.titleLarge, color = WarmInk)
                if (birthdays.isNotEmpty()) {
                    val daysUntilNext = nextUp?.let {
                        ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(it.nextBirthdayEpochDay))
                    }
                    Text(
                        text = when {
                            daysUntilNext == null -> ""
                            daysUntilNext == 0L -> "${birthdays.size} saved · birthday today!"
                            else -> "${birthdays.size} saved · next in ${daysUntilNext}d"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkMute,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WarmSurface),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = { showSettingsSheet = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Notification settings",
                            tint = WarmInkMute,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WarmTeal),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(onClick = { showAddSheet = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add birthday",
                            tint = WarmBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (birthdays.isEmpty()) {
            EmptyBirthdaysState(modifier = Modifier.weight(1f))
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                // ── Next-up hero ─────────────────────────────────────────────
                if (nextUp != null) {
                    item {
                        val daysUntil =
                            ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(nextUp.nextBirthdayEpochDay))
                        NextUpHeroCard(birthday = nextUp, daysUntil = daysUntil)
                        Spacer(Modifier.height(14.dp))
                    }
                }

                // ── Coming up ────────────────────────────────────────────────
                if (later.isNotEmpty()) {
                    item {
                        Text(
                            "COMING UP",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmInkDim,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    items(later, key = { it.id }) { birthday ->
                        val daysUntil =
                            ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(birthday.nextBirthdayEpochDay))

                        val userBirthDate = remember {
                            val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
                            prefs.getString("birth_date", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                        }
                        val compatibility = remember(userBirthDate, birthday) {
                            userBirthDate?.let { birthDate ->
                                viewModel.getCompatibilityWithSavedBirthday(birthday)
                            }
                        }

                        BirthdayTimelineRow(
                            birthday = birthday,
                            daysUntil = daysUntil,
                            onDelete = { viewModel.deleteBirthday(birthday) },
                            onToggleNotification = { viewModel.toggleNotification(birthday) },
                            compatibilityResult = compatibility,
                        )
                    }
                } else if (nextUp != null) {
                    item {
                        // Only one birthday saved
                        val daysUntil =
                            ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(nextUp.nextBirthdayEpochDay))

                        val userBirthDate = remember {
                            val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
                            prefs.getString("birth_date", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                        }
                        val compatibility = remember(userBirthDate, nextUp) {
                            userBirthDate?.let { birthDate ->
                                viewModel.getCompatibilityWithSavedBirthday(nextUp)
                            }
                        }

                        BirthdayTimelineRow(
                            birthday = nextUp,
                            daysUntil = daysUntil,
                            onDelete = { viewModel.deleteBirthday(nextUp) },
                            onToggleNotification = { viewModel.toggleNotification(nextUp) },
                            compatibilityResult = compatibility,
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Next-up hero card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NextUpHeroCard(birthday: SavedBirthday, daysUntil: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(WarmAmberDeep, WarmAmber.copy(alpha = 0.6f)),
                ),
            )
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = if (daysUntil == 0L) "TODAY" else "UP NEXT · IN ${daysUntil}D",
                style = MaterialTheme.typography.labelSmall,
                color = WarmInk.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(birthday.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    birthday.name,
                    fontFamily = SerifFamily,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp,
                    color = WarmInk,
                )
            }
            Spacer(Modifier.height(4.dp))
            val nextDate = LocalDate.ofEpochDay(birthday.nextBirthdayEpochDay)
            val today = LocalDate.now()
            val turningAge = today.year - birthday.birthDate.year +
                    if (nextDate.year > today.year) 1 else 0
            Text(
                "${nextDate.format(DateTimeFormatter.ofPattern("EEE, d MMMM"))} · turning $turningAge",
                style = MaterialTheme.typography.bodySmall,
                color = WarmInk.copy(alpha = 0.9f),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Birthday timeline row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BirthdayTimelineRow(
    birthday: SavedBirthday,
    daysUntil: Long,
    onDelete: () -> Unit,
    onToggleNotification: () -> Unit,
    compatibilityResult: CompatibilityResult? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(birthday.emoji, fontSize = 22.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                birthday.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = WarmInk
            )
            Text(
                text = when {
                    daysUntil == 0L -> "Birthday today!"
                    else -> "in ${daysUntil}d"
                },
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                color = if (daysUntil <= 7) WarmAmber else WarmInkDim,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            val nextDate = LocalDate.ofEpochDay(birthday.nextBirthdayEpochDay)
            Text(
                if (daysUntil == 0L) "Today! 🎂" else nextDate.format(DateTimeFormatter.ofPattern("MMM d")),
                fontFamily = SerifFamily,
                fontSize = 15.sp,
                letterSpacing = (-0.3).sp,
                color = if (daysUntil == 0L) WarmAmber else WarmInkMute,
            )
        }
        IconButton(onClick = onToggleNotification, modifier = Modifier.size(32.dp)) {
            Icon(
                if (birthday.notifyEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = "Toggle notification",
                tint = if (birthday.notifyEnabled) WarmTeal else WarmInkDim,
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = WarmInkDim,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(WarmSurfaceSoft))

    compatibilityResult?.let {
        CompatibilityResultRow(result = it)
    }
}

@Composable
private fun CompatibilityResultRow(result: CompatibilityResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurface)
            .padding(16.dp),
    ) {
        Text(
            "COMPATIBILITY",
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Cosmic Match",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarmInk,
            )
            Text(
                "${result.overallScore}%",
                fontFamily = SerifFamily,
                fontSize = 24.sp,
                color = WarmTeal,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            result.headline,
            style = MaterialTheme.typography.bodySmall,
            color = WarmInkMute,
            fontFamily = SerifFamily,
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatChip(label = "Western", value = "${result.westernScore}%", modifier = Modifier.weight(1f))
            StatChip(label = "Chinese", value = "${result.chineseScore}%", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WarmSurfaceSoft)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = WarmInk,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyBirthdaysState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎂", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No birthdays yet",
            fontFamily = SerifFamily,
            fontSize = 20.sp,
            color = WarmInkMute,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Tap + to add a friend or family member.",
            style = MaterialTheme.typography.bodySmall,
            color = WarmInkDim,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Birthday Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Notification Settings Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

private val HOUR_PRESETS = listOf(7, 8, 9, 10, 12, 17, 18, 21)

private fun formatHour(hour: Int): String {
    val suffix = if (hour < 12) "AM" else "PM"
    val display = when {
        hour == 0 || hour == 12 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$display $suffix"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationSettingsSheet(
    sheetState: androidx.compose.material3.SheetState,
    currentHour: Int,
    onDismiss: () -> Unit,
    onHourSelected: (Int) -> Unit,
) {
    var selected by remember(currentHour) { mutableIntStateOf(currentHour) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmSurface,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Reminder Time",
                fontFamily = SerifFamily,
                fontSize = 22.sp,
                color = WarmInk,
                fontWeight = FontWeight.Normal,
            )
            Text(
                "Notifications fire 1 day before each birthday at the time you choose.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkDim,
            )

            // Hour preset chips
            val rows = HOUR_PRESETS.chunked(4)
            rows.forEach { rowHours ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowHours.forEach { hour ->
                        val isSelected = hour == selected
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WarmTeal.copy(alpha = 0.2f) else WarmSurfaceSoft)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) WarmTeal else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { selected = hour }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                formatHour(hour),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) WarmTeal else WarmInkMute,
                            )
                        }
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onHourSelected(selected) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmTeal,
                    contentColor = WarmBlack,
                ),
            ) {
                Text("Save — ${formatHour(selected)}", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private val EMOJI_OPTIONS = listOf("🎂", "🎉", "🎈", "❤️", "⭐", "🌟", "🌷", "✨", "🚀", "🎸", "👶", "🐾")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBirthdaySheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onSave: (name: String, date: LocalDate, emoji: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEmoji by remember { mutableStateOf("🎂") }
    var nameError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now()
            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        if (picked.isAfter(LocalDate.now())) dateError = true
                        else {
                            selectedDate = picked
                            dateError = false
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = datePickerState) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WarmSurface,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Add Birthday",
                fontFamily = SerifFamily,
                fontSize = 22.sp,
                color = WarmInk,
                fontWeight = FontWeight.Normal,
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Name", color = WarmInkMute) },
                isError = nameError,
                supportingText = if (nameError) ({ Text("Name is required") }) else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmTeal,
                    unfocusedBorderColor = WarmSurfaceSoft,
                    focusedTextColor = WarmInk,
                    unfocusedTextColor = WarmInk,
                    cursorColor = WarmTeal,
                ),
            )

            Column {
                Text(
                    "PICK AN EMOJI",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmInkDim,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EMOJI_OPTIONS.size) { idx ->
                        val emoji = EMOJI_OPTIONS[idx]
                        val isSelected = emoji == selectedEmoji
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WarmTeal.copy(alpha = 0.25f) else WarmSurfaceSoft)
                                .then(if (isSelected) Modifier else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            TextButton(onClick = { selectedEmoji = emoji }, contentPadding = PaddingValues(0.dp)) {
                                Text(emoji, fontSize = 22.sp)
                            }
                        }
                    }
                }
            }

            // Date picker trigger
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = if (dateError) WarmSurfaceSoft else WarmSurfaceSoft,
                ),
                shape = RoundedCornerShape(12.dp),
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
                            "DATE OF BIRTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (dateError) MaterialTheme.colorScheme.error else WarmInkDim,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (dateError) "Date cannot be in the future"
                            else selectedDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                                ?: "Tap to select",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (dateError) MaterialTheme.colorScheme.error
                            else if (selectedDate != null) WarmInk
                            else WarmInkMute,
                        )
                    }
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Select date",
                        tint = if (dateError) MaterialTheme.colorScheme.error else WarmTeal,
                    )
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val date = selectedDate ?: run {
                        dateError = true
                        return@Button
                    }
                    if (date.isAfter(LocalDate.now())) {
                        dateError = true
                        return@Button
                    }
                    onSave(name.trim(), date, selectedEmoji)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmTeal,
                    contentColor = WarmBlack,
                ),
            ) {
                Text("Save Birthday", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
