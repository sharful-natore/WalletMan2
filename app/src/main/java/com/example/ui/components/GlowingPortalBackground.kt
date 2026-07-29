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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Reusable animated background layer component for Finance Note.
 * Provides a rotating glowing neon portal/vortex ring with electric sparks,
 * floating particles, and a central circular hollow frame for fingerprint/biometric icons.
 */
@Composable
fun GlowingPortalBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "portal_vortex_transition")

    // Continuous 360 degree rotation for the swirling vortex ring
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_rotation"
    )

    // Smooth pulse animation for ambient radial glow, particle opacities, and electric sparks
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portal_pulse"
    )

    // Secondary counter-rotation for energy depth
    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_counter_rotation"
    )

    // Pre-calculated particles for stable rendering
    val particleCount = 20
    val particles = remember {
        List(particleCount) { index ->
            val baseAngle = index * (360f / particleCount) + (index * 7 % 13)
            val radiusFactor = 0.38f + (index % 5) * 0.035f
            val speedFactor = 0.6f + (index % 4) * 0.3f
            val particleSize = 1.8f + (index % 3) * 1.2f
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
            val ringThickness = outerRadius * 0.22f
            val ringRadius = outerRadius - (ringThickness / 2f) - 4.dp.toPx()
            val hollowRadius = ringRadius - (ringThickness / 2f)

            // Neon Colors
            val electricCyan = Color(0xFF00F0FF)
            val neonPurple = Color(0xFF8B5CF6)
            val neonBlue = Color(0xFF3B82F6)
            val deepViolet = Color(0xFF6D28D9)
            val brightMagenta = Color(0xFFEC4899)

            // 1. Central Ambient Glow Behind Ring
            val ambientGlowRadius = outerRadius * (0.95f + pulseAnim * 0.12f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        electricCyan.copy(alpha = 0.28f + pulseAnim * 0.12f),
                        neonPurple.copy(alpha = 0.20f + pulseAnim * 0.08f),
                        neonBlue.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = ambientGlowRadius
                ),
                radius = ambientGlowRadius,
                center = center
            )

            // 2. Main Rotating Vortex Sweep Ring (Clockwise)
            rotate(degrees = rotationAngle, pivot = center) {
                val vortexColors = listOf(
                    electricCyan,
                    neonPurple,
                    neonBlue,
                    brightMagenta,
                    electricCyan
                )

                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = vortexColors,
                        center = center
                    ),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(width = ringThickness, cap = StrokeCap.Round)
                )
            }

            // 3. Secondary Inner Energy Wave Ring (Counter-Clockwise)
            rotate(degrees = counterRotationAngle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            electricCyan.copy(alpha = 0.60f),
                            deepViolet.copy(alpha = 0.30f),
                            Color.Transparent
                        ),
                        center = center
                    ),
                    radius = ringRadius - ringThickness * 0.2f,
                    center = center,
                    style = Stroke(width = ringThickness * 0.35f)
                )
            }

            // 4. Central Circular Hollow Frame / Hole Borders
            // Crisp Inner Border
            drawCircle(
                color = electricCyan.copy(alpha = 0.7f + pulseAnim * 0.25f),
                radius = hollowRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Subtle Outer Ring Boundary
            drawCircle(
                color = neonPurple.copy(alpha = 0.45f),
                radius = ringRadius + (ringThickness / 2f),
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 5. Electric Sparks & Floating Particles
            particles.forEach { p ->
                val dynamicAngle = Math.toRadians((p.baseAngle + rotationAngle * p.speedFactor) % 360)
                val radialDist = outerRadius * (p.radiusFactor + sin(pulseAnim * Math.PI + p.index).toFloat() * 0.06f)
                val pX = center.x + radialDist * cos(dynamicAngle).toFloat()
                val pY = center.y + radialDist * sin(dynamicAngle).toFloat()
                val particleAlpha = (0.35f + 0.65f * sin(pulseAnim * Math.PI * 2 + p.index).toFloat()).coerceIn(0.1f, 1.0f)

                // Particle Dot
                val pColor = when (p.index % 3) {
                    0 -> electricCyan
                    1 -> neonPurple
                    else -> neonBlue
                }

                drawCircle(
                    color = pColor.copy(alpha = particleAlpha),
                    radius = p.size.dp.toPx(),
                    center = Offset(pX, pY)
                )

                // Subtle Electric Spark Lines
                if (p.index % 4 == 0) {
                    val sparkLen = (6.dp.toPx() + pulseAnim * 4.dp.toPx())
                    val sparkAngle = dynamicAngle + Math.toRadians((20.0 * if (p.index % 2 == 0) 1 else -1))
                    val endX = pX + sparkLen * cos(sparkAngle).toFloat()
                    val endY = pY + sparkLen * sin(sparkAngle).toFloat()

                    drawLine(
                        color = Color.White.copy(alpha = particleAlpha * 0.85f),
                        start = Offset(pX, pY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Central Content Slot (Fingerprint Icon)
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
