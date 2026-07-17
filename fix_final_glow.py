import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# 1. Reduce shadow alpha from 0.9f to 0.6f
content = content.replace("alpha = 0.9f", "alpha = 0.6f")

# 2. Reset shadow stroke width to just strokeWidthPx (removing the + 8.dp.toPx())
content = content.replace("this.strokeWidth = strokeWidthPx + 8.dp.toPx()", "this.strokeWidth = strokeWidthPx")

# 3. Reduce Chart page's donut chart thickness
old_chart_thickness = "val strokeWidthPx = 24.dp.toPx()"
new_chart_thickness = "val strokeWidthPx = 16.dp.toPx()"
content = content.replace(old_chart_thickness, new_chart_thickness)

# 4. Adjust Chart page's donut chart radius (was using 24.dp space, let's use 12.dp since thickness is smaller)
old_chart_radius = """                                val strokeWidthPx = 16.dp.toPx()
                                // Subtract 24.dp to leave 12.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 24.dp.toPx()) / 2f"""
new_chart_radius = """                                val strokeWidthPx = 16.dp.toPx()
                                // Subtract 16.dp to leave 8.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 16.dp.toPx()) / 2f"""
content = content.replace(old_chart_radius, new_chart_radius)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
