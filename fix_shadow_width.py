import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

p = r"this\.strokeWidth = strokeWidthPx\n"
r_str = "this.strokeWidth = strokeWidthPx + 8.dp.toPx()\n"

content = re.sub(p, r_str, content)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
