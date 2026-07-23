import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

p = r"    val centerBgColor = if \(targetAmount > 0\.0\) \{[\s\S]*?\} else \{\n        FintechBlue\.copy\(alpha = 0\.1f\)\n    \}"

r_str = "    val centerBgColor = if (isDark) Color(0xFF333333).copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.4f)"

content = re.sub(p, r_str, content)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
