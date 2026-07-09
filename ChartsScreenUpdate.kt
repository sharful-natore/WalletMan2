@Composable
fun ChartsScreen(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    onBack: () -> Unit
) {
    val totalIncome = transactions.filter { it.type == "INCOME" || it.type == "BORROW" || it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
    val expenseTransactions = transactions.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }
    
    val expensesByCategory = expenseTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    val totalExpense = expenseTransactions.sumOf { it.amount }
    
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

    val palette = listOf(Color(0xFFEF4444), Color(0xFF3B82F6), Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6), Color(0xFF06B6D4), Color(0xFF10B981))
    val colors = chartValues.indices.map { 
        if (showRemaining && it == chartValues.lastIndex) Color(0xFF10B981) // Green for remaining
        else palette[it % palette.size] 
    }

    val baseTotal = if (totalIncome > 0) totalIncome.toFloat() else totalExpense.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        androidx.activity.compose.BackHandler(onBack = onBack)
        FintechGradientCard(
            gradientColors = listOf(Color(0xFF1E222F), Color(0xFF2A2E3D)),
            cornerRadius = 24.dp,
            padding = PaddingValues(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (language == AppLanguage.BN) "রিপোর্ট চার্ট" else "Report Chart",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                val total = chartValues.sum()
                if (total == 0f) {
                    Text(
                        text = Translation.get("no_tx", language),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                } else {
                    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            chartValues.forEachIndexed { index, value ->
                                if (value > 0) {
                                    val sweepAngle = (value / total) * 360f
                                    drawArc(
                                        color = colors[index],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (language == AppLanguage.BN) "মোট আয়" else "Total Income",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatCurrency(totalIncome, language),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (chartValues.isNotEmpty()) {
            FintechGradientCard(
                gradientColors = listOf(Color(0xFF1E222F), Color(0xFF2A2E3D)),
                cornerRadius = 24.dp,
                padding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    chartValues.forEachIndexed { index, value ->
                        val percent = if (baseTotal > 0) ((value / baseTotal) * 100).toInt() else 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(colors[index]))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = chartLabels[index], color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = formatCurrency(value.toDouble(), language), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "$percent%", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
