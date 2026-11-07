# Jenny-AI

Jenny-AI is an Android-first Persian-language assistant prototype.

## Current build: 0.3.1

- Kotlin and Jetpack Compose Android app
- Persian right-to-left interface
- User-supplied Jenny avatar image
- Android speech recognition configured for Persian (fa-IR), where the device speech service supports it
- Android text-to-speech configured for Persian (fa-IR), where a Persian voice is installed
- Basic offline demo replies for a few common phrases
- Conversation transcript is saved locally and restored when the app is reopened
- Stop-speaking control
- GitHub Actions builds a debug APK, verifies its APK signature and package metadata, and uploads it as `Jenny-AI-debug`
- CI caches the debug signing key so consecutive test APKs can normally be installed as updates without a signature mismatch

## Important limitations

This is not yet the finished AI assistant. The offline reply engine is a small demo, not a local language model. No online AI backend is connected. Speech recognition may use an online service depending on the Android speech provider and downloaded language packs. The supplied avatar is currently a still image; real facial animation and audio-driven lip synchronization are not implemented.

A secure online AI service needs a private backend and a secret API credential. Never commit API keys to this public repository or embed them in the APK. A production backend and its credentials must be configured separately before online AI chat can be enabled.

## Android compatibility

- Application ID: `com.jennyai.app`
- Minimum Android SDK: 26
- Compile / target SDK: 35
- Java: 17
- Target test device: Redmi 14C, 6 GB RAM

## Installing test builds

The debug APK is signed with the CI debug key. The workflow now caches that key so later test builds normally keep the same signing identity. If an older test APK was signed before this cache was introduced, Android may reject an update because its signature differs. In that one-time case, uninstall the older Jenny AI test build before installing the new one. Uninstalling removes that app's local data.

A successful CI build and signature check do not replace a real installation test on the Redmi 14C. If a clean install still fails, the exact Android installer error is needed to identify the device-specific cause.

## Build an APK

Open the Actions tab, choose **Build Jenny AI APK**, and select **Run workflow** on `main`. After a successful run, download the `Jenny-AI-debug` artifact and extract `app-debug.apk`
