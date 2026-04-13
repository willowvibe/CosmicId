package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.model.SavedBirthday
import com.willowvibe.agereveal.data.repository.BirthdayRepository
import com.willowvibe.agereveal.notification.BirthdayNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val repository: BirthdayRepository,
    private val notificationScheduler: BirthdayNotificationScheduler,
) : ViewModel() {

    val birthdays: StateFlow<List<SavedBirthday>> = repository.allBirthdays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addBirthday(name: String, birthDate: LocalDate, emoji: String = "🎂") {
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
}
