package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import com.willowvibe.agereveal.data.repository.BirthdayRepository
import com.willowvibe.agereveal.notification.BirthdayNotificationScheduler
import com.willowvibe.agereveal.notification.MilestoneNotificationScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import com.willowvibe.agereveal.util.BirthdayCsvExporter
import com.willowvibe.agereveal.util.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the consolidated Settings screen.
 * Exposes theme, language, notifications, and milestone preferences — plus the
 * CSV export utility.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPrefs: UserPreferencesRepository,
    private val repository: BirthdayRepository,
    private val birthdayScheduler: BirthdayNotificationScheduler,
    private val milestoneScheduler: MilestoneNotificationScheduler,
    private val csvExporter: BirthdayCsvExporter,
) : ViewModel() {

    val themeMode: StateFlow<Int> = userPrefs.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesRepository.THEME_SYSTEM)

    val languageTag: StateFlow<String> = userPrefs.languageTag
        .stateIn(viewModelScope, SharingStarted.Eagerly, "system")

    val notificationsEnabled: StateFlow<Boolean> = userPrefs.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Expose notification hour from BirthdayNotificationScheduler
    val notificationHour: StateFlow<Int> = birthdayScheduler.notificationHour

    val birthdays: StateFlow<List<SavedBirthday>> = repository.allBirthdays
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun milestoneEnabled(target: Int): Flow<Boolean> = userPrefs.milestoneEnabled(target)

    fun setTheme(mode: Int) = viewModelScope.launch { userPrefs.setThemeMode(mode) }

    fun setLanguage(tag: String) {
        viewModelScope.launch { userPrefs.setLanguageTag(tag) }
        LocaleManager.apply(tag)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.setNotificationsEnabled(enabled)
            val list = birthdays.value
            if (enabled) {
                list.filter { it.notifyEnabled }.forEach {
                    birthdayScheduler.scheduleFor(it.id, it.name, it.birthDate)
                }
            } else {
                list.forEach { birthdayScheduler.cancel(it.id) }
                milestoneScheduler.cancelAll()
            }
        }
    }

    fun setNotificationHour(hour: Int) {
        birthdayScheduler.setNotificationHour(hour)
    }

    fun setMilestoneEnabled(target: Int, enabled: Boolean) {
        viewModelScope.launch { userPrefs.setMilestoneEnabled(target, enabled) }
    }

    fun clearAllBirthdays() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val list = birthdays.first()
            csvExporter.export(list)
        }
    }

    val combinedState: Flow<Triple<Int, String, Boolean>> = combine(
        userPrefs.themeMode, userPrefs.languageTag, userPrefs.notificationsEnabled,
    ) { t, l, n -> Triple(t, l, n) }
}
