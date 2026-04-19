package com.willowvibe.agereveal.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.domain.AgeCalculator
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.notification.MilestoneNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
    val name: String = "",                // Optional name for display purposes
    val result: AgeResult? = null,
    val isUnlocked: Boolean = false,       // True after rewarded ad watched
    val isAdLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val ageCalculator: AgeCalculator,
    private val shareCardGenerator: ShareCardGenerator,
    private val milestoneNotificationScheduler: MilestoneNotificationScheduler,
    @ApplicationContext context: Context,
) : ViewModel() {

    private val prefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        // Restore previously entered birth date without resetting milestones or unlock state
        prefs.getString("birth_date", null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.takeIf { !it.isAfter(LocalDate.now()) }
            ?.let { date ->
                _uiState.update { it.copy(birthDate = date, result = computeResult(date, includeUnlocked = false)) }
            }
    }

    /** Get the user's birth date (if set). */
    fun getUserBirthDate(): LocalDate? = _uiState.value.birthDate

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
        // Cancel stale milestone notifications from any previously saved birth date
        milestoneNotificationScheduler.cancelAll()
        prefs.edit().putString("birth_date", date.toString()).apply()
        _uiState.update { state ->
            state.copy(
                birthDate = date,
                error = null,
                isUnlocked = false,
                result = computeResult(date, includeUnlocked = false),
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
        milestoneNotificationScheduler.scheduleUpcomingMilestones(birthDate)
    }

    /**
     * Generate and share the age card via the Android share sheet.
     * No-op if no birth date has been selected yet.
     */
    fun shareCard(theme: ShareCardGenerator.CardTheme = ShareCardGenerator.CardTheme.DARK_COSMOS) {
        val result = _uiState.value.result ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shareCardGenerator.share(result, theme)
        }
    }

    /** Share a dedicated milestone card (e.g. "You'll turn 10,000 days old on…"). */
    fun shareMilestoneCard(
        milestone: Milestone,
        theme: ShareCardGenerator.CardTheme = ShareCardGenerator.CardTheme.FESTIVE_INDIA,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            shareCardGenerator.shareMilestone(milestone, theme)
        }
    }

    fun setAdLoading(loading: Boolean) = _uiState.update { it.copy(isAdLoading = loading) }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun onNameChanged(name: String) = _uiState.update { it.copy(name = name) }

    // ---------------------------------------------------------------------------

    private fun computeResult(birthDate: LocalDate, includeUnlocked: Boolean): AgeResult {
        val now = LocalDateTime.now()
        val totalSeconds = ChronoUnit.SECONDS.between(
            birthDate.atStartOfDay(), now,
        )
        val name = _uiState.value.name.ifEmpty { "You" }
        return ageCalculator.calculate(
            birthDate = birthDate,
            totalSecondsOverride = totalSeconds,
            includeUnlocked = includeUnlocked,
        ).copy(name = name)
    }
}
