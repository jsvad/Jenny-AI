package com.jennyai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.sceneview.Scene
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.math.Position
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.SurfaceType

/**
 * Displays the rigged Jenny character bundled with the application.
 */
@Composable
fun Jenny3DViewer(animationName: String = "idle", modifier: Modifier = Modifier) {
    var modelReady by remember { mutableStateOf(false) }
    val engine = rememberEngine()
    val cameraNode = rememberCameraNode(engine) {
        position = Position(0f, 1.35f, 5.2f)
        lookAt(Position(0f, 1.15f, 0f))
    }

    Box(
        modifier = modifier.background(Color(0xFF171827)),
        contentAlignment = Alignment.Center
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            cameraNode = cameraNode,
            surfaceType = SurfaceType.TextureSurface
        ) {
            // Load the actual bedroom first so it renders behind Jenny.
            val room = rememberModelInstance(modelLoader, "models/Jenny_room_environment.glb")
            // Jenny remains a separate rigged model so voice commands can switch her clips.
            val jenny = rememberModelInstance(modelLoader, "models/Jenny_rigged.glb")

            SideEffect {
                modelReady = room != null && jenny != null
            }

            room?.let { roomInstance ->
                ModelNode(
                    modelInstance = roomInstance,
                    autoAnimate = false,
                    scaleToUnits = 7.5f,
                    position = Position(0f, 0f, -1.2f)
                )
            }

            jenny?.let { avatarInstance ->
                // Resolve the requested short command ("walk", "run", "wave") to the
                // exact, case-sensitive animation clip name embedded in the GLB.
                val requestedClip = animationName.substringBefore("-").lowercase()
                val clipNames = (0 until avatarInstance.animator.animationCount)
                    .map { avatarInstance.animator.getAnimationName(it) }
                val resolvedClip = clipNames.firstOrNull {
                    it.equals(animationName, ignoreCase = true)
                } ?: clipNames.firstOrNull {
                    it.substringBefore("_").equals(requestedClip, ignoreCase = true) ||
                        it.contains(requestedClip, ignoreCase = true)
                } ?: clipNames.firstOrNull { it.contains("idle", ignoreCase = true) }

                // Walk/run commands also move Jenny to a nearby spot in the room.
                val targetX = when {
                    requestedClip == "walk" -> 0.42f
                    requestedClip == "run" -> 0.82f
                    else -> 0f
                }
                val avatarX by animateFloatAsState(
                    targetValue = targetX,
                    animationSpec = tween(durationMillis = 1100),
                    label = "Jenny room movement"
                )

                ModelNode(
                    modelInstance = avatarInstance,
                    autoAnimate = false,
                    animationName = resolvedClip,
                    animationLoop = true,
                    animationSpeed = 1f,
                    scaleToUnits = 1.75f,
                    position = Position(avatarX, 0.9f, 0.15f)
                )
            }
        }

        if (!modelReady) {
            Text(
                text = "در حال بارگذاری مدل سه‌بعدی جنی…",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
