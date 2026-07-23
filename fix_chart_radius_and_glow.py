import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# Fix radius space
old_radius_cat = """            val strokeWidthPx = strokeWidthDp.toPx()
            // Subtract 8.dp to leave 4.dp space for glowing shadow
            val radius = (sizeMin - strokeWidthPx - 8.dp.toPx()) / 2f"""
new_radius_cat = """            val strokeWidthPx = strokeWidthDp.toPx()
            // Subtract 24.dp to leave 12.dp space for glowing shadow to avoid clipping
            val radius = (sizeMin - strokeWidthPx - 24.dp.toPx()) / 2f"""
content = content.replace(old_radius_cat, new_radius_cat)

old_radius_chart = """                                val strokeWidthPx = 24.dp.toPx()
                                // Subtract 12.dp to leave 6.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 12.dp.toPx()) / 2f"""
new_radius_chart = """                                val strokeWidthPx = 24.dp.toPx()
                                // Subtract 24.dp to leave 12.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 24.dp.toPx()) / 2f"""
content = content.replace(old_radius_chart, new_radius_chart)

# Increase chart sizes by 10%
# Currently 100.dp, increase to 110.dp
old_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(100.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
new_income = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = income,
                                segments = incomeByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(110.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
content = content.replace(old_income, new_income)

old_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(100.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
new_expense = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = expense,
                                segments = expenseByCategory,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(110.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
content = content.replace(old_expense, new_expense)

old_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(100.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
new_savings = """                            CategorySegmentedDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = totalSavingsAmount,
                                segments = savingsByGoal,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(110.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,"""
content = content.replace(old_savings, new_savings)


with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(content)
