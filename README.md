# AI App

Native Android chat client for **local uncensored LLMs** (Ollama / LM Studio).

**Repo:** https://github.com/Strobingn/AI_APP

## Current Features
- Real SSE streaming responses
- Room conversation history
- DataStore settings (server URL + model name)
- Settings screen
- Voice input (Android SpeechRecognizer) — Whisper structure ready
- Material 3 + dynamic color
- Tailscale-ready

## Desktop / Server Setup (Low Latency)

```bash
export OLLAMA_KEEP_ALIVE=-1
export OLLAMA_FLASH_ATTENTION=1
export OLLAMA_KV_CACHE_TYPE=q8_0
ollama serve
```

Load your uncensored model, e.g.:
```bash
ollama run huihui_ai/gemma-4-abliterated:12b
```

## Phone Setup
1. Install Tailscale on phone + desktop
2. Note desktop Tailscale IP (`100.x.x.x`)
3. Build & install the APK (or download from Actions)
4. Open app → Settings → paste `http://100.x.x.x:11434/v1`
5. Set model name to whatever is loaded in Ollama

## Build
```bash
git clone https://github.com/Strobingn/AI_APP.git
cd AI_APP
./gradlew assembleDebug
```

Or just push and grab the APK from GitHub Actions artifacts.

## Next (when you get home)
- Full on-device Whisper.cpp
- Multi-conversation list
- Image upload for multimodal
- Model auto-discovery from /v1/models

Built for your 12GB 4070 Ti + phone. Keep expanding.
