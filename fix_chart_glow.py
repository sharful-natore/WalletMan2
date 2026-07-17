import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

old_shadow = """                                        // Soft glowing shadow matching segment color
                                        drawArc(
                                            color = color.copy(alpha = 0.04f),
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle + 0.8f,
                                            useCenter = false,
                                            topLeft = arcTopLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidthPx + 12.dp.toPx(), cap = StrokeCap.Butt)
                                        )
                                        drawArc(
                                            color = color.copy(alpha = 0.12f),
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle + 0.8f,
                                            useCenter = false,
                                            topLeft = arcTopLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidthPx + 6.dp.toPx(), cap = StrokeCap.Butt)
                                        )"""

new_shadow = """                                        // Soft glowing shadow extending ONLY outwards (blurred)
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
                                            this.color = color.copy(alpha = 0.85f)
                                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                            this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                                        }
                                        drawContext.canvas.drawArc(
                                            rect = androidx.compose.ui.geometry.Rect(
                                                center.x - radius, center.y - radius,
                                                center.x + radius, center.y + radius
                                            ),
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle + 0.8f,
                                            useCenter = false,
                                            paint = shadowPaint
                                        )
                                        drawContext.canvas.restore()"""

if old_shadow in content:
    content = content.replace(old_shadow, new_shadow)
    print("Replaced ChartSection shadow.")
else:
    print("ChartSection shadow NOT FOUND.")

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
