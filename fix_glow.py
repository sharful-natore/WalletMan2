import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

def replace_glow(content, old_alpha, new_alpha, old_width, new_width, old_blur, new_blur):
    pattern1 = f"""                        this.color = color.copy(alpha = {old_alpha})
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = strokeWidthPx + {old_width}
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({old_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    rep1 = f"""                        this.color = color.copy(alpha = {new_alpha})
                        this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                        this.strokeWidth = strokeWidthPx
                        this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                        asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({new_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    content = content.replace(pattern1, rep1)

    pattern2 = f"""                            this.color = color.copy(alpha = {old_alpha})
                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidthPx + {old_width}
                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({old_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    rep2 = f"""                            this.color = color.copy(alpha = {new_alpha})
                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                            this.strokeWidth = strokeWidthPx
                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({new_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    content = content.replace(pattern2, rep2)
    
    pattern3 = f"""                                this.color = segment.second.copy(alpha = {old_alpha})
                                this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                this.strokeWidth = strokeWidthPx + {old_width}
                                this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({old_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    rep3 = f"""                                this.color = segment.second.copy(alpha = {new_alpha})
                                this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                this.strokeWidth = strokeWidthPx
                                this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({new_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    content = content.replace(pattern3, rep3)

    pattern4 = f"""                                            this.color = color.copy(alpha = {old_alpha})
                                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                            this.strokeWidth = strokeWidthPx + {old_width}
                                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({old_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    rep4 = f"""                                            this.color = color.copy(alpha = {new_alpha})
                                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                            this.strokeWidth = strokeWidthPx
                                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter({new_blur}, android.graphics.BlurMaskFilter.Blur.NORMAL)"""
    content = content.replace(pattern4, rep4)

    return content

content = replace_glow(content, "0.4f", "0.5f", "4.dp.toPx()", "0.dp.toPx()", "12.dp.toPx()", "16.dp.toPx()")

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
