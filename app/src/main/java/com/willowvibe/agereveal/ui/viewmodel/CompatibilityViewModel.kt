package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.domain.ZodiacCompatibilityCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

data class CompatibilityUiState(
    val dateA: LocalDate? = null,
    val dateB: LocalDate? = null,
    val result: CompatibilityResult? = null,
    val error: String? = null,
)

@HiltViewModel
class CompatibilityViewModel @Inject constructor(
    private val calculator: ZodiacCompatibilityCalculator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompatibilityUiState())
    val uiState: StateFlow<CompatibilityUiState> = _uiState.asStateFlow()

    fun onDateASelected(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Date cannot be in the future") }
            return
        }
        _uiState.update { state ->
            val result = state.dateB?.let { calculator.calculate(date, it) }
            state.copy(dateA = date, result = result, error = null)
        }
    }

    fun onDateBSelected(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Date cannot be in the future") }
            return
        }
        _uiState.update { state ->
            val result = state.dateA?.let { calculator.calculate(it, date) }
            state.copy(dateB = date, result = result, error = null)
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
