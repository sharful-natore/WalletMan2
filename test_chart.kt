@Composable
fun ChartsScreen(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    onBack: () -> Unit
) {
    val totalIncome = transactions.filter { it.type == "INCOME" || it.type == "BORROW" || it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
    
    val expenseTransactions = transactions.filter { it.type == "EXPENSE" }
    val expensesByCategory = expenseTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    val totalExpense = expenseTransactions.sumOf { it.amount }
    
    // We want to show expense categories as slices.
    // To make it percentage of totalIncome, we can add "Remaining" if income > expense.
    // If expense > income, it's over budget, maybe just show expenses.
    
    val categoryNames = expensesByCategory.keys.toList()
    val categoryValues = categoryNames.map { expensesByCategory[it]!!.toFloat() }
    
    val showRemaining = totalIncome > totalExpense
    val remainingValue = if (showRemaining) (totalIncome - totalExpense).toFloat() else 0f
    
    val chartLabels = categoryNames.toMutableList()
    val chartValues = categoryValues.toMutableList()
    
    if (showRemaining && remainingValue > 0) {
        chartLabels.add(if (language == AppLanguage.BN) "অবশিষ্ট আয়" else "Remaining Income")
        chartValues.add(remainingValue)
    }

    // Colors
    val palette = listOf(Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6))
    val chartColors = chartValues.indices.map { 
        if (showRemaining && it == chartValues.lastIndex) Color(0xFF10B981) // Green for remaining
        else palette[it % palette.size] 
    }
    ...
