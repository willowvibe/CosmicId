#!/usr/bin/env bash
# Ollama MCP Server startup script
# Usage: ./start-ollama-mcp.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/venv"

# Activate virtual environment
if [ -f "$VENV_DIR/bin/activate" ]; then
    source "$VENV_DIR/bin/activate"
else
    echo "Error: Virtual environment not found at $VENV_DIR"
    echo "Run: python3 -m venv $VENV_DIR && pip install mcp-ollama-python"
    exit 1
fi

# Default Ollama host (local)
export OLLAMA_HOST="${OLLAMA_HOST:-http://localhost:11434}"

# Optional: set default model
export OLLAMA_MODEL="${OLLAMA_MODEL:-qwen3-coder-next:cloud}"

echo "Starting Ollama MCP Server..."
echo "  OLLAMA_HOST: $OLLAMA_HOST"
echo "  OLLAMA_MODEL: $OLLAMA_MODEL"
echo ""

# Run the MCP server via stdio
exec python3 -m mcp_ollama_python
