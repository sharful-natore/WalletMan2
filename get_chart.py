import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

p = r"(@Composable\nfun ChartSection\([\s\S]*?)\n@Composable\nfun CategoryLegend"
match = re.search(p, content)
if match:
    with open("chart_section_code.txt", "w") as out:
        out.write(match.group(1))
