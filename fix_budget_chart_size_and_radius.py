import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# Fix radius reduction to 8.dp
old_radius_1 = """            // Subtract 16.dp to leave 8.dp space for glowing shadow
            val radius = (sizeMin - strokeWidthPx - 16.dp.toPx()) / 2f"""
new_radius_1 = """            // Subtract 8.dp to leave 4.dp space for glowing shadow
            val radius = (sizeMin - strokeWidthPx - 8.dp.toPx()) / 2f"""
content = content.replace(old_radius_1, new_radius_1)

old_radius_2 = """                                // Subtract 16.dp to leave 8.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 16.dp.toPx()) / 2f"""
new_radius_2 = """                                // Subtract 12.dp to leave 6.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 12.dp.toPx()) / 2f"""
content = content.replace(old_radius_2, new_radius_2)

# Fix chart size to 100.dp
old_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(94.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""

new_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(100.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
content = content.replace(old_income, new_income)

old_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(94.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
new_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(100.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
content = content.replace(old_expense, new_expense)

old_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(94.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
new_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(100.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
content = content.replace(old_savings, new_savings)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
