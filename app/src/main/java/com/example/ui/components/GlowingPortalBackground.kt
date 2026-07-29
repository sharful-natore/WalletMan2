package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * Clean rotating long-dashed round border container around the fingerprint icon.
 */
@Composable
fun GlowingPortalBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotating_dashed_border")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_rotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minDim = minOf(size.width, size.height)
            val borderRadius = (minDim / 2f) - 6.dp.toPx()

            // Subtle static guide ring behind dashes
            drawCircle(
                color = Color(0xFF0284C7).copy(alpha = 0.15f),
                radius = borderRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Rotating Long-Dashed / Long-Dotted Round Border
            rotate(degrees = rotationAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF0284C7), // Vivid Cyan-Blue
                            Color(0xFF38BDF8), // Sky Blue
                            Color(0xFF8B5CF6), // Violet
                            Color(0xFF0284C7)  // Back to Cyan-Blue
                        ),
                        center = center
                    ),
                    radius = borderRadius,
                    center = center,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(22.dp.toPx(), 11.dp.toPx()),
                            phase = 0f
                        )
                    )
                )
            }
        }

        // Center Fingerprint Content Slot
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
