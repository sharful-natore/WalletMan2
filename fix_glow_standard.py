import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

old_glow_pattern = """                                            this.color = color.copy(alpha = 0.85f)
                                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                            this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""

new_glow_pattern = """                                            this.color = color.copy(alpha = 0.4f)
                                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                            this.strokeWidth = strokeWidthPx + 4.dp.toPx()
                                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(12.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""

content = content.replace(old_glow_pattern, new_glow_pattern)

old_glow_pattern_2 = """                        this.color = color.copy(alpha = 0.85f)
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""

new_glow_pattern_2 = """                        this.color = color.copy(alpha = 0.4f)
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = strokeWidthPx + 4.dp.toPx()
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(12.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""
                        
content = content.replace(old_glow_pattern_2, new_glow_pattern_2)

old_glow_pattern_3 = """                            this.color = color.copy(alpha = 0.85f)
                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""

new_glow_pattern_3 = """                            this.color = color.copy(alpha = 0.4f)
                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidthPx + 4.dp.toPx()
                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(12.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""
                            
content = content.replace(old_glow_pattern_3, new_glow_pattern_3)

old_glow_pattern_4 = """                                this.color = segment.second.copy(alpha = 0.85f)
                                this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                this.strokeWidth = strokeWidthPx + 8.dp.toPx()
                                this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(20.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""

new_glow_pattern_4 = """                                this.color = segment.second.copy(alpha = 0.4f)
                                this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                this.strokeWidth = strokeWidthPx + 4.dp.toPx()
                                this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(12.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)"""

content = content.replace(old_glow_pattern_4, new_glow_pattern_4)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
