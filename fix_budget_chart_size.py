import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# Replace INCOME
old_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(96.dp),
                                strokeWidthDp = 18.dp,
                                centerTextSize = 16.sp,"""

new_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(94.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""

# Replace EXPENSE
old_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(96.dp),
                                strokeWidthDp = 18.dp,
                                centerTextSize = 16.sp,"""

new_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(94.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""

# Replace SAVINGS
old_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(96.dp),
                                strokeWidthDp = 18.dp,
                                centerTextSize = 16.sp,"""

new_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(94.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""

content = content.replace(old_income, new_income)
content = content.replace(old_expense, new_expense)
content = content.replace(old_savings, new_savings)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
