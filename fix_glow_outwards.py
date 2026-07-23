import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

clip_code = """                    val innerClipRadius = radius - strokeWidthPx / 2f
                    val innerClipPath = androidx.compose.ui.graphics.Path().apply {
                        addOval(androidx.compose.ui.geometry.Rect(
                            center.x - innerClipRadius, center.y - innerClipRadius,
                            center.x + innerClipRadius, center.y + innerClipRadius
                        ))
                    }
                    drawContext.canvas.clipPath(innerClipPath, androidx.compose.ui.graphics.ClipOp.Difference)
"""

clip_code_2 = """                        val innerClipRadius = radius - strokeWidthPx / 2f
                        val innerClipPath = androidx.compose.ui.graphics.Path().apply {
                            addOval(androidx.compose.ui.geometry.Rect(
                                center.x - innerClipRadius, center.y - innerClipRadius,
                                center.x + innerClipRadius, center.y + innerClipRadius
                            ))
                        }
                        drawContext.canvas.clipPath(innerClipPath, androidx.compose.ui.graphics.ClipOp.Difference)
"""

clip_code_3 = """                            val innerClipRadius = radius - strokeWidthPx / 2f
                            val innerClipPath = androidx.compose.ui.graphics.Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(
                                    center.x - innerClipRadius, center.y - innerClipRadius,
                                    center.x + innerClipRadius, center.y + innerClipRadius
                                ))
                            }
                            drawContext.canvas.clipPath(innerClipPath, androidx.compose.ui.graphics.ClipOp.Difference)
"""

clip_code_4 = """                                        val innerClipRadius = radius - strokeWidthPx / 2f
                                        val innerClipPath = androidx.compose.ui.graphics.Path().apply {
                                            addOval(androidx.compose.ui.geometry.Rect(
                                                center.x - innerClipRadius, center.y - innerClipRadius,
                                                center.x + innerClipRadius, center.y + innerClipRadius
                                            ))
                                        }
                                        drawContext.canvas.clipPath(innerClipPath, androidx.compose.ui.graphics.ClipOp.Difference)
"""

def apply_fix():
    global content
    
    # 1. Budget Chart Progress
    p1 = r"                    drawContext\.canvas\.save\(\)\n\n                    val shadowPaint = androidx\.compose\.ui\.graphics\.Paint\(\)\.apply \{\n                        this\.color = color\.copy\(alpha = 0\.6f\)"
    r1 = "                    drawContext.canvas.save()\n\n" + clip_code + "                    val shadowPaint = androidx.compose.ui.graphics.Paint().apply {\n                        this.color = color.copy(alpha = 0.9f)"
    content = re.sub(p1, r1, content)
    
    # 2. SegmentedDonutChart Total Progress
    p2 = r"                        drawContext\.canvas\.save\(\)\n\n                        val shadowPaint = androidx\.compose\.ui\.graphics\.Paint\(\)\.apply \{\n                            this\.color = color\.copy\(alpha = 0\.6f\)"
    r2 = "                        drawContext.canvas.save()\n\n" + clip_code_2 + "                        val shadowPaint = androidx.compose.ui.graphics.Paint().apply {\n                            this.color = color.copy(alpha = 0.9f)"
    content = re.sub(p2, r2, content)

    # 3 & 4. CategorySegmentedDonutChart Segments
    p3 = r"                            drawContext\.canvas\.save\(\)\n\n                            val shadowPaint = androidx\.compose\.ui\.graphics\.Paint\(\)\.apply \{\n                                this\.color = segment\.second\.copy\(alpha = 0\.6f\)"
    r3 = "                            drawContext.canvas.save()\n\n" + clip_code_3 + "                            val shadowPaint = androidx.compose.ui.graphics.Paint().apply {\n                                this.color = segment.second.copy(alpha = 0.9f)"
    content = re.sub(p3, r3, content)

    # 5. ChartSection Segments
    p4 = r"                                        drawContext\.canvas\.save\(\)\n\n                                        val shadowPaint = androidx\.compose\.ui\.graphics\.Paint\(\)\.apply \{\n                                            this\.color = color\.copy\(alpha = 0\.6f\)"
    r4 = "                                        drawContext.canvas.save()\n\n" + clip_code_4 + "                                        val shadowPaint = androidx.compose.ui.graphics.Paint().apply {\n                                            this.color = color.copy(alpha = 0.9f)"
    content = re.sub(p4, r4, content)

apply_fix()

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
