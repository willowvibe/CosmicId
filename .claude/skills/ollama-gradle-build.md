# Ollama Gradle Build Skill

Use this skill when you need to run Gradle tasks, debug build errors, or check test results for the Cosmic ID Android project.

## Trigger
Invoke with: `/ollama-gradle-build`

## What This Skill Does

Routes Gradle-related tasks through the local Ollama model to:
- Parse and explain build/compile errors from `./gradlew assembleDebug`
- Suggest fixes for dependency conflicts in `app/build.gradle.kts`
- Interpret `./gradlew test` or `./gradlew connectedAndroidTest` output
- Recommend ProGuard / R8 rule adjustments in `proguard-rules.pro`

## Preferred Models

| Task | Model |
|------|-------|
| Build error diagnosis | `qwen3-coder-next:cloud` |
| Dependency conflict reasoning | `deepseek-v3.2:cloud` |
| ProGuard rules | `qwen3-coder-next:cloud` |

## Example Prompts

```
/ollama-gradle-build
Run ./gradlew assembleDebug and fix any errors you find.
```

```
/ollama-gradle-build
The Room schema export is failing. Check app/build.gradle.kts and suggest the fix.
```

## Workflow

1. Claude reads the relevant Gradle files via the `filesystem` MCP
2. Sends context + error log to Ollama via `generate` tool
3. Applies suggested fix directly to the build file
4. Re-runs the Gradle task to verify the fix

## Notes
- Always run `./gradlew clean` before a fresh debug build diagnosis
- Use `--stacktrace` flag for detailed error output: `./gradlew assembleDebug --stacktrace`
- Room schema JSON files are in `app/schemas/` — useful context for migration errors
