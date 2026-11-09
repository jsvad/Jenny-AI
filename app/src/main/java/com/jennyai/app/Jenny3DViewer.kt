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

/**
 * Displays the rigged Jenny character bundled with the application.
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
            // Use the explicitly rigged asset so later pose tracking can drive its bones.
            val jenny = rememberModelInstance(modelLoader, "models/Jenny_rigged.glb")

            SideEffect {
                modelReady = jenny != null
            }

            jenny?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    autoAnimate = false,
                    animationName = "idle",
                    animationLoop = true,
                    animationSpeed = 1f,
                    scaleToUnits = 2.0f
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
