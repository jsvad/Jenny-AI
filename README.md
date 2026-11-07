# Jenny-AI

Jenny-AI is an Android-first Persian-language AI assistant project.

## Current status

This repository contains the first Android application skeleton (version 0.1.0):
- Kotlin and Jetpack Compose Android app
- Initial Persian right-to-left friendly interface
- Placeholder text chat and microphone controls
- GitHub Actions workflow to build a debug APK

The chat and microphone buttons are currently placeholders. No AI service, speech recognition, text-to-speech, realistic avatar, lip synchronization, or offline language model has been connected yet.

## Build an APK

1. Open the **Actions** tab in this repository.
2. Select **Build Jenny AI APK**.
3. If prompted, enable GitHub Actions for the repository.
4. Run the workflow with **Run workflow** on the `main` branch.
5. When the run succeeds, open the run and download the `Jenny-AI-debug` artifact.
6. Extract the ZIP file to get `app-debug.apk`.

## Technical baseline

- Android application ID: `com.jennyai.app`
- Minimum Android SDK: 26
- Compile / target SDK: 35
- Java: 17
- Kotlin: 2.0.21
- Android Gradle Plugin: 8.7.3

## Planned development

1. Validate the first APK on a physical Android phone.
2. Add Persian speech-to-text and text-to-speech.
3. Connect a secure online AI backend (API keys must not be stored in the APK or public repository).
4. Add a consistent realistic avatar and audio-driven lip synchronization.
5. Evaluate lightweight offline features suitable for the target phone.
