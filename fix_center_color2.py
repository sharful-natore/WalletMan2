with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    lines = f.readlines()

start = -1
end = -1
for i, line in enumerate(lines):
    if "val centerBgColor = if (targetAmount > 0.0) {" in line:
        start = i
    if start != -1 and "} else {" in line and lines[i+1].strip() == "if (isDark) Color(0xFF1E2235) else Color.White" and lines[i+2].strip() == "}":
        end = i + 2
        break

if start != -1 and end != -1:
    del lines[start:end+1]
    lines.insert(start, "    val centerBgColor = if (isDark) Color(0xFF333333).copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f)\n")

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.writelines(lines)
