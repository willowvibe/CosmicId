package com.willowvibe.agereveal.ui.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.willowvibe.agereveal.R
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import com.willowvibe.agereveal.notification.MilestoneNotificationScheduler
import com.willowvibe.agereveal.ui.theme.SerifFamily
import com.willowvibe.agereveal.ui.theme.WarmAmber
import com.willowvibe.agereveal.ui.theme.WarmBlack
import com.willowvibe.agereveal.ui.theme.WarmInk
import com.willowvibe.agereveal.ui.theme.WarmInkDim
import com.willowvibe.agereveal.ui.theme.WarmInkMute
import com.willowvibe.agereveal.ui.theme.WarmSurface
import com.willowvibe.agereveal.ui.theme.WarmSurfaceSoft
import com.willowvibe.agereveal.ui.theme.WarmTeal
import com.willowvibe.agereveal.ui.viewmodel.SettingsViewModel

private const val PRIVACY_POLICY_URL = "https://willowvibe.com/agereveal/privacy"

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by settingsViewModel.themeMode.collectAsState()
    val languageTag by settingsViewModel.languageTag.collectAsState()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val notificationHour by settingsViewModel.notificationHour.collectAsState()
    val targetAge by settingsViewModel.targetAge.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = stringResource(R.string.cd_warning),
                    tint = WarmAmber,
                )
            },
            title = {
                Text(
                    stringResource(R.string.clear_all_confirm_title),
                    fontFamily = SerifFamily,
                    color = WarmInk,
                )
            },
            text = {
                Text(
                    stringResource(R.string.clear_all_confirm_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkDim,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.clearAllBirthdays()
                    showClearDialog = false
                }) {
                    Text(
                        stringResource(R.string.clear_all_action),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel), color = WarmInkDim)
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
                    contentDescription = stringResource(R.string.cd_back),
                    tint = WarmInk,
                )
            }
            Text(
                stringResource(R.string.settings_title),
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
            SettingsSection(title = stringResource(R.string.section_notifications)) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Global notifications toggle
                    SwitchRow(
                        title = stringResource(R.string.enable_birthday_notifications),
                        subtitle = stringResource(R.string.enable_birthday_notifications_desc),
                        checked = notificationsEnabled,
                        onCheckedChange = settingsViewModel::setNotificationsEnabled,
                    )

                    HorizontalDivider(color = WarmSurfaceSoft)

                    Text(
                        stringResource(R.string.reminder_time_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.reminder_time_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    NotificationHourGrid(
                        currentHour = notificationHour,
                        onHourSelected = settingsViewModel::setNotificationHour,
                    )
                }
            }

            // ── Milestone toggles ────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.section_milestones)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(R.string.milestones_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    MilestoneToggleGrid(
                        settingsViewModel = settingsViewModel,
                    )
                }
            }

            // ── Appearance ────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.section_appearance)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.app_theme),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.choose_theme_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    Spacer(Modifier.height(4.dp))

                    val themeOptions = listOf(
                        UserPreferencesRepository.THEME_SYSTEM to stringResource(R.string.theme_system),
                        UserPreferencesRepository.THEME_LIGHT to stringResource(R.string.theme_light),
                        UserPreferencesRepository.THEME_DARK to stringResource(R.string.theme_dark),
                    )

                    themeOptions.forEach { (mode, label) ->
                        val isSelected = mode == themeMode
                        OptionRow(
                            label = label,
                            isSelected = isSelected,
                            onClick = { settingsViewModel.setTheme(mode) },
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = WarmSurfaceSoft)
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Lifespan target",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Target age for the lifespan progress widget",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(30, 40, 50, 60, 70, 80, 90, 100).forEach { age ->
                            val selected = age == targetAge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) WarmTeal else WarmSurfaceSoft)
                                    .clickable { settingsViewModel.setTargetAge(age) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "$age",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) WarmBlack else WarmInk,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }

            // ── Language ───────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.section_language)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.language_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.language_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmInkDim,
                    )
                    Spacer(Modifier.height(4.dp))

                    val languageOptions = listOf(
                        "system" to stringResource(R.string.theme_system),
                        "en" to stringResource(R.string.language_english),
                        "hi" to stringResource(R.string.language_hindi),
                    )
                    languageOptions.forEach { (tag, label) ->
                        OptionRow(
                            label = label,
                            isSelected = tag == languageTag,
                            onClick = { settingsViewModel.setLanguage(tag) },
                        )
                    }
                }
            }

            // ── Data ─────────────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.section_data)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionRow(
                        title = stringResource(R.string.export_csv_title),
                        subtitle = stringResource(R.string.export_csv_desc),
                        icon = Icons.Default.FileDownload,
                        tint = WarmTeal,
                        onClick = settingsViewModel::exportCsv,
                    )
                    HorizontalDivider(color = WarmSurfaceSoft)
                    ActionRow(
                        title = stringResource(R.string.clear_all_title),
                        subtitle = stringResource(R.string.clear_all_desc),
                        icon = Icons.Default.Delete,
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { showClearDialog = true },
                        destructive = true,
                    )
                }
            }

            // ── About ─────────────────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.section_about)) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    AboutRow(label = stringResource(R.string.version_label), value = com.willowvibe.agereveal.BuildConfig.VERSION_NAME)
                    HorizontalDivider(color = WarmSurfaceSoft, modifier = Modifier.padding(vertical = 10.dp))
                    AboutRow(label = stringResource(R.string.app_label), value = stringResource(R.string.app_short_name))
                    HorizontalDivider(color = WarmSurfaceSoft, modifier = Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                                runCatching { context.startActivity(intent) }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.privacy_policy),
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmTeal,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    HorizontalDivider(color = WarmSurfaceSoft, modifier = Modifier.padding(vertical = 10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text(
                            stringResource(R.string.made_with_love),
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmInkMute,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MilestoneToggleGrid(settingsViewModel: SettingsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MilestoneNotificationScheduler.MILESTONE_TARGETS.chunked(3).forEach { rowTargets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowTargets.forEach { target ->
                    val enabled by settingsViewModel.milestoneEnabled(target).collectAsState(initial = true)
                    MilestoneChip(
                        target = target,
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                        onToggle = { settingsViewModel.setMilestoneEnabled(target, !enabled) },
                    )
                }
                // fill remaining weight slots so last row isn't stretched
                repeat(3 - rowTargets.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MilestoneChip(
    target: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
) {
    val formatted = "%,d".format(target)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (enabled) WarmTeal.copy(alpha = 0.18f) else WarmSurfaceSoft)
            .border(
                width = if (enabled) 1.5.dp else 0.dp,
                color = if (enabled) WarmTeal else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable { onToggle() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            formatted,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (enabled) FontWeight.SemiBold else FontWeight.Normal,
            color = if (enabled) WarmTeal else WarmInkMute,
            fontSize = 12.sp,
        )
    }
}

/** Collect flow with initial value — compact helper for composables. */
@Composable
@Suppress("unused")
private fun <T> kotlinx.coroutines.flow.Flow<T>.collectAsStateInitial(initial: T) =
    collectAsState(initial = initial)


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
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = WarmInk, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WarmInkDim)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WarmBlack,
                checkedTrackColor = WarmTeal,
                uncheckedThumbColor = WarmInkDim,
                uncheckedTrackColor = WarmSurfaceSoft,
            ),
        )
    }
}

@Composable
private fun OptionRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) WarmTeal else WarmInkMute,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(WarmSurfaceSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (destructive) MaterialTheme.colorScheme.error else WarmInk,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WarmInkDim)
        }
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = WarmInkMute)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = WarmInk, fontWeight = FontWeight.Medium)
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
