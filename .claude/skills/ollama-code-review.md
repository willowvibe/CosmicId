---
name: ollama-code-review
description: Run local code review on Kotlin/Android changes using Ollama models via MCP.
---

# Ollama Code Review

Use the `ollama` MCP server to perform code reviews with local LLMs before committing.

## Prompts Available

The Ollama MCP server includes built-in prompts:

| Prompt | Purpose |
|--------|---------|
| `code_review` | Deep code review focusing on bugs, edge cases, security |
| `explain_code` | Detailed explanation of what code does |
| `write_docstring` | Generate documentation for code |

## How to Review Code

1. Read the file(s) you want to review
2. Use `ollama_generate` with a code review prompt, or
3. Use the built-in `code_review` prompt via MCP

## Example Workflow

```
Read the changed Kotlin file, then use ollama_generate with:
"Review this Kotlin code for null safety, coroutine usage, and Compose best practices."
```

## Review Checklist for Android/Kotlin

When reviewing via Ollama, ask it to check for:
- Null safety (`?.`, `?:`, `!!` usage)
- Coroutine scope management (viewModelScope, lifecycleScope)
- Compose recomposition optimization
- Hilt dependency injection correctness
- Room database migration safety
- Resource leaks (flows not collected properly)
- AdMob integration best practices

## Model Recommendations

- `qwen3-coder-next:cloud` — Best for code review (fast, code-optimized)
- `deepseek-v3.2:cloud` — Best for deep architectural review (slower but thorough)
