with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

import re

# Remove showAddPersonDialog = true from ACTION_DEBT_CREDIT
content = content.replace(
'''                "ACTION_DEBT_CREDIT" -> {
                    activeTab = "debts"
                    showAddPersonDialog = true
                }''',
'''                "ACTION_DEBT_CREDIT" -> {
                    activeTab = "debts"
                }'''
)

# Replace activeTab = "debts" with setting filters
content = content.replace(
'''                "ACTION_VIEW_DEBT" -> {
                    activeTab = "debts"
                    debtFilter = "DEBT"
                }''',
'''                "ACTION_VIEW_DEBT" -> {
                    activeTab = "debts"
                    debtFilter = "দেনা"
                }'''
)

content = content.replace(
'''                "ACTION_VIEW_CREDIT" -> {
                    activeTab = "debts"
                    debtFilter = "CREDIT"
                }''',
'''                "ACTION_VIEW_CREDIT" -> {
                    activeTab = "debts"
                    debtFilter = "পাওনা"
                }'''
)

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(content)
