import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# For CategorySegmentedDonutChart (single segment)
old_1 = """                    val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                        this.color = color.copy(alpha = 0.5f)
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = strokeWidthPx
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(15.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }"""

new_1 = """                    val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                        this.color = color.copy(alpha = 0.85f)
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }"""

content = content.replace(old_1, new_1)

# For CategorySegmentedDonutChart (multi segment)
old_2 = """                        val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                            this.color = color.copy(alpha = 0.5f)
                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidthPx
                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(15.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                        }"""

new_2 = """                        val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                            this.color = color.copy(alpha = 0.85f)
                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                        }"""

content = content.replace(old_2, new_2)

# For SegmentedDonutChart (single segment)
old_3 = """                            val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                                this.color = segment.second.copy(alpha = 0.5f)
                                this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                this.strokeWidth = strokeWidthPx
                                this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(15.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                            }"""

new_3 = """                            val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                                this.color = segment.second.copy(alpha = 0.85f)
                                this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                                this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                            }"""

content = content.replace(old_3, new_3)

# For SegmentedDonutChart (multi segment)
# Same as old_3 so it will be replaced by old_3 replacement, but let's be careful.

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
