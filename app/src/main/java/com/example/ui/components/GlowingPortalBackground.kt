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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated cosmic vortex portal background matching user design reference.
 * Features a swirling 3D energy ring, spiral vortex tendrils, electric lightning discharges,
 * floating cosmic dust, and a hollow center.
 */
@Composable
fun GlowingPortalBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portal_vortex_transition")

    // Continuous rotation for swirling energy vortex
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_rotation"
    )

    // Secondary counter-rotation for inner energy spiral
    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_counter_rotation"
    )

    // Pulse animation for electric flickering and atmospheric glow
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portal_pulse"
    )

    // Lightning flicker trigger
    val lightningPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightning_flicker"
    )

    // Pre-calculated particles & lightning bolt angles
    val particleCount = 28
    val particles = remember {
        List(particleCount) { index ->
            val baseAngle = index * (360f / particleCount) + (index * 11 % 17)
            val radiusFactor = 0.35f + (index % 6) * 0.04f
            val speedFactor = 0.5f + (index % 5) * 0.25f
            val particleSize = 1.2f + (index % 4) * 0.9f
            ParticleSpec(baseAngle.toDouble(), radiusFactor, speedFactor, particleSize, index)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minDim = minOf(size.width, size.height)
            val outerRadius = minDim / 2f
            val ringThickness = outerRadius * 0.26f
            val ringRadius = outerRadius - (ringThickness / 2f) - 6.dp.toPx()
            val hollowRadius = ringRadius - (ringThickness / 2f) - 2.dp.toPx()

            // Cosmic Neon Palette matching image
            val electricCyan = Color(0xFF00F0FF)
            val electricBlue = Color(0xFF3B82F6)
            val neonPurple = Color(0xFF8B5CF6)
            val deepViolet = Color(0xFF6D28D9)
            val hotMagenta = Color(0xFFEC4899)
            val pureWhite = Color(0xFFFFFFFF)

            // 1. Dark Void Outer Glow
            val atmosphericGlow = outerRadius * (0.95f + pulseAnim * 0.1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        electricCyan.copy(alpha = 0.32f + pulseAnim * 0.12f),
                        neonPurple.copy(alpha = 0.22f + pulseAnim * 0.08f),
                        deepViolet.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = atmosphericGlow
                ),
                radius = atmosphericGlow,
                center = center
            )

            // 2. Main Swirling Vortex Gradient Ring (Clockwise Sweep)
            rotate(degrees = rotationAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            electricCyan,
                            electricBlue,
                            neonPurple,
                            hotMagenta,
                            pureWhite,
                            electricCyan
                        ),
                        center = center
                    ),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = ringThickness, cap = StrokeCap.Round)
                )
            }

            // 3. Spiral Vortex Tendrils (Curved Spiral Arms wrapping into the portal)
            val armCount = 8
            rotate(degrees = rotationAngle, pivot = center) {
                for (i in 0 until armCount) {
                    val startAngle = i * (360f / armCount)
                    val spiralPath = Path()
                    val steps = 18
                    val startR = ringRadius + ringThickness * 0.4f
                    val endR = hollowRadius + 2.dp.toPx()

                    for (step in 0..steps) {
                        val progress = step.toFloat() / steps
                        val currentR = startR - (startR - endR) * progress
                        val currentAngleRad = Math.toRadians((startAngle + progress * 75.0))
                        val x = center.x + currentR * cos(currentAngleRad).toFloat()
                        val y = center.y + currentR * sin(currentAngleRad).toFloat()

                        if (step == 0) {
                            spiralPath.moveTo(x, y)
                        } else {
                            spiralPath.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = spiralPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                pureWhite.copy(alpha = 0.85f),
                                electricCyan.copy(alpha = 0.70f),
                                neonPurple.copy(alpha = 0.30f),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(
                            width = (2.5f + (i % 3) * 1.2f).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            // 4. Secondary Counter-Rotating Energy Wave
            rotate(degrees = counterRotationAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            pureWhite.copy(alpha = 0.90f),
                            electricCyan.copy(alpha = 0.65f),
                            electricBlue.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = center
                    ),
                    radius = ringRadius - ringThickness * 0.15f,
                    center = center,
                    style = Stroke(width = ringThickness * 0.3f)
                )
            }

            // 5. Electric Lightning Bolts / Sparks around the ring
            val boltCount = 6
            val rand = Random(42)
            for (b in 0 until boltCount) {
                val boltBaseAngle = b * (360f / boltCount) + (rotationAngle * 0.8f % 360f)
                val boltAlpha = if ((b + (lightningPhase * 10).toInt()) % 2 == 0) 0.85f else 0.25f

                if (boltAlpha > 0.4f) {
                    val angleRad = Math.toRadians(boltBaseAngle.toDouble())
                    val innerPt = Offset(
                        center.x + (hollowRadius - 2.dp.toPx()) * cos(angleRad).toFloat(),
                        center.y + (hollowRadius - 2.dp.toPx()) * sin(angleRad).toFloat()
                    )
                    val outerPt = Offset(
                        center.x + (ringRadius + ringThickness * 0.5f) * cos(angleRad).toFloat(),
                        center.y + (ringRadius + ringThickness * 0.5f) * sin(angleRad).toFloat()
                    )

                    // Draw jagged lightning segments
                    val mid1 = Offset(
                        innerPt.x + (outerPt.x - innerPt.x) * 0.35f + (if (b % 2 == 0) 8.dp.toPx() else -8.dp.toPx()),
                        innerPt.y + (outerPt.y - innerPt.y) * 0.35f + (if (b % 3 == 0) -8.dp.toPx() else 8.dp.toPx())
                    )
                    val mid2 = Offset(
                        innerPt.x + (outerPt.x - innerPt.x) * 0.70f + (if (b % 2 == 0) -6.dp.toPx() else 6.dp.toPx()),
                        innerPt.y + (outerPt.y - innerPt.y) * 0.70f + (if (b % 3 == 0) 6.dp.toPx() else -6.dp.toPx())
                    )

                    val boltPath = Path().apply {
                        moveTo(innerPt.x, innerPt.y)
                        lineTo(mid1.x, mid1.y)
                        lineTo(mid2.x, mid2.y)
                        lineTo(outerPt.x, outerPt.y)
                    }

                    // Lightning outer glow
                    drawPath(
                        path = boltPath,
                        color = electricCyan.copy(alpha = boltAlpha * 0.6f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Lightning crisp white core
                    drawPath(
                        path = boltPath,
                        color = pureWhite.copy(alpha = boltAlpha),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 6. Crisp Inner/Outer Ring Rims (Defining Hollow Boundary)
            drawCircle(
                color = electricCyan.copy(alpha = 0.85f + pulseAnim * 0.15f),
                radius = hollowRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = neonPurple.copy(alpha = 0.5f),
                radius = ringRadius + (ringThickness / 2f),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 7. Cosmic Stars / Embers
            particles.forEach { p ->
                val dynamicAngle = Math.toRadians((p.baseAngle + rotationAngle * p.speedFactor) % 360)
                val radialDist = outerRadius * (p.radiusFactor + sin(pulseAnim * Math.PI + p.index).toFloat() * 0.05f)
                val pX = center.x + radialDist * cos(dynamicAngle).toFloat()
                val pY = center.y + radialDist * sin(dynamicAngle).toFloat()
                val particleAlpha = (0.3f + 0.7f * sin(pulseAnim * Math.PI * 2 + p.index).toFloat()).coerceIn(0.1f, 1.0f)

                val pColor = when (p.index % 3) {
                    0 -> pureWhite
                    1 -> electricCyan
                    else -> hotMagenta
                }

                drawCircle(
                    color = pColor.copy(alpha = particleAlpha),
                    radius = p.size.dp.toPx(),
                    center = Offset(pX, pY)
                )
            }
        }

        // Central Content Slot (Fingerprint Icon with White Background)
        Box(
            modifier = Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

private data class ParticleSpec(
    val baseAngle: Double,
    val radiusFactor: Float,
    val speedFactor: Float,
    val size: Float,
    val index: Int
)
