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
    initialCategory: String = "ALL_DATA",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isBn = language == AppLanguage.BN
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }

    // 1. Format state (PDF, Excel, CSV)
    var selectedFormat by remember { mutableStateOf("PDF") }

    // 2. Data Type state
    var selectedCategory by remember { mutableStateOf(initialCategory) }

    // 3. Time Filter state
    var selectedTimeFilter by remember { mutableStateOf("MONTH") }

    // Selected Month & Year
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    // Date range
    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L) }
    var endDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    // Month range
    var startMonth by remember { mutableStateOf(0) } // Jan
    var startYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var endMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var endYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    // Year range
    var startYearRange by remember { mutableStateOf(calendar.get(Calendar.YEAR) - 1) }
    var endYearRange by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    // Person filter state
    var filterByPerson by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<Person?>(null) }
    var showPersonDropdown by remember { mutableStateOf(false) }

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
        selectedPerson, filterByPerson
    ) {
        var list = transactions

        // Category Filter
        list = when (selectedCategory) {
            "ALL_DATA", "TRANSACTIONS" -> list
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
        if (filterByPerson && selectedPerson != null) {
            list = list.filter { it.personId == selectedPerson!!.id }
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
        startMonth, startYear, endMonth, endYear, startYearRange, endYearRange
    ) {
        if (selectedCategory != "ALL_DATA" && selectedCategory != "ONLY_SAVINGS") {
            return@remember emptyList<SavingsTransaction>()
        }

        var list = savingsTransactions
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
        startMonth, startYear, endMonth, endYear, startYearRange, endYearRange
    ) {
        if (selectedCategory != "ALL_DATA" && selectedCategory != "ONLY_BUDGET") {
            return@remember emptyList<MonthlyBudget>()
        }

        var list = monthlyBudgets

        list = when (selectedTimeFilter) {
            "MONTH" -> {
                list.filter {
                    it.year == selectedYear && it.month == selectedMonth
                }
            }
            "YEAR" -> {
                list.filter {
                    it.year == selectedYear
                }
            }
            "DATE_RANGE" -> {
                val startCal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                val endCal = Calendar.getInstance().apply { timeInMillis = endDateMillis }
                val startVal = startCal.get(Calendar.YEAR) * 12 + startCal.get(Calendar.MONTH)
                val endVal = endCal.get(Calendar.YEAR) * 12 + endCal.get(Calendar.MONTH)
                list.filter {
                    val currentVal = it.year * 12 + it.month
                    currentVal in startVal..endVal
                }
            }
            "MONTH_RANGE" -> {
                val startVal = startYear * 12 + startMonth
                val endVal = endYear * 12 + endMonth
                list.filter {
                    val currentVal = it.year * 12 + it.month
                    currentVal in startVal..endVal
                }
            }
            "YEAR_RANGE" -> {
                list.filter {
                    it.year in startYearRange..endYearRange
                }
            }
            else -> list
        }

        list.sortedWith(compareByDescending<MonthlyBudget> { it.year }.thenByDescending { it.month })
    }

    // Month lists
    val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (isBn) monthNamesBn else monthNamesEn

    // SAF CreateDocument Launcher for saving to a custom folder chosen by the user
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isExporting = true
            scope.launch(Dispatchers.IO) {
                try {
                    val totalInc = filteredTx.filter { it.type == "INCOME" }.sumOf { it.amount }
                    val totalExp = filteredTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                    val totalLend = filteredTx.filter { it.type == "LEND" }.sumOf { it.amount }
                    val totalBorrow = filteredTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                    val totalRepayPaid = filteredTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                    val totalRepayRecv = filteredTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                    val netBalance = totalInc - totalExp

                    val summary = mapOf(
                        "income" to totalInc,
                        "expense" to totalExp,
                        "balance" to netBalance,
                        "lend" to totalLend,
                        "borrow" to totalBorrow,
                        "repayPaid" to totalRepayPaid,
                        "repayRecv" to totalRepayRecv
                    )

                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        if (selectedFormat == "PDF") {
                            val tempFile = File(context.cacheDir, "temp_export.pdf")
                            generatePdfFile(context, language, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, summary, tempFile)
                            tempFile.inputStream().use { input ->
                                input.copyTo(os)
                            }
                            tempFile.delete()
                        } else {
                            val csvContent = generateCsvData(language, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, selectedFormat == "EXCEL")
                            os.write(csvContent.toByteArray(Charsets.UTF_8))
                        }
                    }

                    withContext(Dispatchers.Main) {
                        isExporting = false
                        Toast.makeText(context, if (isBn) "সফলভাবে ফোল্ডারে ফাইলটি সংরক্ষণ করা হয়েছে!" else "File successfully saved to your selected folder!", Toast.LENGTH_LONG).show()
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
        val totalInc = filteredTx.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExp = filteredTx.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totalLend = filteredTx.filter { it.type == "LEND" }.sumOf { it.amount }
        val totalBorrow = filteredTx.filter { it.type == "BORROW" }.sumOf { it.amount }
        val totalRepayPaid = filteredTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
        val totalRepayRecv = filteredTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
        val netBalance = totalInc - totalExp

        val summary = mapOf(
            "income" to totalInc,
            "expense" to totalExp,
            "balance" to netBalance,
            "lend" to totalLend,
            "borrow" to totalBorrow,
            "repayPaid" to totalRepayPaid,
            "repayRecv" to totalRepayRecv
        )

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val extension = when (format) {
            "PDF" -> ".pdf"
            "EXCEL" -> ".xls"
            else -> ".csv"
        }
        val categoryPrefix = selectedCategory.lowercase(Locale.ROOT)
        val fileName = "finance_${categoryPrefix}_$timestamp$extension"

        val shareDir = File(context.cacheDir, "shared_images")
        if (!shareDir.exists()) shareDir.mkdirs()
        val tempShareFile = File(shareDir, fileName)

        if (action == "SHARE") {
            isExporting = true
            scope.launch(Dispatchers.IO) {
                if (format == "PDF") {
                    try {
                        generatePdfFile(context, language, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, summary, tempShareFile)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isExporting = false
                            Toast.makeText(context, if (isBn) "পিডিএফ তৈরিতে ত্রুটি!" else "Failed to generate PDF!", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                } else {
                    val csvContent = generateCsvData(language, filteredTx, persons, savingsGoals, filteredSavingsTx, filteredBudgets, format == "EXCEL")
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

                        val mimeType = when (format) {
                            "PDF" -> "application/pdf"
                            "EXCEL" -> "application/vnd.ms-excel"
                            else -> "text/csv"
                        }

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

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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

                    // 6. Preview and Statistics Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBn) "রেকর্ড সংখ্যা:" else "Matching Records:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                    val recordsSize = filteredTx.size + 
                                                      (if (selectedCategory == "ALL_DATA" || selectedCategory == "ONLY_SAVINGS") filteredSavingsTx.size else 0) +
                                                      (if (selectedCategory == "ALL_DATA" || selectedCategory == "ONLY_BUDGET") filteredBudgets.size else 0)
                                    val recordsText = if (isBn) {
                                        "${replaceToBnDigits(recordsSize.toString())} টি"
                                    } else {
                                        "$recordsSize items"
                                    }
                                    Text(
                                        text = recordsText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF10B981)
                                    )
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
                            Text(text = if (isBn) "সংরক্ষণ করুন" else "Save / Download", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
    transactions: List<Transaction>,
    persons: List<Person>,
    savingsGoals: List<SavingsGoal>,
    savingsTransactions: List<SavingsTransaction>,
    filteredBudgets: List<MonthlyBudget>,
    isExcelMode: Boolean
): String {
    val delimiter = if (isExcelMode) "\t" else ","
    val isBn = language == AppLanguage.BN
    val sb = StringBuilder()

    sb.append("\uFEFF")

    sb.append(if (isBn) "আর্থিক প্রতিবেদন ও ডাটা" else "Financial Statement Report").append("\n")
    sb.append(if (isBn) "তৈরি হয়েছে: " else "Generated on: ").append(SimpleDateFormat("dd MMMM, yyyy hh:mm a", Locale.getDefault()).format(Date())).append("\n\n")

    val totalInc = transactions.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
    val totalExp = transactions.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }
    val netBalance = totalInc - totalExp

    sb.append(if (isBn) "আর্থিক সারসংক্ষেপ (Financial Summary)" else "Financial Summary").append("\n")
    sb.append(if (isBn) "ক্যাটাগরি" else "Metric").append(delimiter).append(if (isBn) "টাকার পরিমাণ" else "Amount").append("\n")
    sb.append(if (isBn) "মোট আয়" else "Total Income").append(delimiter).append(totalInc).append("\n")
    sb.append(if (isBn) "মোট ব্যয়" else "Total Expense").append(delimiter).append(totalExp).append("\n")
    sb.append(if (isBn) "নিট ব্যালেন্স" else "Net Balance").append(delimiter).append(netBalance).append("\n")
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

            sb.append(
                listOf(
                    (idx + 1).toString(),
                    dateStr,
                    catSafe,
                    typeStr,
                    personSafe,
                    noteSafe,
                    tx.amount.toString()
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

            sb.append(
                listOf(
                    (idx + 1).toString(),
                    dateStr,
                    goalSafe,
                    typeStr,
                    noteSafe,
                    stx.amount.toString()
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

    return sb.toString()
}

// Native Multi-Page A4 PDF Generator
private fun generatePdfFile(
    context: Context,
    language: AppLanguage,
    transactions: List<Transaction>,
    persons: List<Person>,
    savingsGoals: List<SavingsGoal>,
    savingsTransactions: List<SavingsTransaction>,
    filteredBudgets: List<MonthlyBudget>,
    summary: Map<String, Double>,
    file: File
) {
    val pdfDocument = PdfDocument()
    val isBn = language == AppLanguage.BN

    val pageWidth = 595
    val pageHeight = 842

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var currentPage = pdfDocument.startPage(pageInfo)
    var canvas = currentPage.canvas

    val primaryPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E3A8A")
        textSize = 18f
        isFakeBoldText = true
    }

    val subtitlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#6B7280")
        textSize = 10f
    }

    val statsHeaderPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E3A8A")
        textSize = 10f
        isFakeBoldText = true
    }

    val statsValPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 13f
        isFakeBoldText = true
    }

    val tableHeaderBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#111827")
    }

    val tableHeaderTxtPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 9f
        isFakeBoldText = true
    }

    val tableRowTxtPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1F2937")
        textSize = 8.5f
    }

    val tableRowTxtBoldPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 8.5f
        isFakeBoldText = true
    }

    val zebraBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F9FAFB")
    }

    val dividerPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#E5E7EB")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (isBn) monthNamesBn else monthNamesEn

    fun drawPageFooter(canvas: Canvas, pageNum: Int) {
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#9CA3AF")
            textSize = 8f
        }
        val appName = if (isBn) "ফিনান্স নোট রিপোর্ট" else "Finance Note Report"
        val pageStr = if (isBn) "পৃষ্ঠা $pageNum" else "Page $pageNum"
        canvas.drawText(appName, 40f, 810f, paint)
        val w = paint.measureText(pageStr)
        canvas.drawText(pageStr, 555f - w, 810f, paint)
    }

    // Styled App Logo & Name Header
    val logoPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#10B981") // Emerald Green
        isAntiAlias = true
    }
    canvas.drawCircle(52f, 48f, 12f, logoPaint)

    val symbolPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 14f
        isFakeBoldText = true
        isAntiAlias = true
    }
    // Draw "F" white emblem inside the green circle
    canvas.drawText("F", 47f, 53f, symbolPaint)

    val appNamePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#1E293B") // Dark Slate Blue
        textSize = 14f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText(if (isBn) "ফিনান্স নোট অ্যাপ" else "Finance Note App", 72f, 53f, appNamePaint)

    val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#0F172A") // Slate Dark
        textSize = 15f
        isFakeBoldText = true
        isAntiAlias = true
    }
    canvas.drawText(if (isBn) "লেনদেন ও বাজেট কন্ট্রোল প্রতিবেদন" else "Transaction & Budget Statement", 40f, 92f, titlePaint)
    canvas.drawText("${if (isBn) "তৈরি হয়েছে: " else "Generated on: "} ${SimpleDateFormat("dd MMMM, yyyy hh:mm a", Locale.getDefault()).format(Date())}", 40f, 108f, subtitlePaint)

    val cardBgPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#F1F5F9") // Soft modern gray card background
    }
    canvas.drawRect(40f, 122f, 555f, 182f, cardBgPaint)

    val inc = summary["income"] ?: 0.0
    val exp = summary["expense"] ?: 0.0
    val bal = summary["balance"] ?: 0.0

    canvas.drawText(if (isBn) "মোট আয় (Income)" else "Total Income", 60f, 142f, statsHeaderPaint)
    canvas.drawText(formatCurrency(inc, language), 60f, 162f, statsValPaint)

    canvas.drawText(if (isBn) "মোট ব্যয় (Expense)" else "Total Expense", 230f, 142f, statsHeaderPaint)
    canvas.drawText(formatCurrency(exp, language), 230f, 162f, statsValPaint)

    canvas.drawText(if (isBn) "নিট ব্যালেন্স (Balance)" else "Net Balance", 400f, 142f, statsHeaderPaint)
    val colorGreen = android.graphics.Color.parseColor("#059669")
    val colorRed = android.graphics.Color.parseColor("#DC2626")
    val balColorPaint = Paint().apply {
        color = if (bal >= 0) colorGreen else colorRed
        textSize = 13f
        isFakeBoldText = true
    }
    canvas.drawText(formatCurrency(bal, language), 400f, 162f, balColorPaint)

    fun drawTableHeader(canvas: Canvas, y: Float) {
        canvas.drawRect(40f, y, 555f, y + 22f, tableHeaderBgPaint)
        canvas.drawText(if (isBn) "তারিখ" else "Date", 45f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "ক্যাটাগরি" else "Category", 115f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "ধরন" else "Type", 205f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "ব্যক্তি" else "Person", 275f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "বিবরণ" else "Note", 355f, y + 14f, tableHeaderTxtPaint)
        canvas.drawText(if (isBn) "টাকা" else "Amount", 495f, y + 14f, tableHeaderTxtPaint)
    }

    var currentY = 202f

    // Draw Budget donut charts if ONLY_BUDGET is selected
    if (transactions.isEmpty() && savingsTransactions.isEmpty() && filteredBudgets.isNotEmpty()) {
        val firstBudget = filteredBudgets.first()
        val ymText = "${months[firstBudget.month]} ${firstBudget.year}"

        val budgetTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("${if (isBn) "বাজেট সারসংক্ষেপ - " else "Budget Summary - "}$ymText", 40f, currentY + 12f, budgetTitlePaint)
        currentY += 24f

        val actualInc = transactions.filter {
            val (y, m) = getYearAndMonth(it.timestamp)
            y == firstBudget.year && m == firstBudget.month && (it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT"))
        }.sumOf { it.amount }

        val actualExp = transactions.filter {
            val (y, m) = getYearAndMonth(it.timestamp)
            y == firstBudget.year && m == firstBudget.month && (it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT"))
        }.sumOf { it.amount }

        val actualSav = savingsTransactions.filter {
            val (y, m) = getYearAndMonth(it.timestamp)
            y == firstBudget.year && m == firstBudget.month
        }.sumOf { if (it.isDeposit) it.amount else -it.amount }

        val donutRadius = 26f
        val startY = currentY + 35f

        val arcPaintBg = Paint().apply {
            color = android.graphics.Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 7f
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#475569")
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val valPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 8.5f
            isAntiAlias = true
        }

        // 1. Income Donut
        val incX = 100f
        val rectInc = android.graphics.RectF(incX - donutRadius, startY - donutRadius, incX + donutRadius, startY + donutRadius)
        canvas.drawArc(rectInc, 0f, 360f, false, arcPaintBg)
        val incPct = if (firstBudget.income > 0) (actualInc / firstBudget.income) else 0.0
        val sweepInc = (incPct * 360f).coerceIn(0.0, 360.0).toFloat()
        val arcPaintInc = Paint().apply {
            color = android.graphics.Color.parseColor("#10B981") // Green
            style = Paint.Style.STROKE
            strokeWidth = 7f
            isAntiAlias = true
        }
        canvas.drawArc(rectInc, -90f, sweepInc, false, arcPaintInc)

        val incPctText = "${(incPct * 100).toInt()}%"
        val pctPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0F172A")
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val incPctW = pctPaint.measureText(incPctText)
        canvas.drawText(incPctText, incX - (incPctW / 2f), startY + 3f, pctPaint)

        canvas.drawText(if (isBn) "আয় (Income)" else "Income", incX + 45f, startY - 12f, labelPaint)
        canvas.drawText("${if (isBn) "টার্গেট: " else "Target: "}${formatCurrencyNoSymbol(firstBudget.income, language)}", incX + 45f, startY + 2f, valPaint)
        canvas.drawText("${if (isBn) "অর্জিত: " else "Actual: "}${formatCurrencyNoSymbol(actualInc, language)}", incX + 45f, startY + 14f, valPaint)

        // 2. Expense Donut
        val expX = 320f
        val rectExp = android.graphics.RectF(expX - donutRadius, startY - donutRadius, expX + donutRadius, startY + donutRadius)
        canvas.drawArc(rectExp, 0f, 360f, false, arcPaintBg)
        val expPct = if (firstBudget.expense > 0) (actualExp / firstBudget.expense) else 0.0
        val sweepExp = (expPct * 360f).coerceIn(0.0, 360.0).toFloat()
        val arcPaintExp = Paint().apply {
            color = android.graphics.Color.parseColor("#F97316") // Orange
            style = Paint.Style.STROKE
            strokeWidth = 7f
            isAntiAlias = true
        }
        canvas.drawArc(rectExp, -90f, sweepExp, false, arcPaintExp)

        val expPctText = "${(expPct * 100).toInt()}%"
        val expPctW = pctPaint.measureText(expPctText)
        canvas.drawText(expPctText, expX - (expPctW / 2f), startY + 3f, pctPaint)

        canvas.drawText(if (isBn) "ব্যয় (Expense)" else "Expense", expX + 45f, startY - 12f, labelPaint)
        canvas.drawText("${if (isBn) "টার্গেট: " else "Target: "}${formatCurrencyNoSymbol(firstBudget.expense, language)}", expX + 45f, startY + 2f, valPaint)
        canvas.drawText("${if (isBn) "ব্যয়িত: " else "Spent: "}${formatCurrencyNoSymbol(actualExp, language)}", expX + 45f, startY + 14f, valPaint)

        currentY = startY + donutRadius + 35f
    }

    if (transactions.isNotEmpty()) {
        drawTableHeader(canvas, currentY)
        currentY += 22f

        transactions.forEachIndexed { index, tx ->
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

            if (index % 2 == 1) {
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, zebraBgPaint)
            }
            canvas.drawLine(40f, currentY + 20f, 555f, currentY + 20f, dividerPaint)

            val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(tx.timestamp))
            val personName = persons.find { it.id == tx.personId }?.name ?: ""
            val typeStr = getTransactionTypeName(tx.type, language)

            val colorType = when (tx.type) {
                "INCOME", "REPAY_RECEIVED" -> android.graphics.Color.parseColor("#059669")
                "EXPENSE", "REPAY_PAID" -> android.graphics.Color.parseColor("#DC2626")
                "LEND" -> android.graphics.Color.parseColor("#2563EB")
                "BORROW" -> android.graphics.Color.parseColor("#D97706")
                else -> android.graphics.Color.BLACK
            }
            val typeColorPaint = Paint().apply {
                color = colorType
                textSize = 8.5f
                isFakeBoldText = true
            }

            val catTrunc = if (tx.category.length > 15) tx.category.substring(0, 13) + ".." else tx.category
            val personTrunc = if (personName.length > 12) personName.substring(0, 10) + ".." else personName
            val noteTrunc = if (tx.note.length > 22) tx.note.substring(0, 19) + ".." else tx.note

            canvas.drawText(dateStr, 45f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(catTrunc, 115f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(typeStr, 205f, currentY + 13f, typeColorPaint)
            canvas.drawText(personTrunc, 275f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(noteTrunc, 355f, currentY + 13f, tableRowTxtPaint)

            val amtText = formatCurrencyNoSymbol(tx.amount, language)
            val amtW = tableRowTxtBoldPaint.measureText(amtText)
            canvas.drawText(amtText, 550f - amtW, currentY + 13f, tableRowTxtBoldPaint)

            currentY += 20f
        }
    }

    if (savingsTransactions.isNotEmpty()) {
        currentY += 15f

        if (currentY + 45f > 780f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)

            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas

            currentY = 45f
        }

        val secTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E3A8A")
            textSize = 11f
            isFakeBoldText = true
        }
        canvas.drawText(if (isBn) "সঞ্চয় রেকর্ডসমূহ (Savings Logs)" else "Savings Logs", 40f, currentY + 12f, secTitlePaint)
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
            val typeStr = if (stx.isDeposit) (if (isBn) "সঞ্চয় জমা" else "Deposit") else (if (isBn) "সঞ্চয় উত্তোলন" else "Withdrawal")
            val typeColor = if (stx.isDeposit) android.graphics.Color.parseColor("#059669") else android.graphics.Color.parseColor("#DC2626")

            val typeColorPaint = Paint().apply {
                color = typeColor
                textSize = 8.5f
                isFakeBoldText = true
            }

            val goalTrunc = if (goalTitle.length > 20) goalTitle.substring(0, 18) + ".." else goalTitle
            val noteTrunc = if (stx.note.length > 25) stx.note.substring(0, 22) + ".." else stx.note

            canvas.drawText(dateStr, 45f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(goalTrunc, 130f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(typeStr, 260f, currentY + 13f, typeColorPaint)
            canvas.drawText(noteTrunc, 350f, currentY + 13f, tableRowTxtPaint)

            val amtText = formatCurrencyNoSymbol(stx.amount, language)
            val amtW = tableRowTxtBoldPaint.measureText(amtText)
            canvas.drawText(amtText, 550f - amtW, currentY + 13f, tableRowTxtBoldPaint)

            currentY += 20f
        }
    }

    // Draw Budgets Table
    if (filteredBudgets.isNotEmpty()) {
        currentY += 15f
        if (currentY + 45f > 780f) {
            drawPageFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas
            currentY = 45f
        }

        val secTitlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1E3A8A")
            textSize = 11f
            isFakeBoldText = true
        }
        canvas.drawText(if (isBn) "বাজেট বিবরণী (Budgets Log)" else "Budgets Log", 40f, currentY + 12f, secTitlePaint)
        currentY += 18f

        fun drawBudgetTableHeader(canvas: Canvas, y: Float) {
            canvas.drawRect(40f, y, 555f, y + 22f, tableHeaderBgPaint)
            canvas.drawText(if (isBn) "মাস/বছর" else "Month/Year", 45f, y + 14f, tableHeaderTxtPaint)
            canvas.drawText(if (isBn) "আয় (টার্গেট/অর্জিত)" else "Income (Target/Actual)", 140f, y + 14f, tableHeaderTxtPaint)
            canvas.drawText(if (isBn) "ব্যয় (টার্গেট/ব্যয়িত)" else "Expense (Target/Spent)", 290f, y + 14f, tableHeaderTxtPaint)
            canvas.drawText(if (isBn) "সঞ্চয় (টার্গেট/অর্জিত)" else "Savings (Target/Actual)", 430f, y + 14f, tableHeaderTxtPaint)
        }

        drawBudgetTableHeader(canvas, currentY)
        currentY += 22f

        filteredBudgets.forEachIndexed { index, budget ->
            if (currentY + 22f > 780f) {
                drawPageFooter(canvas, pageNumber)
                pdfDocument.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDocument.startPage(pageInfo)
                canvas = currentPage.canvas
                currentY = 45f
                drawBudgetTableHeader(canvas, currentY)
                currentY += 22f
            }

            if (index % 2 == 1) {
                canvas.drawRect(40f, currentY, 555f, currentY + 20f, zebraBgPaint)
            }
            canvas.drawLine(40f, currentY + 20f, 555f, currentY + 20f, dividerPaint)

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

            val incStr = "${formatCurrencyNoSymbol(budget.income, language)} / ${formatCurrencyNoSymbol(bActualInc, language)}"
            val expStr = "${formatCurrencyNoSymbol(budget.expense, language)} / ${formatCurrencyNoSymbol(bActualExp, language)}"
            val savStr = "${formatCurrencyNoSymbol(budget.savings, language)} / ${formatCurrencyNoSymbol(bActualSav, language)}"

            canvas.drawText(ymVal, 45f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(incStr, 140f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(expStr, 290f, currentY + 13f, tableRowTxtPaint)
            canvas.drawText(savStr, 430f, currentY + 13f, tableRowTxtPaint)

            currentY += 20f
        }
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
