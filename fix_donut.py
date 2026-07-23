import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# Replace CategorySegmentedDonutChart shadow logic
old_shadow_single = """                    // Soft glowing shadow matching segment color extending ONLY outwards (blurred)
                    val shadowSteps = 10
                    val maxShadowWidthPx = 8.dp.toPx()
                    val stepPx = maxShadowWidthPx / shadowSteps
                    for (i in 1..shadowSteps) {
                        val shadowRadius = radius + strokeWidthPx / 2f + (i - 0.5f) * stepPx
                        val shadowTopLeft = androidx.compose.ui.geometry.Offset(center.x - shadowRadius, center.y - shadowRadius)
                        val shadowSize = androidx.compose.ui.geometry.Size(shadowRadius * 2f, shadowRadius * 2f)
                        val fraction = i.toFloat() / shadowSteps
                        val alpha = 0.16f * (1f - fraction) * (1f - fraction)
                        drawArc(
                            color = color.copy(alpha = alpha),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = shadowTopLeft,
                            size = shadowSize,
                            style = Stroke(width = stepPx)
                        )
                    }"""

new_shadow_single = """                    // Soft glowing shadow extending ONLY outwards (blurred)
                    drawContext.canvas.save()
                    val innerClipRadius = radius + strokeWidthPx / 2f - 1f
                    val innerClipPath = androidx.compose.ui.graphics.Path().apply {
                        addOval(androidx.compose.ui.geometry.Rect(
                            center.x - innerClipRadius, center.y - innerClipRadius,
                            center.x + innerClipRadius, center.y + innerClipRadius
                        ))
                    }
                    drawContext.canvas.clipPath(innerClipPath, androidx.compose.ui.graphics.ClipOp.Difference)
                    val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                        this.color = color.copy(alpha = 0.5f)
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = strokeWidthPx
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(15.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                    drawContext.canvas.drawArc(
                        rect = androidx.compose.ui.geometry.Rect(
                            center.x - radius, center.y - radius,
                            center.x + radius, center.y + radius
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        paint = shadowPaint
                    )
                    drawContext.canvas.restore()"""

old_shadow_multi = """                        // Soft glowing shadow matching segment color extending ONLY outwards (blurred)
                        val shadowSteps = 10
                        val maxShadowWidthPx = 8.dp.toPx()
                        val stepPx = maxShadowWidthPx / shadowSteps
                        for (i in 1..shadowSteps) {
                            val shadowRadius = radius + strokeWidthPx / 2f + (i - 0.5f) * stepPx
                            val shadowTopLeft = androidx.compose.ui.geometry.Offset(center.x - shadowRadius, center.y - shadowRadius)
                            val shadowSize = androidx.compose.ui.geometry.Size(shadowRadius * 2f, shadowRadius * 2f)
                            val fraction = i.toFloat() / shadowSteps
                            val alpha = 0.16f * (1f - fraction) * (1f - fraction)
                            drawArc(
                                color = color.copy(alpha = alpha),
                                startAngle = startAngle,
                                sweepAngle = allocatedSweep + 0.8f,
                                useCenter = false,
                                topLeft = shadowTopLeft,
                                size = shadowSize,
                                style = Stroke(width = stepPx, cap = StrokeCap.Butt)
                            )
                        }"""

new_shadow_multi = """                        // Soft glowing shadow extending ONLY outwards (blurred)
                        drawContext.canvas.save()
                        val innerClipRadius = radius + strokeWidthPx / 2f - 1f
                        val innerClipPath = androidx.compose.ui.graphics.Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(
                                center.x - innerClipRadius, center.y - innerClipRadius,
                                center.x + innerClipRadius, center.y + innerClipRadius
                            ))
                        }
                        drawContext.canvas.clipPath(innerClipPath, androidx.compose.ui.graphics.ClipOp.Difference)
                        val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                            this.color = color.copy(alpha = 0.5f)
                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidthPx
                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(15.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                        }
                        drawContext.canvas.drawArc(
                            rect = androidx.compose.ui.geometry.Rect(
                                center.x - radius, center.y - radius,
                                center.x + radius, center.y + radius
                            ),
                            startAngle = startAngle,
                            sweepAngle = allocatedSweep + 0.8f,
                            useCenter = false,
                            paint = shadowPaint
                        )
                        drawContext.canvas.restore()"""

if old_shadow_single in content:
    content = content.replace(old_shadow_single, new_shadow_single)
    print("Replaced single segment shadow.")
if old_shadow_multi in content:
    content = content.replace(old_shadow_multi, new_shadow_multi)
    print("Replaced multi segment shadow.")

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
