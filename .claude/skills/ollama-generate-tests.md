---
name: ollama-generate-tests
description: Generate Android instrumented and unit tests using local Ollama models via MCP.
---

# Ollama Test Generation

Use the `ollama` MCP server to generate test cases for Android components.

## Test Types

1. **Compose UI Tests** (`createComposeRule`)
2. **ViewModel Tests** (coroutines, StateFlow)
3. **Repository/Database Tests** (Room, DataStore)
4. **Integration Tests** (end-to-end flows)

## Example Prompts

```
Use ollama_generate to create Compose UI tests for a new screen with:
- Text input fields
- Date picker dialog
- Button interactions
- Navigation verification
```

```
Use ollama_generate to write a ViewModel test for:
- StateFlow emissions
- Error handling
- Loading states
- Business logic validation
```

## Best Practices to Request

When generating tests, ask Ollama to include:
- `@get:Rule val composeTestRule = createComposeRule()`
- `runTest` for coroutine testing
- ` Turbine` for Flow testing (if available)
- `Fake`/`Mock` dependency injection for unit tests
- Parameterized tests for edge cases

## Available Models

- `qwen3-coder-next:cloud` — Fast test generation
- `deepseek-v3.2:cloud` — Comprehensive test scenarios

## Note

Always verify generated tests compile before committing. Run:
```bash
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:testDebugUnitTest
```
