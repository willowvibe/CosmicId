package com.willowvibe.agereveal.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.domain.AgeCalculator
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
    val differenceYears: Int = 0,
    val differenceMonths: Int = 0,
    val differenceDays: Int = 0,
    val comparisonCount: Int = 0,   // used to trigger interstitial after 2nd compare
    val error: String? = null,
)

@HiltViewModel
class CompareViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    fun onPersonADateSelected(date: LocalDate, label: String = "Person A") {
        _uiState.update { it.copy(dateA = date, labelA = label, error = null) }
        maybeCompare()
    }

    fun onPersonBDateSelected(date: LocalDate, label: String = "Person B") {
        _uiState.update { it.copy(dateB = date, labelB = label, error = null) }
        maybeCompare()
    }

    fun compare() {
        maybeCompare()
    }

    private fun maybeCompare() {
        val state = _uiState.value
        val dateA = state.dateA ?: return
        val dateB = state.dateB ?: return

        val (older, newer, olderLabel) = if (dateA.isBefore(dateB)) {
            Triple(dateA, dateB, state.labelA)
        } else {
            Triple(dateB, dateA, state.labelB)
        }

        val period = Period.between(older, newer)

        _uiState.update {
            it.copy(
                olderLabel = olderLabel,
                differenceYears = period.years,
                differenceMonths = period.months,
                differenceDays = period.days,
                comparisonCount = it.comparisonCount + 1,
            )
        }
    }
}
