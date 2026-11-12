package com.jennyai.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.sceneview.Scene
import io.github.sceneview.SurfaceType
import io.github.sceneview.rememberModelInstance

/**
 * Renders Jenny inside the bundled 3D bedroom environment.
 *
 * The room is loaded first so its floor and walls form the scene around Jenny.
 * Jenny remains a separate rigged model so her animation clips can be driven
 * independently by voice commands and the chat interface.
 */
@Composable
fun Jenny3DViewer(
    modifier: Modifier = Modifier,
    animationName: String = "idle",
    fallbackBitmap: ImageBitmap? = null
) {
    var modelReady by remember { mutableStateOf(false) }
    var roomReady by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.background(Color(0xFF171827)),
        contentAlignment = Alignment.Center
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface
        ) {
            // The room asset is already bundled in app/src/main/assets/models.
            // Fit the complete room to a modest real-world scale for a portrait scene.
            val room = rememberModelInstance(
                modelLoader,
                "models/Jenny_room_environment.glb"
            )
            val jenny = rememberModelInstance(
                modelLoader,
                "models/Jenny_rigged.glb"
            )

            SideEffect {
                roomReady = room != null
                modelReady = jenny != null
            }

            // Draw the room before Jenny so the character is visually placed
            // within the bedroom rather than on a flat fallback background.
            room?.let { roomInstance ->
                ModelNode(
                    modelInstance = roomInstance,
                    autoAnimate = false,
                    scaleToUnits = 4.0f
                )
            }

            jenny?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    autoAnimate = false,
                    animationName = animationName,
                    animationLoop = true,
                    animationSpeed = 1f,
                    scaleToUnits = 1.35f
                )
            }
        }

        if (!modelReady) {
            if (fallbackBitmap != null) {
                Image(
                    bitmap = fallbackBitmap,
                    contentDescription = "تصویر جنی",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = "در حال آماده‌سازی مدل سه‌بعدی جنی…",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else if (!roomReady) {
            Text(
                text = "در حال بارگذاری اتاق جنی…",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
