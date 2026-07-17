import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

def replacer(match):
    indent = match.group(1)
    color_var = match.group(2)
    start_angle = match.group(3)
    sweep_angle = match.group(4)
    
    # 0.65f to 0.15f for layering
    return f"""{indent}// Android 8.1 compatible outward glow
{indent}val glowLayers = 5
{indent}val glowSize = 14.dp.toPx()
{indent}for (i in glowLayers downTo 1) {{
{indent}    val fraction = i.toFloat() / glowLayers
{indent}    val currentGlowWidth = glowSize * fraction
{indent}    val glowRadius = radius + (strokeWidthPx / 2f) + (currentGlowWidth / 2f)
{indent}    drawArc(
{indent}        color = {color_var}.copy(alpha = 0.15f),
{indent}        startAngle = {start_angle},
{indent}        sweepAngle = {sweep_angle},
{indent}        useCenter = false,
{indent}        topLeft = androidx.compose.ui.geometry.Offset(center.x - glowRadius, center.y - glowRadius),
{indent}        size = androidx.compose.ui.geometry.Size(glowRadius * 2f, glowRadius * 2f),
{indent}        style = androidx.compose.ui.graphics.drawscope.Stroke(width = currentGlowWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
{indent}    )
{indent}}}"""

# Regex to capture:
# 1. Indentation
# 2. color variable (e.g. `color` or `segment.second`)
# 3. startAngle
# 4. sweepAngle
pattern = r"([ \t]*)drawContext\.canvas\.save\(\)[\s\S]*?this\.color = (.*?)\.copy\([\s\S]*?startAngle = (.*?),[\s\S]*?sweepAngle = (.*?),[\s\S]*?drawContext\.canvas\.restore\(\)"

content = re.sub(pattern, replacer, content)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
