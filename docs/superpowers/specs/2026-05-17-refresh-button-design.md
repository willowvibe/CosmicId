# Refresh Button Design — CalculatorScreen

## Problem
When a user edits fields on the My Cosmos tab (e.g., changes their name in the `OutlinedTextField`), the astrology calculations (`computeResult()`) are not re-triggered. The displayed `AgeResult` remains stale.

## Solution
Add an explicit refresh action to `CalculatorViewModel` and expose it as a small icon button in the `CalculatorScreen` header.

## Components

### 1. CalculatorViewModel
- **New function:** `refresh()`
  - Reads current `_uiState` values: `birthDate`, `birthTime`, `location`, `name`
  - If `birthDate != null`, re-runs `computeResult()`, `TimeRemainingCalculator`, `RetirementCalculator`, and `celebrityMatchCalculator.findMatches()`
  - Updates `_uiState` with new `result`, `timeRemaining`, `retirement`, and `celebrityMatches`
  - Also refreshes `dailyFortune` via `computeDailyFortune()`
  - No-op (safe) if `birthDate` is null

### 2. CalculatorScreen
- **New UI element:** Refresh icon button in the header `Row`, placed between the LIVE chip and the Settings icon.
- **Interaction:** Tapping the button calls `viewModel.refresh()` and triggers `HapticFeedbackType.LongPress`.
- **Icon:** `Icons.Default.Refresh` (or `Icons.Default.Sync`)
- **Accessibility:** `contentDescription = "Refresh calculations"`

## Data Flow
```
User taps refresh icon
  → viewModel.refresh()
    → reads current birthDate, birthTime, location, name from _uiState
    → computeResult(date, time, includeUnlocked=true, location)
    → TimeRemainingCalculator().calculate(...)
    → RetirementCalculator().calculate(...)
    → celebrityMatchCalculator.findMatches(date)
    → computeDailyFortune(date)
    → _uiState.update { ... }  // all new values
  → UI recomposes with refreshed result
```

## Out of Scope
- Auto-refresh on every keystroke (too expensive; manual trigger is deliberate)
- Syncing with external preference changes (settings, deep links)
- Changing any calculation logic — only re-triggering existing logic

## Files to Modify
- `app/src/main/java/com/willowvibe/agereveal/ui/viewmodel/CalculatorViewModel.kt`
- `app/src/main/java/com/willowvibe/agereveal/ui/screen/CalculatorScreen.kt`
