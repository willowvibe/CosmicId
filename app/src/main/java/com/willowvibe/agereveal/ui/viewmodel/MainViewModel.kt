package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Top-level ViewModel for app-wide state (onboarding gate, deep-link routing).
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPrefs: UserPreferencesRepository,
) : ViewModel() {

    val hasCompletedOnboarding: StateFlow<Boolean> = userPrefs.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun completeOnboarding() {
        viewModelScope.launch { userPrefs.setOnboardingCompleted(true) }
    }
}
