with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

# Add it back to FAB onClick
content = content.replace(
    '                    "debt_credit" -> {\n                        \n                    }',
    '                    "debt_credit" -> {\n                        showAddPersonDialog = true\n                    }'
)

content = content.replace(
    '                            "debt_credit" -> {\n                                \n                            }',
    '                            "debt_credit" -> {\n                                showAddPersonDialog = true\n                            }'
)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(content)
