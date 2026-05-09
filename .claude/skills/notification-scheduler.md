# Skill: Notification Scheduling

## When to use
When working with birthday reminders, daily fortune notifications, or any scheduled alerts.

## Rules
- Use WorkManager for all scheduled notifications — never AlarmManager directly
- Notification channels must be created in Application.onCreate(), not in workers
- Birthday reminder: schedule 1 day before AND on the day, re-schedule after each birthday
- Always check POST_NOTIFICATIONS permission on Android 13+ before showing
- Notification tap: deep link back to the relevant tab using NavDeepLink

## File locations
- app/src/main/java/.../notification/
