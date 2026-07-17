import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# Replace the specific calls
p1 = """        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী আয়" else "Income by Category",
            data = incomesByCategory,
            total = totalIncome,
            palette = palette,
            language = language,
            isDark = isDark,
            targetAmount = budgetIncomeAmount,
            categoryType = "INCOME"
        )"""

r1 = """        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী আয়" else "Income by Category",
            data = incomesByCategory,
            total = totalIncome,
            palette = palette,
            language = language,
            isDark = isDark
        )"""

p2 = """        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী ব্যয়" else "Expense by Category",
            data = expensesByCategory,
            total = totalExpense,
            palette = palette.reversed(), // slightly different colors
            language = language,
            isDark = isDark,
            targetAmount = budgetExpenseAmount,
            categoryType = "EXPENSE"
        )"""

r2 = """        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী ব্যয়" else "Expense by Category",
            data = expensesByCategory,
            total = totalExpense,
            palette = palette.reversed(), // slightly different colors
            language = language,
            isDark = isDark
        )"""

content = content.replace(p1, r1)
content = content.replace(p2, r2)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
