package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.domain.ZodiacCompatibilityCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CompatibilityUiState(
    val dateA: LocalDate? = null,
    val dateB: LocalDate? = null,
    val nameA: String = "",
    val nameB: String = "",
    val result: CompatibilityResult? = null,
    val isSameDate: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CompatibilityViewModel @Inject constructor(
    private val calculator: ZodiacCompatibilityCalculator,
    private val shareCardGenerator: ShareCardGenerator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompatibilityUiState())
    val uiState: StateFlow<CompatibilityUiState> = _uiState.asStateFlow()

    fun onDateASelected(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Date cannot be in the future") }
            return
        }
        _uiState.update { state ->
            val same = state.dateB != null && state.dateB == date
            val result = if (!same) state.dateB?.let {
                calculator.calculate(date, it, state.nameA.ifEmpty { "Person A" }, state.nameB.ifEmpty { "Person B" })
            } else null
            state.copy(dateA = date, result = result, isSameDate = same, error = if (same) "Both dates are the same — compatibility requires two different birth dates" else null)
        }
    }

    fun onDateBSelected(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Date cannot be in the future") }
            return
        }
        _uiState.update { state ->
            val same = state.dateA != null && state.dateA == date
            val result = if (!same) state.dateA?.let {
                calculator.calculate(it, date, state.nameA.ifEmpty { "Person A" }, state.nameB.ifEmpty { "Person B" })
            } else null
            state.copy(dateB = date, result = result, isSameDate = same, error = if (same) "Both dates are the same — compatibility requires two different birth dates" else null)
        }
    }

    fun setNameA(name: String) = _uiState.update { it.copy(nameA = name) }
    fun setNameB(name: String) = _uiState.update { it.copy(nameB = name) }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun shareCard(theme: ShareCardGenerator.CardTheme = ShareCardGenerator.CardTheme.DARK_COSMOS) {
        val result = _uiState.value.result ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shareCardGenerator.shareCompatibility(result, theme)
        }
    }
}
