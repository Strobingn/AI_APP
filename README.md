# AI App

Native Android chat client for **local uncensored LLMs** (Ollama / LM Studio).

**Features**
- Streaming chat (OpenAI-compatible `/v1/chat/completions`)
- Whisper + system SpeechRecognizer voice input
- Conversation history (Room)
- Multiple server profiles (Tailscale 100.x.x.x ready)
- Live model list
- Material 3 + dynamic color + adaptive layouts
- Optimized for low-latency Ollama

## Quick Start

### 1. Desktop / Server (Ollama latency optimized)

```bash
export OLLAMA_KEEP_ALIVE=-1
export OLLAMA_FLASH_ATTENTION=1
export OLLAMA_KV_CACHE_TYPE=q8_0
ollama serve
```

Load your uncensored model (e.g. `huihui_ai/gemma-4-abliterated:12b`).

### 2. Tailscale
Install Tailscale on desktop + phone. Note the desktop Tailscale IP (`100.x.x.x`).

### 3. Build the App

```bash
git clone https://github.com/Strobingn/AI_APP.git
cd AI_APP
./gradlew assembleDebug
```

Or push to trigger GitHub Actions and download the APK from Artifacts.

### 4. Configure in App
Settings → Server URL: `http://100.x.x.x:11434/v1` (Ollama) or `:1234/v1` (LM Studio)

## Architecture
- Kotlin + Jetpack Compose + Material 3
- Retrofit + OkHttp (SSE streaming)
- Room for history
- SpeechRecognizer + Whisper-ready structure

Built for maximum speed and privacy on your 12GB 4070 Ti + phone.
