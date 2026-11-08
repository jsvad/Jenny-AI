package com.jennyai.app

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.sceneview.Scene
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position

/**
 * Jenny's 3D room and avatar viewer.
 * Both GLB assets are loaded from app/src/main/assets/models.
 */
@Composable
fun Jenny3DViewer(modifier: Modifier = Modifier) {
    var modelsReady by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.background(Color(0xFF171827)),
        contentAlignment = Alignment.Center
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface
        ) {
            val room = rememberModelInstance(modelLoader, "models/Jenny_room_environment.glb")
            val jenny = rememberModelInstance(modelLoader, "models/Jenny_rigged.glb")

            SideEffect {
                modelsReady = room != null && jenny != null
            }

            room?.let {
                ModelNode(
                    modelInstance = it,
                    autoAnimate = false,
                    scaleToUnits = 5.2f,
                    centerOrigin = Position(x = 0f, y = -1.5f, z = 0f)
                )
            }

            jenny?.let {
                ModelNode(
                    modelInstance = it,
                    autoAnimate = false,
                    animationName = "Idle",
                    animationLoop = true,
                    animationSpeed = 1f,
                    scaleToUnits = 2.0f,
                    centerOrigin = Position(x = 0f, y = -1f, z = 0f)
                )
            }
        }

        if (!modelsReady) {
            Text(
                text = "در حال بارگذاری اتاق و مدل سه‌بعدی جنی…",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
