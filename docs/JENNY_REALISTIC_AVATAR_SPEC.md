# Jenny Realistic Avatar Asset Specification

Status: production specification; the replacement GLB has not yet been generated.

## Visual direction
Use the user-approved Jenny reference image as the single visual source of truth:
- Long black hair with deep crimson/red accents and bangs
- Feline ears integrated naturally into the hairstyle
- Natural, realistic adult facial proportions and skin shading
- Black-and-red gothic streetwear, striped arm warmers, dark skirt, chain details and boots
- Preserve the recognizable silhouette and palette; do not substitute a generic avatar

## Required deliverable
A self-contained glTF 2.0 binary asset at:
`app/src/main/assets/models/Jenny_rigged.glb`

The asset must include:
- Full-body skinned mesh with a humanoid skeleton
- PBR materials and baked/base-color, normal and roughness textures
- Separate, properly weighted hair and cat-ear geometry
- Facial controls suitable for speech: jawOpen, mouthClose, mouthFunnel, mouthPucker, mouthSmileLeft/Right, eyeBlinkLeft/Right, brow controls and cheek controls (or equivalent documented morph targets)
- At minimum: idle, walk, run, wave, jump, and a speaking/viseme-ready facial setup
- Neutral A-pose or T-pose with clean deformation at shoulders, elbows, hips and knees

## Android performance targets
- Must render in the existing SceneView-based Android viewer
- Prefer 1K-2K textures; avoid unnecessary 4K maps
- Keep materials and draw calls modest; use mesh compression where supported
- Target a practical mobile asset size under 20 MB where quality permits
- Validate on a mid-range Android device before replacing the current model

## Integration and validation
1. Keep the existing working model until the replacement passes validation.
2. Verify GLB parses, skeleton is present, animations load, and facial morph targets are discoverable.
3. Verify all animation clip names against the viewer controls; names are case-sensitive.
4. Test idle/walk/run/wave/jump playback and speech facial controls.
5. Only then replace the production asset and run the Android CI build.

## Important limitation
A 2D reference image or generated character sheet is not a rigged 3D model. Do not rename or copy a still image as a GLB, and do not claim facial animation until the GLB contains working facial controls.
