# Release Notes

## v1.0 (versionCode 2) — Play Store launch

Highlights
- 🎯 **Birth Time support** — optional time picker for exact Nakshatra & Rashi
- ⏰ **Milestone notifications** — pick which life-day milestones (500, 1k, 10k…) notify you
- 📺 **Life Timeline** — scrollable view of every milestone, tap to share
- 🎨 **Theme & language** — System/Light/Dark, English & Hindi
- 🗓️ **Global reminder toggle** — one switch to mute all birthday reminders
- 🧾 **Export** — share your saved birthdays as CSV
- 🪟 **4×2 wide widget** — next 3 upcoming birthdays on your home screen
- ⭐ **In-app review** — rate us right from the app after sharing your card
- 🔒 **Room migration** — safe DB schema upgrades (your saved birthdays survive future updates)

Fixes
- Theme selector now persists across restarts
- Saved Settings layout consolidated — no more split settings
- Share cards are a perfect square (900×900) so no more cropping on WhatsApp/Instagram
- Milestone notification IDs can no longer collide with birthday IDs
- BirthdayRepository notifies both the 2×2 and 4×2 widgets after every change
