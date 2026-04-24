package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.domain.CompatibilityResult
import com.willowvibe.agereveal.domain.RelationshipType
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
    val relationshipType: RelationshipType = RelationshipType.Romantic,
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

    init {
        // Set up share error handlers
        shareCardGenerator.setCompatibilityShareErrorHandler { error ->
            _uiState.update { it.copy(error = "Failed to share compatibility: ${error.message}") }
        }
    }

    fun onDateASelected(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Date cannot be in the future") }
            return
        }
        _uiState.update { state ->
            val same = state.dateB != null && state.dateB == date
            val result = if (!same) recalculate(state.copy(dateA = date)) else null
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
            val result = if (!same) recalculate(state.copy(dateB = date)) else null
            state.copy(dateB = date, result = result, isSameDate = same, error = if (same) "Both dates are the same — compatibility requires two different birth dates" else null)
        }
    }

    fun setNameA(name: String) = _uiState.update { state ->
        val result = if (state.dateA != null && state.dateB != null && !state.isSameDate) recalculate(state.copy(nameA = name)) else state.result
        state.copy(nameA = name, result = result)
    }

    fun setNameB(name: String) = _uiState.update { state ->
        val result = if (state.dateA != null && state.dateB != null && !state.isSameDate) recalculate(state.copy(nameB = name)) else state.result
        state.copy(nameB = name, result = result)
    }

    fun setRelationshipType(type: RelationshipType) = _uiState.update { state ->
        val result = if (state.dateA != null && state.dateB != null && !state.isSameDate) recalculate(state.copy(relationshipType = type)) else state.result
        state.copy(relationshipType = type, result = result)
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun shareCard(theme: ShareCardGenerator.CardTheme = ShareCardGenerator.CardTheme.DARK_COSMOS) {
        val result = _uiState.value.result ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shareCardGenerator.shareCompatibility(result, theme)
        }
    }

    private fun recalculate(state: CompatibilityUiState): CompatibilityResult? {
        val dateA = state.dateA ?: return null
        val dateB = state.dateB ?: return null
        if (dateA == dateB) return null
        return calculator.calculate(
            dateA = dateA,
            dateB = dateB,
            nameA = state.nameA.ifEmpty { "Person A" },
            nameB = state.nameB.ifEmpty { "Person B" },
            relationshipType = state.relationshipType,
        )
    }
}
