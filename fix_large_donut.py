import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

old_shadow_large_single = """                            // Soft glowing shadow matching segment color
                            drawArc(
                                color = segment.second.copy(alpha = 0.04f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx + 12.dp.toPx(), cap = StrokeCap.Butt)
                            )
                            drawArc(
                                color = segment.second.copy(alpha = 0.12f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx + 6.dp.toPx(), cap = StrokeCap.Butt)
                            )"""

new_shadow_large_single = """                            // Soft glowing shadow extending ONLY outwards (blurred)
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
                                this.color = segment.second.copy(alpha = 0.5f)
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
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                paint = shadowPaint
                            )
                            drawContext.canvas.restore()"""


old_shadow_large_multi = """                            // Soft glowing shadow matching segment color
                            drawArc(
                                color = segment.second.copy(alpha = 0.04f),
                                startAngle = adjustedStart,
                                sweepAngle = adjustedSweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx + 12.dp.toPx(), cap = StrokeCap.Butt)
                            )
                            drawArc(
                                color = segment.second.copy(alpha = 0.12f),
                                startAngle = adjustedStart,
                                sweepAngle = adjustedSweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx + 6.dp.toPx(), cap = StrokeCap.Butt)
                            )"""


new_shadow_large_multi = """                            // Soft glowing shadow extending ONLY outwards (blurred)
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
                                this.color = segment.second.copy(alpha = 0.5f)
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
                                startAngle = adjustedStart,
                                sweepAngle = adjustedSweep,
                                useCenter = false,
                                paint = shadowPaint
                            )
                            drawContext.canvas.restore()"""


if old_shadow_large_single in content:
    content = content.replace(old_shadow_large_single, new_shadow_large_single)
    print("Replaced large single segment shadow.")
if old_shadow_large_multi in content:
    content = content.replace(old_shadow_large_multi, new_shadow_large_multi)
    print("Replaced large multi segment shadow.")

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
