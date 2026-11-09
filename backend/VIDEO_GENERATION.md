# Jenny video generation: current status and integration plan

## Current capability

Jenny's chat model can prepare a production package:
- concept and visual direction
- shot-by-shot storyboard
- camera framing and movement
- lighting, color, and continuity notes
- separate generation prompts for each shot
- a consolidated prompt and a short negative prompt

This is planning support only. It does not render a video, create an MP4, or animate an uploaded image.

## Why there is no "free unlimited" cloud provider wired in

A hosted video model needs GPU compute. An open model license does not make hosted inference free. Provider quotas, credit balances, model availability, and terms can change.

Hugging Face currently documents $0.10 monthly Inference Provider credits for free accounts. That is useful for tiny experiments, not a dependable promise of ongoing video generation.

LTX's hosted API bills video generation by output duration and resolution. It is not a free unlimited API.

LTX Desktop documents fully local generation as free on supported Windows/Linux systems with a qualifying NVIDIA GPU (32 GB+ VRAM recommended). This is a desktop GPU workflow, not something a Redmi 14C can run locally.

## Safe integration design

When a provider is selected and its current terms are checked:
1. Keep provider credentials on the server, never in the Android APK or public repository.
2. Add a server-side asynchronous job endpoint (submit, status, result).
3. Validate prompt length, input media type, file size, and job limits.
4. Return an explicit job ID and status; do not claim success until a real output URL is returned.
5. Let the app preview and save a verified MP4 only after generation completes.
6. Display any provider quota, credit use, or cost before submitting a paid job.

## Current project status

- [x] Jenny's assistant prompt includes video-director and storyboard behavior.
- [x] Documentation distinguishes prompt planning from actual video generation.
- [x] Fal video model selected and verified against the live schema.
- [x] Server-side video job submission and status/result routes implemented.
- [x] Android video studio prompt UI and job polling implemented.
- [x] MP4 handoff to the device player implemented.
- [ ] Add the `FAL_KEY` Cloudflare Worker secret and deploy the updated Worker.
- [ ] Add production rate limiting / per-user quota enforcement.
- [ ] End-to-end test with a real generated clip and installable APK.

The integration code is in the open pull request, but live generation is not enabled until the server secret is configured and the Worker is deployed.


## Fal integration (backend)

Jenny's Cloudflare Worker now exposes two Fal-backed routes:

- `POST /video/generate` with JSON `{ "prompt": "...", "duration": 5, "resolution": "480P", "aspect_ratio": "16:9", "image_url": "https://..." }`. Omit `image_url` for text-to-video; provide it for image-to-video.
- `GET /video/status?request_id=<uuid>` to poll the same queued job. A completed response includes `video_url` and `content_type`.

The route uses the verified Fal endpoint `minimax/h3-max-turbo/image-to-video`, which supports prompt-only and image-anchored generation. It limits duration to 3–10 seconds, defaults to 480P, enables the model safety checker, and returns a job ID rather than pretending the video is ready immediately.

### Required Cloudflare setup

1. Create a Fal API key in the Fal dashboard with permission to submit model requests.
2. In the Cloudflare Worker settings for `jenny-ai-private-api`, add a secret named `FAL_KEY` (never put the key in Git, app source, or the APK).
3. Deploy the updated `backend/worker.js`.
4. Test the two routes using a short prompt and a small duration first. Hosted video generation is paid; Fal's account pricing and balance apply.

**Security note:** these routes must be protected against public abuse before a public production release. Configure Cloudflare rate limiting / bot protection for both video routes and monitor Fal spending limits. The current prototype validates inputs and keeps the provider key server-side, but it does not yet implement per-user authentication or durable per-user quotas.

### Android client status

The backend integration is implemented and tested in CI. The Android screen/client still needs to call these routes, display queue/progress states, and preview/save the returned MP4. Until that client work and a real Fal-key end-to-end test are complete, the feature is not yet available inside the installed APK.
