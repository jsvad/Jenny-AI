# Jenny 3D model asset

Place the approved rigged character model at:

`app/src/main/assets/models/rigged-model.glb`

Verified source asset (provided in the Jenny-AI development conversation):
- Format: GLB 2.0
- File size: 8,051,708 bytes
- Meshes: 1
- Nodes: 29
- Skins: 1 (27 joints)
- Animations: Bind, Idle, Walk, Run, Wave

The source GLB has not yet been uploaded into this repository. Do not treat this placeholder as the model itself.

Before enabling runtime rendering, validate the GLB in a viewer and select an Android renderer compatible with the project's minSdk 26. Preserve the source file unchanged; any optimized or converted model should be stored as a separately named asset.

Expected animation names:
- `Idle`
- `Walk`
- `Run`
- `Wave`
- `Bind`

A future Android integration should load the model from the app assets, expose animation selection, and handle renderer lifecycle with the Activity lifecycle.
