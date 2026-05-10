package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CompareUiState(
    val dateA: LocalDate? = null,
    val labelA: String = "Person A",
    val dateB: LocalDate? = null,
    val labelB: String = "Person B",
    val olderLabel: String = "",
    val isSameAge: Boolean = false,
    val differenceYears: Int = 0,
    val differenceMonths: Int = 0,
    val differenceDays: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class CompareViewModel @Inject constructor(
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    fun onPersonANameChanged(name: String) = _uiState.update { it.copy(labelA = name.ifBlank { "Person A" }) }

    fun onPersonBNameChanged(name: String) = _uiState.update { it.copy(labelB = name.ifBlank { "Person B" }) }

    fun onPersonADateSelected(date: LocalDate, label: String = "Person A") {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Birth date cannot be in the future") }
            return
        }
        _uiState.update { it.copy(dateA = date, labelA = label, error = null) }
        maybeCompare()
    }

    fun onPersonBDateSelected(date: LocalDate, label: String = "Person B") {
        if (date.isAfter(LocalDate.now())) {
            _uiState.update { it.copy(error = "Birth date cannot be in the future") }
            return
        }
        _uiState.update { it.copy(dateB = date, labelB = label, error = null) }
        maybeCompare()
    }

    fun compare() {
        maybeCompare()
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun maybeCompare() {
        val state = _uiState.value
        val dateA = state.dateA ?: return
        val dateB = state.dateB ?: return

        if (dateA == dateB) {
            _uiState.update {
                it.copy(
                    olderLabel = "",
                    isSameAge = true,
                    differenceYears = 0,
                    differenceMonths = 0,
                    differenceDays = 0,
                    )
            }
            return
        }

        val (older, newer, olderLabel) = if (dateA.isBefore(dateB)) {
            Triple(dateA, dateB, state.labelA)
        } else {
            Triple(dateB, dateA, state.labelB)
        }

        val period = Period.between(older, newer)

        _uiState.update {
            it.copy(
                olderLabel = olderLabel,
                isSameAge = false,
                differenceYears = period.years,
                differenceMonths = period.months,
                differenceDays = period.days,
            )
        }
    }

}
