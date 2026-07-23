import re

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Pattern to find triggerCustomNotification right before a closing brace of a function
# which is after a viewModelScope.launch block.
pattern = r'\}\s*\n\s*triggerCustomNotification\((.*?)\)\}'
replacement = r'    triggerCustomNotification(\1)\n        }\n    }'
content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
