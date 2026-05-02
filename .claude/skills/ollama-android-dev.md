---
name: ollama-android-dev
description: Use local Ollama models for Android/Kotlin code generation, refactoring, and explanation via the ollama MCP server.
---

# Ollama Android Development Assistant

Use the `ollama` MCP server to delegate Android/Kotlin coding tasks to local LLMs.

## Available MCP Tools

| Tool | Purpose |
|------|---------|
| `ollama_chat` | Multi-turn chat with local models |
| `ollama_generate` | Single-shot text/code generation |
| `ollama_execute` | Generate and execute code (with AI assistance) |
| `ollama_embed` | Generate embeddings for text |
| `ollama_list` | List available local models |
| `ollama_show` | Show model details |

## Default Model

The default model is `qwen3-coder-next:cloud` — optimized for code tasks.
Other available models:
- `deepseek-v3.2:cloud` — large reasoning model
- `gemma4:31b-cloud` — general purpose
- `kimi-k2.6:cloud` — long context

## When to Use

1. **Code explanation**: Ask Ollama to explain complex Kotlin/Compose code
2. **Refactoring suggestions**: Get local model input on improving code structure
3. **Generate snippets**: Create boilerplate Android code (composables, ViewModels, etc.)
4. **Test ideas**: Brainstorm test cases with a local model before implementing

## Usage Examples

```
Use ollama_generate to create a Jetpack Compose shimmer loading skeleton.
```

```
Use ollama_chat to explain how the Ba Zi calculator works in this codebase.
```

```
Use ollama_execute to write a Python script that validates our gradle dependencies.
```

## Tips

- Always specify the model if you need a specific capability (e.g., `"model": "deepseek-v3.2:cloud"` for reasoning)
- Use `format: markdown` for readable responses
- The Ollama server runs locally — no data leaves your machine
