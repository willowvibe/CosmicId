# Contributing to AgeReveal

First off, thank you for considering contributing to AgeReveal! It's people like you that make the open source community such a great place to learn, inspire, and create.

## How Can I Contribute?

### Reporting Bugs
This section guides you through submitting a bug report for AgeReveal. Following these guidelines helps maintainers and the community understand your report, reproduce the behavior, and find related reports.
* Make sure you test against the latest version.
* Provide a clear and descriptive title for the issue.
* Describe the exact steps which reproduce the problem in as many details as possible.

### Suggesting Enhancements
This section guides you through submitting an enhancement suggestion for AgeReveal, including completely new features and minor improvements to existing functionality.
* Use a clear and descriptive title for the issue to identify the suggestion.
* Provide a step-by-step description of the suggested enhancement in as many details as possible.

### Pull Requests
* Fill in the required template
* Do not include issue numbers in the PR title
* Include screenshots and animated GIFs in your pull request whenever possible.
* Follow the Kotlin styleguide.
* End files with a newline.

## Setup for Development
AgeReveal is built with Kotlin and Jetpack Compose.
1. Download and install Android Studio.
2. Clone this repository.
3. Import the project into Android Studio.
4. Let Gradle sync and build.

**Optional — Custom Inter Typography:**
The app falls back to the system sans-serif by default. To enable the Inter custom font:
1. Download Inter TTF files from [rsms.me/inter](https://rsms.me/inter)
2. Place `inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `inter_bold.ttf` in `app/src/main/res/font/`
3. Uncomment the `InterFamily` block in `app/src/main/java/com/willowvibe/agereveal/ui/theme/Type.kt`
4. Replace `FontFamily.Default` with `InterFamily` in that file

**AdMob:** The bundled IDs are Google's test values — safe to run locally but generate no revenue. See [TASKS.md](TASKS.md) for how to swap in production IDs before release.
