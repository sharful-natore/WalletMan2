with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

import re

# Add viewModel parameter to the four composables
content = content.replace("fun AddTransactionDialog(", "fun AddTransactionDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")
content = content.replace("fun AddPersonDialog(", "fun AddPersonDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")
content = content.replace("fun AddSavingsGoalDialog(", "fun AddSavingsGoalDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")
content = content.replace("fun SavingsContributionDialog(", "fun SavingsContributionDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")

# And update their call sites in FinanceNoteApp
content = content.replace("AddTransactionDialog(", "AddTransactionDialog(viewModel = viewModel, ")
content = content.replace("AddPersonDialog(", "AddPersonDialog(viewModel = viewModel, ")
content = content.replace("AddSavingsGoalDialog(", "AddSavingsGoalDialog(viewModel = viewModel, ")
content = content.replace("SavingsContributionDialog(", "SavingsContributionDialog(viewModel = viewModel, ")

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(content)
