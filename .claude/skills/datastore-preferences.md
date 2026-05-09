# Skill: DataStore Preferences

## When to use
When reading or writing user preferences (name, birth date, location, settings).

## Rules
- Never use SharedPreferences — all prefs go through DataStore<Preferences>
- Keys: define as companion object constants in the Repository, not inline strings
- Always read as Flow<T>, never blockingFirst()
- Write ops: use .edit { } suspend block, always in a coroutine scope
- Default values must be defined at the key declaration site

## File locations
- app/src/main/java/.../data/
