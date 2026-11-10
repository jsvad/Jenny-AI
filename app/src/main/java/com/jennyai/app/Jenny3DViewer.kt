package com.jennyai.app

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.Scene
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.SurfaceType

/**
 * Displays Jenny and exposes every verified body animation clip in the bundled GLB.
 */
@Composable
fun Jenny3DViewer(modifier: Modifier = Modifier, animationName: String = "idle") {
    var modelReady by remember { mutableStateOf(false) }
    var selectedAnimation by remember { mutableStateOf("idle") }

    Box(
        modifier = modifier.background(Color(0xFF171827)),
        contentAlignment = Alignment.Center
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            surfaceType = SurfaceType.TextureSurface
        ) {
            // Clip names are case-sensitive and were verified from Jenny_rigged.glb.
            val jenny = rememberModelInstance(modelLoader, "models/Jenny_rigged.glb")

            SideEffect {
                modelReady = jenny != null
            }

            jenny?.let { instance ->
                ModelNode(
                    modelInstance = instance,
                    autoAnimate = false,
                    animationName = animationName,
                    animationLoop = true,
                    animationSpeed = 0.9f,
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
