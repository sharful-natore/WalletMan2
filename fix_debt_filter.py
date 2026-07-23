with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''                "ACTION_VIEW_DEBT" -> {
                    activeTab = "debts"
                    debtFilter = "দেনা"
                }''',
'''                "ACTION_VIEW_DEBT" -> {
                    activeTab = "debts"
                    debtFilter = "DENA"
                }'''
)

content = content.replace(
'''                "ACTION_VIEW_CREDIT" -> {
                    activeTab = "debts"
                    debtFilter = "পাওনা"
                }''',
'''                "ACTION_VIEW_CREDIT" -> {
                    activeTab = "debts"
                    debtFilter = "PAWN"
                }'''
)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(content)
