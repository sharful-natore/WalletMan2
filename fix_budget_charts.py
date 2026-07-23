import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

old_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(72.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 13.sp,"""

new_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(86.dp),
                                strokeWidthDp = 16.dp,
                                centerTextSize = 15.sp,"""

old_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(72.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 13.sp,"""

new_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(86.dp),
                                strokeWidthDp = 16.dp,
                                centerTextSize = 15.sp,"""

old_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(72.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 13.sp,"""

new_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(86.dp),
                                strokeWidthDp = 16.dp,
                                centerTextSize = 15.sp,"""

content = content.replace(old_income, new_income)
content = content.replace(old_expense, new_expense)
content = content.replace(old_savings, new_savings)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
