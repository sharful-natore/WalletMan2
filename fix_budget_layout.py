import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# 1. Fix Card padding
old_card_padding = ".padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp)"
new_card_padding = ".padding(start = 10.dp, end = 10.dp, top = 16.dp, bottom = 14.dp)"
content = content.replace(old_card_padding, new_card_padding)

# 2. Fix Row spacing
old_row_spacing = """                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {"""
new_row_spacing = """                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {"""
content = content.replace(old_row_spacing, new_row_spacing)

# 3. Change modifier size to fillMaxWidth().aspectRatio(1f)
old_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(110.dp),
                                strokeWidthDp = 14.dp,"""
new_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(4.dp),
                                strokeWidthDp = 14.dp,"""
content = content.replace(old_income, new_income)

old_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(110.dp),
                                strokeWidthDp = 14.dp,"""
new_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(4.dp),
                                strokeWidthDp = 14.dp,"""
content = content.replace(old_expense, new_expense)

old_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(110.dp),
                                strokeWidthDp = 14.dp,"""
new_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(4.dp),
                                strokeWidthDp = 14.dp,"""
content = content.replace(old_savings, new_savings)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
