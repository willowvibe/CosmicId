package com.willowvibe.agereveal.ui.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.willowvibe.agereveal.data.model.AgeResult
import com.willowvibe.agereveal.data.model.GeoLocation
import com.willowvibe.agereveal.data.model.Milestone
import com.willowvibe.agereveal.data.preferences.UserPreferencesRepository
import com.willowvibe.agereveal.domain.AgeCalculator
import com.willowvibe.agereveal.domain.ShareCardGenerator
import com.willowvibe.agereveal.notification.MilestoneNotificationScheduler
import com.willowvibe.agereveal.util.ReviewHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CalculatorUiState(
    val birthDate: LocalDate? = null,
    val birthTime: LocalTime? = null,    // Optional time of birth for precise astrology
    val location: GeoLocation? = null,   // Optional birth location for exact Lagna
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
    private val userPrefs: UserPreferencesRepository,
    private val reviewHelper: ReviewHelper,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        // Set up share error handlers
        shareCardGenerator.setShareErrorHandler { error ->
            _uiState.update { it.copy(error = "Failed to share card: ${error.message}") }
        }
        shareCardGenerator.setMilestoneShareErrorHandler { error ->
            _uiState.update { it.copy(error = "Failed to share milestone: ${error.message}") }
        }
        shareCardGenerator.setCompatibilityShareErrorHandler { error ->
            _uiState.update { it.copy(error = "Failed to share compatibility: ${error.message}") }
        }

        // Restore previously entered birth date + time + location
        val savedDate = prefs.getString("birth_date", null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?.takeIf { !it.isAfter(LocalDate.now()) }
        val savedTime = prefs.getString("birth_time", null)
            ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        val savedLocation = prefs.getString("birth_location", null)
            ?.let { runCatching { parseLocation(it) }.getOrNull() }
        if (savedDate != null) {
            _uiState.update {
                it.copy(
                    birthDate = savedDate,
                    birthTime = savedTime,
                    location = savedLocation,
                    result = computeResult(savedDate, savedTime, includeUnlocked = false, location = savedLocation),
                )
            }
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
        // Schedule milestone notifications for the new birth date, respecting user prefs
        viewModelScope.launch {
            val enabled = MilestoneNotificationScheduler.MILESTONE_TARGETS
                .filter { userPrefs.milestoneEnabled(it).first() }
                .toSet()
            milestoneNotificationScheduler.scheduleUpcomingMilestones(date, enabled)
        }
        prefs.edit().putString("birth_date", date.toString()).apply()
        _uiState.update { state ->
            state.copy(
                birthDate = date,
                error = null,
                isUnlocked = false,
                result = computeResult(date, state.birthTime, includeUnlocked = false, location = state.location),
            )
        }
    }

    fun onBirthTimeSelected(time: LocalTime?) {
        if (time != null) prefs.edit().putString("birth_time", time.toString()).apply()
        else prefs.edit().remove("birth_time").apply()
        _uiState.update { state ->
            val date = state.birthDate ?: return@update state.copy(birthTime = time)
            state.copy(
                birthTime = time,
                result = computeResult(date, time, state.isUnlocked, location = state.location),
            )
        }
    }

    /** Called every second by the UI, driven by [tickerSeconds]. */
    fun onTick() {
        val state = _uiState.value
        val birthDate = state.birthDate ?: return
        _uiState.update {
            it.copy(result = computeResult(birthDate, it.birthTime, it.isUnlocked, location = it.location))
        }
    }

    /** Called by [CalculatorScreen] when the user watches the rewarded ad successfully. */
    fun onRewardedAdEarned() {
        val state = _uiState.value
        val birthDate = state.birthDate ?: return
        _uiState.update {
            it.copy(
                isUnlocked = true,
                result = computeResult(birthDate, it.birthTime, includeUnlocked = true, location = it.location),
            )
        }
        viewModelScope.launch {
            val enabled = MilestoneNotificationScheduler.MILESTONE_TARGETS
                .filter { userPrefs.milestoneEnabled(it).first() }
                .toSet()
            milestoneNotificationScheduler.scheduleUpcomingMilestones(birthDate, enabled)
        }
    }

    /** Toggle a single milestone target on/off and update WorkManager accordingly. */
    fun setMilestoneEnabled(targetDays: Int, enabled: Boolean) {
        val date = _uiState.value.birthDate
        viewModelScope.launch {
            userPrefs.setMilestoneEnabled(targetDays, enabled)
            if (date != null) {
                if (enabled) milestoneNotificationScheduler.scheduleSingle(date, targetDays)
                else milestoneNotificationScheduler.cancelSingle(date, targetDays)
            }
        }
    }

    /**
     * Generate and share the age card via the Android share sheet.
     * No-op if no birth date has been selected yet.
     * [activity] (optional) is used to trigger the in-app review flow after a successful share.
     */
    fun shareCard(
        theme: ShareCardGenerator.CardTheme = ShareCardGenerator.CardTheme.DARK_COSMOS,
        activity: Activity? = null,
    ) {
        val result = _uiState.value.result ?: return
        viewModelScope.launch(Dispatchers.IO) {
            shareCardGenerator.share(result, theme)
        }
        reviewHelper.maybePromptAfterShare(activity)
    }

    /** Share a dedicated milestone card (e.g. "You'll turn 10,000 days old on…"). */
    fun shareMilestoneCard(
        milestone: Milestone,
        theme: ShareCardGenerator.CardTheme = ShareCardGenerator.CardTheme.FESTIVE_INDIA,
        activity: Activity? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            shareCardGenerator.shareMilestone(milestone, theme)
        }
        reviewHelper.maybePromptAfterShare(activity)
    }

    fun setAdLoading(loading: Boolean) = _uiState.update { it.copy(isAdLoading = loading) }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun onNameChanged(name: String) {
        val sanitized = name.filterNot { it.isISOControl() }.take(50)
        _uiState.update { it.copy(name = sanitized) }
    }

    /** Set or clear the birth location for exact Lagna calculation. */
    fun onLocationSelected(location: GeoLocation?) {
        if (location != null) {
            prefs.edit().putString("birth_location", serializeLocation(location)).apply()
        } else {
            prefs.edit().remove("birth_location").apply()
        }
        _uiState.update { state ->
            val date = state.birthDate ?: return@update state.copy(location = location)
            state.copy(
                location = location,
                result = computeResult(date, state.birthTime, state.isUnlocked, location = location),
            )
        }
    }

    // ---------------------------------------------------------------------------

    private fun computeResult(
        birthDate: LocalDate,
        birthTime: LocalTime?,
        includeUnlocked: Boolean,
        location: GeoLocation? = null,
    ): AgeResult {
        val now = LocalDateTime.now()
        val totalSeconds = ChronoUnit.SECONDS.between(
            birthDate.atStartOfDay(), now,
        )
        val name = _uiState.value.name.ifEmpty { "You" }
        return ageCalculator.calculate(
            birthDate = birthDate,
            birthTime = birthTime,
            totalSecondsOverride = totalSeconds,
            includeUnlocked = includeUnlocked,
            zoneOffset = OffsetDateTime.now().offset,
            location = location,
        ).copy(name = name)
    }

    private fun serializeLocation(location: GeoLocation): String {
        return "${location.latitude},${location.longitude},${location.label}"
    }

    private fun parseLocation(serialized: String): GeoLocation {
        val parts = serialized.split(",", limit = 3)
        return GeoLocation(
            latitude = parts[0].toDouble(),
            longitude = parts[1].toDouble(),
            label = parts.getOrElse(2) { "" },
        )
    }
}
