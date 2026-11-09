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
- [ ] Video provider selected and verified for the required age, region, terms, and budget.
- [ ] Server-side video job API.
- [ ] Android video studio UI and upload flow.
- [ ] MP4 preview and save flow.
- [ ] End-to-end test with a real generated clip.

No video provider has been connected and no video-generation endpoint is deployed.
