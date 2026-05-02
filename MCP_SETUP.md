# MCP Setup for AgeReveal

This project includes local MCP (Model Context Protocol) servers for Claude Code, enabling local LLM integration via Ollama.

## Installed MCP Servers

### Ollama MCP Server (`mcp-ollama-python`)

**Location:** `tools/mcp/`

**What it does:**
- Exposes local Ollama instances as MCP tools for Claude Code
- 8 tools: `chat`, `generate`, `execute`, `embed`, `list`, `show`, `pull`, `delete`
- 5 built-in prompts: `code_review`, `explain_code`, `write_docstring`, `explain_lora`, `hello_world`

**Startup:**
```bash
./tools/mcp/start-ollama-mcp.sh
```

**Available Models:**
| Model | Best For |
|-------|----------|
| `qwen3-coder-next:cloud` | Code generation/review (default) |
| `deepseek-v3.2:cloud` | Deep reasoning, architecture review |
| `gemma4:31b-cloud` | General purpose tasks |
| `kimi-k2.6:cloud` | Long context processing |

## Configuration

### Project-level `.mcp.json`

The Ollama MCP server is registered in `.mcp.json` at project root:

```json
{
  "mcpServers": {
    "ollama": {
      "command": "/mnt/data2/git_repos/AgeReveal/tools/mcp/start-ollama-mcp.sh",
      "env": {
        "OLLAMA_HOST": "http://localhost:11434",
        "OLLAMA_MODEL": "qwen3-coder-next:cloud"
      }
    }
  }
}
```

### Skills

Three Claude Code skills are installed in `.claude/skills/`:

| Skill | Purpose |
|-------|---------|
| `ollama-android-dev` | Android/Kotlin code generation with local models |
| `ollama-code-review` | Local code review before commits |
| `ollama-generate-tests` | Generate instrumented/unit tests |

Invoke with: `/ollama-android-dev`, `/ollama-code-review`, `/ollama-generate-tests`

## Requirements

- **Ollama** must be running locally (`ollama serve` or system service)
- **Python 3.12+** for the MCP server venv
- **Node.js 22+** for npm-based MCP servers (optional)

## Verifying the Setup

1. Check Ollama is running:
   ```bash
   curl http://localhost:11434/api/tags
   ```

2. Test the MCP server directly:
   ```bash
   cd tools/mcp
   source venv/bin/activate
   python3 -m mcp_ollama_python
   ```

3. In Claude Code, the server should appear in MCP tools when the project loads.

## Adding More MCP Servers

To add another open-source MCP server:

```bash
# Example: Filesystem MCP server (Node.js)
npx -y @modelcontextprotocol/server-filesystem /path/to/allow

# Example: GitHub MCP server
npx -y @modelcontextprotocol/server-github
```

Then add to `.mcp.json`:
```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "."]
    }
  }
}
```

## Resources

- [mcp-ollama-python](https://github.com/pblagoje/mcp-ollama-python) — v1.0.7, MIT License
- [Ollama](https://ollama.com) — Local LLM runner
- [Model Context Protocol](https://modelcontextprotocol.io) — Anthropic protocol spec
