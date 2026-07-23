import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# Fix SegmentedDonutChart
old_radius_1 = """            val strokeWidthPx = strokeWidthDp.toPx()
            val radius = (sizeMin - strokeWidthPx) / 2f"""

new_radius_1 = """            val strokeWidthPx = strokeWidthDp.toPx()
            // Subtract 16.dp to leave 8.dp space for glowing shadow
            val radius = (sizeMin - strokeWidthPx - 16.dp.toPx()) / 2f"""
            
if old_radius_1 in content:
    content = content.replace(old_radius_1, new_radius_1, 1) # only in SegmentedDonutChart, the other uses 16.dp already

# Fix ChartSection
old_radius_2 = """                                val strokeWidthPx = 24.dp.toPx()
                                val radius = (sizeMin - strokeWidthPx) / 2f"""
                                
new_radius_2 = """                                val strokeWidthPx = 24.dp.toPx()
                                // Subtract 16.dp to leave 8.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 16.dp.toPx()) / 2f"""

content = content.replace(old_radius_2, new_radius_2)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
