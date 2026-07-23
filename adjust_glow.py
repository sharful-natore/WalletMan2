import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

def replacer(match):
    indent = match.group(1)
    color_var = match.group(2)
    start_angle = match.group(3)
    sweep_angle = match.group(4)
    
    # Reducing glowSize from 12dp to 8dp (less spread)
    # Increasing glowLayers from 15 to 20 (smoother/more blurred look)
    # Alpha slightly reduced for softer feel
    return f"""{indent}// Android 8.1 compatible natural outward glow
{indent}val glowLayers = 20
{indent}val glowSize = 8.dp.toPx()
{indent}for (i in glowLayers downTo 1) {{
{indent}    val fraction = i.toFloat() / glowLayers
{indent}    val currentGlowWidth = glowSize * fraction
{indent}    val glowRadius = radius + (strokeWidthPx / 2f) + (currentGlowWidth / 2f)
{indent}    drawArc(
{indent}        color = {color_var}.copy(alpha = 0.035f),
{indent}        startAngle = {start_angle},
{indent}        sweepAngle = {sweep_angle},
{indent}        useCenter = false,
{indent}        topLeft = androidx.compose.ui.geometry.Offset(center.x - glowRadius, center.y - glowRadius),
{indent}        size = androidx.compose.ui.geometry.Size(glowRadius * 2f, glowRadius * 2f),
{indent}        style = androidx.compose.ui.graphics.drawscope.Stroke(width = currentGlowWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
{indent}    )
{indent}}}"""

pattern = r"([ \t]*)// Android 8.1 compatible natural outward glow\s*val glowLayers = 15[\s\S]*?color = (.*?)\.copy\(alpha = 0\.04f\),[\s\S]*?startAngle = (.*?),[\s\S]*?sweepAngle = (.*?),[\s\S]*?style = androidx\.compose\.ui\.graphics\.drawscope\.Stroke\(width = currentGlowWidth, cap = androidx\.compose\.ui\.graphics\.StrokeCap\.Butt\)[\s\S]*?\n[ \t]*\}"

new_content = re.sub(pattern, replacer, content)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(new_content)
