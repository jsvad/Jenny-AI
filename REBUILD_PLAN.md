# Jenny AI clean rebuild plan

Target: Android phone Redmi 14C (6 GB RAM), Android app id `com.jennyai.app`.

## Starting point audited
- Kotlin + Jetpack Compose Android application.
- Persian UI, Android speech recognition (fa-IR), Android text-to-speech (fa-IR).
- Local conversation transcript persistence.
- Existing GLB asset: `app/src/main/assets/models/rigged-model.glb` (8,051,708 bytes in the feature branch).
- Existing app is currently concentrated in one `MainActivity.kt`.
- Current reply logic is a small offline demo, not an AI model.
- The current 3D preview renders incorrectly on the target phone; do not assume the model or renderer is valid until tested.

## Rebuild principles
1. Preserve the existing GLB and avatar image as source assets; never overwrite the originals.
2. Keep the current `main` branch untouched. Work only on `rebuild/jenny-ai-clean-architecture`.
3. Separate UI, avatar rendering, speech, chat, settings, and persistence into small testable components.
4. Use a safe static-avatar fallback whenever 3D loading or rendering fails.
5. Do not embed private API keys in the Android app. Online AI must use a separately configured backend.
6. Treat offline chat as offline only when a local model is actually bundled and verified; otherwise label it accurately as demo/fallback.
7. Keep memory use and rendering quality appropriate for a 6 GB Android device.

## Implementation sequence
- Phase 1: establish a clean Compose app shell, navigation/state ownership, Persian RTL theme, and stable avatar fallback.
- Phase 2: validate the GLB asset (mesh, skinning, materials, textures, skeleton, animation names) independently of the app.
- Phase 3: integrate the validated model with lifecycle-safe SceneView loading, camera framing, scale, and animation controls.
- Phase 4: implement speech input/output with permission handling and clear online/offline status.
- Phase 5: add a secure online AI backend integration; keep secrets server-side.
- Phase 6: evaluate local inference options against the phone's RAM/storage budget.
- Phase 7: run CI build, APK signature/package checks, and a real-device installation/interaction checklist.

## Acceptance checks
- App launches without a crash and remains usable if the 3D model fails.
- Avatar is framed correctly and never appears stretched or clipped.
- Idle/Wave/Walk/Run controls visibly change the intended animation.
- Persian text entry, microphone permission, speech recognition, TTS, and stop-speaking are independently testable.
- Conversation history survives app restart.
- UI labels clearly distinguish demo replies, local model replies, and online AI replies.
- A successful CI build is not described as device-verified until tested on the Redmi 14C.
