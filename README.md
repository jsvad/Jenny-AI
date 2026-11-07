# Jenny-AI

Jenny-AI is an Android-first Persian-language assistant prototype.

## Current build: 0.3.0

- Kotlin and Jetpack Compose Android app
- Persian right-to-left interface
- User-supplied Jenny avatar image
- Android speech recognition configured for Persian (fa-IR), where the device speech service supports it
- Android text-to-speech configured for Persian (fa-IR), where a Persian voice is installed
- Basic offline demo replies for a few common phrases
- Conversation transcript remains visible during the current app session
- Stop-speaking control
- GitHub Actions builds a debug APK and uploads it as `Jenny-AI-debug`

## Important limitations

This is not yet the finished AI assistant. The offline reply engine is a small demo, not a local language model. No online AI backend is connected. Speech recognition may use an online service depending on the Android speech provider and downloaded language packs. The supplied avatar is currently a still image; real facial animation and audio-driven lip synchronization are not implemented.

A secure online AI service needs a private backend and a secret API credential. Never commit API keys to this public repository or embed them in the APK. A production backend and its credentials must be configured separately before online AI chat can be enabled.

## Android compatibility

- Application ID: `com.jennyai.app`
- Minimum Android SDK: 26
- Compile / target SDK: 35
- Java: 17
- Target test device: Redmi 14C, 6 GB RAM

## Build an APK

Open the Actions tab, choose **Build Jenny AI APK**, and select **Run workflow** on `main`. After a successful run, download the `Jenny-AI-debug` artifact and extract `app-debug.apk`.
