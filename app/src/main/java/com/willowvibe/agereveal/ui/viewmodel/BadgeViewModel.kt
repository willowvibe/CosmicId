package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.model.BadgeDefinition
import com.willowvibe.agereveal.data.repository.BadgeRepository
import com.willowvibe.agereveal.domain.BadgeDefinitions
import com.willowvibe.agereveal.domain.ShareCardGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class BadgeUiState(
    val allBadges: List<BadgeDefinition> = BadgeDefinitions.ALL,
    val unlockedIds: Set<String> = emptySet(),
    val newlyUnlocked: List<BadgeDefinition> = emptyList(),
    val showConfetti: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class BadgeViewModel @Inject constructor(
    private val badgeRepository: BadgeRepository,
    private val shareCardGenerator: ShareCardGenerator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BadgeUiState())
    val uiState: StateFlow<BadgeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            badgeRepository.unlockedBadges.collect { unlocked ->
                _uiState.update {
                    it.copy(unlockedIds = unlocked.map { ub -> ub.badgeId }.toSet())
                }
            }
        }
    }

    /** Check for newly unlocked badges given a birth date. */
    fun checkUnlocks(birthDate: LocalDate, birthTime: LocalTime? = null) {
        viewModelScope.launch {
            val newlyUnlocked = badgeRepository.checkAndUnlock(birthDate, birthTime)
            if (newlyUnlocked.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        newlyUnlocked = newlyUnlocked,
                        showConfetti = true,
                    )
                }
            }
        }
    }

    fun dismissConfetti() {
        _uiState.update { it.copy(showConfetti = false, newlyUnlocked = emptyList()) }
    }

    fun shareBadge(
        badge: BadgeDefinition,
        theme: ShareCardGenerator.CardTheme = ShareCardGenerator.CardTheme.DARK_COSMOS,
    ) {
        viewModelScope.launch {
            val unlockedAt = badgeRepository.unlockedBadges.first()
                .find { it.badgeId == badge.id }?.unlockedAt
            shareCardGenerator.shareBadge(badge, unlockedAt, theme)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
