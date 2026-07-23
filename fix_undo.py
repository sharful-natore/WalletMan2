with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'r') as f:
    content = f.read()

content = content.replace("fun AddTransactionDialog(viewModel = viewModel, viewModel: com.example.ui.viewmodel.FinanceViewModel, ", "fun AddTransactionDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")
content = content.replace("fun AddPersonDialog(viewModel = viewModel, viewModel: com.example.ui.viewmodel.FinanceViewModel, ", "fun AddPersonDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")
content = content.replace("fun AddSavingsGoalDialog(viewModel = viewModel, viewModel: com.example.ui.viewmodel.FinanceViewModel, ", "fun AddSavingsGoalDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")
content = content.replace("fun SavingsContributionDialog(viewModel = viewModel, viewModel: com.example.ui.viewmodel.FinanceViewModel, ", "fun SavingsContributionDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, ")

with open('app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt', 'w') as f:
    f.write(content)
