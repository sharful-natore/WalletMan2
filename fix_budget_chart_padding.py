import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

old_padding = "modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(4.dp),"
new_padding = "modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(0.dp),"

content = content.replace(old_padding, new_padding)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
