with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    lines = f.readlines()

# Look for 'val centerBgColor = if (isDark)' at line 209
start = -1
end = -1
for i, line in enumerate(lines):
    if "val centerBgColor = if (isDark) Color(0xFF333333).copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f)" in line:
        start = i
        break

if start != -1:
    # Find the end of the block which is '    }' before '// Unfilled base color'
    for i in range(start + 1, start + 30):
        if "// Unfilled base color" in lines[i]:
            end = i - 1
            break
            
    if end != -1:
        del lines[start+1:end+1]

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.writelines(lines)
