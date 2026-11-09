# Jenny-AI

Jenny-AI is an Android-first Persian-language assistant prototype.

## Current build: 0.3.8

- Kotlin and Jetpack Compose Android app
- Persian right-to-left interface
- 3D Jenny character loaded directly from the bundled `models/jenny.glb` asset, with its Idle animation
- Android speech recognition configured for Persian (`fa-IR`), where the device speech service supports it
- Android text-to-speech configured for Persian (`fa-IR`), where a Persian voice is installed
- Online AI chat through the Jenny Cloudflare Worker endpoint
- Fal-powered text-to-video and image-to-video job submission from the in-app video studio (requires a server-side `FAL_KEY` secret)
- Video job status polling and MP4 handoff to the device's video player
- Limited offline fallback replies when the network or AI service is unavailable
- Conversation transcript saved locally and restored when the app is reopened
- Stop-speaking control
- GitHub Actions builds a debug APK, verifies its APK signature and package metadata, and uploads it as `Jenny-AI-debug`
- CI caches the debug signing key so consecutive test APKs can normally be installed as updates without a signature mismatch

## Important limitations

This is not yet the finished production assistant. Offline replies are a small rule-based fallback, not a local language model. Jenny can help develop video concepts, shot lists, camera directions, and generation prompts through its chat model, but the Fal video studio is available when the Cloudflare Worker has a valid `FAL_KEY` secret configured; a live end-to-end generation still depends on the account's Fal access and balance. Speech recognition may use an online service depending on the Android speech provider and downloaded language packs. Persian text-to-speech requires a compatible voice installed on the device. Audio-driven lip synchronization and advanced facial/body expressions are not implemented yet. No video-generation provider or MP4 rendering pipeline is currently connected; the app must not present prompt planning as completed video generation.

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
