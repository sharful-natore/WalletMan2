package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.data.Person
import com.example.data.SavingsGoal
import com.example.data.SavingsTransaction
import com.example.data.Transaction
import com.example.data.MonthlyBudget
import com.example.ui.AppLanguage
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategoryContribution(
    val categoryName: String,
    val amount: Double,
    val percentageOfTarget: Double,
    val percentageOfTotal: Double
)

data class BudgetTypeAnalysis(
    val typeName: String,
    val targetAmount: Double,
    val actualAmount: Double,
    val fulfillmentPercentage: Double,
    val remainingAmount: Double,
    val categories: List<CategoryContribution>
)

data class ComprehensiveBudgetReportData(
    val incomeAnalysis: BudgetTypeAnalysis,
    val expenseAnalysis: BudgetTypeAnalysis,
    val savingsAnalysis: BudgetTypeAnalysis
)

fun computeComprehensiveBudgetReport(
    language: AppLanguage,
    filteredBudgets: List<MonthlyBudget>,
    transactions: List<Transaction>,
    savingsTransactions: List<SavingsTransaction>,
    savingsGoals: List<SavingsGoal>
): ComprehensiveBudgetReportData {
    val isBn = language == AppLanguage.BN

    val incTarget = filteredBudgets.sumOf { it.income }
    val expTarget = filteredBudgets.sumOf { it.expense }
    val savTarget = filteredBudgets.sumOf { it.savings }

    // Actual Income
    val incTxs = transactions.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }
    val actualInc = incTxs.sumOf { it.amount }
    val incPct = if (incTarget > 0) (actualInc / incTarget) * 100 else 0.0
    val incRem = incTarget - actualInc

    val incCats = incTxs.groupBy { it.category }
        .map { (cat, txs) ->
            val sum = txs.sumOf { it.amount }
            CategoryContribution(
                categoryName = cat,
                amount = sum,
                percentageOfTarget = if (incTarget > 0) (sum / incTarget) * 100 else 0.0,
                percentageOfTotal = if (actualInc > 0) (sum / actualInc) * 100 else 0.0
            )
        }.sortedByDescending { it.amount }

    val incomeAnalysis = BudgetTypeAnalysis(
        typeName = if (isBn) "আয় বাজেট" else "Income Budget",
        targetAmount = incTarget,
        actualAmount = actualInc,
        fulfillmentPercentage = incPct,
        remainingAmount = incRem,
        categories = incCats
    )

    // Actual Expense
    val expTxs = transactions.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }
    val actualExp = expTxs.sumOf { it.amount }
    val expPct = if (expTarget > 0) (actualExp / expTarget) * 100 else 0.0
    val expRem = expTarget - actualExp

    val expCats = expTxs.groupBy { it.category }
        .map { (cat, txs) ->
            val sum = txs.sumOf { it.amount }
            CategoryContribution(
                categoryName = cat,
                amount = sum,
                percentageOfTarget = if (expTarget > 0) (sum / expTarget) * 100 else 0.0,
                percentageOfTotal = if (actualExp > 0) (sum / actualExp) * 100 else 0.0
            )
        }.sortedByDescending { it.amount }

    val expenseAnalysis = BudgetTypeAnalysis(
        typeName = if (isBn) "ব্যয় বাজেট" else "Expense Budget",
        targetAmount = expTarget,
        actualAmount = actualExp,
        fulfillmentPercentage = expPct,
        remainingAmount = expRem,
        categories = expCats
    )

    // Actual Savings
    val actualSav = savingsTransactions.sumOf { if (it.isDeposit) it.amount else -it.amount }
    val savPct = if (savTarget > 0) (actualSav / savTarget) * 100 else 0.0
    val savRem = savTarget - actualSav

    val savCats = savingsTransactions.groupBy { it.goalId }
        .map { (goalId, stxs) ->
            val goalTitle = savingsGoals.find { it.id == goalId }?.title ?: (if (isBn) "সাধারণ সঞ্চয়" else "General Savings")
            val sum = stxs.sumOf { if (it.isDeposit) it.amount else -it.amount }
            CategoryContribution(
                categoryName = goalTitle,
                amount = sum,
                percentageOfTarget = if (savTarget > 0) (sum / savTarget) * 100 else 0.0,
                percentageOfTotal = if (actualSav > 0) (sum / actualSav) * 100 else 0.0
            )
        }.sortedByDescending { it.amount }

    val savingsAnalysis = BudgetTypeAnalysis(
        typeName = if (isBn) "সঞ্চয় বাজেট" else "Savings Budget",
        targetAmount = savTarget,
        actualAmount = actualSav,
        fulfillmentPercentage = savPct,
        remainingAmount = savRem,
        categories = savCats
    )

    return ComprehensiveBudgetReportData(
        incomeAnalysis = incomeAnalysis,
        expenseAnalysis = expenseAnalysis,
        savingsAnalysis = savingsAnalysis
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    savingsGoals: List<SavingsGoal>,
    savingsTransactions: List<SavingsTransaction>,
    monthlyBudgets: List<MonthlyBudget> = emptyList(),
    defaultBudgetIncome: Double = 0.0,
    defaultBudgetExpense: Double = 0.0,
    defaultBudgetSavings: Double = 0.0,
    initialCategory: String = "ALL_DATA",
    initialTimeFilter: String? = null,
    initialPerson: Person? = null,
    initialSavingsGoal: SavingsGoal? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BN
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    // 1. Format state (PDF, Excel, CSV)
    var selectedFormat by remember { mutableStateOf("PDF") }

    // 2. Data Type state
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }

    // 3. Time Filter state
    var selectedTimeFilter by remember(initialTimeFilter, initialPerson, initialSavingsGoal) {
        mutableStateOf(
            when {
                initialPerson != null || initialSavingsGoal != null || initialTimeFilter == "ALL" -> "ALL_TIME"
                initialTimeFilter == null -> "MONTH"
                initialTimeFilter == "TODAY" -> "DATE_RANGE"
                initialTimeFilter == "MONTH" -> "MONTH"
                initialTimeFilter.startsWith("CUSTOM_MONTH:") -> "MONTH"
                initialTimeFilter.startsWith("CUSTOM_DATE:") -> "DATE_RANGE"
                initialTimeFilter.startsWith("RANGE:") -> "DATE_RANGE"
                initialTimeFilter.startsWith("MONTH_RANGE:") -> "MONTH_RANGE"
                initialTimeFilter.startsWith("YEAR_RANGE:") -> "YEAR_RANGE"
                else -> "MONTH"
            }
        )
    }

    // Selected Month & Year
    val calendar = Calendar.getInstance()
    var selectedMonth by remember(initialTimeFilter) {
        val mVal = when {
            initialTimeFilter != null && initialTimeFilter.startsWith("CUSTOM_MONTH:") -> {
                val parts = initialTimeFilter.substringAfter("CUSTOM_MONTH:").split("-")
                (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
            }
            initialTimeFilter != null && initialTimeFilter.startsWith("CUSTOM_DATE:") -> {
                val parts = initialTimeFilter.substringAfter("CUSTOM_DATE:").split("-")
                (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
            }
            else -> calendar.get(Calendar.MONTH)
        }
        mutableStateOf(mVal)
    }

    var selectedYear by remember(initialTimeFilter) {
        val yVal = when {
            initialTimeFilter != null && initialTimeFilter.startsWith("CUSTOM_MONTH:") -> {
                val parts = initialTimeFilter.substringAfter("CUSTOM_MONTH:").split("-")
                parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
            }
            initialTimeFilter != null && initialTimeFilter.startsWith("CUSTOM_DATE:") -> {
                val parts = initialTimeFilter.substringAfter("CUSTOM_DATE:").split("-")
                parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
            }
            else -> calendar.get(Calendar.YEAR)
        }
        mutableStateOf(yVal)
    }

    // Date range
    var startDateMillis by remember(initialTimeFilter) {
        val startVal = when {
            initialTimeFilter == "TODAY" -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            initialTimeFilter != null && initialTimeFilter.startsWith("CUSTOM_DATE:") -> {
                val parts = initialTimeFilter.substringAfter("CUSTOM_DATE:").split("-")
                val y = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
                val m = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
                Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }.timeInMillis
            }
            initialTimeFilter != null && initialTimeFilter.startsWith("RANGE:") -> {
                val parts = initialTimeFilter.substringAfter("RANGE:").split("-")
                parts.getOrNull(0)?.toLongOrNull() ?: (System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L)
            }
            else -> System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
        }
        mutableStateOf(startVal)
    }

    var endDateMillis by remember(initialTimeFilter) {
        val endVal = when {
            initialTimeFilter == "TODAY" -> {
                Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis
            }
            initialTimeFilter != null && initialTimeFilter.startsWith("CUSTOM_DATE:") -> {
                val parts = initialTimeFilter.substringAfter("CUSTOM_DATE:").split("-")
                val y = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
                val m = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
                Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }.timeInMillis
            }
            initialTimeFilter != null && initialTimeFilter.startsWith("RANGE:") -> {
                val parts = initialTimeFilter.substringAfter("RANGE:").split("-")
                parts.getOrNull(1)?.toLongOrNull() ?: System.currentTimeMillis()
            }
            else -> System.currentTimeMillis()
        }
        mutableStateOf(endVal)
    }

    // Month range
    var startMonth by remember(initialTimeFilter) {
        val valM = if (initialTimeFilter != null && initialTimeFilter.startsWith("MONTH_RANGE:")) {
            val parts = initialTimeFilter.substringAfter("MONTH_RANGE:").split("-")
            (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
        } else 0
        mutableStateOf(valM)
    }
    var startYear by remember(initialTimeFilter) {
        val valY = if (initialTimeFilter != null && initialTimeFilter.startsWith("MONTH_RANGE:")) {
            val parts = initialTimeFilter.substringAfter("MONTH_RANGE:").split("-")
            parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        } else calendar.get(Calendar.YEAR)
        mutableStateOf(valY)
    }
    var endMonth by remember(initialTimeFilter) {
        val valM = if (initialTimeFilter != null && initialTimeFilter.startsWith("MONTH_RANGE:")) {
            val parts = initialTimeFilter.substringAfter("MONTH_RANGE:").split("-")
            (parts.getOrNull(3)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
        } else calendar.get(Calendar.MONTH)
        mutableStateOf(valM)
    }
    var endYear by remember(initialTimeFilter) {
        val valY = if (initialTimeFilter != null && initialTimeFilter.startsWith("MONTH_RANGE:")) {
            val parts = initialTimeFilter.substringAfter("MONTH_RANGE:").split("-")
            parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        } else calendar.get(Calendar.YEAR)
        mutableStateOf(valY)
    }

    // Year range
    var startYearRange by remember(initialTimeFilter) {
        val valY = if (initialTimeFilter != null && initialTimeFilter.startsWith("YEAR_RANGE:")) {
            val parts = initialTimeFilter.substringAfter("YEAR_RANGE:").split("-")
            parts.getOrNull(0)?.toIntOrNull() ?: (calendar.get(Calendar.YEAR) - 1)
        } else (calendar.get(Calendar.YEAR) - 1)
        mutableStateOf(valY)
    }
    var endYearRange by remember(initialTimeFilter) {
        val valY = if (initialTimeFilter != null && initialTimeFilter.startsWith("YEAR_RANGE:")) {
            val parts = initialTimeFilter.substringAfter("YEAR_RANGE:").split("-")
            parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
        } else calendar.get(Calendar.YEAR)
        mutableStateOf(valY)
    }

    // Person filter state
    var filterByPerson by remember(initialPerson) { mutableStateOf(initialPerson != null) }
    var selectedPerson by remember(initialPerson) { mutableStateOf<Person?>(initialPerson) }
    var showPersonDropdown by remember { mutableStateOf(false) }

    // Savings Goal filter state
    var filterBySavingsGoal by remember(initialSavingsGoal) { mutableStateOf(initialSavingsGoal != null) }
    var selectedSavingsGoal by remember(initialSavingsGoal) { mutableStateOf<SavingsGoal?>(initialSavingsGoal) }
    var showSavingsGoalDropdown by remember { mutableStateOf(false) }

    // List of unique years from transactions
    val uniqueYears = remember(transactions) {
        val years = mutableSetOf<Int>()
        years.add(Calendar.getInstance().get(Calendar.YEAR))
        transactions.forEach {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.timestamp
            years.add(cal.get(Calendar.YEAR))
        }
        years.toList().sortedDescending()
    }

    // Filter computation
    val filteredTx = remember(
        transactions, selectedCategory, selectedTimeFilter,
        selectedMonth, selectedYear, startDateMillis, endDateMillis,
        startMonth, startYear, endMonth, endYear, startYearRange, endYearRange,
        selectedPerson, filterByPerson, selectedSavingsGoal, filterBySavingsGoal
    ) {
        if (filterBySavingsGoal && selectedSavingsGoal != null) {
            return@remember emptyList<Transaction>()
        }

        var list = transactions

        // Category Filter
        list = when (selectedCategory) {
            "ALL_DATA", "TRANSACTIONS", "ONLY_BUDGET" -> list
            "DEBT_REPAYMENT" -> list.filter { it.type == "LEND" || it.type == "BORROW" || it.type == "REPAY_PAID" || it.type == "REPAY_RECEIVED" }
            "INCOME_EXPENSE" -> list.filter { it.type == "INCOME" || it.type == "EXPENSE" }
            "ONLY_DEBT" -> list.filter { it.type == "BORROW" || it.type == "REPAY_PAID" }
            "ONLY_PAONA" -> list.filter { it.type == "LEND" || it.type == "REPAY_RECEIVED" }
            "ONLY_INCOME" -> list.filter { it.type == "INCOME" }
            "ONLY_EXPENSE" -> list.filter { it.type == "EXPENSE" }
            "ONLY_SAVINGS" -> emptyList()
            else -> list
        }

        // Person Filter
        val showPersonFilterOption = selectedCategory != "ONLY_SAVINGS" && selectedCategory != "ONLY_BUDGET"
        if (showPersonFilterOption && filterByPerson && selectedPerson != null) {
            list = list.filter { 
                it.personId == selectedPerson!!.id && 
                (it.type == "LEND" || it.type == "BORROW" || it.type == "REPAY_PAID" || it.type == "REPAY_RECEIVED")
            }
        }

        // Time Filter
        val cal = Calendar.getInstance()
        list = when (selectedTimeFilter) {
            "MONTH" -> {
                list.filter {
                    cal.timeInMillis = it.timestamp
                    cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                }
            }
            "YEAR" -> {
                list.filter {
                    cal.timeInMillis = it.timestamp
                    cal.get(Calendar.YEAR) == selectedYear
                }
            }
            "DATE_RANGE" -> {
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = startDateMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = endDateMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                list.filter { it.timestamp in startCal.timeInMillis..endCal.timeInMillis }
            }
            "MONTH_RANGE" -> {
                val startVal = startYear * 12 + startMonth
                val endVal = endYear * 12 + endMonth
                list.filter {
                    cal.timeInMillis = it.timestamp
                    val currentVal = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
                    currentVal in startVal..endVal
                }
            }
            "YEAR_RANGE" -> {
                list.filter {
                    cal.timeInMillis = it.timestamp
                    val y = cal.get(Calendar.YEAR)
                    y in startYearRange..endYearRange
                }
            }
            else -> list // ALL_TIME
        }

        list.sortedByDescending { it.timestamp }
    }

    // Filter computation for savings
    val filteredSavingsTx = remember(
        savingsTransactions, selectedCategory, selectedTimeFilter,
        selectedMonth, selectedYear, startDateMillis, endDateMillis,
        startMonth, startYear, endMonth, endYear, startYearRange, endYearRange,
        filterByPerson, selectedPerson, filterBySavingsGoal, selectedSavingsGoal
    ) {
        if ((selectedCategory != "ALL_DATA" && selectedCategory != "ONLY_SAVINGS") || (filterByPerson && selectedPerson != null)) {
            return@remember emptyList<SavingsTransaction>()
        }

        var list = savingsTransactions
        if (filterBySavingsGoal && selectedSavingsGoal != null) {
            list = list.filter { it.goalId == selectedSavingsGoal!!.id }
        }

        val cal = Calendar.getInstance()

        list = when (selectedTimeFilter) {
            "MONTH" -> {
                list.filter {
                    cal.timeInMillis = it.timestamp
                    cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
                }
            }
            "YEAR" -> {
                list.filter {
                    cal.timeInMillis = it.timestamp
                    cal.get(Calendar.YEAR) == selectedYear
                }
            }
            "DATE_RANGE" -> {
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = startDateMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = endDateMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                list.filter { it.timestamp in startCal.timeInMillis..endCal.timeInMillis }
            }
            "MONTH_RANGE" -> {
                val startVal = startYear * 12 + startMonth
                val endVal = endYear * 12 + endMonth
                list.filter {
                    cal.timeInMillis = it.timestamp
                    val currentVal = cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
                    currentVal in startVal..endVal
                }
            }
            "YEAR_RANGE" -> {
                list.filter {
                    cal.timeInMillis = it.timestamp
                    val y = cal.get(Calendar.YEAR)
                    y in startYearRange..endYearRange
                }
            }
            else -> list
        }

        list.sortedByDescending { it.timestamp }
    }

    // Filter computation for budgets
    val filteredBudgets = remember(
        monthlyBudgets, selectedCategory, selectedTimeFilter,
        selectedMonth, selectedYear, startDateMillis, endDateMillis,
        startMonth, startYear, endMonth, endYear, startYearRange, endYearRange,
        defaultBudgetIncome, defaultBudgetExpense, defaultBudgetSavings,
        filterByPerson, selectedPerson
    ) {
        if ((selectedCategory != "ALL_DATA" && selectedCategory != "ONLY_BUDGET") || (filterByPerson && selectedPerson != null)) {
            return@remember emptyList<MonthlyBudget>()
        }

        val list = monthlyBudgets

        if (selectedTimeFilter == "ALL_TIME") {
            if (list.isEmpty() && (defaultBudgetIncome > 0 || defaultBudgetExpense > 0 || defaultBudgetSavings > 0)) {
                listOf(
                    MonthlyBudget(
                        year = selectedYear,
                        month = selectedMonth,
                        income = defaultBudgetIncome,
                        expense = defaultBudgetExpense,
                        savings = defaultBudgetSavings
                    )
                )
            } else {
                list.map {
                    MonthlyBudget(
                        id = it.id,
                        year = it.year,
                        month = it.month,
                        income = it.income ?: 0.0,
                        expense = it.expense ?: 0.0,
                        savings = it.savings ?: 0.0,
                        workspaceId = it.workspaceId
                    )
                }.sortedWith(compareByDescending<MonthlyBudget> { it.year }.thenByDescending { it.month })
            }
        } else {
            val requiredMonths = mutableListOf<Pair<Int, Int>>() // Pair(year, month 0..11)
            when (selectedTimeFilter) {
                "MONTH" -> {
                    requiredMonths.add(Pair(selectedYear, selectedMonth))
                }
                "YEAR" -> {
                    for (m in 0..11) {
                        requiredMonths.add(Pair(selectedYear, m))
                    }
                }
                "DATE_RANGE" -> {
                    val startCal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                    val endCal = Calendar.getInstance().apply { timeInMillis = endDateMillis }
                    var currentVal = startCal.get(Calendar.YEAR) * 12 + startCal.get(Calendar.MONTH)
                    val endVal = endCal.get(Calendar.YEAR) * 12 + endCal.get(Calendar.MONTH)
                    while (currentVal <= endVal) {
                        val y = currentVal / 12
                        val m = currentVal % 12
                        requiredMonths.add(Pair(y, m))
                        currentVal++
                    }
                }
                "MONTH_RANGE" -> {
                    var currentVal = startYear * 12 + startMonth
                    val endVal = endYear * 12 + endMonth
                    while (currentVal <= endVal) {
                        val y = currentVal / 12
                        val m = currentVal % 12
                        requiredMonths.add(Pair(y, m))
                        currentVal++
                    }
                }
                "YEAR_RANGE" -> {
                    for (y in startYearRange..endYearRange) {
                        for (m in 0..11) {
                            requiredMonths.add(Pair(y, m))
                        }
                    }
                }
            }

            val result = mutableListOf<MonthlyBudget>()
            for ((y, m) in requiredMonths) {
                val existing = list.find { it.year == y && (it.month == m || it.month == m + 1) }
                if (existing != null) {
                    result.add(
                        MonthlyBudget(
                            id = existing.id,
                            year = y,
                            month = m,
                            income = existing.income ?: 0.0,
                            expense = existing.expense ?: 0.0,
                            savings = existing.savings ?: 0.0,
                            workspaceId = existing.workspaceId
                        )
                    )
                } else {
                    result.add(
                        MonthlyBudget(
                            year = y,
                            month = m,
                            income = 0.0,
                            expense = 0.0,
                            savings = 0.0
                        )
                    )
                }
            }
            result.sortedWith(compareByDescending<MonthlyBudget> { it.year }.thenByDescending { it.month })
        }
    }

    // Month lists
    val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (isBn) monthNamesBn else monthNamesEn

    val timePeriodText = remember(
        selectedTimeFilter, selectedMonth, selectedYear, startDateMillis, endDateMillis,
        startMonth, startYear, endMonth, endYear, startYearRange, endYearRange, isBn
    ) {
        getFormattedTimePeriod(
            language = language,
            timeFilter = selectedTimeFilter,
            month = selectedMonth,
            year = selectedYear,
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            startMonth = startMonth,
            startYear = startYear,
            endMonth = endMonth,
            endYear = endYear,
            startYearRange = startYearRange,
            endYearRange = endYearRange
        )
    }

    // SAF CreateDocument Launcher for saving to a custom folder chosen by the user
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isExporting = true
            scope.launch(Dispatchers.IO) {
                try {
                    val totalInc = filteredTx.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
                    val totalExp = filteredTx.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }
                    val totalLend = filteredTx.filter { it.type == "LEND" }.sumOf { it.amount }
                    val totalBorrow = filteredTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                    val totalRepayPaid = filteredTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                    val totalRepayRecv = filteredTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                    val netBalance = totalInc - totalExp

                    val totalSavDeposit = filteredSavingsTx.filter { it.isDeposit }.sumOf { it.amount }
                    val totalSavWithdraw = filteredSavingsTx.filter { !it.isDeposit }.sumOf { it.amount }
                    val netSavings = totalSavDeposit - totalSavWithdraw

                    val budIncTarget = filteredBudgets.sumOf { it.income }
                    val budExpTarget = filteredBudgets.sumOf { it.expense }
                    val budSavTarget = filteredBudgets.sumOf { it.savings }

                    val summary = mapOf(
                        "income" to totalInc,
                        "expense" to totalExp,
                        "balance" to netBalance,
                        "lend" to totalLend,
                        "borrow" to totalBorrow,
                        "repayPaid" to totalRepayPaid,
                        "repayRecv" to totalRepayRecv,
                        "savingsDeposit" to totalSavDeposit,
                        "savingsWithdraw" to totalSavWithdraw,
                        "savingsNet" to netSavings,
                        "budIncTarget" to budIncTarget,
                        "budExpTarget" to budExpTarget,
                        "budSavTarget" to budSavTarget
                    )

                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        if (selectedFormat == "PDF") {
                            val tempFile = File(context.cacheDir, "temp_export.pdf")
                            generatePdfFile(context, language, selectedCategory, timePeriodText, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, summary, tempFile, filterByPerson, selectedPerson, filterBySavingsGoal, selectedSavingsGoal)
                            tempFile.inputStream().use { input ->
                                input.copyTo(os)
                            }
                            tempFile.delete()
                        } else {
                            val csvContent = generateCsvData(language, selectedCategory, timePeriodText, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, selectedFormat == "EXCEL", filterByPerson, selectedPerson)
                            os.write(csvContent.toByteArray(Charsets.UTF_8))
                        }
                    }

                    withContext(Dispatchers.Main) {
                        isExporting = false
                        Toast.makeText(context, if (isBn) "সফলভাবে ফোল্ডারে ফাইলটি সংরক্ষণ করা হয়েছে!" else "File successfully saved to your selected folder!", Toast.LENGTH_LONG).show()

                        try {
                            val mimeType = when (selectedFormat) {
                                "PDF" -> "application/pdf"
                                "EXCEL" -> "application/vnd.ms-excel"
                                else -> "text/csv"
                            }
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(viewIntent, if (isBn) "ফাইলটি খুলুন" else "Open File"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, if (isBn) "ফাইলটি ওপেন করা সম্ভব হয়নি!" else "Could not open file automatically!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        isExporting = false
                        Toast.makeText(context, if (isBn) "ফাইল সংরক্ষণে ত্রুটি!" else "Failed to save file!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Helper functions for sharing/downloading
    fun performExport(context: Context, format: String, action: String) {
        val totalInc = filteredTx.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
        val totalExp = filteredTx.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }
        val totalLend = filteredTx.filter { it.type == "LEND" }.sumOf { it.amount }
        val totalBorrow = filteredTx.filter { it.type == "BORROW" }.sumOf { it.amount }
        val totalRepayPaid = filteredTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
        val totalRepayRecv = filteredTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
        val netBalance = totalInc - totalExp

        val totalSavDeposit = filteredSavingsTx.filter { it.isDeposit }.sumOf { it.amount }
        val totalSavWithdraw = filteredSavingsTx.filter { !it.isDeposit }.sumOf { it.amount }
        val netSavings = totalSavDeposit - totalSavWithdraw

        val budIncTarget = filteredBudgets.sumOf { it.income }
        val budExpTarget = filteredBudgets.sumOf { it.expense }
        val budSavTarget = filteredBudgets.sumOf { it.savings }

        val summary = mapOf(
            "income" to totalInc,
            "expense" to totalExp,
            "balance" to netBalance,
            "lend" to totalLend,
            "borrow" to totalBorrow,
            "repayPaid" to totalRepayPaid,
            "repayRecv" to totalRepayRecv,
            "savingsDeposit" to totalSavDeposit,
            "savingsWithdraw" to totalSavWithdraw,
            "savingsNet" to netSavings,
            "budIncTarget" to budIncTarget,
            "budExpTarget" to budExpTarget,
            "budSavTarget" to budSavTarget
        )

        // Generate PDF name based on selected sorting/filter and time period
        val filePrefix = when {
            filterByPerson && selectedPerson != null -> {
                selectedPerson!!.name
            }
            filterBySavingsGoal && selectedSavingsGoal != null -> {
                selectedSavingsGoal!!.title
            }
            else -> {
                when (selectedCategory) {
                    "ALL_DATA", "TRANSACTIONS" -> "All Transaction"
                    "DEBT_REPAYMENT" -> if (isBn) "দেনা পাওনা ও পরিশোধ" else "Debt and Repayments"
                    "INCOME_EXPENSE" -> if (isBn) "আয় ব্যয়" else "Income and Expense"
                    "ONLY_DEBT" -> if (isBn) "দেনা" else "Debt"
                    "ONLY_PAONA" -> if (isBn) "পাওনা" else "Credit"
                    "ONLY_INCOME" -> if (isBn) "আয়" else "Income"
                    "ONLY_EXPENSE" -> if (isBn) "ব্যয়" else "Expense"
                    "ONLY_SAVINGS" -> if (isBn) "সঞ্চয়" else "Savings"
                    "ONLY_BUDGET" -> if (isBn) "বাজেট" else "Budget"
                    else -> "All Transaction"
                }
            }
        }

        val cleanPrefix = filePrefix.replace(Regex("[\\\\/:*?\"<>|\\s+]"), "_")
        val cleanPeriod = timePeriodText.replace(Regex("[\\\\/:*?\"<>|\\s+]"), "_")
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = when (format) {
            "PDF" -> ".pdf"
            "EXCEL" -> ".xls"
            else -> ".csv"
        }
        val fileName = "${cleanPrefix}_${cleanPeriod}_$timestamp$extension"

        val mimeType = when (format) {
            "PDF" -> "application/pdf"
            "EXCEL" -> "application/vnd.ms-excel"
            else -> "text/csv"
        }

        if (action == "SHARE") {
            val shareDir = File(context.cacheDir, "shared_images")
            if (!shareDir.exists()) shareDir.mkdirs()
            val tempShareFile = File(shareDir, fileName)

            isExporting = true
            scope.launch(Dispatchers.IO) {
                if (format == "PDF") {
                    try {
                        generatePdfFile(context, language, selectedCategory, timePeriodText, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, summary, tempShareFile, filterByPerson, selectedPerson, filterBySavingsGoal, selectedSavingsGoal)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isExporting = false
                            Toast.makeText(context, if (isBn) "পিডিএফ তৈরিতে ত্রুটি!" else "Failed to generate PDF!", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                } else {
                    val csvContent = generateCsvData(language, selectedCategory, timePeriodText, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, format == "EXCEL", filterByPerson, selectedPerson)
                    try {
                        FileOutputStream(tempShareFile).use { fos ->
                            fos.write(csvContent.toByteArray(Charsets.UTF_8))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isExporting = false
                            Toast.makeText(context, if (isBn) "ফাইল তৈরিতে ত্রুটি!" else "Failed to generate file!", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                }

                withContext(Dispatchers.Main) {
                    isExporting = false
                    try {
                        val authority = "${context.packageName}.fileprovider"
                        val fileUri = FileProvider.getUriForFile(context, authority, tempShareFile)

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = mimeType
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            putExtra(Intent.EXTRA_SUBJECT, if (isBn) "লেনদেন প্রতিবেদন" else "Finance Report")
                            putExtra(Intent.EXTRA_TEXT, if (isBn) "আমার ফিনান্স নোট প্রতিবেদনটি সংযুক্ত করা হলো।" else "Attached is my finance note report.")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, if (isBn) "প্রতিবেদনটি শেয়ার করুন" else "Share Report"))
                    } catch (e: Exception) {
                        Toast.makeText(context, if (isBn) "শেয়ার করতে ত্রুটি হয়েছে!" else "Error sharing report!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // "DOWNLOAD" triggers custom directory saving via SAF (Storage Access Framework)
            createDocumentLauncher.launch(fileName)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val dialogBg = if (isDark) Color(0xFF121212) else Color.White
        val cardBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
        val textPrimary = if (isDark) Color.White else Color(0xFF1F2937)
        val textSecondary = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF4B5563)

        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)), RoundedCornerShape(28.dp))
                .testTag("export_dialog_root"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = if (isBn) "রিপোর্ট ও ডেটা এক্সপোর্ট" else "Export Reports & Data",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close", tint = textSecondary)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. File Format
                    item {
                        Text(
                            text = if (isBn) "ফাইল ফরম্যাট বেছে নিন" else "Choose File Format",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val formats = listOf("PDF", "EXCEL", "CSV")
                            formats.forEach { format ->
                                val selected = selectedFormat == format
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .clickable { selectedFormat = format },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) Color(0xFF10B981) else cardBg
                                    ),
                                    border = if (selected) null else BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = when (format) {
                                                "EXCEL" -> if (isBn) "এক্সেল (XLS)" else "Excel (.xls)"
                                                else -> format
                                            },
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.White else textPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Export Category
                    item {
                        Text(
                            text = if (isBn) "এক্সপোর্ট বিবরণ বা টাইপ" else "Select Data to Export",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val categories = listOf(
                            Triple("ALL_DATA", "সমস্ত ডেটা", "All Data"),
                            Triple("TRANSACTIONS", "লেনদেনসমূহ", "All Transactions"),
                            Triple("INCOME_EXPENSE", "আয়-ব্যয়", "Income & Expense"),
                            Triple("DEBT_REPAYMENT", "দেনা-পাওনা", "Debts & Credits"),
                            Triple("ONLY_INCOME", "শুধু আয়", "Only Income"),
                            Triple("ONLY_EXPENSE", "শুধু ব্যয়", "Only Expense"),
                            Triple("ONLY_DEBT", "শুধু দেনা", "Only Debts"),
                            Triple("ONLY_PAONA", "শুধু পাওনা", "Only Receivables"),
                            Triple("ONLY_SAVINGS", "শুধু সঞ্চয়", "Only Savings"),
                            Triple("ONLY_BUDGET", "বাজেট", "Budget")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (cat in categories) {
                                val catId = cat.first
                                val catBn = cat.second
                                val catEn = cat.third
                                val selected = selectedCategory == catId
                                Card(
                                    modifier = Modifier
                                        .clickable { selectedCategory = catId },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) Color(0xFF10B981) else cardBg
                                    ),
                                    border = if (selected) null else BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                                ) {
                                    Text(
                                        text = if (isBn) catBn else catEn,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color.White else textPrimary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Time Filter
                    item {
                        Text(
                            text = if (isBn) "সময়কাল নির্বাচন করুন" else "Select Time Period",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val timeFilters = listOf(
                            Pair("MONTH", if (isBn) "নির্দিষ্ট মাস" else "Month"),
                            Pair("YEAR", if (isBn) "নির্দিষ্ট বছর" else "Year"),
                            Pair("DATE_RANGE", if (isBn) "তারিখ টু তারিখ" else "Custom Range"),
                            Pair("MONTH_RANGE", if (isBn) "মাস টু মাস" else "Month Range"),
                            Pair("YEAR_RANGE", if (isBn) "বছর টু বছর" else "Year Range"),
                            Pair("ALL_TIME", if (isBn) "সব সময়" else "All Time")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (tf in timeFilters) {
                                val filterId = tf.first
                                val label = tf.second
                                val selected = selectedTimeFilter == filterId
                                Card(
                                    modifier = Modifier
                                        .clickable { selectedTimeFilter = filterId },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) Color(0xFF10B981) else cardBg
                                    ),
                                    border = if (selected) null else BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color.White else textPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Time Context Selectors
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(cardBg, RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            when (selectedTimeFilter) {
                                "MONTH" -> {
                                    Text(
                                        text = if (isBn) "মাস ও বছর নির্বাচন:" else "Select Month & Year:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Month Select Dropdown
                                        var showMonthMenu by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.weight(1.5f)) {
                                            OutlinedButton(
                                                onClick = { showMonthMenu = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(months[selectedMonth], fontSize = 12.sp)
                                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                            DropdownMenu(
                                                expanded = showMonthMenu,
                                                onDismissRequest = { showMonthMenu = false }
                                            ) {
                                                months.forEachIndexed { idx, name ->
                                                    DropdownMenuItem(
                                                        text = { Text(name) },
                                                        onClick = {
                                                            selectedMonth = idx
                                                            showMonthMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Year Select Dropdown
                                        var showYearMenu by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedButton(
                                                onClick = { showYearMenu = true },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Text(selectedYear.toString(), fontSize = 12.sp)
                                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                            DropdownMenu(
                                                expanded = showYearMenu,
                                                onDismissRequest = { showYearMenu = false }
                                            ) {
                                                uniqueYears.forEach { yr ->
                                                    DropdownMenuItem(
                                                        text = { Text(yr.toString()) },
                                                        onClick = {
                                                            selectedYear = yr
                                                            showYearMenu = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                "YEAR" -> {
                                    Text(
                                        text = if (isBn) "বছর নির্বাচন করুন:" else "Select Year:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                    var showYearMenu by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { showYearMenu = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(selectedYear.toString(), fontSize = 13.sp)
                                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                                        }
                                        DropdownMenu(
                                            expanded = showYearMenu,
                                            onDismissRequest = { showYearMenu = false }
                                        ) {
                                            uniqueYears.forEach { yr ->
                                                DropdownMenuItem(
                                                    text = { Text(yr.toString()) },
                                                    onClick = {
                                                        selectedYear = yr
                                                        showYearMenu = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                "DATE_RANGE" -> {
                                    Text(
                                        text = if (isBn) "তারিখের সীমা নির্বাচন:" else "Select Date Range:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Start Date Button
                                        Button(
                                            onClick = {
                                                val c = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                                                DatePickerDialog(context, { _, y, m, d ->
                                                    val res = Calendar.getInstance().apply { set(y, m, d) }
                                                    startDateMillis = res.timeInMillis
                                                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF2C2C2C) else Color.White, contentColor = textPrimary),
                                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(if (isBn) "শুরু:" else "Start:", fontSize = 9.sp, color = textSecondary)
                                                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(startDateMillis)), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // End Date Button
                                        Button(
                                            onClick = {
                                                val c = Calendar.getInstance().apply { timeInMillis = endDateMillis }
                                                DatePickerDialog(context, { _, y, m, d ->
                                                    val res = Calendar.getInstance().apply { set(y, m, d) }
                                                    endDateMillis = res.timeInMillis
                                                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF2C2C2C) else Color.White, contentColor = textPrimary),
                                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(if (isBn) "শেষ:" else "End:", fontSize = 9.sp, color = textSecondary)
                                                Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(endDateMillis)), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                "MONTH_RANGE" -> {
                                    Text(
                                        text = if (isBn) "মাসের সীমা নির্বাচন:" else "Select Month Range:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Start Month Selectors
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(if (isBn) "শুরু:" else "From:", fontSize = 11.sp, modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                                            
                                            // Start Month Dropdown
                                            var showStartMonthMenu by remember { mutableStateOf(false) }
                                            Box(modifier = Modifier.weight(1.5f)) {
                                                OutlinedButton(
                                                    onClick = { showStartMonthMenu = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(months[startMonth], fontSize = 11.sp)
                                                }
                                                DropdownMenu(expanded = showStartMonthMenu, onDismissRequest = { showStartMonthMenu = false }) {
                                                    months.forEachIndexed { idx, name ->
                                                        DropdownMenuItem(text = { Text(name, fontSize = 12.sp) }, onClick = {
                                                            startMonth = idx
                                                            showStartMonthMenu = false
                                                        })
                                                    }
                                                }
                                            }

                                            // Start Year Dropdown
                                            var showStartYearMenu by remember { mutableStateOf(false) }
                                            Box(modifier = Modifier.weight(1f)) {
                                                OutlinedButton(
                                                    onClick = { showStartYearMenu = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(startYear.toString(), fontSize = 11.sp)
                                                }
                                                DropdownMenu(expanded = showStartYearMenu, onDismissRequest = { showStartYearMenu = false }) {
                                                    uniqueYears.forEach { yr ->
                                                        DropdownMenuItem(text = { Text(yr.toString()) }, onClick = {
                                                            startYear = yr
                                                            showStartYearMenu = false
                                                        })
                                                    }
                                                }
                                            }
                                        }

                                        // End Month Selectors
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(if (isBn) "শেষ:" else "To:", fontSize = 11.sp, modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
                                            
                                            // End Month Dropdown
                                            var showEndMonthMenu by remember { mutableStateOf(false) }
                                            Box(modifier = Modifier.weight(1.5f)) {
                                                OutlinedButton(
                                                    onClick = { showEndMonthMenu = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(months[endMonth], fontSize = 11.sp)
                                                }
                                                DropdownMenu(expanded = showEndMonthMenu, onDismissRequest = { showEndMonthMenu = false }) {
                                                    months.forEachIndexed { idx, name ->
                                                        DropdownMenuItem(text = { Text(name, fontSize = 12.sp) }, onClick = {
                                                            endMonth = idx
                                                            showEndMonthMenu = false
                                                        })
                                                    }
                                                }
                                            }

                                            // End Year Dropdown
                                            var showEndYearMenu by remember { mutableStateOf(false) }
                                            Box(modifier = Modifier.weight(1f)) {
                                                OutlinedButton(
                                                    onClick = { showEndYearMenu = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(endYear.toString(), fontSize = 11.sp)
                                                }
                                                DropdownMenu(expanded = showEndYearMenu, onDismissRequest = { showEndYearMenu = false }) {
                                                    uniqueYears.forEach { yr ->
                                                        DropdownMenuItem(text = { Text(yr.toString()) }, onClick = {
                                                            endYear = yr
                                                            showEndYearMenu = false
                                                        })
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                "YEAR_RANGE" -> {
                                    Text(
                                        text = if (isBn) "বছরের সীমা নির্বাচন:" else "Select Year Range:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Start Year Selection
                                        var showY1 by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedButton(onClick = { showY1 = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                                                Text(startYearRange.toString(), fontSize = 11.sp)
                                            }
                                            DropdownMenu(expanded = showY1, onDismissRequest = { showY1 = false }) {
                                                uniqueYears.forEach { yr ->
                                                    DropdownMenuItem(text = { Text(yr.toString()) }, onClick = {
                                                        startYearRange = yr
                                                        showY1 = false
                                                    })
                                                }
                                            }
                                        }
                                        Text(if (isBn) "থেকে" else "to", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        // End Year Selection
                                        var showY2 by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedButton(onClick = { showY2 = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                                                Text(endYearRange.toString(), fontSize = 11.sp)
                                            }
                                            DropdownMenu(expanded = showY2, onDismissRequest = { showY2 = false }) {
                                                uniqueYears.forEach { yr ->
                                                    DropdownMenuItem(text = { Text(yr.toString()) }, onClick = {
                                                        endYearRange = yr
                                                        showY2 = false
                                                    })
                                                }
                                            }
                                        }
                                    }
                                }
                                "ALL_TIME" -> {
                                    Text(
                                        text = if (isBn) "সমস্ত ঐতিহাসিক লেনদেন অন্তর্ভুক্ত করা হবে।" else "All historical logs will be included.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF10B981),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Specific Person Filter
                    val showPersonFilterOption = selectedCategory != "ONLY_SAVINGS" && selectedCategory != "ONLY_BUDGET"

                    if (showPersonFilterOption) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBn) "ব্যক্তি অনুযায়ী ফিল্টার?" else "Filter by Person?",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                    Switch(
                                        checked = filterByPerson,
                                        onCheckedChange = { filterByPerson = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF10B981)
                                        )
                                    )
                                }

                                if (filterByPerson) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        val buttonLabel = selectedPerson?.name ?: (if (isBn) "ব্যক্তি নির্বাচন করুন" else "Select Person")
                                        OutlinedButton(
                                            onClick = { showPersonDropdown = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(buttonLabel, fontSize = 13.sp)
                                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.padding(start = 4.dp))
                                        }

                                        DropdownMenu(
                                            expanded = showPersonDropdown,
                                            onDismissRequest = { showPersonDropdown = false },
                                            modifier = Modifier.fillMaxWidth(0.8f)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(if (isBn) "সব ব্যক্তি (সবার লেনদেন)" else "All Persons") },
                                                onClick = {
                                                    selectedPerson = null
                                                    showPersonDropdown = false
                                                }
                                            )
                                            persons.forEach { person ->
                                                DropdownMenuItem(
                                                    text = { Text(person.name) },
                                                    onClick = {
                                                        selectedPerson = person
                                                        showPersonDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 6. Live Document Report Preview Card
                    item {
                        val recordsSize = filteredTx.size + 
                                          (if (selectedCategory == "ALL_DATA" || selectedCategory == "ONLY_SAVINGS") filteredSavingsTx.size else 0) +
                                          (if (selectedCategory == "ALL_DATA" || selectedCategory == "ONLY_BUDGET") filteredBudgets.size else 0)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.6f) else Color(0xFFF5F3FF)
                            ),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF6366F1).copy(alpha = 0.3f) else Color(0xFFC7D2FE))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Live Preview Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(Color(0xFF6366F1), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("F", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Column {
                                            Text(
                                                text = if (isBn) "রিপোর্ট প্রিভিউ (Report Preview)" else "Report Preview",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF6366F1)
                                            )
                                            Text(
                                                text = getCategoryTitle(selectedCategory, language),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFF6366F1).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = timePeriodText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFDDD6FE))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Dynamic Summary Badges for Selected Category
                                val totalInc = filteredTx.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
                                val totalExp = filteredTx.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }
                                val totalLend = filteredTx.filter { it.type == "LEND" }.sumOf { it.amount }
                                val totalBorrow = filteredTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                                val totalRepayPaid = filteredTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                                val totalRepayRecv = filteredTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                                val totalSavDep = filteredSavingsTx.filter { it.isDeposit }.sumOf { it.amount }
                                val totalSavWith = filteredSavingsTx.filter { !it.isDeposit }.sumOf { it.amount }
                                val netSav = totalSavDep - totalSavWith

                                val budIncTarget = filteredBudgets.sumOf { it.income }
                                val budExpTarget = filteredBudgets.sumOf { it.expense }
                                val budSavTarget = filteredBudgets.sumOf { it.savings }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = if (isBn) "মোট পরিমাণ (সারসংক্ষেপ):" else "Category Totals Summary:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (filterByPerson && selectedPerson != null) {
                                            PreviewSummaryBadge(label = if (isBn) "মোট দেনা" else "Total Debt", amount = totalBorrow - totalRepayPaid, color = Color(0xFFD97706), language = language, isDark = isDark)
                                            PreviewSummaryBadge(label = if (isBn) "মোট পাওনা" else "Total Credit", amount = totalLend - totalRepayRecv, color = Color(0xFF2563EB), language = language, isDark = isDark)
                                        } else {
                                            when (selectedCategory) {
                                                "ONLY_INCOME" -> {
                                                    PreviewSummaryBadge(label = if (isBn) "মোট আয়" else "Total Income", amount = totalInc, color = Color(0xFF059669), language = language, isDark = isDark)
                                                }
                                                "ONLY_EXPENSE" -> {
                                                    PreviewSummaryBadge(label = if (isBn) "মোট ব্যয়" else "Total Expense", amount = totalExp, color = Color(0xFFDC2626), language = language, isDark = isDark)
                                                }
                                                "INCOME_EXPENSE" -> {
                                                    PreviewSummaryBadge(label = if (isBn) "মোট আয়" else "Total Income", amount = totalInc, color = Color(0xFF059669), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "মোট ব্যয়" else "Total Expense", amount = totalExp, color = Color(0xFFDC2626), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "ব্য্যালেন্স" else "Balance", amount = totalInc - totalExp, color = Color(0xFF6366F1), language = language, isDark = isDark)
                                                }
                                                "DEBT_REPAYMENT", "ONLY_DEBT", "ONLY_PAONA" -> {
                                                    if (selectedCategory != "ONLY_PAONA") {
                                                        PreviewSummaryBadge(label = if (isBn) "মোট দেনা" else "Total Debt", amount = totalBorrow - totalRepayPaid, color = Color(0xFFD97706), language = language, isDark = isDark)
                                                    }
                                                    if (selectedCategory != "ONLY_DEBT") {
                                                        PreviewSummaryBadge(label = if (isBn) "মোট পাওনা" else "Total Credit", amount = totalLend - totalRepayRecv, color = Color(0xFF2563EB), language = language, isDark = isDark)
                                                    }
                                                }
                                                "ONLY_SAVINGS" -> {
                                                    PreviewSummaryBadge(label = if (isBn) "সঞ্চয় জমা" else "Deposit", amount = totalSavDep, color = Color(0xFF059669), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "সঞ্চয় উত্তোলন" else "Withdrawal", amount = totalSavWith, color = Color(0xFFDC2626), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "অবশিষ্ট সঞ্চয়" else "Net Saved", amount = netSav, color = Color(0xFF8B5CF6), language = language, isDark = isDark)
                                                }
                                                "ONLY_BUDGET" -> {
                                                    PreviewSummaryBadge(label = if (isBn) "আয় বাজেট" else "Inc Target", amount = budIncTarget, color = Color(0xFF059669), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "ব্যয় বাজেট" else "Exp Target", amount = budExpTarget, color = Color(0xFFDC2626), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "সঞ্চয় বাজেট" else "Sav Target", amount = budSavTarget, color = Color(0xFF8B5CF6), language = language, isDark = isDark)
                                                }
                                                else -> {
                                                    PreviewSummaryBadge(label = if (isBn) "মোট আয়" else "Total Income", amount = totalInc, color = Color(0xFF059669), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "মোট ব্যয়" else "Total Expense", amount = totalExp, color = Color(0xFFDC2626), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "মোট দেনা" else "Total Debt", amount = totalBorrow - totalRepayPaid, color = Color(0xFFD97706), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "মোট পাওনা" else "Total Credit", amount = totalLend - totalRepayRecv, color = Color(0xFF2563EB), language = language, isDark = isDark)
                                                    PreviewSummaryBadge(label = if (isBn) "মোট সঞ্চয়" else "Total Savings", amount = netSav, color = Color(0xFF8B5CF6), language = language, isDark = isDark)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFDDD6FE))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Matching Items Preview List
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBn) "রিপোর্টে অন্তর্ভুক্ত ডেটা:" else "Records in Report:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                    val recordsText = if (isBn) "${replaceToBnDigits(recordsSize.toString())} টি রেকর্ড" else "$recordsSize items"
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = recordsText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF059669),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                if (recordsSize == 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isBn) "নির্বাচিত ফিল্টারে কোনো তথ্য পাওয়া যায়নি" else "No matching data found for this selection",
                                            fontSize = 12.sp,
                                            color = textSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val sampleTxs = filteredTx.take(4)
                                        if (selectedCategory == "ONLY_BUDGET") {
                                            val previewBudgetReport = computeComprehensiveBudgetReport(language, filteredBudgets, filteredTx, filteredSavingsTx, savingsGoals)
                                            
                                            BudgetFulfillmentPreviewCard(
                                                title = if (isBn) "আয় বাজেট অর্জন" else "Income Achievement",
                                                actual = previewBudgetReport.incomeAnalysis.actualAmount,
                                                target = previewBudgetReport.incomeAnalysis.targetAmount,
                                                fulfillmentPctText = "${String.format(Locale.US, "%.1f", previewBudgetReport.incomeAnalysis.fulfillmentPercentage)}%",
                                                color = Color(0xFF059669),
                                                language = language,
                                                isDark = isDark
                                            )
                                            
                                            BudgetFulfillmentPreviewCard(
                                                title = if (isBn) "ব্যয় বাজেট ব্যবহার" else "Expense Usage",
                                                actual = previewBudgetReport.expenseAnalysis.actualAmount,
                                                target = previewBudgetReport.expenseAnalysis.targetAmount,
                                                fulfillmentPctText = "${String.format(Locale.US, "%.1f", previewBudgetReport.expenseAnalysis.fulfillmentPercentage)}%",
                                                color = Color(0xFFDC2626),
                                                language = language,
                                                isDark = isDark
                                            )
                                            
                                            BudgetFulfillmentPreviewCard(
                                                title = if (isBn) "সঞ্চয় লক্ষ্য পূরণ" else "Savings Achievement",
                                                actual = previewBudgetReport.savingsAnalysis.actualAmount,
                                                target = previewBudgetReport.savingsAnalysis.targetAmount,
                                                fulfillmentPctText = "${String.format(Locale.US, "%.1f", previewBudgetReport.savingsAnalysis.fulfillmentPercentage)}%",
                                                color = Color(0xFF8B5CF6),
                                                language = language,
                                                isDark = isDark
                                            )
                                        } else {
                                            // Show up to 4 preview records
                                            for (tx in sampleTxs) {
                                            val person = persons.find { it.id == tx.personId }
                                            val personName = person?.name.orEmpty()

                                            val primaryTitle = if (personName.isNotEmpty()) {
                                                personName
                                            } else {
                                                tx.category
                                            }

                                            val typeLabel = when (tx.type) {
                                                "INCOME" -> if (isBn) "আয়" else "Income"
                                                "EXPENSE" -> if (isBn) "ব্যয়" else "Expense"
                                                "LEND" -> if (isBn) "পাওনা" else "Receivable"
                                                "BORROW" -> if (isBn) "দেনা" else "Debt"
                                                "REPAY_PAID" -> if (isBn) "দেনা পরিশোধ" else "Debt Paid"
                                                "REPAY_RECEIVED" -> if (isBn) "পাওনা আদায়" else "Credit Recv"
                                                else -> getTransactionTypeName(tx.type, language)
                                            }

                                            val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(tx.timestamp))
                                            val formattedDate = if (isBn) replaceToBnDigits(dateStr) else dateStr

                                            val amtColor = when (tx.type) {
                                                "INCOME", "REPAY_PAID" -> Color(0xFF059669) // Green (repay_paid reduces liability)
                                                "EXPENSE", "REPAY_RECEIVED" -> Color(0xFFDC2626) // Red (repay_received reduces receivable)
                                                "LEND" -> Color(0xFF2563EB)
                                                "BORROW" -> Color(0xFFD97706)
                                                else -> textPrimary
                                            }

                                            val typeBgColor = when (tx.type) {
                                                "INCOME", "REPAY_PAID" -> Color(0xFF059669).copy(alpha = 0.12f)
                                                "EXPENSE", "REPAY_RECEIVED" -> Color(0xFFDC2626).copy(alpha = 0.12f)
                                                "LEND" -> Color(0xFF2563EB).copy(alpha = 0.12f)
                                                "BORROW" -> Color(0xFFD97706).copy(alpha = 0.12f)
                                                else -> Color.Gray.copy(alpha = 0.12f)
                                            }

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                                                ),
                                                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFE5E7EB))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = primaryTitle,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = textPrimary
                                                            )
                                                            Surface(
                                                                color = typeBgColor,
                                                                shape = RoundedCornerShape(4.dp)
                                                            ) {
                                                                Text(
                                                                    text = typeLabel,
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = amtColor,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }

                                                        if (tx.note.isNotBlank()) {
                                                            Text(
                                                                text = tx.note.trim(),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Normal,
                                                                color = textSecondary,
                                                                maxLines = 1,
                                                                modifier = Modifier.padding(top = 2.dp)
                                                            )
                                                        }

                                                        Text(
                                                            text = formattedDate,
                                                            fontSize = 9.sp,
                                                            color = textSecondary.copy(alpha = 0.7f),
                                                            modifier = Modifier.padding(top = 1.dp)
                                                        )
                                                    }

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    val displayAmt = if (tx.type == "REPAY_PAID" || tx.type == "REPAY_RECEIVED") -tx.amount else tx.amount
                                                    Text(
                                                        text = formatCurrency(displayAmt, language),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = amtColor
                                                    )
                                                }
                                            }
                                        }
                                        }

                                        if (selectedCategory == "ONLY_SAVINGS" && filteredSavingsTx.isNotEmpty()) {
                                            val sampleSav = filteredSavingsTx.take(3)
                                            for (stx in sampleSav) {
                                                val goalTitle = savingsGoals.find { it.id == stx.goalId }?.title ?: ""
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.White, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(text = goalTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                                        Text(text = if (stx.isDeposit) (if (isBn) "সঞ্চয় জমা" else "Deposit") else (if (isBn) "উত্তোলন" else "Withdrawal"), fontSize = 9.sp, color = textSecondary)
                                                    }
                                                    val displaySavAmt = if (stx.isDeposit) stx.amount else -stx.amount
                                                    Text(
                                                        text = formatCurrency(displaySavAmt, language),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (stx.isDeposit) Color(0xFF059669) else Color(0xFFDC2626)
                                                    )
                                                }
                                            }
                                        }

                                        val shownCount = if (selectedCategory == "ONLY_BUDGET") recordsSize else sampleTxs.size + (if (selectedCategory == "ONLY_SAVINGS") filteredSavingsTx.take(3).size else 0)
                                        if (recordsSize > shownCount) {
                                            Text(
                                                text = if (isBn) "...এবং আরও ${replaceToBnDigits((recordsSize - shownCount).toString())} টি রেকর্ড রিপোর্টে থাকছে" else "...and ${recordsSize - shownCount} more items included in report",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF6366F1),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Divider and footer actions
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download Button
                    Button(
                        onClick = { performExport(context, selectedFormat, "DOWNLOAD") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_download_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Rounded.FileDownload, contentDescription = "Download", modifier = Modifier.size(18.dp))
                            Text(text = if (isBn) "সংরক্ষন" else "Save / Download", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Share Button
                    Button(
                        onClick = { performExport(context, selectedFormat, "SHARE") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("export_share_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Rounded.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
                            Text(text = if (isBn) "শেয়ার করুন" else "Share Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (isExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {}, // Prevent clicks through the overlay
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFF10B981))
                            Text(
                                text = if (isBn) "ফাইল তৈরি হচ্ছে..." else "Generating file...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }
    }
}
}

// Helper function to replace to Bengali Digits
private fun replaceToBnDigits(str: String): String {
    return str
        .replace("0", "০")
        .replace("1", "১")
        .replace("2", "২")
        .replace("3", "৩")
        .replace("4", "৪")
        .replace("5", "৫")
        .replace("6", "৬")
        .replace("7", "৭")
        .replace("8", "৮")
        .replace("9", "৯")
}

// Generate CSV / Tab-separated Excel layout string
private fun generateCsvData(
    language: AppLanguage,
    selectedCategory: String,
    timePeriodText: String,
    transactions: List<Transaction>,
    persons: List<Person>,
    savingsGoals: List<SavingsGoal>,
    savingsTransactions: List<SavingsTransaction>,
    filteredBudgets: List<MonthlyBudget>,
    isExcelMode: Boolean,
    filterByPerson: Boolean = false,
    selectedPerson: Person? = null
): String {
    val finalPerson = if (filterByPerson) selectedPerson else null
    val delimiter = if (isExcelMode) "\t" else ","
    val isBn = language == AppLanguage.BN
    val sb = StringBuilder()

    sb.append("\uFEFF")

    val reportTitle = if (finalPerson != null) {
        if (isBn) "${finalPerson.name} - লেনদেন বিবরণী" else "${finalPerson.name} - Transaction Statement"
    } else {
        getCategoryTitle(selectedCategory, language)
    }
    sb.append(if (isBn) "ফাইন্যান্স নোট - " else "Finance Note - ").append(reportTitle).append("\n")
    sb.append(if (isBn) "সময়কাল: " else "Period: ").append(timePeriodText).append("\n")
    sb.append(if (isBn) "তৈরি হয়েছে: " else "Generated on: ").append(SimpleDateFormat("dd MMMM, yyyy hh:mm a", Locale.getDefault()).format(Date())).append("\n\n")

    val totalInc = transactions.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
    val totalExp = transactions.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }
    val netBalance = totalInc - totalExp

    val budgetReport = computeComprehensiveBudgetReport(language, filteredBudgets, transactions, savingsTransactions, savingsGoals)

    if (selectedCategory == "ONLY_BUDGET") {
        // SECTION 1: Budget Performance Overview Summary
        sb.append(if (isBn) "১. বাজেট পারফরম্যান্স ও অর্জনের সারসংক্ষেপ" else "1. Budget Performance & Achievement Summary").append("\n")
        sb.append(
            listOf(
                if (isBn) "বাজেটের বিষয়" else "Budget Metric",
                if (isBn) "নির্ধারিত টার্গেট" else "Target Amount",
                if (isBn) "প্রকৃত অর্জন/ব্যয়" else "Actual Amount",
                if (isBn) "পূরণ / ব্যবহারের %" else "Fulfillment / Usage %",
                if (isBn) "অবশিষ্ট / পার্থক্য" else "Variance / Remaining"
            ).joinToString(delimiter)
        ).append("\n")

        val incData = budgetReport.incomeAnalysis
        sb.append(
            listOf(
                incData.typeName,
                incData.targetAmount.toString(),
                incData.actualAmount.toString(),
                "${String.format(Locale.US, "%.1f", incData.fulfillmentPercentage)}%",
                incData.remainingAmount.toString()
            ).joinToString(delimiter)
        ).append("\n")

        val expData = budgetReport.expenseAnalysis
        sb.append(
            listOf(
                expData.typeName,
                expData.targetAmount.toString(),
                expData.actualAmount.toString(),
                "${String.format(Locale.US, "%.1f", expData.fulfillmentPercentage)}%",
                expData.remainingAmount.toString()
            ).joinToString(delimiter)
        ).append("\n")

        val savData = budgetReport.savingsAnalysis
        sb.append(
            listOf(
                savData.typeName,
                savData.targetAmount.toString(),
                savData.actualAmount.toString(),
                "${String.format(Locale.US, "%.1f", savData.fulfillmentPercentage)}%",
                savData.remainingAmount.toString()
            ).joinToString(delimiter)
        ).append("\n\n")

        // SECTION 2: Income Category Breakdown
        if (incData.categories.isNotEmpty()) {
            sb.append(if (isBn) "২. আয় খাতভিত্তিক বাজেট বিশ্লেষণ (খাতওয়ারি তথ্য)" else "2. Income Category Breakdown").append("\n")
            sb.append(
                listOf(
                    if (isBn) "আয় খাত" else "Income Category",
                    if (isBn) "অর্জিত পরিমাণ" else "Actual Amount",
                    if (isBn) "আয় বাজেটের %" else "% of Target Budget",
                    if (isBn) "মোট আয়ের %" else "% of Total Income"
                ).joinToString(delimiter)
            ).append("\n")

            incData.categories.forEach { c ->
                sb.append(
                    listOf(
                        c.categoryName,
                        c.amount.toString(),
                        "${String.format(Locale.US, "%.1f", c.percentageOfTarget)}%",
                        "${String.format(Locale.US, "%.1f", c.percentageOfTotal)}%"
                    ).joinToString(delimiter)
                ).append("\n")
            }
            sb.append("\n")
        }

        // SECTION 3: Expense Category Breakdown
        if (expData.categories.isNotEmpty()) {
            sb.append(if (isBn) "৩. ব্যয় খাতভিত্তিক বাজেট বিশ্লেষণ (খাতওয়ারি তথ্য)" else "3. Expense Category Breakdown").append("\n")
            sb.append(
                listOf(
                    if (isBn) "ব্যয় খাত" else "Expense Category",
                    if (isBn) "ব্যয়িত পরিমাণ" else "Actual Amount",
                    if (isBn) "ব্যয় বাজেটের %" else "% of Target Budget",
                    if (isBn) "মোট ব্যয়ের %" else "% of Total Expense"
                ).joinToString(delimiter)
            ).append("\n")

            expData.categories.forEach { c ->
                sb.append(
                    listOf(
                        c.categoryName,
                        c.amount.toString(),
                        "${String.format(Locale.US, "%.1f", c.percentageOfTarget)}%",
                        "${String.format(Locale.US, "%.1f", c.percentageOfTotal)}%"
                    ).joinToString(delimiter)
                ).append("\n")
            }
            sb.append("\n")
        }

        // SECTION 4: Savings Goal Breakdown
        if (savData.categories.isNotEmpty()) {
            sb.append(if (isBn) "৪. সঞ্চয় লক্ষ্যভিত্তিক বাজেট বিশ্লেষণ (খাতওয়ারি তথ্য)" else "4. Savings Goal Breakdown").append("\n")
            sb.append(
                listOf(
                    if (isBn) "সঞ্চয় লক্ষ্য / খাত" else "Savings Goal",
                    if (isBn) "জমাকৃত পরিমাণ" else "Actual Amount",
                    if (isBn) "সঞ্চয় বাজেটের %" else "% of Target Budget",
                    if (isBn) "মোট সঞ্চয়ের %" else "% of Total Savings"
                ).joinToString(delimiter)
            ).append("\n")

            savData.categories.forEach { c ->
                sb.append(
                    listOf(
                        c.categoryName,
                        c.amount.toString(),
                        "${String.format(Locale.US, "%.1f", c.percentageOfTarget)}%",
                        "${String.format(Locale.US, "%.1f", c.percentageOfTotal)}%"
                    ).joinToString(delimiter)
                ).append("\n")
            }
            sb.append("\n")
        }

        // SECTION 5: Itemized Transactions Log (for context)
        if (transactions.isNotEmpty()) {
            sb.append(if (isBn) "৫. আইটেমাইজড লেনদেন লগ (বিবরণী)" else "5. Itemized Transactions Log").append("\n")
            sb.append(
                listOf(
                    if (isBn) "ক্রমিক" else "Sl No",
                    if (isBn) "তারিখ" else "Date",
                    if (isBn) "ক্যাটাগরি" else "Category",
                    if (isBn) "ধরন" else "Type",
                    if (isBn) "ব্যক্তি" else "Person",
                    if (isBn) "বিবরণ/নোট" else "Note/Description",
                    if (isBn) "টাকা" else "Amount"
                ).joinToString(delimiter)
            ).append("\n")

            transactions.forEachIndexed { idx, tx ->
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(tx.timestamp))
                val typeStr = getTransactionTypeName(tx.type, language)
                val personName = persons.find { it.id == tx.personId }?.name ?: ""
                val noteSafe = tx.note.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val catSafe = tx.category.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val personSafe = personName.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val actualAmt = if (tx.type == "REPAY_PAID" || tx.type == "REPAY_RECEIVED") -tx.amount else tx.amount

                sb.append(
                    listOf(
                        (idx + 1).toString(),
                        dateStr,
                        catSafe,
                        typeStr,
                        personSafe,
                        noteSafe,
                        actualAmt.toString()
                    ).joinToString(delimiter)
                ).append("\n")
            }
        }
    } else {
        // Standard Flow for non-budget categories
        sb.append(if (isBn) "আর্থিক সারসংক্ষেপ (Financial Summary)" else "Financial Summary").append("\n")
        sb.append(if (isBn) "ক্যাটাগরি" else "Metric").append(delimiter).append(if (isBn) "টাকার পরিমাণ" else "Amount").append("\n")

        if (finalPerson != null) {
            val personBorrow = transactions.filter { it.type == "BORROW" }.sumOf { it.amount }
            val personRepayPaid = transactions.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
            val personDebtNet = personBorrow - personRepayPaid

            val personLend = transactions.filter { it.type == "LEND" }.sumOf { it.amount }
            val personRepayReceived = transactions.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
            val personReceivableNet = personLend - personRepayReceived

            val netStatus = personReceivableNet - personDebtNet
            val netStatusStr = if (netStatus > 0) {
                if (isBn) "পাবেন $netStatus" else "Get $netStatus"
            } else if (netStatus < 0) {
                if (isBn) "দেবেন ${-netStatus}" else "Owe ${-netStatus}"
            } else {
                if (isBn) "পরিশোধিত" else "Settled"
            }

            sb.append(if (isBn) "মোট দেনা (Debt)" else "Total Debt").append(delimiter).append(personDebtNet).append("\n")
            sb.append(if (isBn) "মোট পাওনা (Receivable)" else "Total Receivable").append(delimiter).append(personReceivableNet).append("\n")
            sb.append(if (isBn) "বর্তমান অবস্থা (Net Status)" else "Net Status").append(delimiter).append(netStatusStr).append("\n")
        } else {
            when (selectedCategory) {
                "ONLY_INCOME" -> {
                    sb.append(if (isBn) "মোট আয়" else "Total Income").append(delimiter).append(totalInc).append("\n")
                }
                "ONLY_EXPENSE" -> {
                    sb.append(if (isBn) "মোট ব্যয়" else "Total Expense").append(delimiter).append(totalExp).append("\n")
                }
                "INCOME_EXPENSE" -> {
                    sb.append(if (isBn) "মোট আয়" else "Total Income").append(delimiter).append(totalInc).append("\n")
                    sb.append(if (isBn) "মোট ব্যয়" else "Total Expense").append(delimiter).append(totalExp).append("\n")
                    sb.append(if (isBn) "নিট ব্যালেন্স" else "Net Balance").append(delimiter).append(netBalance).append("\n")
                }
                "DEBT_REPAYMENT", "ONLY_DEBT", "ONLY_PAONA" -> {
                    val totalLend = transactions.filter { it.type == "LEND" }.sumOf { it.amount }
                    val totalRepayRecv = transactions.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                    val netLnd = totalLend - totalRepayRecv

                    val totalBorrow = transactions.filter { it.type == "BORROW" }.sumOf { it.amount }
                    val totalRepayPaid = transactions.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                    val netBrw = totalBorrow - totalRepayPaid

                    if (selectedCategory != "ONLY_PAONA") sb.append(if (isBn) "মোট দেনা" else "Total Debt").append(delimiter).append(netBrw).append("\n")
                    if (selectedCategory != "ONLY_DEBT") sb.append(if (isBn) "মোট পাওনা" else "Total Receivable").append(delimiter).append(netLnd).append("\n")
                }
                "ONLY_SAVINGS" -> {
                    val totalSavDep = savingsTransactions.filter { it.isDeposit }.sumOf { it.amount }
                    val totalSavWith = savingsTransactions.filter { !it.isDeposit }.sumOf { it.amount }
                    sb.append(if (isBn) "সঞ্চয় জমা" else "Total Deposit").append(delimiter).append(totalSavDep).append("\n")
                    sb.append(if (isBn) "সঞ্চয় উত্তোলন" else "Total Withdrawal").append(delimiter).append(totalSavWith).append("\n")
                    sb.append(if (isBn) "অবশিষ্ট সঞ্চয়" else "Net Savings").append(delimiter).append(totalSavDep - totalSavWith).append("\n")
                }
                else -> {
                    val totalLend = transactions.filter { it.type == "LEND" }.sumOf { it.amount }
                    val totalRepayRecv = transactions.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                    val netLnd = totalLend - totalRepayRecv

                    val totalBorrow = transactions.filter { it.type == "BORROW" }.sumOf { it.amount }
                    val totalRepayPaid = transactions.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                    val netBrw = totalBorrow - totalRepayPaid

                    sb.append(if (isBn) "মোট আয়" else "Total Income").append(delimiter).append(totalInc).append("\n")
                    sb.append(if (isBn) "মোট ব্যয়" else "Total Expense").append(delimiter).append(totalExp).append("\n")
                    sb.append(if (isBn) "নিট ব্যালেন্স" else "Net Balance").append(delimiter).append(netBalance).append("\n")
                    sb.append(if (isBn) "মোট দেনা" else "Total Debt").append(delimiter).append(netBrw).append("\n")
                    sb.append(if (isBn) "মোট পাওনা" else "Total Receivable").append(delimiter).append(netLnd).append("\n")
                }
            }
        }
        sb.append("\n\n")

        if (transactions.isNotEmpty()) {
            sb.append(if (isBn) "লেনদেন বিবরণী (Transactions Log)" else "Transactions Log").append("\n")
            sb.append(
                listOf(
                    if (isBn) "ক্রমিক" else "Sl No",
                    if (isBn) "তারিখ" else "Date",
                    if (isBn) "ক্যাটাগরি" else "Category",
                    if (isBn) "ধরন" else "Type",
                    if (isBn) "ব্যক্তি" else "Person",
                    if (isBn) "বিবরণ/নোট" else "Note/Description",
                    if (isBn) "টাকা" else "Amount"
                ).joinToString(delimiter)
            ).append("\n")

            transactions.forEachIndexed { idx, tx ->
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(tx.timestamp))
                val typeStr = getTransactionTypeName(tx.type, language)
                val personName = persons.find { it.id == tx.personId }?.name ?: ""
                val noteSafe = tx.note.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val catSafe = tx.category.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val personSafe = personName.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val actualAmt = if (tx.type == "REPAY_PAID" || tx.type == "REPAY_RECEIVED") -tx.amount else tx.amount

                sb.append(
                    listOf(
                        (idx + 1).toString(),
                        dateStr,
                        catSafe,
                        typeStr,
                        personSafe,
                        noteSafe,
                        actualAmt.toString()
                    ).joinToString(delimiter)
                ).append("\n")
            }
            sb.append("\n\n")
        }

        if (savingsTransactions.isNotEmpty()) {
            sb.append(if (isBn) "সঞ্চয় বিবরণী (Savings Log)" else "Savings Log").append("\n")
            sb.append(
                listOf(
                    if (isBn) "ক্রমিক" else "Sl No",
                    if (isBn) "তারিখ" else "Date",
                    if (isBn) "লক্ষ্য শিরোনাম" else "Savings Goal",
                    if (isBn) "ধরন" else "Type",
                    if (isBn) "বিবরণ" else "Note",
                    if (isBn) "টাকা" else "Amount"
                ).joinToString(delimiter)
            ).append("\n")

            savingsTransactions.forEachIndexed { idx, stx ->
                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(stx.timestamp))
                val goalTitle = savingsGoals.find { it.id == stx.goalId }?.title ?: ""
                val typeStr = if (stx.isDeposit) (if (isBn) "সঞ্চয় জমা" else "Deposit") else (if (isBn) "সঞ্চয় উত্তোলন" else "Withdrawal")
                val noteSafe = stx.note.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val goalSafe = goalTitle.replace(",", " ").replace("\t", " ").replace("\n", " ")
                val actualAmt = if (stx.isDeposit) stx.amount else -stx.amount

                sb.append(
                    listOf(
                        (idx + 1).toString(),
                        dateStr,
                        goalSafe,
                        typeStr,
                        noteSafe,
                        actualAmt.toString()
                    ).joinToString(delimiter)
                ).append("\n")
            }
            sb.append("\n\n")
        }

        if (filteredBudgets.isNotEmpty()) {
            val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
            val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
            val months = if (isBn) monthNamesBn else monthNamesEn

            sb.append(if (isBn) "বাজেট বিবরণী (Budgets Log)" else "Budgets Log").append("\n")
            sb.append(
                listOf(
                    if (isBn) "ক্রমিক" else "Sl No",
                    if (isBn) "মাস/বছর" else "Month/Year",
                    if (isBn) "আয় টার্গেট" else "Income Target",
                    if (isBn) "আয় অর্জিত" else "Income Actual",
                    if (isBn) "ব্যয় টার্গেট" else "Expense Target",
                    if (isBn) "ব্যয়িত" else "Expense Actual",
                    if (isBn) "সঞ্চয় টার্গেট" else "Savings Target",
                    if (isBn) "সঞ্চিত" else "Savings Actual"
                ).joinToString(delimiter)
            ).append("\n")

            filteredBudgets.forEachIndexed { idx, budget ->
                val ymVal = "${months[budget.month]} ${budget.year}"
                val bActualInc = transactions.filter {
                    val (y, m) = getYearAndMonth(it.timestamp)
                    y == budget.year && m == budget.month && (it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT"))
                }.sumOf { it.amount }

                val bActualExp = transactions.filter {
                    val (y, m) = getYearAndMonth(it.timestamp)
                    y == budget.year && m == budget.month && (it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT"))
                }.sumOf { it.amount }

                val bActualSav = savingsTransactions.filter {
                    val (y, m) = getYearAndMonth(it.timestamp)
                    y == budget.year && m == budget.month
                }.sumOf { if (it.isDeposit) it.amount else -it.amount }

                sb.append(
                    listOf(
                        (idx + 1).toString(),
                        ymVal,
                        budget.income.toString(),
                        bActualInc.toString(),
                        budget.expense.toString(),
                        bActualExp.toString(),
                        budget.savings.toString(),
                        bActualSav.toString()
                    ).joinToString(delimiter)
                ).append("\n")
            }
        }
    }

    return sb.toString()
}

// Native Multi-Page A4 PDF Generator
private fun generatePdfFile(
    context: Context,
    language: AppLanguage,
    selectedCategory: String,
    timePeriodText: String,
    transactions: List<Transaction>,
    persons: List<Person>,
    savingsGoals: List<SavingsGoal>,
    savingsTransactions: List<SavingsTransaction>,
    filteredBudgets: List<MonthlyBudget>,
    summary: Map<String, Double>,
    file: File,
    filterByPerson: Boolean = false,
    selectedPerson: Person? = null,
    filterBySavingsGoal: Boolean = false,
    selectedSavingsGoal: SavingsGoal? = null
) {
    val finalPerson = if (filterByPerson) selectedPerson else null
    val finalSavingsGoal = if (filterBySavingsGoal) selectedSavingsGoal else null
    val pdfDocument = PdfDocument()
    val isBn = language == AppLanguage.BN

    val pageWidth = 595
    val pageHeight = 842

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var currentPage = pdfDocument.startPage(pageInfo)
    var canvas = currentPage.canvas

    val primaryPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B") // Executive Dark Slate
        textSize = 16f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#64748B") // Slate Gray
        textSize = 9.5f
        isAntiAlias = true
    }

    val statsHeaderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#334155") // Medium Slate
        textSize = 9.5f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val statsValPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 12f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val tableHeaderBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#334155") // Soft Slate Header
        isAntiAlias = true
    }

    val tableHeaderTxtPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 9f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val tableRowTxtPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B")
        textSize = 8.5f
        isAntiAlias = true
    }

    val tableRowTxtBoldPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A")
        textSize = 8.5f
        isFakeBoldText = true
        isAntiAlias = true
    }

    val zebraBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F8FAFC") // Soft clean off-white row tint
    }

    val dividerPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#E2E8F0")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (isBn) monthNamesBn else monthNamesEn

    fun drawPageFooter(canvas: Canvas, pageNum: Int) {
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 8.5f
            isAntiAlias = true
        }
        val genTime = SimpleDateFormat("dd MMMM, yyyy hh:mm a", Locale.getDefault()).format(Date())
        val footerStr = "${if (isBn) "ফাইন্যান্স নোট অ্যাপ • জেনারেট সময়: " else "Finance Note App • Generated: "}${if (isBn) replaceToBnDigits(genTime) else genTime}"
        val pageStr = if (isBn) "পৃষ্ঠা $pageNum" else "Page $pageNum"
        canvas.drawText(footerStr, 40f, 810f, paint)
        val w = paint.measureText(pageStr)
        canvas.drawText(pageStr, 555f - w, 810f, paint)
    }

    // Top Header Gradient / Banner
    val headerBannerPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B") // Executive Dark Navy Banner
        isAntiAlias = true
    }
    val headerRect = android.graphics.RectF(40f, 30f, 555f, 70f)
    canvas.drawRoundRect(headerRect, 10f, 10f, headerBannerPaint)

    // White Logo Emblem Circle
    val logoPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
    }
    canvas.drawCircle(58f, 50f, 12f, logoPaint)

    val symbolPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B")
        textSize = 13f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText("F", 54f, 55f, symbolPaint)

    val appNamePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 13f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText(if (isBn) "ফাইন্যান্স নোট" else "Finance Note App", 78f, 54f, appNamePaint)

    val appSubPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#94A3B8")
        textSize = 9f
        isAntiAlias = true
    }
    val officialStr = if (isBn) "অফিশিয়াল রিপোর্ট ও ডাটা স্টেটমেন্ট" else "Official Financial Statement"
    val offW = appSubPaint.measureText(officialStr)
    canvas.drawText(officialStr, 545f - offW, 54f, appSubPaint)

    // Category Title & Selected Time Period Subheader
    val reportTitle = if (finalPerson != null) {
        if (isBn) "${finalPerson.name} - লেনদেন বিবরণী" else "${finalPerson.name} - Transaction Statement"
    } else if (finalSavingsGoal != null) {
        if (isBn) "${finalSavingsGoal.title} - সঞ্চয় বিবরণী" else "${finalSavingsGoal.title} - Savings Statement"
    } else {
        getCategoryTitle(selectedCategory, language)
    }
    canvas.drawText(reportTitle, 40f, 92f, primaryPaint)

    val timePeriodStr = "${if (isBn) "সময়কাল: " else "Period: "}$timePeriodText"
    canvas.drawText(timePeriodStr, 40f, 108f, subtitlePaint)

    val genTimeStr = "${if (isBn) "তৈরির সময়: " else "Generated on: "}${SimpleDateFormat("dd MMMM, yyyy hh:mm a", Locale.getDefault()).format(Date())}"
    val genW = subtitlePaint.measureText(genTimeStr)
    canvas.drawText(genTimeStr, 555f - genW, 108f, subtitlePaint)

    // Category Specific Summary Box
    val cardBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F8FAFC") // Soft clean off-white
    }
    val cardBorderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#E2E8F0")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    val summaryRect = android.graphics.RectF(40f, 118f, 555f, 178f)
    canvas.drawRoundRect(summaryRect, 8f, 8f, cardBgPaint)
    canvas.drawRoundRect(summaryRect, 8f, 8f, cardBorderPaint)

    val inc = summary["income"] ?: 0.0
    val exp = summary["expense"] ?: 0.0
    val bal = summary["balance"] ?: 0.0
    val lnd = summary["lend"] ?: 0.0
    val brw = summary["borrow"] ?: 0.0
    val savDep = summary["savingsDeposit"] ?: 0.0
    val savWith = summary["savingsWithdraw"] ?: 0.0
    val savNet = summary["savingsNet"] ?: 0.0
    val bInc = summary["budIncTarget"] ?: 0.0
    val bExp = summary["budExpTarget"] ?: 0.0
    val repayPaid = summary["repayPaid"] ?: 0.0
    val repayRecv = summary["repayRecv"] ?: 0.0

    val netBrw = brw - repayPaid
    val netLnd = lnd - repayRecv

    val budgetReport = computeComprehensiveBudgetReport(language, filteredBudgets, transactions, savingsTransactions, savingsGoals)

    if (finalPerson != null) {
        val personTx = transactions
        val personBorrow = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
        val personRepayPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
        val personDebtNet = personBorrow - personRepayPaid

        val personLend = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
        val personRepayReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
        val personReceivableNet = personLend - personRepayReceived

        val netStatus = personReceivableNet - personDebtNet

        canvas.drawText(if (isBn) "মোট দেনা (Debt)" else "Total Debt", 60f, 140f, statsHeaderPaint)
        canvas.drawText(formatCurrency(personDebtNet, language), 60f, 160f, statsValPaint)

        canvas.drawText(if (isBn) "মোট পাওনা (Receivable)" else "Total Receivable", 230f, 140f, statsHeaderPaint)
        canvas.drawText(formatCurrency(personReceivableNet, language), 230f, 160f, statsValPaint)

        canvas.drawText(if (isBn) "বর্তমান অবস্থা (Net Status)" else "Net Status", 400f, 140f, statsHeaderPaint)
        val balColorPaint = Paint().apply {
            color = if (netStatus > 0) android.graphics.Color.parseColor("#059669")
                    else if (netStatus < 0) android.graphics.Color.parseColor("#DC2626")
                    else android.graphics.Color.parseColor("#64748B")
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val netStatusStr = if (netStatus > 0) {
            if (isBn) "পাবেন ${formatCurrency(netStatus, language)}" else "Get ${formatCurrency(netStatus, language)}"
        } else if (netStatus < 0) {
            if (isBn) "দেবেন ${formatCurrency(-netStatus, language)}" else "Owe ${formatCurrency(-netStatus, language)}"
        } else {
            if (isBn) "পরিশোধিত" else "Settled"
        }
        canvas.drawText(netStatusStr, 400f, 160f, balColorPaint)
    } else {
        when (selectedCategory) {
            "ONLY_INCOME" -> {
                canvas.drawText(if (isBn) "মোট আয় (Income)" else "Total Income", 60f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(inc, language), 60f, 160f, statsValPaint)
            }
            "ONLY_EXPENSE" -> {
                canvas.drawText(if (isBn) "মোট ব্যয় (Expense)" else "Total Expense", 60f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(exp, language), 60f, 160f, statsValPaint)
            }
            "INCOME_EXPENSE" -> {
                canvas.drawText(if (isBn) "মোট আয় (Income)" else "Total Income", 60f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(inc, language), 60f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "মোট ব্যয় (Expense)" else "Total Expense", 230f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(exp, language), 230f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "নিট ব্যালেন্স (Balance)" else "Net Balance", 400f, 140f, statsHeaderPaint)
                val balColorPaint = Paint().apply {
                    color = if (bal >= 0) android.graphics.Color.parseColor("#059669") else android.graphics.Color.parseColor("#DC2626")
                    textSize = 12f
                    isFakeBoldText = true
                }
                canvas.drawText(formatCurrency(bal, language), 400f, 160f, balColorPaint)
            }
            "DEBT_REPAYMENT", "ONLY_DEBT", "ONLY_PAONA" -> {
                var posX = 60f
                if (selectedCategory != "ONLY_PAONA") {
                    canvas.drawText(if (isBn) "মোট দেনা (Debt)" else "Total Debt", posX, 140f, statsHeaderPaint)
                    canvas.drawText(formatCurrency(netBrw, language), posX, 160f, statsValPaint)
                    posX += 170f
                }
                if (selectedCategory != "ONLY_DEBT") {
                    canvas.drawText(if (isBn) "মোট পাওনা (Receivable)" else "Total Receivable", posX, 140f, statsHeaderPaint)
                    canvas.drawText(formatCurrency(netLnd, language), posX, 160f, statsValPaint)
                }
            }
            "ONLY_SAVINGS" -> {
                canvas.drawText(if (isBn) "সঞ্চয় জমা (Deposit)" else "Savings Deposit", 60f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(savDep, language), 60f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "উত্তোলন (Withdrawal)" else "Withdrawal", 230f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(savWith, language), 230f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "অবশিষ্ট সঞ্চয় (Net Saved)" else "Net Savings", 400f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(savNet, language), 400f, 160f, statsValPaint)
            }
            "ONLY_BUDGET" -> {
                canvas.drawText(if (isBn) "আয় টার্গেট vs প্রকৃত" else "Inc Target vs Actual", 60f, 138f, statsHeaderPaint)
                canvas.drawText("${formatCurrencyNoSymbol(budgetReport.incomeAnalysis.targetAmount, language)} / ${formatCurrencyNoSymbol(budgetReport.incomeAnalysis.actualAmount, language)}", 60f, 153f, statsValPaint)
                val incPctStr = "${String.format(Locale.US, "%.1f", budgetReport.incomeAnalysis.fulfillmentPercentage)}%"
                val incPctPaint = Paint().apply { color = android.graphics.Color.parseColor("#059669"); textSize = 9f; isFakeBoldText = true }
                canvas.drawText("${if (isBn) "পূরণ: " else "Fulfilled: "}${if (isBn) replaceToBnDigits(incPctStr) else incPctStr}", 60f, 168f, incPctPaint)

                canvas.drawText(if (isBn) "ব্যয় টার্গেট vs প্রকৃত" else "Exp Target vs Actual", 230f, 138f, statsHeaderPaint)
                canvas.drawText("${formatCurrencyNoSymbol(budgetReport.expenseAnalysis.targetAmount, language)} / ${formatCurrencyNoSymbol(budgetReport.expenseAnalysis.actualAmount, language)}", 230f, 153f, statsValPaint)
                val expPctStr = "${String.format(Locale.US, "%.1f", budgetReport.expenseAnalysis.fulfillmentPercentage)}%"
                val expPctPaint = Paint().apply { color = android.graphics.Color.parseColor("#DC2626"); textSize = 9f; isFakeBoldText = true }
                canvas.drawText("${if (isBn) "ব্যবহৃত: " else "Used: "}${if (isBn) replaceToBnDigits(expPctStr) else expPctStr}", 230f, 168f, expPctPaint)

                canvas.drawText(if (isBn) "সঞ্চয় টার্গেট vs প্রকৃত" else "Sav Target vs Actual", 400f, 138f, statsHeaderPaint)
                canvas.drawText("${formatCurrencyNoSymbol(budgetReport.savingsAnalysis.targetAmount, language)} / ${formatCurrencyNoSymbol(budgetReport.savingsAnalysis.actualAmount, language)}", 400f, 153f, statsValPaint)
                val savPctStr = "${String.format(Locale.US, "%.1f", budgetReport.savingsAnalysis.fulfillmentPercentage)}%"
                val savPctPaint = Paint().apply { color = android.graphics.Color.parseColor("#8B5CF6"); textSize = 9f; isFakeBoldText = true }
                canvas.drawText("${if (isBn) "পূরণ: " else "Achieved: "}${if (isBn) replaceToBnDigits(savPctStr) else savPctStr}", 400f, 168f, savPctPaint)
            }
            else -> {
                canvas.drawText(if (isBn) "মোট আয়" else "Income", 50f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(inc, language), 50f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "মোট ব্যয়" else "Expense", 155f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(exp, language), 155f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "মোট দেনা" else "Debt", 260f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(netBrw, language), 260f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "মোট পাওনা" else "Credit", 365f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(netLnd, language), 365f, 160f, statsValPaint)

                canvas.drawText(if (isBn) "মোট সঞ্চয়" else "Savings", 470f, 140f, statsHeaderPaint)
                canvas.drawText(formatCurrency(savNet, language), 470f, 160f, statsValPaint)
            }
        }
    }

    fun drawTableHeader(canvas: Canvas, y: Float) {
        canvas.drawRect(40f, y, 555f, y + 22f, tableHeaderBgPaint)
        canvas.drawText(if (isBn) "তারিখ" else "Date", 45f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "ক্যাটাগরি" else "Category", 115f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "ধরন" else "Type", 205f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "ব্যক্তি" else "Person", 275f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "বিবরণ" else "Note", 355f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "টাকা" else "Amount", 495f, y + 14f, tableHeaderTxtPaint)
    }

    var currentY = 195f

    // 1. If ONLY_BUDGET, draw full budget & percentage breakdown tables
    if (selectedCategory == "ONLY_BUDGET") {
        val secTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 10.5f
            isFakeBoldText = true
            isAntiAlias = true
        }

        canvas.drawText(if (isBn) "১. বাজেট পারফরম্যান্স ও অর্জনের সারসংক্ষেপ" else "1. Budget Performance & Achievement Summary", 40f, currentY + 12f, secTitlePaint)
        currentY += 18f

        fun drawBudgetOverviewHeader(canvas: Canvas, y: Float) {
            canvas.drawRect(40f, y, 555f, y + 22f, tableHeaderBgPaint)
            canvas.drawText(if (isBn) "বাজেটের বিষয়" else "Budget Metric", 45f, y + 14f, tableHeaderTxtPaint)
            canvas.drawText(if (isBn) "নির্ধারিত টার্গেট" else "Target Amount", 150f, y + 14f, tableHeaderTxtPaint)
            canvas.drawText(if (isBn) "প্রকৃত অর্জন/ব্যয়" else "Actual Amount", 260f, y + 14f, tableHeaderTxtPaint)
            canvas.drawText(if (isBn) "পূরণ/ব্যবহার %" else "Fulfillment %", 370f, y + 14f, tableHeaderTxtPaint)
            canvas.drawText(if (isBn) "অবশিষ্ট / পার্থক্য" else "Variance", 460f, y + 14f, tableHeaderTxtPaint)
        }

        drawBudgetOverviewHeader(canvas, currentY)
        currentY += 22f

        val metricsList = listOf(budgetReport.incomeAnalysis, budgetReport.expenseAnalysis, budgetReport.savingsAnalysis)

        metricsList.forEachIndexed { idx, m ->
            if (currentY + 22f > 780f) {
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                currentY = 45f
                drawBudgetOverviewHeader(canvas, currentY)
                currentY += 22f
            }

            if (idx % 2 == 1) {
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, zebraBgPaint)
            }
            canvas.drawLine(40f, currentY + 20f, 555f, currentY + 20f, dividerPaint)

            canvas.drawText(m.typeName, 45f, currentY + 13f, tableRowTxtBoldPaint)
            canvas.drawText(formatCurrencyNoSymbol(m.targetAmount, language), 150f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(formatCurrencyNoSymbol(m.actualAmount, language), 260f, currentY + 13f, tableRowTxtPaint)

            val pctStr = "${String.format(Locale.US, "%.1f", m.fulfillmentPercentage)}%"
            val pctStrBn = if (isBn) replaceToBnDigits(pctStr) else pctStr
            canvas.drawText(pctStrBn, 370f, currentY + 13f, tableRowTxtBoldPaint)

            canvas.drawText(formatCurrencyNoSymbol(m.remainingAmount, language), 460f, currentY + 13f, tableRowTxtPaint)

            currentY += 20f
        }

        currentY += 12f

        // Category Breakdowns
        val categoriesSections = listOf(
            Pair(if (isBn) "২. আয় খাতভিত্তিক বাজেট বিশ্লেষণ (খাতওয়ারি তথ্য)" else "2. Income Category Breakdown", budgetReport.incomeAnalysis.categories),
            Pair(if (isBn) "৩. ব্যয় খাতভিত্তিক বাজেট বিশ্লেষণ (খাতওয়ারি তথ্য)" else "3. Expense Category Breakdown", budgetReport.expenseAnalysis.categories),
            Pair(if (isBn) "৪. সঞ্চয় লক্ষ্যভিত্তিক বাজেট বিশ্লেষণ (খাতওয়ারি তথ্য)" else "4. Savings Goal Breakdown", budgetReport.savingsAnalysis.categories)
        )

        categoriesSections.forEach { (secTitle, catList) ->
            if (catList.isNotEmpty()) {
                if (currentY + 50f > 780f) {
                    drawPageFooter(canvas, pageNumber)
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    currentY = 45f
                }

                canvas.drawText(secTitle, 40f, currentY + 12f, secTitlePaint)
                currentY += 18f

                fun drawCatHeader(canvas: Canvas, y: Float) {
                    canvas.drawRect(40f, y, 555f, y + 22f, tableHeaderBgPaint)
                    canvas.drawText(if (isBn) "খাত / লক্ষ্য শিরোনাম" else "Category / Goal", 45f, y + 14f, tableHeaderTxtPaint)
                    canvas.drawText(if (isBn) "প্রকৃত পরিমাণ" else "Actual Amount", 210f, y + 14f, tableHeaderTxtPaint)
                    canvas.drawText(if (isBn) "বাজেটের % (% Target)" else "% of Target Budget", 330f, y + 14f, tableHeaderTxtPaint)
                    canvas.drawText(if (isBn) "মোট এর % (% Total)" else "% of Total", 450f, y + 14f, tableHeaderTxtPaint)
                }

                drawCatHeader(canvas, currentY)
                currentY += 22f

                catList.forEachIndexed { idx, c ->
                    if (currentY + 22f > 780f) {
                        drawPageFooter(canvas, pageNumber)
                        pdfDocument.finishPage(currentPage)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        currentPage = pdfDocument.startPage(pageInfo)
                        canvas = currentPage.canvas
                        currentY = 45f
                        drawCatHeader(canvas, currentY)
                        currentY += 22f
                    }

                    if (idx % 2 == 1) {
                        canvas.drawRect(40f, currentY, 555f, currentY + 20f, zebraBgPaint)
                    }
                    canvas.drawLine(40f, currentY + 20f, 555f, currentY + 20f, dividerPaint)

                    val catNameTrunc = if (c.categoryName.length > 22) c.categoryName.substring(0, 20) + ".." else c.categoryName
                    canvas.drawText(catNameTrunc, 45f, currentY + 13f, tableRowTxtPaint)
                    canvas.drawText(formatCurrencyNoSymbol(c.amount, language), 210f, currentY + 13f, tableRowTxtPaint)

                    val tgtPctStr = "${String.format(Locale.US, "%.1f", c.percentageOfTarget)}%"
                    canvas.drawText(if (isBn) replaceToBnDigits(tgtPctStr) else tgtPctStr, 330f, currentY + 13f, tableRowTxtBoldPaint)

                    val totPctStr = "${String.format(Locale.US, "%.1f", c.percentageOfTotal)}%"
                    canvas.drawText(if (isBn) replaceToBnDigits(totPctStr) else totPctStr, 450f, currentY + 13f, tableRowTxtPaint)

                    currentY += 20f
                }

                currentY += 12f
            }
        }
    }

    // 2. Grouped Transactions Table with Subtotals
    class PdfGroup(
        val title: String,
        val items: List<Transaction>,
        val subtotalLabel: String,
        val colorHex: String,
        val bgHex: String
    )

    val pdfGroups = mutableListOf<PdfGroup>()
    val isPersonReport = filterByPerson || finalPerson != null

    if (selectedCategory != "ONLY_BUDGET" && selectedCategory != "ONLY_SAVINGS" && transactions.isNotEmpty()) {
        if (isPersonReport || selectedCategory == "ONLY_DEBT" || selectedCategory == "ONLY_PAONA" || selectedCategory == "DEBT_REPAYMENT") {
            // Person / Debt / Paona report: Show Debt first, then Receivable!
            if (selectedCategory != "ONLY_PAONA") {
                val debtItems = transactions.filter { it.type == "BORROW" || it.type == "REPAY_PAID" }
                if (debtItems.isNotEmpty()) {
                    pdfGroups.add(
                        PdfGroup(
                            title = if (isBn) "১. দেনা হিসাব (Debt / Borrow Logs)" else "1. Debt / Borrow Logs",
                            items = debtItems,
                            subtotalLabel = if (isBn) "মোট দেনা (Total Debt):" else "Total Debt:",
                            colorHex = "#D97706",
                            bgHex = "#FFFBEB"
                        )
                    )
                }
            }
            if (selectedCategory != "ONLY_DEBT") {
                val recItems = transactions.filter { it.type == "LEND" || it.type == "REPAY_RECEIVED" }
                if (recItems.isNotEmpty()) {
                    val gIdx = if (pdfGroups.isEmpty()) "১" else "২"
                    pdfGroups.add(
                        PdfGroup(
                            title = if (isBn) "$gIdx. পাওনা হিসাব (Receivable / Lend Logs)" else "$gIdx. Receivable / Lend Logs",
                            items = recItems,
                            subtotalLabel = if (isBn) "মোট পাওনা (Total Credit):" else "Total Credit:",
                            colorHex = "#2563EB",
                            bgHex = "#EFF6FF"
                        )
                    )
                }
            }
        } else {
            // General Report: Income -> Expense -> Debt -> Receivable
            var gIdx = 1
            if (selectedCategory == "ALL_DATA" || selectedCategory == "TRANSACTIONS" || selectedCategory == "INCOME_EXPENSE" || selectedCategory == "ONLY_INCOME") {
                val incItems = transactions.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }
                if (incItems.isNotEmpty()) {
                    val prefix = if (isBn) "${replaceToBnDigits(gIdx.toString())}." else "$gIdx."
                    pdfGroups.add(
                        PdfGroup(
                            title = "$prefix ${if (isBn) "আয় বিবরণী (Income Logs)" else "Income Logs"}",
                            items = incItems,
                            subtotalLabel = if (isBn) "মোট আয় (Total Income):" else "Total Income:",
                            colorHex = "#059669",
                            bgHex = "#F0FDF4"
                        )
                    )
                    gIdx++
                }
            }

            if (selectedCategory == "ALL_DATA" || selectedCategory == "TRANSACTIONS" || selectedCategory == "INCOME_EXPENSE" || selectedCategory == "ONLY_EXPENSE") {
                val expItems = transactions.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }
                if (expItems.isNotEmpty()) {
                    val prefix = if (isBn) "${replaceToBnDigits(gIdx.toString())}." else "$gIdx."
                    pdfGroups.add(
                        PdfGroup(
                            title = "$prefix ${if (isBn) "ব্যয় বিবরণী (Expense Logs)" else "Expense Logs"}",
                            items = expItems,
                            subtotalLabel = if (isBn) "মোট ব্যয় (Total Expense):" else "Total Expense:",
                            colorHex = "#DC2626",
                            bgHex = "#FEF2F2"
                        )
                    )
                    gIdx++
                }
            }

            if (selectedCategory == "ALL_DATA" || selectedCategory == "TRANSACTIONS") {
                val debtItems = transactions.filter { it.type == "BORROW" || it.type == "REPAY_PAID" }
                if (debtItems.isNotEmpty()) {
                    val prefix = if (isBn) "${replaceToBnDigits(gIdx.toString())}." else "$gIdx."
                    pdfGroups.add(
                        PdfGroup(
                            title = "$prefix ${if (isBn) "দেনা হিসাব (Debt / Borrow Logs)" else "Debt Logs"}",
                            items = debtItems,
                            subtotalLabel = if (isBn) "মোট দেনা (Total Debt):" else "Total Debt:",
                            colorHex = "#D97706",
                            bgHex = "#FFFBEB"
                        )
                    )
                    gIdx++
                }

                val recItems = transactions.filter { it.type == "LEND" || it.type == "REPAY_RECEIVED" }
                if (recItems.isNotEmpty()) {
                    val prefix = if (isBn) "${replaceToBnDigits(gIdx.toString())}." else "$gIdx."
                    pdfGroups.add(
                        PdfGroup(
                            title = "$prefix ${if (isBn) "পাওনা হিসাব (Receivable Logs)" else "Receivable Logs"}",
                            items = recItems,
                            subtotalLabel = if (isBn) "মোট পাওনা (Total Credit):" else "Total Credit:",
                            colorHex = "#2563EB",
                            bgHex = "#EFF6FF"
                        )
                    )
                    gIdx++
                }
            }
        }
    }

    // Render each Grouped Transaction Table with Subtotal
    pdfGroups.forEach { grp ->
        if (currentY + 50f > 780f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas
            currentY = 45f
        }

        val secTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 10.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(grp.title, 40f, currentY + 12f, secTitlePaint)
        currentY += 18f

        drawTableHeader(canvas, currentY)
        currentY += 22f

        grp.items.forEachIndexed { index, tx ->
            if (currentY + 22f > 780f) {
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                currentY = 45f
                drawTableHeader(canvas, currentY)
                currentY += 22f
            }

            // Determine background color and text colors for subtracted rows
            var rowBgPaint: Paint? = null
            var rowCustomTxtPaint: Paint? = null
            var rowCustomAmtPaint: Paint? = null
            
            if (tx.type == "REPAY_PAID") {
                // দেনা ফেরত: দায় কমছে -> সবুজ (soft green row, green text)
                rowBgPaint = Paint().apply { color = android.graphics.Color.parseColor("#E8F5E9") }
                rowCustomTxtPaint = Paint(tableRowTxtPaint).apply { color = android.graphics.Color.parseColor("#1B5E20") }
                rowCustomAmtPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#2E7D32")
                    textSize = 8.5f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            } else if (tx.type == "REPAY_RECEIVED") {
                // পাওনা ফেরত: পাওনা কমছে -> লাল (soft red row, red text)
                rowBgPaint = Paint().apply { color = android.graphics.Color.parseColor("#FFEBEE") }
                rowCustomTxtPaint = Paint(tableRowTxtPaint).apply { color = android.graphics.Color.parseColor("#B71C1C") }
                rowCustomAmtPaint = Paint().apply {
                    color = android.graphics.Color.parseColor("#C62828")
                    textSize = 8.5f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            }

            if (rowBgPaint != null) {
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, rowBgPaint)
            } else if (index % 2 == 1) {
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, zebraBgPaint)
            }
            canvas.drawLine(40f, currentY + 20f, 555f, currentY + 20f, dividerPaint)

            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(tx.timestamp))
            val personName = persons.find { it.id == tx.personId }?.name ?: ""
            val typeStr = getTransactionTypeName(tx.type, language)

            val currentTxtPaint = rowCustomTxtPaint ?: tableRowTxtPaint
            val colorType = if (tx.type == "REPAY_PAID") {
                android.graphics.Color.parseColor("#2E7D32")
            } else if (tx.type == "REPAY_RECEIVED") {
                android.graphics.Color.parseColor("#C62828")
            } else {
                android.graphics.Color.parseColor(grp.colorHex)
            }
            val typeColorPaint = Paint().apply {
                color = colorType
                textSize = 8.5f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val catTrunc = if (tx.category.length > 15) tx.category.substring(0, 13) + ".." else tx.category
            val personTrunc = if (personName.length > 12) personName.substring(0, 10) + ".." else personName
            val noteTrunc = if (tx.note.length > 22) tx.note.substring(0, 19) + ".." else tx.note

            canvas.drawText(dateStr, 45f, currentY + 13f, currentTxtPaint)
            canvas.drawText(catTrunc, 115f, currentY + 13f, currentTxtPaint)
            canvas.drawText(typeStr, 205f, currentY + 13f, typeColorPaint)
            canvas.drawText(personTrunc, 275f, currentY + 13f, currentTxtPaint)
            canvas.drawText(noteTrunc, 355f, currentY + 13f, currentTxtPaint)

            val actualAmt = if (tx.type == "REPAY_PAID" || tx.type == "REPAY_RECEIVED") -tx.amount else tx.amount
            val amtText = formatCurrencyNoSymbol(actualAmt, language)
            val rowAmtPaint = rowCustomAmtPaint ?: if (actualAmt < 0) {
                Paint().apply {
                    color = android.graphics.Color.parseColor("#DC2626") // Subtraction Crimson Red
                    textSize = 8.5f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            } else {
                tableRowTxtBoldPaint
            }
            val amtW = rowAmtPaint.measureText(amtText)
            canvas.drawText(amtText, 550f - amtW, currentY + 13f, rowAmtPaint)

            currentY += 20f
        }

        // Subtotal row at end of table
        if (currentY + 25f > 780f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas
            currentY = 45f
        }

        val subBgPaint = Paint().apply { color = android.graphics.Color.parseColor(grp.bgHex) }
        val subBorderPaint = Paint().apply { color = android.graphics.Color.parseColor("#CBD5E1"); strokeWidth = 1f; style = Paint.Style.STROKE }

        canvas.drawRect(40f, currentY, 555f, currentY + 22f, subBgPaint)
        canvas.drawLine(40f, currentY, 555f, currentY, subBorderPaint)
        canvas.drawLine(40f, currentY + 22f, 555f, currentY + 22f, subBorderPaint)

        val subLblPaint = Paint().apply { color = android.graphics.Color.parseColor("#1E293B"); textSize = 9.5f; isFakeBoldText = true; isAntiAlias = true }
        val subValPaint = Paint().apply { color = android.graphics.Color.parseColor(grp.colorHex); textSize = 10f; isFakeBoldText = true; isAntiAlias = true }

        val grpTotal = grp.items.sumOf { if (it.type == "REPAY_PAID" || it.type == "REPAY_RECEIVED") -it.amount else it.amount }
        canvas.drawText(grp.subtotalLabel, 45f, currentY + 15f, subLblPaint)

        val grpTotalStr = formatCurrencyNoSymbol(grpTotal, language)
        val grpTotalW = subValPaint.measureText(grpTotalStr)
        canvas.drawText(grpTotalStr, 550f - grpTotalW, currentY + 15f, subValPaint)

        currentY += 32f
    }

    // 3. Draw Savings Table (Excluding Person Export)
    if ((selectedCategory == "ALL_DATA" || selectedCategory == "ONLY_SAVINGS") && savingsTransactions.isNotEmpty() && !isPersonReport) {
        if (currentY + 50f > 780f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas

            currentY = 45f
        }

        val secTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E293B")
            textSize = 10.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val savSecTitle = if (isBn) "সঞ্চয় বিবরণী (Savings Logs)" else "Savings Logs"
        canvas.drawText(savSecTitle, 40f, currentY + 12f, secTitlePaint)
        currentY += 18f

        canvas.drawRect(40f, currentY, 555f, currentY + 22f, tableHeaderBgPaint)
        canvas.drawText(if (isBn) "তারিখ" else "Date", 45f, currentY + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "লক্ষ্য শিরোনাম" else "Goal Title", 130f, currentY + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "ধরন" else "Type", 260f, currentY + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "বিবরণ" else "Note", 350f, currentY + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "টাকা" else "Amount", 495f, currentY + 14f, tableHeaderTxtPaint)
        currentY += 22f

        savingsTransactions.forEachIndexed { index, stx ->
            if (currentY + 22f > 780f) {
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(currentPage)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas

                currentY = 45f
                canvas.drawRect(40f, currentY, 555f, currentY + 22f, tableHeaderBgPaint)
                canvas.drawText(if (isBn) "তারিখ" else "Date", 45f, currentY + 14f, tableHeaderTxtPaint)
                canvas.drawText(if (isBn) "লক্ষ্য শিরোনাম" else "Goal Title", 130f, currentY + 14f, tableHeaderTxtPaint)
                canvas.drawText(if (isBn) "ধরন" else "Type", 260f, currentY + 14f, tableHeaderTxtPaint)
                canvas.drawText(if (isBn) "বিবরণ" else "Note", 350f, currentY + 14f, tableHeaderTxtPaint)
                canvas.drawText(if (isBn) "টাকা" else "Amount", 495f, currentY + 14f, tableHeaderTxtPaint)
                currentY += 22f
            }

            if (index % 2 == 1) {
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, zebraBgPaint)
            }
            canvas.drawLine(40f, currentY + 20f, 555f, currentY + 20f, dividerPaint)

            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(stx.timestamp))
            val goalTitle = savingsGoals.find { it.id == stx.goalId }?.title ?: ""
            val typeStr = if (stx.isDeposit) (if (isBn) "সঞ্চয় জমা" else "Deposit") else (if (isBn) "উত্তোলন" else "Withdrawal")
            val typeColor = if (stx.isDeposit) android.graphics.Color.parseColor("#059669") else android.graphics.Color.parseColor("#DC2626")

            val typeColorPaint = Paint().apply {
                color = typeColor
                textSize = 8.5f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val goalTrunc = if (goalTitle.length > 20) goalTitle.substring(0, 18) + ".." else goalTitle
            val noteTrunc = if (stx.note.length > 25) stx.note.substring(0, 22) + ".." else stx.note

            canvas.drawText(dateStr, 45f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(goalTrunc, 130f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(typeStr, 260f, currentY + 13f, typeColorPaint)
            canvas.drawText(noteTrunc, 350f, currentY + 13f, tableRowTxtPaint)

            val actualAmt = if (stx.isDeposit) stx.amount else -stx.amount
            val amtText = formatCurrencyNoSymbol(actualAmt, language)
            val rowAmtPaint = if (actualAmt < 0) {
                Paint().apply {
                    color = android.graphics.Color.parseColor("#DC2626") // Subtraction Crimson Red
                    textSize = 8.5f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            } else {
                tableRowTxtBoldPaint
            }
            val amtW = rowAmtPaint.measureText(amtText)
            canvas.drawText(amtText, 550f - amtW, currentY + 13f, rowAmtPaint)

            currentY += 20f
        }

        // Savings Subtotal Row
        if (currentY + 25f > 780f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas
            currentY = 45f
        }

        val savDepTot = savingsTransactions.filter { it.isDeposit }.sumOf { it.amount }
        val savWithTot = savingsTransactions.filter { !it.isDeposit }.sumOf { it.amount }
        val savNetTot = savDepTot - savWithTot

        val savBgPaint = Paint().apply { color = android.graphics.Color.parseColor("#F5F3FF") }
        val savBorderPaint = Paint().apply { color = android.graphics.Color.parseColor("#CBD5E1"); strokeWidth = 1f; style = Paint.Style.STROKE }

        canvas.drawRect(40f, currentY, 555f, currentY + 22f, savBgPaint)
        canvas.drawLine(40f, currentY, 555f, currentY, savBorderPaint)
        canvas.drawLine(40f, currentY + 22f, 555f, currentY + 22f, savBorderPaint)

        val savLblPaint = Paint().apply { color = android.graphics.Color.parseColor("#1E293B"); textSize = 9f; isFakeBoldText = true; isAntiAlias = true }
        val savValPaint = Paint().apply { color = android.graphics.Color.parseColor("#8B5CF6"); textSize = 9.5f; isFakeBoldText = true; isAntiAlias = true }

        val savLblStr = if (isBn) {
            "মোট জমা: ${formatCurrencyNoSymbol(savDepTot, language)} | উত্তোলন: ${formatCurrencyNoSymbol(savWithTot, language)} | নিট সঞ্চয়:"
        } else {
            "Total Dep: ${formatCurrencyNoSymbol(savDepTot, language)} | Withdraw: ${formatCurrencyNoSymbol(savWithTot, language)} | Net Saved:"
        }
        canvas.drawText(savLblStr, 45f, currentY + 15f, savLblPaint)

        val netStr = formatCurrencyNoSymbol(savNetTot, language)
        val netW = savValPaint.measureText(netStr)
        canvas.drawText(netStr, 550f - netW, currentY + 15f, savValPaint)

        currentY += 32f
    }

    drawPageFooter(canvas, pageNumber)
    pdfDocument.finishPage(currentPage)

    try {
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        pdfDocument.close()
    }
}

@Composable
private fun PreviewSummaryBadge(
    label: String,
    amount: Double,
    color: Color,
    language: AppLanguage,
    isDark: Boolean
) {
    Surface(
        color = color.copy(alpha = if (isDark) 0.2f else 0.12f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.8f) else color)
            Text(text = formatCurrency(amount, language), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White else color)
        }
    }
}

@Composable
private fun BudgetFulfillmentPreviewCard(
    title: String,
    actual: Double,
    target: Double,
    fulfillmentPctText: String,
    color: Color,
    language: AppLanguage,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "${if (language == AppLanguage.BN) "প্রকৃত" else "Actual"}: ${formatCurrency(actual, language)} / ${if (language == AppLanguage.BN) "টার্গেট" else "Target"}: ${formatCurrency(target, language)}",
                    fontSize = 10.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = fulfillmentPctText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

fun getFormattedTimePeriod(
    language: AppLanguage,
    timeFilter: String,
    month: Int,
    year: Int,
    startDateMillis: Long,
    endDateMillis: Long,
    startMonth: Int,
    startYear: Int,
    endMonth: Int,
    endYear: Int,
    startYearRange: Int,
    endYearRange: Int
): String {
    val isBn = language == AppLanguage.BN
    val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (isBn) monthNamesBn else monthNamesEn

    return when (timeFilter) {
        "MONTH" -> "${months[month]} ${if (isBn) replaceToBnDigits(year.toString()) else year.toString()}"
        "YEAR" -> if (isBn) "${replaceToBnDigits(year.toString())} সাল" else "Year $year"
        "DATE_RANGE" -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startStr = sdf.format(Date(startDateMillis))
            val endStr = sdf.format(Date(endDateMillis))
            val s = if (isBn) replaceToBnDigits(startStr) else startStr
            val e = if (isBn) replaceToBnDigits(endStr) else endStr
            "$s - $e"
        }
        "MONTH_RANGE" -> {
            val sm = "${months[startMonth]} ${if (isBn) replaceToBnDigits(startYear.toString()) else startYear}"
            val em = "${months[endMonth]} ${if (isBn) replaceToBnDigits(endYear.toString()) else endYear}"
            "$sm - $em"
        }
        "YEAR_RANGE" -> {
            val sy = if (isBn) replaceToBnDigits(startYearRange.toString()) else startYearRange.toString()
            val ey = if (isBn) replaceToBnDigits(endYearRange.toString()) else endYearRange.toString()
            "$sy - $ey"
        }
        else -> if (isBn) "সব সময়ের" else "All Time"
    }
}

fun getCategoryTitle(category: String, language: AppLanguage): String {
    val isBn = language == AppLanguage.BN
    return when (category) {
        "ALL_DATA" -> if (isBn) "সকল তথ্যের সর্বমোট হিসাব" else "Complete Financial Statement"
        "TRANSACTIONS" -> if (isBn) "সকল লেনদেনের হিসাব" else "All Transactions Report"
        "INCOME_EXPENSE" -> if (isBn) "আয়-ব্যয়ের হিসাব" else "Income & Expense Report"
        "ONLY_INCOME" -> if (isBn) "আয়ের হিসাব" else "Income Statement"
        "ONLY_EXPENSE" -> if (isBn) "ব্যয়ের হিসাব" else "Expense Statement"
        "DEBT_REPAYMENT" -> if (isBn) "দেনা-পাওনার হিসাব" else "Debts & Receivables Report"
        "ONLY_DEBT" -> if (isBn) "দেনার হিসাব" else "Debts Statement"
        "ONLY_PAONA" -> if (isBn) "পাওনার হিসাব" else "Receivables Statement"
        "ONLY_SAVINGS" -> if (isBn) "সঞ্চয়ের হিসাব" else "Savings Statement"
        "ONLY_BUDGET" -> if (isBn) "মাসিক বাজেট কন্ট্রোল হিসাব" else "Monthly Budget Report"
        else -> if (isBn) "আর্থিক প্রতিবেদন" else "Financial Statement"
    }
}

private fun formatCurrencyNoSymbol(amount: Double, lang: AppLanguage): String {
    val formatter = DecimalFormat("#,##,###.##")
    val formatted = formatter.format(amount)
    return if (lang == AppLanguage.BN) {
        formatted
            .replace("0", "০")
            .replace("1", "১")
            .replace("2", "২")
            .replace("3", "৩")
            .replace("4", "৪")
            .replace("5", "৫")
            .replace("6", "৬")
            .replace("7", "৭")
            .replace("8", "৮")
            .replace("9", "৯")
    } else {
        formatted
    }
}

private fun getTransactionTypeName(type: String, language: AppLanguage): String {
    val isBn = language == AppLanguage.BN
    return when (type) {
        "INCOME" -> if (isBn) "আয়" else "Income"
        "EXPENSE" -> if (isBn) "ব্যয়" else "Expense"
        "LEND" -> if (isBn) "পাওনা (ঋণ দেওয়া)" else "Lent"
        "BORROW" -> if (isBn) "দেনা (ঋণ নেওয়া)" else "Borrowed"
        "REPAY_PAID" -> if (isBn) "দেনা পরিশোধ" else "Debt Paid"
        "REPAY_RECEIVED" -> if (isBn) "পাওনা আদায়" else "Pawn Collected"
        else -> type
    }
}

private fun getYearAndMonth(timestamp: Long): Pair<Int, Int> {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
}
