# Skill: DataStore Preferences

## When to use
When reading or writing user preferences (name, birth date, location, settings, premium status, onboarding state).

## Rules
- Never use SharedPreferences — all prefs go through DataStore<Preferences>
- Keys: define as companion object constants in the Repository, not inline strings
- Always read as Flow<T>, never blockingFirst()
- Write ops: use .edit { } suspend block, always in a coroutine scope
- Default values must be defined at the key declaration site
- Premium status (`is_premium`) and onboarding completion (`onboarding_completed`) are DataStore-backed
- Billing purchase state syncs to DataStore via `userPrefs.setPremium()`

## File locations
- app/src/main/java/.../data/
