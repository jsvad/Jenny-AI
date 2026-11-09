# Jenny-AI

Jenny-AI is an Android-first Persian-language assistant prototype.

## Rebuild track: 1.0.0-rc1 (not production-ready)

- Kotlin and Jetpack Compose Android app
- Persian right-to-left interface
- Rigged Jenny character loaded from `models/Jenny_rigged.glb`; animation selection is now driven by voice-command state
- Persian (`fa-IR`) speech recognition with a foreground continuous-listening session that can be toggled by the user
- Offline Persian movement-command routing for walk, run, idle/stop, sit, stand, wave, and turn
- Android text-to-speech configured for Persian (`fa-IR`), where a Persian voice is installed
- Online AI chat through the Jenny Cloudflare Worker endpoint
- Limited offline fallback replies when the network or AI service is unavailable
- Conversation transcript saved locally and restored when the app is reopened
- Stop-speaking control
- GitHub Actions builds a debug APK, verifies its APK signature and package metadata, and uploads it as `Jenny-AI-debug`
- CI caches the debug signing key so consecutive test APKs can normally be installed as updates without a signature mismatch

## Important limitations

This is a release-candidate rebuild, not an official production release. Voice commands are wired to animation identifiers, but the exact animation clip names and availability in the bundled GLB must be verified on-device; do not assume every action will animate until that check passes. Offline replies are a small rule-based fallback, not a local language model. Jenny can help develop video concepts, shot lists, camera directions, and generation prompts through its chat model, but direct text-to-video or image-to-video generation is not integrated yet. Speech recognition may use an online service depending on the Android speech provider and downloaded language packs. Persian text-to-speech requires a compatible voice installed on the device. Audio-driven lip synchronization and advanced facial/body expressions are not implemented yet. The new voice-command router is deterministic and local; it does not require an AI request for supported movement commands. No video-generation provider or MP4 rendering pipeline is currently connected; the app must not present prompt planning as completed video generation.

The online AI service requires a correctly configured Cloudflare Worker AI binding. Never commit API keys or private credentials to this public repository or embed them in the APK.

## Android compatibility

- Application ID: `com.jennyai.app`
- Minimum Android SDK: 26
- Compile / target SDK: 35
- Java: 17
- Target test device: Redmi 14C, 6 GB RAM

## Installing test builds

The debug APK is signed with the CI debug key. The workflow caches that key so later test builds normally keep the same signing identity. If an older test APK was signed before this cache was introduced, Android may reject an update because its signature differs. In that one-time case, uninstall the older Jenny AI test build before installing the new one. Uninstalling removes that app's local data.

A successful CI build and signature check do not replace a real installation test on the Redmi 14C.

## Build an APK

Open the Actions tab, choose **Build Jenny AI APK**, and select **Run workflow** on `main`. After a successful run, download the `Jenny-AI-debug` artifact and extract `app-debug.apk`.


## 1.0 rebuild branch

The isolated `release/1.0.0-rebuild` branch preserves `main`. It includes a Persian voice-command interpreter, avatar animation state wiring, a hands-free foreground listening session, and CI build/emulator workflows. The editable product UI concept is in the Figma file: https://www.figma.com/design/9uVwjoS0O1LQLP7DWzoJCj

### Voice commands

Tap the microphone once to start a foreground listening session; tap it again to stop. Say commands such as «جنی راه برو», «جنی بدو», «جنی بایست», «جنی بشین», «جنی بلند شو», «جنی دست تکون بده» or «جنی برگرد». Normal phrases are sent to chat instead of being treated as motion commands. Speech recognition availability depends on the Android speech service and its Persian language support.

### Release gate

The `1.0.0-rc1` label is a development candidate only. Do not distribute it as official until the build and emulator workflows pass, the bundled GLB animation clip names are verified, and the app is installed and tested on the target Redmi 14C device.
