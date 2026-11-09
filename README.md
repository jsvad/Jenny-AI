# Jenny-AI

Jenny-AI is an Android-first Persian-language assistant prototype.

## Current build: 0.3.7

- Kotlin and Jetpack Compose Android app
- Persian right-to-left interface
- 3D Jenny character loaded from the bundled `models/Jenny_rigged.glb` asset
- Scrollable on-screen controls for all seven verified body animation clips
- Android speech recognition configured for Persian (`fa-IR`), where the device speech service supports it
- Android text-to-speech configured for Persian (`fa-IR`), where a Persian voice is installed
- Online AI chat through the Jenny Cloudflare Worker endpoint
- Limited offline fallback replies when the network or AI service is unavailable
- Conversation transcript saved locally and restored when the app is reopened
- Stop-speaking control
- GitHub Actions builds a debug APK, verifies its APK signature and package metadata, and uploads it as `Jenny-AI-debug`
- CI caches the debug signing key so consecutive test APKs can normally be installed as updates without a signature mismatch

## Important limitations

This is not yet the finished production assistant. Offline replies are a small rule-based fallback, not a local language model. Jenny can help develop video concepts, shot lists, camera directions, and generation prompts through its chat model, but direct text-to-video or image-to-video generation is not integrated yet. Speech recognition may use an online service depending on the Android speech provider and downloaded language packs. Persian text-to-speech requires a compatible voice installed on the device. The bundled GLB has a skeletal rig and body animation clips, but no facial morph targets; facial-expression controls and audio-driven lip synchronization are not implemented. No video-generation provider or MP4 rendering pipeline is currently connected; the app must not present prompt planning as completed video generation.

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

## Avatar animation status

The GLB contains seven case-sensitive animation clips: `idle`, `walk`, `run`, `walk-cartoon`, `run-cartoon`, `wave`, and `jump`. The viewer uses `autoAnimate = false` and explicitly selects a clip, so it does not play every animation at once. The current scrollable UI exposes all seven clips. The model has 22 skin joints and no morph targets, so these clips animate the body but do not provide facial expressions or lip synchronization.

## Build an APK

Open the Actions tab, choose **Build Jenny AI APK**, and select **Run workflow** on `main`. Pull requests targeting `main` also trigger the build workflow. After a successful run, download the `Jenny-AI-debug` artifact and extract `app-debug.apk`.
