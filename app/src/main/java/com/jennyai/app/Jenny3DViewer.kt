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
import io.github.sceneview.SurfaceType
import io.github.sceneview.math.Position

/**
 * Jenny's interactive GLB viewer.
 *
 * The model is expected at app/src/main/assets/models/jenny.glb.
 * Touch gestures are handled by SceneView's orbit camera.
 */
@Composable
fun Jenny3DViewer(modifier: Modifier = Modifier) {
    var modelReady by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.background(Color(0xFF171827)),
        contentAlignment = Alignment.Center
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface
        ) {
            val instance = rememberModelInstance(modelLoader, "models/jenny.glb")
            SideEffect {
                modelReady = instance != null
            }
            instance?.let {
                ModelNode(
                    modelInstance = it,
                    autoAnimate = false,
                    animationName = "Idle",
                    animationLoop = true,
                    animationSpeed = 1f,
                    scaleToUnits = 1.7f,
                    centerOrigin = Position(x = 0f, y = -1f, z = 0f)
                )
            }
        }

        if (!modelReady) {
            Text(
                text = "مدل سه‌بعدی جنی در حال بارگذاری است…",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
