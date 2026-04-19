package com.willowvibe.agereveal.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.willowvibe.agereveal.ui.viewmodel.RemindersViewModel
import androidx.appcompat.app.AppCompatDelegate

// Theme options data class
data class ThemeOption(
    val name: String,
    val mode: Int,
    val selected: Boolean
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: RemindersViewModel = hiltViewModel(),
) {
    val notificationHour by viewModel.notificationHour.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Warning: This will permanently delete all birthdays",
                    tint = WarmAmber,
                )
            },
            title = {
                Text(
                    "Clear all birthdays?",
                    fontFamily = SerifFamily,
                    color = WarmInk,
                )
            },
            text = {
                Text(
                    "This will permanently remove all saved birthdays and cancel their notifications.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkDim,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllBirthdays()
                    showClearDialog = false
                }) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = WarmInkDim)
                }
            },
            containerColor = WarmSurface,
            titleContentColor = WarmInk,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmBlack),
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WarmInk,
                )
            }
            Text(
                "Settings",
                style = MaterialTheme.typography.titleLarge,
                color = WarmInk,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Notifications ────────────────────────────────────────────────
            SettingsSection(title = "NOTIFICATIONS") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Birthday reminder time",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Notifications fire 1 day before each birthday at the selected time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    Spacer(Modifier.height(2.dp))
                    NotificationHourGrid(
                        currentHour = notificationHour,
                        onHourSelected = viewModel::setNotificationHour,
                    )
                }
            }

            // ── Data ─────────────────────────────────────────────────────────
            SettingsSection(title = "DATA") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(WarmSurfaceSoft)
                        .clickable { showClearDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "Clear all birthdays",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Remove all saved birthdays and their notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmInkDim,
                        )
                    }
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Clear all saved birthdays",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            SettingsSection(title = "ABOUT") {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    AboutRow(label = "Version", value = "0.9")
                    HorizontalDivider(
                        color = WarmSurfaceSoft,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                    AboutRow(label = "App", value = "AgeReveal")
                    HorizontalDivider(
                        color = WarmSurfaceSoft,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            "Made with ❤ in India",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmInkMute,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Appearance ────────────────────────────────────────────────────
            SettingsSection(title = "APPEARANCE") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "App theme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Choose how the app looks",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    Spacer(Modifier.height(8.dp))

                    // Theme selection buttons - using direct integer values
                    val themeOptions = listOf(
                        ThemeOption("System Default", -1, false),
                        ThemeOption("Light", 1, false),
                        ThemeOption("Dark", 2, false),
                    )

                    themeOptions.forEach { option ->
                        val isSelected = false // Always false since we can't track current mode easily
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WarmTeal.copy(alpha = 0.18f) else WarmSurfaceSoft)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) WarmTeal else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable {
                                    AppCompatDelegate.setDefaultNightMode(option.mode)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                option.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) WarmTeal else WarmInkMute,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = WarmInkDim,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WarmSurface)
                .padding(16.dp),
            content = content,
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkMute,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInk,
            fontWeight = FontWeight.Medium,
        )
    }
}

private val NOTIFICATION_HOUR_PRESETS = listOf(7, 8, 9, 10, 12, 17, 18, 21)

private fun formatHour(hour: Int): String {
    val suffix = if (hour < 12) "AM" else "PM"
    val display = when {
        hour == 0 || hour == 12 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$display $suffix"
}

@Composable
private fun NotificationHourGrid(currentHour: Int, onHourSelected: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NOTIFICATION_HOUR_PRESETS.chunked(4).forEach { rowHours ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowHours.forEach { hour ->
                    val isSelected = hour == currentHour
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) WarmTeal.copy(alpha = 0.18f) else WarmSurfaceSoft)
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) WarmTeal else Color.Transparent,
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable { onHourSelected(hour) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            formatHour(hour),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) WarmTeal else WarmInkMute,
                            fontSize = if (isSelected) 13.sp else 12.sp,
                        )
                    }
                }
            }
        }
    }
}
