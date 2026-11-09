# Jenny AI 1.0 — Rebuild Specification

Status: implementation specification; not a release build.

This branch is the isolated 1.0 rebuild track. Keep the existing main branch and its 0.3.x prototype intact until the replacement passes device testing.

## Product contract

Jenny is a voice-first Android 3D assistant. The primary interaction is natural Persian speech. Animation controls are not exposed as a row of manual buttons in the main experience. A spoken command is interpreted and dispatched to the avatar controller.

Examples:
- «جنی راه برو» -> WALK
- «جنی بایست» / «همین‌جا وایسا» -> IDLE
- «جنی بشین» -> SIT
- «بلند شو» -> STAND_UP
- «جنی بدو» -> RUN
- «برگرد» -> TURN
- «دست تکون بده» -> WAVE
- «جنی بیا جلو» -> MOVE_FORWARD

The app must acknowledge uncertain commands rather than silently running an unrelated animation. A visible emergency stop remains available for accessibility and reliability.

## Required architecture

Use separate, testable modules/services with narrow interfaces:

1. **SpeechInput** — Android SpeechRecognizer using Persian locale (fa-IR), runtime microphone permission, lifecycle-safe start/stop, partial/final results, clear unavailable/offline/error states. Do not claim continuous hotword listening when Android/device restrictions do not support it. Default to a user-started listening session.
2. **CommandInterpreter** — normalize Persian text (Arabic/Persian ي/ی and ك/ک, spacing and punctuation), match explicit command phrases and safe synonyms, return a typed AvatarCommand or Unknown. Keep command interpretation independent from network AI so basic movement works offline and with low latency.
3. **AvatarController** — single source of truth for avatar state; map typed commands to animation clips; cross-fade transitions; prevent conflicting actions; support interrupt/stop; report missing clips clearly. Resolve actual clip names from the bundled GLB at runtime rather than assuming names.
4. **AvatarView** — render the complete GLB model, fit camera/framing to model bounds, preserve aspect ratio, handle device lifecycle and surface recreation, show loading/error fallback. Verify the full body is in frame on the Redmi 14C screen.
5. **ConversationAI** — separate online chat from deterministic movement commands. AI may propose an action only through validated typed commands; never execute arbitrary model-generated code or raw animation names.
6. **PersianSpeechOutput** — optional TTS response with graceful fallback when fa-IR voice data is absent; do not block avatar motion while speaking.
7. **Persistence/Settings** — retain chat history and preferences; keep secrets off-device and out of source control.

## Interaction flow

Microphone -> SpeechInput -> Persian text -> CommandInterpreter -> AvatarCommand -> AvatarController -> GLB animation.

If no movement command is detected, pass the text to ConversationAI. The chat response may be spoken through TTS. If the user interrupts with a new movement command, cancel/transition the current action smoothly.

## UI requirements

- One dominant, correctly framed full-body avatar stage.
- Persian RTL layout.
- One clear microphone/listening control and transcript/status feedback.
- Conversation composer and message history.
- Remove the manual animation-button strip from the main screen.
- Keep a small, accessible stop control.
- Do not show installation prompts for Persian speech as if they were app errors; provide a settings/help path and text-chat fallback.

## Reliability and acceptance tests

- Unit tests for Persian normalization and every command phrase above.
- Unknown phrases do not trigger movement.
- A new command interrupts or transitions from the previous animation without freezing.
- Missing animation clips produce a recoverable status, not a crash.
- Full body (head through feet) remains visible on portrait screens; validate actual model bounds and camera target.
- Permission denied, speech service unavailable, no network, TTS voice missing, GLB load failure, app background/foreground, and rotation/recreation all have explicit fallback behavior.
- Build and install on a Redmi 14C (Android 16) and verify microphone, speech recognition, rendering, and touch controls on-device.
- A successful CI build is not equivalent to a device-tested release.

## Release gate

Do not label a build “official” or “production” until release signing, dependency/security review, crash-free install/launch, command tests, full-body framing, and real-device acceptance tests are complete. Never commit API keys, signing credentials, or Cloudflare secrets.
