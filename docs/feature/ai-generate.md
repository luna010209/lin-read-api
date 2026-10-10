# AI generation (Gemini)

Same stack as lin-langa-api: Google GenAI SDK, configured in `application-local.yaml`.

## Configuration

```yaml
gemini:
  api-key: ${GEMINI_APIKEY}
  models:
    image: gemini-2.5-flash-image
    text: gemini-3-flash-preview
    audio: gemini-2.5-flash-preview-tts
```

Set `GEMINI_APIKEY` in `.env.local.properties` (see `.env.local.example`).

## Code layout

| Package | Purpose |
|---------|---------|
| `io.lin.read.config.gemini` | `GeminiProperties`, `Client` bean |
| `io.lin.read.aiGenerate` | `GeminiText`, `GeminiImage`, `GeminiAudio`, `ListeningDialogueParser` |
| `io.lin.read.utils.file` | `AudioUtil` (PCM/WAV/MP3 for TTS) |
| `io.lin.read.prompt` | Read-specific prompts (add as features grow) |

Inject `GeminiText` / `GeminiImage` / `GeminiAudio` from services under `feature/*` when you add Read workflows.
