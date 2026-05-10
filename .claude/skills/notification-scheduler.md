# Skill: Notification Scheduling

## When to use
When working with birthday reminders, daily fortune notifications, cosmic year reports, or any scheduled alerts.

## Rules
- Use WorkManager for all scheduled notifications — never AlarmManager directly
- Notification channels must be created in Application.onCreate(), not in workers
- Birthday reminder: schedule 1 day before AND on the day, re-schedule after each birthday
- Daily fortune: schedule daily at a user-preferred time (default 8 AM)
- Cosmic year report: schedule on the user's birthday annually
- Always check POST_NOTIFICATIONS permission on Android 13+ before showing
- Notification tap: deep link back to the relevant tab using NavDeepLink
- In v2.0, daily fortune and cosmic year report are premium-tier features

## File locations
- app/src/main/java/.../notification/
