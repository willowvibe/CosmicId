package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.domain.AgeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CalculatorUiState(
    val birthDate: LocalDate? = null,
    val result: AgeResult? = null,
    val isUnlocked: Boolean = false,       // True after rewarded ad watched
    val isAdLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val ageCalculator: AgeCalculator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    /**
     * 1-second ticker — emits current epoch second to drive live totalSeconds display.
     * WhileSubscribed(5000) keeps the flow alive for 5s during config changes / tab switches.
     */
    val tickerSeconds: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis() / 1000L)
            delay(1_000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun onBirthDateSelected(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Birth date cannot be in the future") }
            return
        }
        _uiState.update { state ->
            state.copy(
                birthDate = date,
                error = null,
                result = computeResult(date, state.isUnlocked),
            )
        }
    }

    /** Called every second by the UI, driven by [tickerSeconds]. */
    fun onTick() {
        val state = _uiState.value
        val birthDate = state.birthDate ?: return
        _uiState.update {
            it.copy(result = computeResult(birthDate, it.isUnlocked))
        }
    }

    /** Called by [CalculatorScreen] when the user watches the rewarded ad successfully. */
    fun onRewardedAdEarned() {
        val state = _uiState.value
        val birthDate = state.birthDate ?: return
        _uiState.update {
            it.copy(
                isUnlocked = true,
                result = computeResult(birthDate, includeUnlocked = true),
            )
        }
    }

    fun setAdLoading(loading: Boolean) = _uiState.update { it.copy(isAdLoading = loading) }

    fun clearError() = _uiState.update { it.copy(error = null) }

    // ---------------------------------------------------------------------------

    private fun computeResult(birthDate: LocalDate, includeUnlocked: Boolean): AgeResult {
        val now = LocalDateTime.now()
        val totalSeconds = ChronoUnit.SECONDS.between(
            birthDate.atStartOfDay(), now,
        )
        return ageCalculator.calculate(
            birthDate = birthDate,
            totalSecondsOverride = totalSeconds,
            includeUnlocked = includeUnlocked,
        )
    }
}
