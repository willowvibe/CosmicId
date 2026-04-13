package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.ui.viewmodel.RemindersViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

/**
 * Screen 4 — Saved Birthdays & Reminders.
 *
 * List ordered by next upcoming birthday (soonest first).
 * FAB opens an "Add birthday" bottom sheet.
 * Delete icon removes and cancels the notification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel(),
    onAddBirthday: () -> Unit,
) {
    val birthdays by viewModel.birthdays.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Birthdays & Reminders") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add birthday")
            }
        },
    ) { padding ->
        if (birthdays.isEmpty()) {
            EmptyRemindersState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(birthdays, key = { it.id }) { birthday ->
                    BirthdayReminderCard(
                        birthday = birthday,
                        onDelete = { viewModel.deleteBirthday(birthday) },
                        onToggleNotification = { viewModel.toggleNotification(birthday) },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Add Birthday Bottom Sheet
// ---------------------------------------------------------------------------

/** Quick-pick emoji options displayed as chips in the sheet. */
private val EMOJI_OPTIONS = listOf("🎂", "🎉", "🎈", "❤️", "⭐", "🌟", "👶", "👴", "👵", "🐾")

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
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli(),
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        if (picked.isAfter(LocalDate.now())) {
                            dateError = true
                        } else {
                            selectedDate = picked
                            dateError = false
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Add Birthday", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Name") },
                isError = nameError,
                supportingText = if (nameError) ({ Text("Name is required") }) else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Emoji row
            Text("Pick an emoji", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(EMOJI_OPTIONS.size) { idx ->
                    val emoji = EMOJI_OPTIONS[idx]
                    val isSelected = emoji == selectedEmoji
                    Card(
                        modifier = Modifier
                            .padding(vertical = 2.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        onClick = { selectedEmoji = emoji },
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }

            // Date picker trigger
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true },
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = if (dateError) MaterialTheme.colorScheme.errorContainer
                                     else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Date of Birth", style = MaterialTheme.typography.labelSmall,
                            color = if (dateError) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (dateError) "Date cannot be in the future"
                                   else selectedDate
                                       ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                                       ?: "Tap to select",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (dateError) MaterialTheme.colorScheme.error
                                    else if (selectedDate != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Default.CalendarMonth, contentDescription = null,
                        tint = if (dateError) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.primary)
                }
            }

            // Save button
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    val date = selectedDate ?: LocalDate.now()
                    if (date.isAfter(LocalDate.now())) { dateError = true; return@Button }
                    onSave(name.trim(), date, selectedEmoji)
                },
            ) {
                Text("Save Birthday")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// List item
// ---------------------------------------------------------------------------

@Composable
private fun BirthdayReminderCard(
    birthday: SavedBirthday,
    onDelete: () -> Unit,
    onToggleNotification: () -> Unit,
) {
    val today = LocalDate.now()
    val daysUntil = ChronoUnit.DAYS.between(today, LocalDate.ofEpochDay(birthday.nextBirthdayEpochDay))

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${birthday.emoji}  ${birthday.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(birthday.birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (daysUntil == 0L) "🎂 Birthday today!" else "in $daysUntil days",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (daysUntil <= 7) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onToggleNotification) {
                Icon(
                    if (birthday.notifyEnabled) Icons.Default.Notifications
                    else Icons.Default.NotificationsOff,
                    contentDescription = "Toggle notification",
                    tint = if (birthday.notifyEnabled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyRemindersState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("🎂", style = MaterialTheme.typography.displayMedium)
        Text("No birthdays saved yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Tap + to add a friend or family member.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
