package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.data.repository.BirthdayRepository
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.domain.ZodiacCompatibilityCalculator
import com.willowvibe.agereveal.notification.BirthdayNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import android.content.Context

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val repository: BirthdayRepository,
    private val notificationScheduler: BirthdayNotificationScheduler,
    private val compatibilityCalculator: ZodiacCompatibilityCalculator,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val birthdays: StateFlow<List<SavedBirthday>> = repository.allBirthdays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notificationHour: StateFlow<Int> = notificationScheduler.notificationHour

    fun addBirthday(name: String, birthDate: LocalDate, emoji: String = "🎂") {
        if (birthDate.isAfter(LocalDate.now())) return
        viewModelScope.launch {
            val id = repository.save(
                SavedBirthday(name = name, birthDate = birthDate, emoji = emoji)
            )
            notificationScheduler.scheduleFor(id, name, birthDate)
        }
    }

    fun deleteBirthday(birthday: SavedBirthday) {
        viewModelScope.launch {
            repository.delete(birthday)
            notificationScheduler.cancel(birthday.id)
        }
    }

    fun toggleNotification(birthday: SavedBirthday) {
        viewModelScope.launch {
            val updated = birthday.copy(notifyEnabled = !birthday.notifyEnabled)
            repository.update(updated)
            if (updated.notifyEnabled) {
                notificationScheduler.scheduleFor(updated.id, updated.name, updated.birthDate)
            } else {
                notificationScheduler.cancel(updated.id)
            }
        }
    }

    fun clearAllBirthdays() {
        viewModelScope.launch {
            birthdays.value.forEach { notificationScheduler.cancel(it.id) }
            repository.deleteAll()
        }
    }

    fun setNotificationHour(hour: Int) {
        notificationScheduler.setNotificationHour(hour)
        // Reschedule all active notification jobs to fire at the new time
        viewModelScope.launch {
            birthdays.value
                .filter { it.notifyEnabled }
                .forEach { notificationScheduler.scheduleFor(it.id, it.name, it.birthDate) }
        }
    }

    fun getUserBirthDate(): LocalDate? {
        val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)
        return prefs.getString("birth_date", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    }

    fun getCompatibilityWithSavedBirthday(savedBirthday: SavedBirthday): CompatibilityResult? {
        val userBirthDate = getUserBirthDate() ?: return null
        val nameA = "You"
        val nameB = savedBirthday.name
        return compatibilityCalculator.calculate(userBirthDate, savedBirthday.birthDate, nameA, nameB)
    }
}
