package com.example.ui.screens

import com.example.R
import android.content.Context
import android.net.Uri
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import androidx.compose.ui.window.Dialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



fun formatNumber(number: Int, lang: AppLanguage): String {
    val str = number.toString()
    if (lang == AppLanguage.EN) return str
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

fun getCustomTimeFilterLabel(timeFilter: String, lang: AppLanguage): String {
    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    
    if (timeFilter.startsWith("CUSTOM_MONTH:")) {
        val parts = timeFilter.substringAfter("CUSTOM_MONTH:").split("-")
        if (parts.size == 2) {
            val year = parts[0].toIntOrNull() ?: 2026
            val month = parts[1].toIntOrNull() ?: 1
            val monthStr = if (lang == AppLanguage.BN) monthsBn[month - 1] else monthsEn[month - 1]
            return "$monthStr, ${formatNumber(year, lang)}"
        }
    } else if (timeFilter.startsWith("CUSTOM_DATE:")) {
        val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: 2026
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1
            val monthStr = if (lang == AppLanguage.BN) monthsBn[month - 1] else monthsEn[month - 1]
            return "${formatNumber(day, lang)} $monthStr, ${formatNumber(year, lang)}"
        }
    }
    return timeFilter
}

fun formatNumberString(str: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return str
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

fun getFormattedDateLabel(timeFilter: String, lang: AppLanguage): String {
    if (timeFilter.startsWith("CUSTOM_DATE:")) {
        val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: 2026
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1
            val shortYear = year % 100
            val dayStr = if (day < 10) "0$day" else "$day"
            val monthStr = if (month < 10) "0$month" else "$month"
            val yearStr = if (shortYear < 10) "0$shortYear" else "$shortYear"
            val rawDate = "$dayStr/$monthStr/$yearStr"
            return if (lang == AppLanguage.BN) "${formatNumberString(rawDate, lang)} তারিখের" else "$rawDate"
        }
    }
    return if (lang == AppLanguage.BN) "তারিখ অনুযায়ী" else "By Date"
}

@Composable
fun MonthYearPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    
    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (language == AppLanguage.BN) monthsBn else monthsEn

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == AppLanguage.BN) "মাস নির্বাচন করুন" else "Select Month & Year") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(if (language == AppLanguage.BN) "মাস" else "Month", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(12) { index ->
                            val m = index + 1
                            val isSelected = selectedMonth == m
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedMonth = m }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(months[index], fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (language == AppLanguage.BN) "বছর" else "Year", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val years = (currentYear - 3..currentYear + 2).toList()
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(years.size) { index ->
                            val y = years[index]
                            val isSelected = selectedYear == y
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedYear = y }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(y, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                Text(Translation.get("confirm", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translation.get("cancel", language))
            }
        }
    )
}

@Composable
fun SpecificDatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int, day: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }
    
    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (language == AppLanguage.BN) monthsBn else monthsEn

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == AppLanguage.BN) "তারিখ নির্বাচন করুন" else "Select Date") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (language == AppLanguage.BN) "দিন" else "Day", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(31) { index ->
                            val d = index + 1
                            val isSelected = selectedDay == d
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedDay = d }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(d, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1.2f)) {
                    Text(if (language == AppLanguage.BN) "মাস" else "Month", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(12) { index ->
                            val m = index + 1
                            val isSelected = selectedMonth == m
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedMonth = m }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(months[index], fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(if (language == AppLanguage.BN) "বছর" else "Year", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val years = (currentYear - 3..currentYear + 2).toList()
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(years.size) { index ->
                            val y = years[index]
                            val isSelected = selectedYear == y
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedYear = y }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(y, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth, selectedDay) }) {
                Text(Translation.get("confirm", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translation.get("cancel", language))
            }
        }
    )
}

@Composable
fun DeleteVerificationDialog(
    language: AppLanguage,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val verificationCode = remember { (1000..9999).random().toString() }
    var userInput by remember { mutableStateOf("") }
    val isMatched = userInput.trim() == verificationCode
    
    val title = if (language == AppLanguage.BN) "মুছে ফেলার নিশ্চিতকরণ" else "Confirm Deletion"
    val msg = if (language == AppLanguage.BN) {
        "এটি মুছে ফেলতে নিচে দেখানো কোডটি টাইপ করুন:"
    } else {
        "To delete, please type the verification code below:"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = FintechRed)
                Text(title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(msg, style = MaterialTheme.typography.bodyMedium)
                
                // Big beautiful matched/unmatched verification code display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatNumber(verificationCode.toInt(), language),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error,
                        letterSpacing = 4.sp
                    )
                }
                
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = {
                        Text(
                            if (language == AppLanguage.BN) "কোডটি এখানে লিখুন" else "Enter code here",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("delete_verification_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isMatched,
                colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("delete_verification_confirm_btn")
            ) {
                Text(Translation.get("delete", language), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(Translation.get("cancel", language))
            }
        }
    )
}

// Helper to format currency
fun formatCurrency(amount: Double, lang: AppLanguage): String {
    val formatter = DecimalFormat("#,##,###.##")
    val formatted = formatter.format(amount)
    return if (lang == AppLanguage.BN) {
        val bnDigits = formatted
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
        "৳ $bnDigits"
    } else {
        "৳ $formatted"
    }
}

// Helper to format date
fun formatDate(timestamp: Long, lang: AppLanguage): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.ENGLISH)
    val enDate = format.format(date)
    if (lang == AppLanguage.EN) return enDate

    return enDate
        .replace("PM", "অপরাহ্ন")
        .replace("AM", "পূর্বাহ্ন")
        .replace("Jan", "জানু")
        .replace("Feb", "ফেব্রু")
        .replace("Mar", "মার্চ")
        .replace("Apr", "এপ্রিল")
        .replace("May", "মে")
        .replace("Jun", "জুন")
        .replace("Jul", "জুলাই")
        .replace("Aug", "আগস্ট")
        .replace("Sep", "সেপ্টে")
        .replace("Oct", "অক্টো")
        .replace("Nov", "নভে")
        .replace("Dec", "ডিসে")
}

fun formatDateToDay(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
    return format.format(date)
}

fun formatDateHeader(dateStr: String, lang: AppLanguage): String {
    try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
        val date = sdf.parse(dateStr)
        val format = java.text.SimpleDateFormat("dd MMMM, yyyy", java.util.Locale.ENGLISH)
        val enDate = format.format(date)
        if (lang == AppLanguage.EN) return enDate
        
        return enDate
            .replace("January", "জানুয়ারি")
            .replace("February", "ফেব্রুয়ারি")
            .replace("March", "মার্চ")
            .replace("April", "এপ্রিল")
            .replace("May", "মে")
            .replace("June", "জুন")
            .replace("July", "জুলাই")
            .replace("August", "আগস্ট")
            .replace("September", "সেপ্টেম্বর")
            .replace("October", "অক্টোবর")
            .replace("November", "নভেম্বর")
            .replace("December", "ডিসেম্বর")
    } catch (e: Exception) {
        return dateStr
    }
}

@Composable
fun FinanceNoteApp(viewModel: FinanceViewModel) {
    val language by viewModel.language.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val profileName by viewModel.profileName.collectAsState()
    val profileEmail by viewModel.profileEmail.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()

    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
        kotlinx.coroutines.delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen(isDark = isDarkTheme)
        return
    }

    val persons by viewModel.persons.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val personDebts by viewModel.personDebts.collectAsState()

    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalOwedToMe by viewModel.totalOwedToMe.collectAsState()
    val totalIOwe by viewModel.totalIOwe.collectAsState()

    var timeFilter by remember { mutableStateOf("MONTH") } // Default to "MONTH" as requested by user
    var showMonthPickerState by remember { mutableStateOf(false) }
    var showDatePickerState by remember { mutableStateOf(false) }

    val handleTimeFilterChange: (String) -> Unit = { filterVal ->
        when (filterVal) {
            "TRIGGER_MONTH_PICKER" -> showMonthPickerState = true
            "TRIGGER_DATE_PICKER" -> showDatePickerState = true
            else -> timeFilter = filterVal
        }
    }

    // Filtered Transactions and metrics for the current time period
    val filteredTransactionsForMetrics = remember(transactions, timeFilter) {
        filterTransactionsByTime(transactions, timeFilter)
    }

    val currentTotalIncome = remember(filteredTransactionsForMetrics) {
        filteredTransactionsForMetrics.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val currentTotalExpense = remember(filteredTransactionsForMetrics) {
        filteredTransactionsForMetrics.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }

    // Filter personDebts by time locally for UI if needed
    val filteredPersonDebts = remember(personDebts, transactions, timeFilter, persons) {
        if (timeFilter == "ALL") {
            personDebts
        } else {
            val filteredTxs = filterTransactionsByTime(transactions, timeFilter)
            persons.map { person ->
                val personTx = filteredTxs.filter { it.personId == person.id }
                val lent = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
                val borrowed = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                val repaidPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                val repaidReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                
                val net = (lent + repaidPaid) - (borrowed + repaidReceived)
                PersonDebt(
                    person = person,
                    netBalance = net,
                    totalLent = lent,
                    totalBorrowed = borrowed,
                    totalRepaidPaid = repaidPaid,
                    totalRepaidReceived = repaidReceived
                )
            }
        }
    }

    val currentTotalOwedToMe = remember(filteredPersonDebts) {
        filteredPersonDebts.filter { it.netBalance > 0 }.sumOf { it.netBalance }
    }
    val currentTotalIOwe = remember(filteredPersonDebts) {
        filteredPersonDebts.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
    }

    var activeTab by remember { mutableStateOf("dashboard") }
    var transactionFilter by remember { mutableStateOf("ALL") }
    var debtFilter by remember { mutableStateOf("ALL") }

    // Dialog & overlay states
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showAddSavingsGoalDialog by remember { mutableStateOf(false) }
    var showSavingsContributionDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var isWithdrawMode by remember { mutableStateOf(false) }
    var selectedPersonDetail by remember { mutableStateOf<PersonDebt?>(null) }
    var selectedSavingsGoalDetail by remember { mutableStateOf<SavingsGoal?>(null) }
    var goalToEdit by remember { mutableStateOf<SavingsGoal?>(null) }

    // Back handling for overlays and settings
    androidx.activity.compose.BackHandler(
        enabled = selectedPersonDetail != null || selectedSavingsGoalDetail != null || activeTab == "settings"
    ) {
        when {
            selectedPersonDetail != null -> selectedPersonDetail = null
            selectedSavingsGoalDetail != null -> selectedSavingsGoalDetail = null
            activeTab == "settings" -> activeTab = "dashboard"
        }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme
    ) {
        Scaffold(
            containerColor = if (isDarkTheme) Color.Black else Color.White,
            topBar = {
                Surface(
                    color = FintechBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activeTab == "settings" || activeTab == "charts") {
                                IconButton(
                                    onClick = { activeTab = "dashboard" },
                                    modifier = Modifier.padding(end = 8.dp).size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = Translation.get("back", language),
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            val screenTitle = when {
                                selectedSavingsGoalDetail != null -> if (language == AppLanguage.BN) "সঞ্চয় কার্ড" else "Savings Card"
                                selectedPersonDetail != null -> Translation.get("details", language)
                                activeTab == "dashboard" -> "Finance Note"
                                activeTab == "transactions" -> Translation.get("transactions", language)
                                activeTab == "debts" -> Translation.get("debts", language)
                                activeTab == "savings" -> if (language == AppLanguage.BN) "সঞ্চয় কার্ড" else "Savings Card"
                                activeTab == "settings" -> Translation.get("settings", language)
                                activeTab == "charts" -> if (language == AppLanguage.BN) "চার্টস" else "Charts"
                                else -> Translation.get("dashboard", language)
                            }
                            
                            Text(
                                text = screenTitle,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleLanguage(context) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Language,
                                    contentDescription = "Toggle Language",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.toggleTheme(context) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.NightsStay,
                                    contentDescription = "Theme Toggle",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (activeTab != "settings") {
                                IconButton(
                                    onClick = {
                                        activeTab = "settings"
                                        selectedPersonDetail = null
                                        selectedSavingsGoalDetail = null
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FintechBlue)
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Helper for bottom bar items
                        @Composable
                        fun BottomNavItem(
                            tab: String,
                            icon: ImageVector,
                            testTag: String,
                            iconSize: androidx.compose.ui.unit.Dp = 24.dp
                        ) {
                            val isSelected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { 
                                        activeTab = tab
                                        selectedPersonDetail = null
                                        selectedSavingsGoalDetail = null
                                    }
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                                    .testTag(testTag),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                 ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.size(iconSize)
                                    )
                                }
                            }
                        }

                        // Item 1: Dashboard
                        BottomNavItem(
                            tab = "dashboard",
                            icon = Icons.Rounded.SpaceDashboard,
                            testTag = "nav_dashboard"
                        )

                        // Item 2: Transactions
                        BottomNavItem(
                            tab = "transactions",
                            icon = Icons.Rounded.ListAlt,
                            testTag = "nav_transactions"
                        )

                        // Center: Central FAB!
                        Box(
                            modifier = Modifier
                                .offset(y = (-34).dp)
                                .size(96.dp)
                                .background(if (isDarkTheme) Color.Black else Color.White, CircleShape)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showAddTransactionDialog = true }
                                .testTag("fab_add_transaction_outer"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF6F7BF7), Color(0xFF38BDF8))
                                        )
                                    )
                                    .testTag("fab_add_transaction"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add Transaction",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Item 3: Debts
                        BottomNavItem(
                            tab = "debts",
                            icon = Icons.Rounded.Group,
                            testTag = "nav_debts"
                        )

                        // Item 4: Savings
                        BottomNavItem(
                            tab = "savings",
                            icon = Icons.Rounded.AccountBalance,
                            testTag = "nav_savings",
                            iconSize = 24.dp
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkTheme) Color(0xFF0B0D14) else Color(0xFFF8FAFC))
                    .padding(innerPadding)
            ) {
                // Background Glows for Premium fintech look (Only on Dark mode for sleek visuals)
                if (isDarkTheme) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF3F51B5).copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                            .align(Alignment.TopStart)
                    )
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF009688).copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                            .align(Alignment.BottomEnd)
                    )
                }

                // Main screen switches
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(0.dp))

                    // Active Tab Render
                    Box(modifier = Modifier.weight(1f)) {
                        val mainOrSettingsOrCharts = when (activeTab) {
                            "settings" -> "settings"
                            "charts" -> "charts"
                            else -> "main"
                        }
                        AnimatedContent(
                            targetState = mainOrSettingsOrCharts,
                            transitionSpec = {
                                (slideInHorizontally(initialOffsetX = { it }) + fadeIn())
                                    .togetherWith(slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                            },
                            label = "MainContentTransition"
                        ) { tabState ->
                            if (tabState == "settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    language = language,
                                    isDark = isDarkTheme,
                                    onBack = { activeTab = "dashboard" }
                                )
                            } else if (tabState == "charts") {
                                ChartsScreen(
                                    language = language,
                                    isDark = isDarkTheme,
                                    transactions = transactions,
                                    persons = persons
                                )
                            } else {
                                val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
                                val coroutineScope = rememberCoroutineScope()

                                // Sync activeTab click to Pager
                                LaunchedEffect(activeTab) {
                                    val page = when (activeTab) {
                                        "dashboard" -> 0
                                        "transactions" -> 1
                                        "debts" -> 2
                                        "savings" -> 3
                                        else -> null
                                    }
                                    if (page != null && pagerState.currentPage != page) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(page)
                                        }
                                    }
                                }

                                // Sync Pager swipe to activeTab
                                LaunchedEffect(pagerState.currentPage) {
                                    val tab = when (pagerState.currentPage) {
                                        0 -> "dashboard"
                                        1 -> "transactions"
                                        2 -> "debts"
                                        3 -> "savings"
                                        else -> "dashboard"
                                    }
                                    if (activeTab != tab) {
                                        activeTab = tab
                                    }
                                }

                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    when (page) {
                                        0 -> DashboardScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            profileName = profileName,
                                            profileEmail = profileEmail,
                                            profilePhotoUri = profilePhotoUri,
                                            balance = totalBalance,
                                            income = currentTotalIncome,
                                            expense = currentTotalExpense,
                                            owedToMe = currentTotalOwedToMe,
                                            iOwe = currentTotalIOwe,
                                            recentTransactions = transactions.take(10),
                                            persons = persons,
                                            onAddTransactionClick = { showAddTransactionDialog = true },
                                            onAddPersonClick = { showAddPersonDialog = true },
                                            onAddSavingClick = { showAddSavingsGoalDialog = true },
                                            onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                            onNavigate = { tab, filter ->
                                                activeTab = tab
                                                if (tab == "transactions") transactionFilter = filter
                                                if (tab == "debts") debtFilter = filter
                                            },
                                            timeFilter = timeFilter,
                                            onTimeFilterChange = handleTimeFilterChange
                                        )
                                        1 -> TransactionsScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            transactions = transactions,
                                            persons = persons,
                                            onAddTransactionClick = { showAddTransactionDialog = true },
                                            onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                            filter = transactionFilter,
                                            onFilterChange = { transactionFilter = it },
                                            timeFilter = timeFilter,
                                            onTimeFilterChange = handleTimeFilterChange
                                        )
                                        2 -> DebtsScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            personDebts = filteredPersonDebts,
                                            onAddPersonClick = { showAddPersonDialog = true },
                                            onPersonClick = { selectedPersonDetail = it },
                                            onDeletePerson = { viewModel.deletePerson(it) },
                                            filter = debtFilter,
                                            onFilterChange = { debtFilter = it },
                                            timeFilter = timeFilter,
                                            onTimeFilterChange = handleTimeFilterChange
                                        )
                                        3 -> SavingsScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            profileName = profileName,
                                            savingsGoals = savingsGoals,
                                            onAddSavingsGoalClick = { showAddSavingsGoalDialog = true },
                                            onGoalClick = { selectedSavingsGoalDetail = it },
                                            onContributeClick = { goal, isWithdraw -> 
                                                showSavingsContributionDialog = goal
                                                isWithdrawMode = isWithdraw
                                            },
                                            onEditGoal = { goalToEdit = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dynamic Overlays & Dialogs
                if (showAddTransactionDialog) {
                    AddTransactionDialog(
                        language = language,
                        persons = persons,
                        isDark = isDarkTheme,
                        onDismiss = { showAddTransactionDialog = false },
                        onConfirm = { amount, type, category, note, personId, timestamp ->
                            viewModel.addTransaction(amount, type, category, note, personId, timestamp)
                            showAddTransactionDialog = false
                        }
                    )
                }

                if (showAddPersonDialog) {
                    AddPersonDialog(
                        language = language,
                        isDark = isDarkTheme,
                        onDismiss = { showAddPersonDialog = false },
                        onConfirm = { name, phone, address, photoUri ->
                            viewModel.addPerson(name, phone, address, photoUri)
                            showAddPersonDialog = false
                        }
                    )
                }

                if (showAddSavingsGoalDialog || goalToEdit != null) {
                    AddSavingsGoalDialog(
                        language = language,
                        isDark = isDarkTheme,
                        initialGoal = goalToEdit,
                        onDismiss = { 
                            showAddSavingsGoalDialog = false
                            goalToEdit = null
                        },
                        onConfirm = { title, target, sector, colorIdx, cardholder ->
                            if (goalToEdit != null) {
                                viewModel.updateSavingsGoal(goalToEdit!!.copy(
                                    title = title,
                                    targetAmount = target,
                                    category = sector,
                                    colorIndex = colorIdx,
                                    cardholderName = cardholder
                                ))
                            } else {
                                viewModel.addSavingsGoal(title, target, sector, colorIdx, cardholder)
                            }
                            showAddSavingsGoalDialog = false
                            goalToEdit = null
                        }
                    )
                }

                if (showSavingsContributionDialog != null) {
                    val goal = showSavingsContributionDialog!!
                    SavingsContributionDialog(
                        language = language,
                        savingsGoal = goal,
                        isDark = isDarkTheme,
                        initialIsWithdraw = isWithdrawMode,
                        onDismiss = { showSavingsContributionDialog = null },
                        onConfirm = { amount, isWithdraw, note ->
                            val finalAmount = if (isWithdraw) -amount else amount
                            viewModel.addSavingsContribution(goal.id, finalAmount, note)
                            showSavingsContributionDialog = null
                        }
                    )
                }

                if (showMonthPickerState) {
                    val calendar = java.util.Calendar.getInstance()
                    MonthYearPickerDialog(
                        initialYear = calendar.get(java.util.Calendar.YEAR),
                        initialMonth = calendar.get(java.util.Calendar.MONTH) + 1,
                        language = language,
                        onDismiss = { showMonthPickerState = false },
                        onConfirm = { year, month ->
                            timeFilter = "CUSTOM_MONTH:$year-$month"
                            showMonthPickerState = false
                        }
                    )
                }

                if (showDatePickerState) {
                    val calendar = java.util.Calendar.getInstance()
                    SpecificDatePickerDialog(
                        initialYear = calendar.get(java.util.Calendar.YEAR),
                        initialMonth = calendar.get(java.util.Calendar.MONTH) + 1,
                        initialDay = calendar.get(java.util.Calendar.DAY_OF_MONTH),
                        language = language,
                        onDismiss = { showDatePickerState = false },
                        onConfirm = { year, month, day ->
                            timeFilter = "CUSTOM_DATE:$year-$month-$day"
                            showDatePickerState = false
                        }
                    )
                }

                // Detailed view for savings transactions
                selectedSavingsGoalDetail?.let { goalSnapshot ->
                    val goal = savingsGoals.find { it.id == goalSnapshot.id } ?: goalSnapshot
                    SavingsGoalDetailOverlay(
                        language = language,
                        isDark = isDarkTheme,
                        profileName = profileName,
                        goal = goal,
                        transactionsFlow = viewModel.getSavingsTransactions(goal.id),
                        onDismiss = { selectedSavingsGoalDetail = null },
                        onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                        onEditGoal = { goalToEdit = it },
                        onContributeClick = { goalObj, isWithdraw ->
                            showSavingsContributionDialog = goalObj
                            isWithdrawMode = isWithdraw
                        }
                    )
                }

                // Detailed view for transactions with a specific person
                selectedPersonDetail?.let { debtInfoSnapshot ->
                    val debtInfo = filteredPersonDebts.find { it.person.id == debtInfoSnapshot.person.id } ?: debtInfoSnapshot
                    PersonDetailOverlay(
                        language = language,
                        isDark = isDarkTheme,
                        personDebt = debtInfo,
                        transactionsFlow = viewModel.getTransactionsByPerson(debtInfo.person.id),
                        onDismiss = { selectedPersonDetail = null },
                        onLendClick = { amt, note ->
                            viewModel.addTransaction(amt, "LEND", "Lending", note.ifBlank { "Lent money to ${debtInfo.person.name}" }, debtInfo.person.id)
                        },
                        onBorrowClick = { amt, note ->
                            viewModel.addTransaction(amt, "BORROW", "Borrowing", note.ifBlank { "Borrowed money from ${debtInfo.person.name}" }, debtInfo.person.id)
                        },
                        onRepayPaidClick = { amt, note ->
                            viewModel.addTransaction(amt, "REPAY_PAID", "Repay Paid", note.ifBlank { "Paid back borrowed loan" }, debtInfo.person.id)
                        },
                        onRepayReceivedClick = { amt, note ->
                            viewModel.addTransaction(amt, "REPAY_RECEIVED", "Repay Received", note.ifBlank { "Received back lent loan" }, debtInfo.person.id)
                        },
                        onDeleteTx = { viewModel.deleteTransaction(it) }
                    )
                }
            }
        }
    }
}

// ---------------- DASHBOARD TAB ----------------
@Composable
fun DashboardScreen(
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    profileEmail: String,
    profilePhotoUri: String?,
    balance: Double,
    income: Double,
    expense: Double,
    owedToMe: Double,
    iOwe: Double,
    recentTransactions: List<Transaction>,
    persons: List<Person>,
    onAddTransactionClick: () -> Unit,
    onAddPersonClick: () -> Unit,
    onAddSavingClick: () -> Unit,
    onDeleteTransaction: (Int) -> Unit,
    onNavigate: (String, String) -> Unit,
    timeFilter: String = "ALL",
    onTimeFilterChange: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card (Fintech Gradient Card styled beautifully with the same indigo-fuchsia gradient)
        item {
            FintechGradientCard(
                gradientColors = GradientsList[0],
                cornerRadius = 24.dp,
                modifier = Modifier.testTag("dashboard_profile_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Avatar Circle with initials or photo
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUri != null) {
                            AsyncImage(
                                model = profilePhotoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = if (profileName.isNotBlank()) {
                                profileName.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .joinToString("")
                                    .uppercase()
                            } else ""

                            if (initials.isNotEmpty()) {
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    // Profile text details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.BN) "স্বাগতম," else "Welcome,",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (profileName.isNotBlank()) profileName else (if (language == AppLanguage.BN) "ব্যবহারকারী" else "User"),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (profileEmail.isNotBlank()) {
                            Text(
                                text = profileEmail,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { onNavigate("settings", "") },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Balance Card (Fintech Gradient Card with sleek styling and beautifully integrated debts/loans cards)
        item {
            FintechGradientCard(
                gradientColors = GradientsList[0], // Sleek Indigo-Violet-Fuchsia Gradient
                cornerRadius = 32.dp,
                modifier = Modifier.testTag("dashboard_balance_card")
            ) {
                // Total Balance Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Translation.get("total_balance", language),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = formatCurrency(balance, language),
                            color = Color.White,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // Bottom-right decorative card chip icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { onNavigate("charts", "") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PieChart,
                            contentDescription = "Charts",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Row 1: Income and Expense Sub-Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Income Sub-Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .clickable { onNavigate("transactions", "INCOME") }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "আয়" else "Income",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(income, language),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Expense Sub-Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .clickable { onNavigate("transactions", "EXPENSE") }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "ব্যয়" else "Expense",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(expense, language),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Time Filter Row
        item {
            TimeFilterRow(
                timeFilter = timeFilter,
                language = language,
                onTimeFilterChange = onTimeFilterChange,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        // I Owe & Owed to Me Cards (দেনা ও পাওনা)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // I Owe Card (দেনা)
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .clickable { onNavigate("debts", "DENA") }
                        .testTag("dashboard_i_owe_card")
                ) {
                    Text(
                        text = Translation.get("i_owe", language),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(iOwe, language),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Owed to Me Card (পাওনা)
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(95.dp)
                        .clickable { onNavigate("debts", "PAWN") }
                        .testTag("dashboard_owed_to_me_card")
                ) {
                    Text(
                        text = Translation.get("owed_to_me", language),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(owedToMe, language),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translation.get("recent_tx", language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.DarkGray
                )
            }
        }

        if (recentTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141724) else Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(64.dp)
                                .padding(bottom = 16.dp)
                        )
                        Text(
                            text = Translation.get("no_tx", language),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val grouped = recentTransactions.sortedByDescending { it.timestamp }.groupBy { formatDateToDay(it.timestamp) }
            grouped.forEach { (date, txs) ->
                item {
                    Text(
                        text = formatDateHeader(date, language),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(txs) { tx ->
                    TransactionRowItem(tx, language, isDark, persons, onDeleteTransaction)
                }
            }
        }

        // Buffer space at bottom
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------- TRANSACTION ROW COMPONENT ----------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRowItem(
    tx: Transaction,
    language: AppLanguage,
    isDark: Boolean,
    persons: List<Person>,
    onDelete: (Int) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val linkedPerson = persons.find { it.id == tx.personId }

    if (showDeleteConfirm) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                onDelete(tx.id)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141724) else Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showDeleteConfirm = true }
            )
            .testTag("tx_item_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Category Icon circle
                val (color, icon) = when (tx.type) {
                    "INCOME" -> Pair(FintechGreen, Icons.AutoMirrored.Rounded.TrendingUp)
                    "EXPENSE" -> Pair(FintechRed, Icons.AutoMirrored.Rounded.TrendingDown)
                    "LEND" -> Pair(FintechBlue, Icons.Rounded.ArrowUpward)
                    "BORROW" -> Pair(Color(0xFFFBBF24), Icons.Rounded.ArrowDownward)
                    else -> Pair(Color.Gray, Icons.AutoMirrored.Rounded.CompareArrows)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val formattedCategory = if (language == AppLanguage.BN) {
                        when (tx.category) {
                            "Salary" -> "বেতন"
                            "Business" -> "ব্যবসা"
                            "Freelancing" -> "ফ্রিল্যান্সিং"
                            "Investment" -> "বিনিয়োগ"
                            "Food" -> "খাবার"
                            "Rent" -> "ভাড়া"
                            "Shopping" -> "শপিং"
                            "Bills" -> "বিল"
                            "Medical" -> "চিকিৎসা"
                            "Entertainment" -> "বিনোদন"
                            "Lending" -> "ধার দেওয়া"
                            "Borrowing" -> "ধার নেওয়া"
                            "Repay Paid" -> Translation.get("debt_repaid", language)
                            "Repay Received" -> Translation.get("pawn_repaid", language)
                            else -> tx.category
                        }
                    } else {
                        tx.category
                    }

                    val isDebtType = tx.type in listOf("LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED")
                    val titleText = if (isDebtType && linkedPerson != null) {
                        val typeSuffix = when (tx.type) {
                            "LEND" -> if (language == AppLanguage.BN) "ধার প্রদান" else "Lending"
                            "BORROW" -> if (language == AppLanguage.BN) "ধার গ্রহন" else "Borrowing"
                            "REPAY_PAID" -> if (language == AppLanguage.BN) "পরিশোধ" else "Repaid"
                            "REPAY_RECEIVED" -> if (language == AppLanguage.BN) "প্রাপ্তি" else "Received"
                            else -> ""
                        }
                        "${linkedPerson.name} - ($typeSuffix)"
                    } else {
                        if (linkedPerson != null) "$formattedCategory (${linkedPerson.name})" else formattedCategory
                    }

                    Text(
                        text = titleText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E222F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tx.note.isNotEmpty()) {
                        Text(
                            text = tx.note,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatDate(tx.timestamp, language),
                        fontSize = 10.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }

            // Amount
            val amountPrefix = when (tx.type) {
                "INCOME", "BORROW", "REPAY_RECEIVED" -> "+"
                else -> "-"
            }
            val amountColor = when (tx.type) {
                "INCOME", "BORROW", "REPAY_RECEIVED" -> FintechGreen
                else -> FintechRed
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amountPrefix${formatCurrency(tx.amount, language)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ---------------- TRANSACTIONS TAB ----------------
@Composable
fun TransactionsScreen(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    onAddTransactionClick: () -> Unit,
    onDeleteTransaction: (Int) -> Unit,
    filter: String = "ALL",
    onFilterChange: (String) -> Unit = {},
    timeFilter: String = "ALL",
    onTimeFilterChange: (String) -> Unit = {}
) {
    val timeFilteredTransactions = remember(transactions, timeFilter) {
        filterTransactionsByTime(transactions, timeFilter)
    }

    val filteredTransactions = remember(timeFilteredTransactions, filter) {
        val list = timeFilteredTransactions
        // Type filter
        when (filter) {
            "INCOME" -> list.filter { it.type == "INCOME" }
            "EXPENSE" -> list.filter { it.type == "EXPENSE" }
            "DENA" -> list.filter { it.type == "BORROW" || it.type == "REPAY_PAID" }
            "PAWN" -> list.filter { it.type == "LEND" || it.type == "REPAY_RECEIVED" }
            else -> list
        }
    }

    val totalIncome = timeFilteredTransactions.filter { it.type == "INCOME" || it.type == "BORROW" || it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
    val totalExpense = timeFilteredTransactions.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Cards (arranged horizontally in a Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    padding = 10.dp,
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                        Text(Translation.get("total_income", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalIncome, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                    }
                }
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    padding = 10.dp,
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                        Text(Translation.get("total_expense", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalExpense, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                    }
                }
            }

            // Time Filter Row
            TimeFilterRow(
                timeFilter = timeFilter,
                language = language,
                onTimeFilterChange = onTimeFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Type Filters chip row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    Pair("ALL", "all"),
                    Pair("INCOME", "income"),
                    Pair("EXPENSE", "expense"),
                    Pair("DENA", "dena"),
                    Pair("PAWN", "pawn")
                )

                filters.forEach { (type, labelKey) ->
                    val isSelected = filter == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else if (isDark) Color(0xFF1E222F) else Color.White
                            )
                            .clickable { onFilterChange(type) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translation.get(labelKey, language),
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(Translation.get("no_tx", language), color = Color.Gray)
                }
            } else {
                val grouped = filteredTransactions.sortedByDescending { it.timestamp }.groupBy { formatDateToDay(it.timestamp) }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    grouped.forEach { (date, txs) ->
                        item {
                            Text(
                                text = formatDateHeader(date, language),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                            )
                        }
                        items(txs) { tx ->
                            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                TransactionRowItem(tx, language, isDark, persons, onDeleteTransaction)
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Floating button padding
                    }
                }
            }
        }

        // Add Floating Action Button
        FloatingActionButton(
            onClick = onAddTransactionClick,
            containerColor = FintechBlue,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
                .testTag("fab_add_tx")
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color.White)
        }
    }
}

// ---------------- DEBTS & CREDITS TAB ----------------
@Composable
fun DebtsScreen(
    language: AppLanguage,
    isDark: Boolean,
    personDebts: List<PersonDebt>,
    onAddPersonClick: () -> Unit,
    onPersonClick: (PersonDebt) -> Unit,
    onDeletePerson: (Int) -> Unit,
    filter: String = "ALL",
    onFilterChange: (String) -> Unit = {},
    timeFilter: String = "ALL",
    onTimeFilterChange: (String) -> Unit = {}
) {
    val filteredDebts = remember(personDebts, filter) {
        when (filter) {
            "DENA" -> personDebts.filter { it.netBalance < 0 }
            "PAWN" -> personDebts.filter { it.netBalance > 0 }
            else -> personDebts
        }
    }

    val totalDena = personDebts.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
    val totalPawn = personDebts.filter { it.netBalance > 0 }.sumOf { it.netBalance }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Cards (arranged horizontally in a Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    padding = 10.dp,
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                        Text(Translation.get("total_dena", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalDena, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                    }
                }
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    padding = 10.dp,
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                        Text(Translation.get("total_pawn", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalPawn, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                    }
                }
            }

            // Time Filter Row
            TimeFilterRow(
                timeFilter = timeFilter,
                language = language,
                onTimeFilterChange = onTimeFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    Pair("ALL", "all"),
                    Pair("DENA", "dena"),
                    Pair("PAWN", "pawn")
                )

                filters.forEach { (type, labelKey) ->
                    val isSelected = filter == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else if (isDark) Color(0xFF1E222F) else Color.White
                            )
                            .clickable { onFilterChange(type) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Translation.get(labelKey, language),
                            color = if (isSelected) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (filteredDebts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(Translation.get("no_persons", language), color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDebts) { item ->
                        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                            PersonDebtRowItem(item, language, isDark, onPersonClick, onDeletePerson)
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Person
        FloatingActionButton(
            onClick = onAddPersonClick,
            containerColor = FintechBlue,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
                .testTag("fab_add_person")
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Person", tint = Color.White)
        }
    }
}

@Composable
fun TimeFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimeFilterRow(
    timeFilter: String,
    language: AppLanguage,
    onTimeFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isMonthActive = timeFilter == "MONTH" || timeFilter.startsWith("CUSTOM_MONTH:")
        val isDateActive = timeFilter.startsWith("CUSTOM_DATE:")
        
        // 1. All Time Chip
        TimeFilterChip(
            selected = timeFilter == "ALL",
            onClick = { onTimeFilterChange("ALL") },
            label = if (language == AppLanguage.BN) "সব সময়ের" else "All Time",
            icon = Icons.Rounded.DateRange
        )
        
        // 2. Month Chip
        val calendar = java.util.Calendar.getInstance()
        val curYear = calendar.get(java.util.Calendar.YEAR)
        val curMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val monthLabel = if (timeFilter.startsWith("CUSTOM_MONTH:")) {
            getCustomTimeFilterLabel(timeFilter, language) + (if (language == AppLanguage.BN) " মাসের" else " Month")
        } else {
            getCustomTimeFilterLabel("CUSTOM_MONTH:$curYear-$curMonth", language) + (if (language == AppLanguage.BN) " মাসের" else " Month")
        }
        
        TimeFilterChip(
            selected = isMonthActive,
            onClick = { onTimeFilterChange("TRIGGER_MONTH_PICKER") },
            label = monthLabel,
            icon = Icons.Rounded.DateRange
        )
        
        // 3. Specific Date Chip
        val dateLabel = if (timeFilter.startsWith("CUSTOM_DATE:")) {
            getFormattedDateLabel(timeFilter, language)
        } else {
            if (language == AppLanguage.BN) "তারিখ অনুযায়ী" else "Select Date"
        }
        
        TimeFilterChip(
            selected = isDateActive,
            onClick = { onTimeFilterChange("TRIGGER_DATE_PICKER") },
            label = dateLabel,
            icon = Icons.Rounded.DateRange
        )
    }
}

fun filterTransactionsByTime(transactions: List<Transaction>, timeFilter: String): List<Transaction> {
    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    return when {
        timeFilter == "TODAY" -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            transactions.filter { it.timestamp >= startOfDay }
        }
        timeFilter == "MONTH" -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            transactions.filter { it.timestamp >= startOfMonth }
        }
        timeFilter.startsWith("CUSTOM_MONTH:") -> {
            val parts = timeFilter.substringAfter("CUSTOM_MONTH:").split("-")
            if (parts.size == 2) {
                val year = parts[0].toIntOrNull() ?: 2026
                val month = parts[1].toIntOrNull() ?: 1
                calendar.clear()
                calendar.set(java.util.Calendar.YEAR, year)
                calendar.set(java.util.Calendar.MONTH, month - 1)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(java.util.Calendar.MONTH, 1)
                val end = calendar.timeInMillis
                transactions.filter { it.timestamp in start until end }
            } else transactions
        }
        timeFilter.startsWith("CUSTOM_DATE:") -> {
            val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: 2026
                val month = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                calendar.clear()
                calendar.set(java.util.Calendar.YEAR, year)
                calendar.set(java.util.Calendar.MONTH, month - 1)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, day)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                val end = calendar.timeInMillis
                transactions.filter { it.timestamp in start until end }
            } else transactions
        }
        else -> transactions
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonDebtRowItem(
    item: PersonDebt,
    language: AppLanguage,
    isDark: Boolean,
    onClick: (PersonDebt) -> Unit,
    onDelete: (Int) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                onDelete(item.person.id)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141724) else Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick(item) },
                onLongClick = { showDeleteConfirm = true }
            )
            .testTag("person_item_${item.person.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Avatar representation
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FintechBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.person.photoUri.isNotEmpty()) {
                        AsyncImage(
                            model = item.person.photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = item.person.name.take(1).uppercase(),
                            color = FintechBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.person.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E222F)
                    )
                    if (item.person.phone.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = item.person.phone, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Net balance representation
            Column(horizontalAlignment = Alignment.End) {
                val (statusText, statusColor, absoluteAmount) = when {
                    item.netBalance > 0 -> Triple(Translation.get("you_get", language), FintechGreen, item.netBalance)
                    item.netBalance < 0 -> Triple(Translation.get("you_owe", language), FintechRed, -item.netBalance)
                    else -> Triple(Translation.get("settled", language), Color.Gray, 0.0)
                }

                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                Text(
                    text = formatCurrency(absoluteAmount, language),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
            }
        }
    }
}

// ---------------- SAVINGS TAB ----------------
@Composable
fun SavingsScreen(
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    savingsGoals: List<SavingsGoal>,
    onAddSavingsGoalClick: () -> Unit,
    onGoalClick: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (language == AppLanguage.BN) "আপনার সঞ্চয় কার্ডসমূহ" else "Your Savings Cards",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechBlue
                )
                
                IconButton(
                    onClick = onAddSavingsGoalClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FintechBlue.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        tint = FintechBlue
                    )
                }
            }

            if (savingsGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalance,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(Translation.get("no_savings", language), color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(savingsGoals) { goal ->
                        SavingsGoalCardItem(goal, language, isDark, profileName, onGoalClick, onContributeClick, onEditGoal)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Savings Goal
        FloatingActionButton(
            onClick = onAddSavingsGoalClick,
            containerColor = FintechBlue,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
                .testTag("fab_add_savings_goal")
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Goal", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavingsGoalCardItem(
    goal: SavingsGoal,
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    onGoalClick: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit
) {
    val gradient = GradientsList[goal.colorIndex % GradientsList.size]

    FintechGradientCard(
        gradientColors = gradient,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .combinedClickable(
                onClick = { onGoalClick(goal) },
                onLongClick = { onEditGoal(goal) }
            )
            .testTag("savings_item_${goal.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mastercard Chip and Contactless Icon
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                // Chip Icon Simulation
                Box(
                    modifier = Modifier
                        .size(38.dp, 28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) {
                            Divider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp).rotate(90f)
                )
            }

            // Category Label at Top Right
            val formattedCategory = if (language == AppLanguage.BN) {
                when (goal.category) {
                    "Emergency" -> "জরুরি ফান্ড"
                    "Laptop" -> "ল্যাপটপ"
                    "Travel" -> "ভ্রমণ"
                    "Marriage" -> "বিয়ে"
                    "Investment" -> "বিনিয়োগ"
                    else -> goal.category
                }
            } else {
                goal.category
            }

            // Card Title at Top Right
            Text(
                text = goal.title.uppercase(),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // Goal Category and Balance in the middle-bottom
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = formattedCategory,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatCurrency(goal.savedAmount, language),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (goal.targetAmount > 0) {
                        Text(
                            text = " / " + formatCurrency(goal.targetAmount, language),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                }
            }

            // Bottom row: "Holder" (Title again or generic) and Mastercard-like logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (language == AppLanguage.BN) "কার্ডধারী" else "CARDHOLDER",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = goal.cardholderName.ifBlank { profileName }.uppercase(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }

                // Mastercard Circles Logo Simulation
                Box(contentAlignment = Alignment.Center) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEB001B).copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF79E1B).copy(alpha = 0.8f))
                        )
                    }
                }
            }
        }
    }
}

// ---------------- DIALOGS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    language: AppLanguage,
    persons: List<Person>,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, String, Int?, Long) -> Unit
) {
    val amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var customTimestamp by remember { mutableStateOf<Long?>(null) }

    // Dropdowns / Option selectors
    var type by remember { mutableStateOf("EXPENSE") } // INCOME, EXPENSE, LEND, BORROW, REPAY_PAID, REPAY_RECEIVED
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var category by remember { mutableStateOf("Food") }

    val categoriesIncome = listOf("Salary", "Business", "Freelancing", "Investment", "Others")
    val categoriesExpense = listOf("Food", "Rent", "Shopping", "Bills", "Medical", "Entertainment", "Others")

    val types = listOf(
        Pair("INCOME", "tx_type_income"),
        Pair("EXPENSE", "tx_type_expense"),
        Pair("LEND", "tx_type_lend"),
        Pair("BORROW", "tx_type_borrow"),
        Pair("REPAY_PAID", "tx_type_repay_paid"),
        Pair("REPAY_RECEIVED", "tx_type_repay_received")
    )

    var personDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val dialogBg = if (isDark) Color(0xFF141724) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)
    val chipBg = if (isDark) Color(0xFF1F2336) else Color(0xFFF1F5F9)

    var amountInputState by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        Translation.get("add_tx", language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Type Chips Grid
                item {
                    Text(Translation.get("type", language), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            types.take(3).forEach { (tValue, tLabelKey) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (type == tValue) MaterialTheme.colorScheme.primary else chipBg)
                                        .clickable {
                                            type = tValue
                                            // auto set categories
                                            if (tValue == "INCOME") category = "Salary"
                                            else if (tValue == "EXPENSE") category = "Food"
                                            else category = "Loan"
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = Translation.get(tLabelKey, language),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (type == tValue) Color.White else textColor
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            types.drop(3).forEach { (tValue, tLabelKey) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (type == tValue) MaterialTheme.colorScheme.primary else chipBg)
                                        .clickable {
                                            type = tValue
                                            category = "Loan"
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = Translation.get(tLabelKey, language),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (type == tValue) Color.White else textColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount Text Field
                item {
                    OutlinedTextField(
                        value = amountInputState,
                        onValueChange = { amountInputState = it },
                        label = { Text(Translation.get("amount", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tx_amount")
                    )
                }

                // Link with Person (Needed for Lending, Borrowing, Repayments)
                val isPersonRequired = type != "INCOME" && type != "EXPENSE"
                if (isPersonRequired || persons.isNotEmpty()) {
                    item {
                        Text(
                            Translation.get("person", language) + (if (isPersonRequired) " *" else " (${Translation.get("phone", language).split(" ").last()})"),
                            color = labelColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = personDropdownExpanded,
                                onExpandedChange = { personDropdownExpanded = !personDropdownExpanded }
                            ) {
                                val selectedPersonName = persons.find { it.id == selectedPersonId }?.name ?: Translation.get("select_person", language)
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedPersonName,
                                    onValueChange = {},
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personDropdownExpanded) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = labelColor
                                    ),
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = personDropdownExpanded,
                                    onDismissRequest = { personDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(Translation.get("select_person", language)) },
                                        onClick = {
                                            selectedPersonId = null
                                            personDropdownExpanded = false
                                        }
                                    )
                                    persons.forEach { person ->
                                        DropdownMenuItem(
                                            text = { Text(person.name) },
                                            onClick = {
                                                selectedPersonId = person.id
                                                personDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Selector (Hidden for debts/credits which is preset as Loan)
                if (type == "INCOME" || type == "EXPENSE") {
                    item {
                        Text(Translation.get("category", language), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                            ) {
                                val formattedCategory = if (language == AppLanguage.BN) {
                                    when (category) {
                                        "Salary" -> "বেতন"
                                        "Business" -> "ব্যবসা"
                                        "Freelancing" -> "ফ্রিল্যান্সিং"
                                        "Investment" -> "বিনিয়োগ"
                                        "Food" -> "খাবার"
                                        "Rent" -> "ভাড়া"
                                        "Shopping" -> "শপিং"
                                        "Bills" -> "বিল"
                                        "Medical" -> "চিকিৎসা"
                                        "Entertainment" -> "বিনোদন"
                                        else -> category
                                    }
                                } else {
                                    category
                                }

                                OutlinedTextField(
                                    readOnly = true,
                                    value = formattedCategory,
                                    onValueChange = {},
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = labelColor
                                    ),
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    val cats = if (type == "INCOME") categoriesIncome else categoriesExpense
                                    cats.forEach { cat ->
                                        val catLabel = if (language == AppLanguage.BN) {
                                            when (cat) {
                                                "Salary" -> "বেতন"
                                                "Business" -> "ব্যবসা"
                                                "Freelancing" -> "ফ্রিল্যান্সিং"
                                                "Investment" -> "বিনিয়োগ"
                                                "Food" -> "খাবার"
                                                "Rent" -> "ভাড়া"
                                                "Shopping" -> "শপিং"
                                                "Bills" -> "বিল"
                                                "Medical" -> "চিকিৎসা"
                                                "Entertainment" -> "বিনোদন"
                                                else -> cat
                                            }
                                        } else cat

                                        DropdownMenuItem(
                                            text = { Text(catLabel) },
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Note/Description field
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(Translation.get("note", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tx_note")
                    )
                }

                // Manual Date/Time Selection
                item {
                    Text(
                        if (language == AppLanguage.BN) "তারিখ ও সময় (ঐচ্ছিক)" else "Date & Time (Optional)",
                        color = labelColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    var showManualDatePicker by remember { mutableStateOf(false) }
                    
                    val dateLabel = if (customTimestamp != null) {
                        formatDate(customTimestamp!!, language)
                    } else {
                        if (language == AppLanguage.BN) "বর্তমান সময় (স্বয়ংক্রিয়)" else "Current Time (Automatic)"
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { showManualDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(dateLabel, color = textColor, fontSize = 14.sp)
                        }
                        if (customTimestamp != null) {
                            IconButton(onClick = { customTimestamp = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = FintechRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    if (showManualDatePicker) {
                        val calendar = java.util.Calendar.getInstance()
                        val curYear = calendar.get(java.util.Calendar.YEAR)
                        val curMonth = calendar.get(java.util.Calendar.MONTH) + 1
                        val curDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        SpecificDatePickerDialog(
                            initialYear = curYear,
                            initialMonth = curMonth,
                            initialDay = curDay,
                            language = language,
                            onDismiss = { showManualDatePicker = false },
                            onConfirm = { year, month, day ->
                                calendar.clear()
                                calendar.set(year, month - 1, day)
                                customTimestamp = calendar.timeInMillis
                                showManualDatePicker = false
                            }
                        )
                    }
                }

                // Confirm and Cancel buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Translation.get("cancel", language), color = labelColor)
                        }

                        Button(
                            onClick = {
                                val amount = amountInputState.toDoubleOrNull() ?: 0.0
                                if (amount <= 0) {
                                    Toast.makeText(context, Translation.get("error_empty_amount", language), Toast.LENGTH_SHORT).show()
                                } else if (isPersonRequired && selectedPersonId == null) {
                                    Toast.makeText(context, Translation.get("error_empty_person", language), Toast.LENGTH_SHORT).show()
                                } else {
                                    onConfirm(amount, type, category, note, selectedPersonId, customTimestamp ?: System.currentTimeMillis())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_confirm_tx")
                        ) {
                            Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPersonDialog(
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf("") }
    val context = LocalContext.current

    val dialogBg = if (isDark) Color(0xFF141724) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    Translation.get("add_person", language),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = textColor
                )

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        photoUri = uri.toString()
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                        .clickable { launcher.launch("image/*") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotEmpty()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = labelColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Text(
                        text = if (photoUri.isEmpty()) Translation.get("select_image", language) else Translation.get("change", language) ?: "Change",
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Translation.get("name", language)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_person_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Translation.get("phone", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_person_phone")
                )
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(Translation.get("address", language)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_person_address")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(Translation.get("cancel", language), color = labelColor)
                    }

                    Button(
                        onClick = {
                            val trimmedPhone = phone.trim()
                            if (name.trim().isEmpty()) {
                                Toast.makeText(context, Translation.get("enter_name", language), Toast.LENGTH_SHORT).show()
                            } else if (trimmedPhone.isNotEmpty() && (!trimmedPhone.all { it.isDigit() } || trimmedPhone.length != 11)) {
                                Toast.makeText(context, if (language == AppLanguage.BN) "সঠিক ১১ ডিজিটের ফোন নম্বর লিখুন" else "Enter a valid 11-digit phone number", Toast.LENGTH_SHORT).show()
                            } else {
                                onConfirm(name.trim(), trimmedPhone, address.trim(), photoUri.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_person")
                    ) {
                        Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalDialog(
    language: AppLanguage,
    isDark: Boolean,
    initialGoal: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Int, String) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var cardholderName by remember { mutableStateOf(initialGoal?.cardholderName ?: "") }
    var targetStr by remember { mutableStateOf(if (initialGoal != null) initialGoal.targetAmount.toString() else "") }
    var sector by remember { mutableStateOf(initialGoal?.category ?: "Emergency") }
    var colorIndex by remember { mutableStateOf(initialGoal?.colorIndex ?: 0) }

    val sectors = listOf("Emergency", "Laptop", "Travel", "Marriage", "Investment", "Other")
    var sectorDropdownExpanded by remember { mutableStateOf(false) }
    var customSectorName by remember { mutableStateOf("") }
    val context = LocalContext.current

    val dialogBg = if (isDark) Color(0xFF141724) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        if (language == AppLanguage.BN) "নতুন সঞ্চয় কার্ড" else "New Savings Card",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (language == AppLanguage.BN) "কার্ডের নাম" else "Card Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_saving_title")
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = cardholderName,
                        onValueChange = { cardholderName = it },
                        label = { Text(if (language == AppLanguage.BN) "কার্ডধারীর নাম" else "Cardholder Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_saving_cardholder")
                    )
                }

                item {
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text(Translation.get("target", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_saving_target")
                    )
                }

                item {
                    Text(Translation.get("category", language), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = sectorDropdownExpanded,
                            onExpandedChange = { sectorDropdownExpanded = !sectorDropdownExpanded }
                        ) {
                            val formattedSector = if (language == AppLanguage.BN) {
                                when (sector) {
                                    "Emergency" -> "জরুরি ফান্ড"
                                    "Laptop" -> "ল্যাপটপ"
                                    "Travel" -> "ভ্রমণ"
                                    "Marriage" -> "বিয়ে"
                                    "Investment" -> "বিনিয়োগ"
                                    "Other" -> "অন্যান্য"
                                    else -> sector
                                }
                            } else {
                                sector
                            }

                            OutlinedTextField(
                                readOnly = true,
                                value = formattedSector,
                                onValueChange = {},
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = labelColor
                                ),
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = sectorDropdownExpanded,
                                onDismissRequest = { sectorDropdownExpanded = false }
                            ) {
                                sectors.forEach { sec ->
                                    val secLabel = if (language == AppLanguage.BN) {
                                        when (sec) {
                                            "Emergency" -> "জরুরি ফান্ড"
                                            "Laptop" -> "ল্যাপটপ"
                                            "Travel" -> "ভ্রমণ"
                                            "Marriage" -> "বিয়ে"
                                            "Investment" -> "বিনিয়োগ"
                                            "Other" -> "অন্যান্য"
                                            else -> sec
                                        }
                                    } else sec

                                    DropdownMenuItem(
                                        text = { Text(secLabel) },
                                        onClick = {
                                            sector = sec
                                            sectorDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (sector == "Other") {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customSectorName,
                            onValueChange = { customSectorName = it },
                            label = { Text(if (language == AppLanguage.BN) "খাতের নাম লিখুন" else "Enter Sector Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = labelColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = labelColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Color Picker Grid
                item {
                    Text(Translation.get("theme", language), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GradientsList.forEachIndexed { idx, grad ->
                            val isSelected = colorIndex == idx
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(grad))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) textColor else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorIndex = idx }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text(Translation.get("cancel", language), color = labelColor)
                        }

                        Button(
                            onClick = {
                                val target = targetStr.toDoubleOrNull() ?: 0.0
                                val finalSector = if (sector == "Other" && customSectorName.isNotBlank()) customSectorName else sector
                                if (title.trim().isNotEmpty()) {
                                    onConfirm(title.trim(), target, finalSector, colorIndex, cardholderName.trim())
                                } else {
                                    Toast.makeText(context, Translation.get("error_empty_title", language), Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_confirm_saving")
                        ) {
                            Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsContributionDialog(
    language: AppLanguage,
    savingsGoal: SavingsGoal,
    isDark: Boolean,
    initialIsWithdraw: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean, String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var noteStr by remember { mutableStateOf("") }
    var isWithdraw by remember { mutableStateOf(initialIsWithdraw) }
    val context = LocalContext.current

    val dialogBg = if (isDark) Color(0xFF141724) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${Translation.get("add_money", language)} (${savingsGoal.title})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = textColor
                )

                // Deposit / Withdraw Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isWithdraw) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(1.dp, if (!isWithdraw) Color.Transparent else labelColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { isWithdraw = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(Translation.get("deposit", language), color = if (!isWithdraw) Color.White else textColor)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isWithdraw) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(1.dp, if (isWithdraw) Color.Transparent else labelColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { isWithdraw = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(Translation.get("withdraw", language), color = if (isWithdraw) Color.White else textColor)
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(Translation.get("amount", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contribution_amount")
                )

                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text(if (language == AppLanguage.BN) "মন্তব্য যোগ করুন" else "Add Comment") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(Translation.get("cancel", language), color = labelColor)
                    }

                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                if (isWithdraw && amount > savingsGoal.savedAmount) {
                                    Toast.makeText(context, if (language == AppLanguage.BN) "পর্যাপ্ত ব্যালেন্স নেই!" else "Insufficient balance!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onConfirm(amount, isWithdraw, noteStr)
                                }
                            } else {
                                Toast.makeText(context, Translation.get("error_empty_amount", language), Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_contribution")
                    ) {
                        Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------- SAVINGS GOAL DETAIL OVERLAY ----------------
@Composable
fun SavingsGoalDetailOverlay(
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    goal: SavingsGoal,
    transactionsFlow: kotlinx.coroutines.flow.Flow<List<SavingsTransaction>>,
    onDismiss: () -> Unit,
    onDeleteGoal: (Int) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit
) {
    val txList by transactionsFlow.collectAsState(initial = emptyList())
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                onDeleteGoal(goal.id)
                showDeleteConfirm = false
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("savings_detail_overlay"),
        color = if (isDark) Color(0xFF0B0D14) else Color(0xFFF3F4F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = if (isDark) Color.White else Color.Black)
                }
                
                Text(
                    text = if (language == AppLanguage.BN) "সঞ্চয় কার্ডের বিস্তারিত" else "Savings Card Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechBlue,
                    modifier = Modifier.align(Alignment.Center)
                )

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    IconButton(onClick = { onEditGoal(goal) }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Show Card at top
            SavingsGoalCardItem(
                goal = goal,
                language = language,
                isDark = isDark,
                profileName = profileName,
                onGoalClick = {}, // No click action here
                onContributeClick = { _, _ -> },
                onEditGoal = {}
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Deposit/Withdraw Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onContributeClick(goal, false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechGreen)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.BN) "জমা করুন" else "Deposit", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onContributeClick(goal, true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.BN) "উত্তোলন" else "Withdraw", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History Label
            Text(
                text = Translation.get("history", language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F1724),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Transactions List
            if (txList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(if (language == AppLanguage.BN) "কোনো লেনদেন নেই" else "No transactions yet", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(txList) { tx ->
                        val isDeposit = tx.isDeposit
                        val amountColor = if (isDeposit) FintechGreen else FintechRed
                        val txIcon = if (isDeposit) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown
                        val txLabel = if (isDeposit) Translation.get("deposit", language) else Translation.get("withdraw", language)

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF141724) else Color.White),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(amountColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = txIcon,
                                            contentDescription = null,
                                            tint = amountColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = txLabel,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF0F1724),
                                            fontSize = 14.sp
                                        )
                                        val d = java.util.Date(tx.timestamp)
                                        val f = java.text.SimpleDateFormat("dd MMM, yyyy", java.util.Locale.getDefault())
                                        Text(
                                            text = f.format(d),
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                        if (tx.note.isNotEmpty()) {
                                            Text(
                                                text = tx.note,
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = (if (isDeposit) "+" else "-") + formatCurrency(tx.amount, language),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = amountColor,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- PERSON DETAIL FULL OVERLAY ----------------
@Composable
fun PersonDetailOverlay(
    language: AppLanguage,
    isDark: Boolean,
    personDebt: PersonDebt,
    transactionsFlow: kotlinx.coroutines.flow.Flow<List<Transaction>>,
    onDismiss: () -> Unit,
    onLendClick: (Double, String) -> Unit,
    onBorrowClick: (Double, String) -> Unit,
    onRepayPaidClick: (Double, String) -> Unit,
    onRepayReceivedClick: (Double, String) -> Unit,
    onDeleteTx: (Int) -> Unit
) {
    val txList by transactionsFlow.collectAsState(initial = emptyList())
    var showActionSheet by remember { mutableStateOf<String?>(null) } // "LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED"
    var actionAmountStr by remember { mutableStateOf("") }
    var actionNoteStr by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("person_detail_overlay"),
        color = if (isDark) Color(0xFF0B0D14) else Color(0xFFF3F4F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header - Simplified (removed Close button to reduce empty space as requested by user)
            Spacer(modifier = Modifier.height(8.dp))

            // Person Bio Card (Frosted Card)
            FrostedGlassCard(isDark = isDark) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (personDebt.person.photoUri.isNotEmpty()) {
                            AsyncImage(
                                model = personDebt.person.photoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = personDebt.person.name.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = personDebt.person.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF1E222F)
                        )
                        if (personDebt.person.phone.isNotEmpty()) {
                            Text(
                                text = personDebt.person.phone,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Net Balance Summary Box inside Card
                val (statusText, statusColor, absoluteAmount) = when {
                    personDebt.netBalance > 0 -> Triple(Translation.get("you_get", language), FintechGreen, personDebt.netBalance)
                    personDebt.netBalance < 0 -> Triple(Translation.get("you_owe", language), FintechRed, -personDebt.netBalance)
                    else -> Triple(Translation.get("settled", language), Color.Gray, 0.0)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF141724) else Color(0xFFF3F4F6))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translation.get("net_status", language),
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatCurrency(absoluteAmount, language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Grid (Lend / Borrow / Repay)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showActionSheet = "LEND" },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(Translation.get("tx_type_lend", language).split(" ").take(2).joinToString(" "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showActionSheet = "BORROW" },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(Translation.get("tx_type_borrow", language).split(" ").take(2).joinToString(" "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showActionSheet = "REPAY_RECEIVED" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E222F) else Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(if (language == AppLanguage.BN) "ফেরত পেলাম" else Translation.get("tx_type_repay_received", language).split(" ").take(2).joinToString(" "), color = if (isDark) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showActionSheet = "REPAY_PAID" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E222F) else Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(if (language == AppLanguage.BN) "ফেরত দিলাম" else Translation.get("tx_type_repay_paid", language).split(" ").take(2).joinToString(" "), color = if (isDark) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // History Label
            Text(
                text = "${personDebt.person.name} ${Translation.get("history_with", language)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Dynamic Action sheet overlay / Dialog
            if (showActionSheet != null) {
                val actType = showActionSheet!!
                val labelKey = when (actType) {
                    "LEND" -> "tx_type_lend"
                    "BORROW" -> "tx_type_borrow"
                    "REPAY_PAID" -> "tx_type_repay_paid"
                    else -> "tx_type_repay_received"
                }

                Dialog(onDismissRequest = { showActionSheet = null }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141724)),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = Translation.get(labelKey, language),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            OutlinedTextField(
                                value = actionAmountStr,
                                onValueChange = { actionAmountStr = it },
                                label = { Text(Translation.get("amount", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = actionNoteStr,
                                onValueChange = { actionNoteStr = it },
                                label = { Text(if (language == AppLanguage.BN) "মন্তব্য যোগ করুন" else "Add Comment") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(onClick = { 
                                    showActionSheet = null
                                    actionNoteStr = ""
                                }, modifier = Modifier.weight(1f)) {
                                    Text(Translation.get("cancel", language), color = Color.Gray)
                                }
                                Button(
                                    onClick = {
                                        val amt = actionAmountStr.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            when (actType) {
                                                "LEND" -> onLendClick(amt, actionNoteStr)
                                                "BORROW" -> onBorrowClick(amt, actionNoteStr)
                                                "REPAY_PAID" -> onRepayPaidClick(amt, actionNoteStr)
                                                "REPAY_RECEIVED" -> onRepayReceivedClick(amt, actionNoteStr)
                                            }
                                            actionAmountStr = ""
                                            actionNoteStr = ""
                                            showActionSheet = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Text(Translation.get("confirm", language), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Transaction history list for this person
            if (txList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(Translation.get("no_tx", language), color = Color.Gray)
                }
            } else {
                val grouped = txList.sortedByDescending { it.timestamp }.groupBy { formatDateToDay(it.timestamp) }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    grouped.forEach { (date, txs) ->
                        item {
                            Text(
                                text = formatDateHeader(date, language),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(txs) { tx ->
                            TransactionRowItem(tx, language, isDark, listOf(personDebt.person), onDeleteTx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FintechGradientCard(
        gradientColors = GradientsList[0],
        cornerRadius = 24.dp,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    isDark: Boolean,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B)
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun SettingCategory(
    title: String,
    isDark: Boolean,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FintechBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDark) Color(0xFF141724) else Color.White)
                .border(
                    width = 1.dp,
                    color = if (isDark) Color(0xFF1F2937) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val profileName by viewModel.profileName.collectAsState()
    val profileEmail by viewModel.profileEmail.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()
    val profilePhone by viewModel.profilePhone.collectAsState()
    val profileSocial by viewModel.profileSocial.collectAsState()
    val profileAddress by viewModel.profileAddress.collectAsState()

    var nameInput by remember(profileName) { mutableStateOf(profileName) }
    var emailInput by remember(profileEmail) { mutableStateOf(profileEmail) }
    var photoUriInput by remember(profilePhotoUri) { mutableStateOf(profilePhotoUri) }
    var phoneInput by remember(profilePhone) { mutableStateOf(profilePhone) }
    var socialInput by remember(profileSocial) { mutableStateOf(profileSocial) }
    var addressInput by remember(profileAddress) { mutableStateOf(profileAddress) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUriInput = it.toString()
        }
    }

    var pasteJsonInput by remember { mutableStateOf("") }
    var showPasteArea by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0F111A) else Color(0xFFF1F5F9))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. USER PROFILE EDIT CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ব্যবহারকারী প্রোফাইল এডিট" else "Edit User Profile",
            isDark = isDark,
            icon = Icons.Rounded.Person
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .clickable { photoLauncher.launch("image/*") }
                        .testTag("settings_avatar_box")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isDark) Color(0xFF1E2235) else Color(0xFFE2E8F0), CircleShape)
                            .border(2.dp, FintechBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUriInput.isNullOrBlank()) {
                            AsyncImage(
                                model = photoUriInput,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile Icon",
                                tint = FintechBlue,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                    // Small camera badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .background(FintechBlue, CircleShape)
                            .border(2.dp, if (isDark) Color(0xFF141724) else Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoCamera,
                            contentDescription = "Edit photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Input fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Your Name *
                    val nameLabel = if (language == AppLanguage.BN) "আপনার নাম *" else "Your Name *"
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(nameLabel) },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = FintechBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )

                    // Phone Number
                    val phoneLabel = if (language == AppLanguage.BN) "ফোন নম্বর" else "Phone Number"
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text(phoneLabel) },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = FintechBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )

                    // Email or Social Profile
                    val socialLabel = if (language == AppLanguage.BN) "ইমেইল বা সোশ্যাল প্রোফাইল" else "Email or Social Profile"
                    OutlinedTextField(
                        value = socialInput,
                        onValueChange = { socialInput = it },
                        label = { Text(socialLabel) },
                        leadingIcon = { Icon(Icons.Rounded.AlternateEmail, contentDescription = null, tint = FintechBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )

                    // Address
                    val addressLabel = if (language == AppLanguage.BN) "ঠিকানা" else "Address"
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text(addressLabel) },
                        leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = FintechBlue) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )
                }

                // Save Information Button
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.saveProfile(
                                context,
                                nameInput,
                                emailInput,
                                photoUriInput,
                                phoneInput,
                                socialInput,
                                addressInput
                            )
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.BN) "তথ্য সফলভাবে সংরক্ষণ করা হয়েছে!" else "Information saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.BN) "দয়া করে নাম লিখুন" else "Please enter your name",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "তথ্য সংরক্ষণ করুন" else "Save Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        // --- 2. DARK MODE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ডার্ক মোড" else "Dark Mode",
            isDark = isDark,
            icon = Icons.Rounded.NightsStay
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEAB308).copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.BN) "ডার্ক মোড" else "Dark Mode",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "চোখের সুরক্ষায় ডার্ক থিম সক্রিয় করুন" else "Activate dark theme for eye protection",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                }
                Switch(
                    checked = isDark,
                    onCheckedChange = { viewModel.toggleTheme(context) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FintechBlue,
                        uncheckedThumbColor = if (isDark) Color.Gray else Color.White,
                        uncheckedTrackColor = if (isDark) Color(0xFF2A2E42) else Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // --- 3. APP LANGUAGE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপের ভাষা / App Language" else "App Language",
            isDark = isDark,
            icon = Icons.Rounded.Language
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BN) "আপনার পছন্দের ভাষা নির্বাচন করুন" else "Choose your preferred language",
                    fontSize = 13.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bangla Button
                    val isBn = language == AppLanguage.BN
                    Button(
                        onClick = { if (!isBn) viewModel.toggleLanguage(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBn) FintechBlue else (if (isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9))
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isBn) FintechBlue else (if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1))
                        ),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(
                            text = "বাংলা (Bangla)",
                            color = if (isBn) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // English Button
                    val isEn = language == AppLanguage.EN
                    Button(
                        onClick = { if (!isEn) viewModel.toggleLanguage(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEn) FintechBlue else (if (isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9))
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isEn) FintechBlue else (if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1))
                        ),
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text(
                            text = "English",
                            color = if (isEn) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // --- 4. DATA BACKUP & RESTORE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ডাটা ব্যাকআপ ও রিস্টোর" else "Data Backup & Restore",
            isDark = isDark,
            icon = Icons.Rounded.Backup
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Local Backup & Restore Sub-header
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN) "লোকাল ব্যাকআপ ও রিস্টোর" else "Local Backup & Restore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "আপনার সমস্ত ডাটা ফোনের মেমোরিতে ফাইল আকারে ব্যাকআপ রাখুন এবং যেকোনো সময় রিস্টোর করুন।" 
                            else "Keep a backup of all your data as a file in phone memory and restore anytime.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.exportBackup(
                                    context = context,
                                    onSuccess = { json ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Finance Note Backup", json)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, if (language == AppLanguage.BN) "ব্যাকআপ মেমোরিতে সেভ হয়েছে এবং ক্লিপবোর্ডে কপি হয়েছে!" else "Backup saved to memory and copied to clipboard!", Toast.LENGTH_LONG).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "ব্যাকআপ তৈরি" else "Create Backup", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = { showPasteArea = !showPasteArea },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E2235) else Color(0xFFE2E8F0)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1))
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "রিস্টোর করুন" else "Restore", fontSize = 12.sp, color = FintechBlue)
                        }
                    }
                }

                // Expandable paste backup area
                AnimatedVisibility(visible = showPasteArea) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option 1: Quick Restore from File
                        Button(
                            onClick = {
                                viewModel.importBackup(
                                    context = context,
                                    json = null,
                                    fromLocalFile = true,
                                    onSuccess = {
                                        showPasteArea = false
                                        Toast.makeText(context, if (language == AppLanguage.BN) "ব্যাকআপ সফলভাবে রিস্টোর হয়েছে!" else "Backup successfully restored!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, if (language == AppLanguage.BN) "কোনো সেভ করা ব্যাকআপ পাওয়া যায়নি!" else "No saved backup found!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Rounded.RestorePage, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (language == AppLanguage.BN) "মেমোরি থেকে রিস্টোর" else "Restore from Memory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Option 2: Paste Code
                        OutlinedTextField(
                            value = pasteJsonInput,
                            onValueChange = { pasteJsonInput = it },
                            label = { Text(if (language == AppLanguage.BN) "ব্যাকআপ কোড পেস্ট করুন" else "Paste Backup Code") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                                focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        )
                        Button(
                            onClick = {
                                if (pasteJsonInput.isNotBlank()) {
                                    viewModel.importBackup(
                                        context = context,
                                        json = pasteJsonInput,
                                        fromLocalFile = false,
                                        onSuccess = {
                                            pasteJsonInput = ""
                                            showPasteArea = false
                                            Toast.makeText(context, if (language == AppLanguage.BN) "ব্যাকআপ সফলভাবে রিস্টোর হয়েছে!" else "Backup successfully restored!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            Text(if (language == AppLanguage.BN) "নিশ্চিত করুন" else "Confirm", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF1E2235) else Color(0xFFE2E8F0))

                // Online Backup (Google Drive) Sub-header
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN) "অনলাইন ব্যাকআপ (গুগল ড্রাইভ)" else "Online Backup (Google Drive)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                                        // User status row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(FintechBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profileName.ifBlank { "Shariful Islam" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Text(
                                text = profileEmail.ifBlank { "connect.shariful@gmail.com" },
                                fontSize = 11.sp,
                                color = if (isDark) Color.Gray else Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = if (language == AppLanguage.BN) "লগআউট" else "Logout",
                            color = FintechRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    Toast.makeText(context, if (language == AppLanguage.BN) "গুগল ড্রাইভ থেকে সফলভাবে লগআউট করা হয়েছে!" else "Successfully logged out of Google Drive!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(4.dp)
                        )
                    }
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "আপনার সমস্ত বেচাকেনা ও ক্রেতার তথ্য সম্পূর্ণ নিরাপদ রাখতে সরাসরি গুগল ড্রাইভে ব্যাকআপ রাখুন।" 
                            else "To keep all your transaction and customer data safe, backup directly to Google Drive.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                                        Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, if (language == AppLanguage.BN) "ক্লাউড ব্যাকআপ বর্তমানে উন্নয়নাধীন!" else "Cloud Backup is currently under development!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "ক্লাউড ব্যাকআপ" else "Cloud Backup", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = {
                                Toast.makeText(context, if (language == AppLanguage.BN) "ক্লাউড রিস্টোর বর্তমানে উন্নয়নাধীন!" else "Cloud Restore is currently under development!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E2235) else Color(0xFFE2E8F0)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1))
                        ) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "ক্লাউড রিস্টোর" else "Cloud Restore", fontSize = 11.sp, color = FintechBlue)
                        }
                    }
                }
            }
        }

        // --- 5. APP ERROR LOG (SUPPORT) CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপ এর লগ (সাপোর্ট)" else "App Error Log (Support)",
            isDark = isDark,
            icon = Icons.Rounded.BugReport
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BN) 
                        "অ্যাপ ব্যবহারে কোনো কারিগরি সমস্যা হলে নিচের বাটনে ক্লিক করে এরর লগগুলো আমাদের ইমেইল বা হোয়াটসঅ্যাপে পাঠাতে পারেন। এটি আমাদের সমস্যা সমাধানে সাহায্য করবে।" 
                        else "If you encounter any technical issues while using the app, you can check the error logs and send them to our email or WhatsApp. This helps us solve the problem.",
                    fontSize = 12.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Button(
                    onClick = {
                        Toast.makeText(context, if (language == AppLanguage.BN) "কোনো এরর লগ পাওয়া যায়নি!" else "No error logs found!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                ) {
                    Icon(Icons.Rounded.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "এরর রিপোর্ট চেক করুন" else "Check Error Report",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- 6. ABOUT DEVELOPER CARD ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Row 1: Profile Avatar and Title/Subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Shariful Islam",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == AppLanguage.BN) "ইউজার এক্সপেরিয়েন্স ও অ্যাপ ডেভেলপার" else "User Experience & App Developer",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Row 2: List Items with chevrons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Item 1: Facebook
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://facebook.com/shariful.uxd"))
                                context.startActivity(intent)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("facebook.com/shariful.uxd", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    // Item 2: Mobile / WhatsApp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:01768899599"))
                                context.startActivity(intent)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("01768899599", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    // Item 3: Email
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:connect.shariful@gmail.com")
                                }
                                context.startActivity(intent)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("connect.shariful@gmail.com", color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Row 3: Contact/Mega button
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/8801768899599"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(Icons.Rounded.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "নতুন আপডেট পেতে সরাসরি আমাদের সাথে হোয়াটসঅ্যাপে যোগাযোগ করতে এখানে চাপুন" 
                            else "Tap here to contact us directly on WhatsApp to get new updates",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 7. APP INFO CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপ ইনফো" else "App Info",
            isDark = isDark,
            icon = Icons.Rounded.Info
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "অ্যাপের নাম" else "App Name",
                        fontSize = 14.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "ফাইন্যান্স নোট (Finance Note)" else "Finance Note",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }
                
                HorizontalDivider(color = if (isDark) Color(0xFF1E2235) else Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "সংস্করণ" else "Version",
                        fontSize = 14.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                    Text(
                        text = "1.3.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }

        // --- 8. PRIVACY & TERMS CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "প্রাইভেসি ও শর্তাবলী" else "Privacy & Terms",
            isDark = isDark,
            icon = Icons.Rounded.Security
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Item 1: Privacy Policy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://google.com"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "প্রাইভেসি পলিসি" else "Privacy Policy",
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = if (isDark) Color.Gray else Color.DarkGray, modifier = Modifier.size(16.dp))
                }

                HorizontalDivider(color = if (isDark) Color(0xFF1E2235) else Color(0xFFE2E8F0))

                // Item 2: Terms of Use
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://google.com"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "ব্যবহারের শর্তাবলী" else "Terms of Use",
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = if (isDark) Color.Gray else Color.DarkGray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // --- 9. BOTTOM SPONSORED AD CARD ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF141724) else Color.White)
                .border(1.dp, if (isDark) Color(0xFF1F2937) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.BN) "📢 স্পন্সরড বিজ্ঞাপন" else "📢 Sponsored Ad",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.Gray else Color(0xFF64748B)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(FintechBlue.copy(alpha = 0.15f))
                    .clickable {
                        Toast.makeText(context, if (language == AppLanguage.BN) "বিজ্ঞাপন মুক্ত করুন!" else "Free Ads!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BN) "মুক্ত বিজ্ঞাপন" else "Free Ad",
                    color = FintechBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}



@Composable
fun ChartsScreen(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>
) {
    val totalIncome = transactions.filter { it.type == "INCOME" || it.type == "BORROW" || it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }.sumOf { it.amount }
    val totalDena = transactions.filter { it.type == "BORROW" }.sumOf { it.amount } - transactions.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
    val totalPawn = transactions.filter { it.type == "LEND" }.sumOf { it.amount } - transactions.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }

    val values = listOf(
        maxOf(0.0, totalIncome).toFloat(),
        maxOf(0.0, totalExpense).toFloat(),
        maxOf(0.0, totalDena).toFloat(),
        maxOf(0.0, totalPawn).toFloat()
    )
    val colors = listOf(Color(0xFF4CAF50), Color(0xFFF44336), Color(0xFFFF9800), Color(0xFF2196F3))
    val labels = listOf(
        if (language == AppLanguage.BN) "আয়" else "Income",
        if (language == AppLanguage.BN) "ব্যয়" else "Expense",
        if (language == AppLanguage.BN) "দেনা" else "Debt",
        if (language == AppLanguage.BN) "পাওনা" else "Credit"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FintechGradientCard(
            gradientColors = listOf(Color(0xFF1E222F), Color(0xFF2A2E3D)),
            cornerRadius = 24.dp,
            padding = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (language == AppLanguage.BN) "আর্থিক সারাংশ" else "Financial Summary",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(32.dp))

                val total = values.sum()
                if (total == 0f) {
                    Text(
                        text = Translation.get("no_tx", language),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                } else {
                    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            values.forEachIndexed { index, value ->
                                if (value > 0) {
                                    val sweepAngle = (value / total) * 360f
                                    drawArc(
                                        color = colors[index],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 50f, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (language == AppLanguage.BN) "মোট" else "Total",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatCurrency(total.toDouble(), language),
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

        FintechGradientCard(
            gradientColors = listOf(Color(0xFF1E222F), Color(0xFF2A2E3D)),
            cornerRadius = 24.dp,
            padding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                values.forEachIndexed { index, value ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(colors[index]))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = labels[index], color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        }
                        Text(text = formatCurrency(value.toDouble(), language), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(isDark: Boolean) {
    val gradientBrush = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF0F121F), Color(0xFF141724)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFF3F8CFF), Color(0xFF2563EB)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(36.dp))
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.White)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "Finance Note Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Finance Note",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Your Personal Money Manager",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
