with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    lines = f.readlines()

start_line = 981
brace_count = 0
found_start = False
for i in range(start_line - 1, len(lines)):
    line = lines[i]
    if "Scaffold(" in line and not found_start:
        found_start = True
        brace_count += line.count("(") - line.count(")")
        continue
    
    if found_start:
        brace_count += line.count("(") - line.count(")")
        if brace_count == 0:
            print(f"End line: {i + 1}")
            break
