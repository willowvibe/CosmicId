package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.ui.viewmodel.RemindersViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Screen 4 — Saved Birthdays & Reminders.
 *
 * List ordered by next upcoming birthday (soonest first).
 * FAB opens an "Add birthday" bottom sheet (TODO).
 * Swipe-to-delete or delete icon removes and cancels the notification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel = hiltViewModel(),
    onAddBirthday: () -> Unit,
) {
    val birthdays by viewModel.birthdays.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Birthdays & Reminders") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBirthday) {
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
                Text(birthday.birthDate.toString(),
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
