import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# Replace the shadow glow logic for all 4 instances
def replace_shadows(content):
    p = r"(\s*)// Soft glowing shadow extending ONLY outwards \(blurred\)\s*drawContext\.canvas\.save\(\)\s*val innerClipRadius = .*?\n.*?clipPath\(innerClipPath, androidx\.compose\.ui\.graphics\.ClipOp\.Difference\)\s*val shadowPaint = androidx\.compose\.ui\.graphics\.Paint\(\)\.apply \{.*?\s*drawContext\.canvas\.drawArc\(.*?\)\s*drawContext\.canvas\.restore\(\)"
    
    r_str = r"""\1// Compatible outward glowing shadow using multiple thin arcs (works on Android 8.1+)
\1val glowLayers = 5
\1val glowSize = 14.dp.toPx()
\1for (i in glowLayers downTo 1) {
\1    val fraction = i.toFloat() / glowLayers
\1    val currentGlowWidth = glowSize * fraction
\1    val glowRadius = radius + (strokeWidthPx / 2f) + (currentGlowWidth / 2f)
\1    drawArc(
\1        color = color.copy(alpha = 0.12f),
\1        startAngle = startAngle,
\1        sweepAngle = sweepAngle + 0.8f,
\1        useCenter = false,
\1        topLeft = androidx.compose.ui.geometry.Offset(center.x - glowRadius, center.y - glowRadius),
\1        size = androidx.compose.ui.geometry.Size(glowRadius * 2f, glowRadius * 2f),
\1        style = Stroke(width = currentGlowWidth, cap = StrokeCap.Butt)
\1    )
\1}"""
    
    # Wait, the color variable for the 3rd instance is segment.second.copy...
    # Let's match more carefully or just do it one by one.
    return content

# I will write a more precise replace logic in Python
