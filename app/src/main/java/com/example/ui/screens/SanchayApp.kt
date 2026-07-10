package com.example.ui.screens

import com.example.BuildConfig
import com.example.R
import android.content.Context
import android.content.Intent
import android.os.Build
import android.net.Uri
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.ui.res.painterResource
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
                Column {
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
                Column {
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

                Column {
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
        val date = sdf.parse(dateStr) ?: return dateStr
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
fun FinanceNoteApp(viewModel: FinanceViewModel, initialAction: String? = null) {
    val language by viewModel.language.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    
    val rawProfileName by viewModel.profileName.collectAsState()
    val rawProfileEmail by viewModel.profileEmail.collectAsState()
    val rawProfilePhotoUri by viewModel.profilePhotoUri.collectAsState()

    val googleName by viewModel.googleName.collectAsState()
    val googleEmail by viewModel.googleEmail.collectAsState()
    val googlePhotoUrl by viewModel.googlePhotoUrl.collectAsState()
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()

    val profileName = if (isGoogleSignedIn) (googleName ?: rawProfileName) else rawProfileName
    val profileEmail = if (isGoogleSignedIn) (googleEmail ?: rawProfileEmail) else rawProfileEmail
    val profilePhotoUri = if (isGoogleSignedIn) (googlePhotoUrl ?: rawProfilePhotoUri) else rawProfilePhotoUri

    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val finalClientId = if (BuildConfig.GOOGLE_CLIENT_ID.isNotEmpty()) {
            BuildConfig.GOOGLE_CLIENT_ID
        } else {
            BuildConfig.DRIVE_API
        }
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val authCode = account?.serverAuthCode
            if (authCode != null) {
                viewModel.exchangeCodeForTokens(
                    context = context,
                    authCode = authCode,
                    clientId = finalClientId,
                    onSuccess = {
                        Toast.makeText(context, if (language == AppLanguage.BN) "গুগল ড্রাইভ কানেক্ট সফল হয়েছে!" else "Google Drive connected successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "${if (language == AppLanguage.BN) "কানেক্ট ব্যর্থ হয়েছে: " else "Connection failed: "}$err", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(context, if (language == AppLanguage.BN) "সাইন ইন কোড পাওয়া যায়নি।" else "Sign-in code not received.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Unknown error"
            Toast.makeText(context, "${if (language == AppLanguage.BN) "সাইন ইন ব্যর্থ হয়েছে: " else "Sign-in failed: "}$errMsg", Toast.LENGTH_LONG).show()
        }
    }

    val triggerGoogleSignIn = {
        val finalClientId = if (BuildConfig.GOOGLE_CLIENT_ID.isNotEmpty()) {
            BuildConfig.GOOGLE_CLIENT_ID
        } else {
            BuildConfig.DRIVE_API
        }
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .requestProfile()
            .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.file"))
            .requestServerAuthCode(finalClientId)
            .build()

        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
        
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

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

    var activeTab by remember(initialAction) {
        mutableStateOf(
            when (initialAction) {
                "ACTION_DEBT_CREDIT", "ACTION_VIEW_DEBT", "ACTION_VIEW_CREDIT" -> "debts"
                "ACTION_SAVINGS" -> "savings"
                "ACTION_SETTINGS", "ACTION_BACKUP" -> "settings"
                "ACTION_CHARTS" -> "charts"
                else -> "dashboard"
            }
        )
    }
    var transactionFilter by remember(initialAction) { 
        mutableStateOf(
            when(initialAction) {
                "ACTION_VIEW_INCOME" -> "INCOME"
                "ACTION_VIEW_EXPENSE" -> "EXPENSE"
                else -> "ALL"
            }
        )
    }
    var debtFilter by remember(initialAction) { 
        mutableStateOf(
            when(initialAction) {
                "ACTION_VIEW_DEBT" -> "DEBT"
                "ACTION_VIEW_CREDIT" -> "CREDIT"
                else -> "ALL"
            }
        )
    }

    // Dialog & overlay states
    val composeCoroutineScope = rememberCoroutineScope()
    var showAddTransactionDialog by remember(initialAction) { mutableStateOf(initialAction == "ACTION_ADD_TRANSACTION") }
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showAddSavingsGoalDialog by remember { mutableStateOf(false) }
    var showSavingsContributionDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var isWithdrawMode by remember { mutableStateOf(false) }
    var selectedPersonDetail by remember { mutableStateOf<PersonDebt?>(null) }
    var selectedSavingsGoalDetail by remember { mutableStateOf<SavingsGoal?>(null) }
    var goalToEdit by remember { mutableStateOf<SavingsGoal?>(null) }

    var showExitConfirm by remember { mutableStateOf(false) }
    var highlightedTxId by remember { mutableStateOf<Int?>(null) }
    var highlightedPersonId by remember { mutableStateOf<Int?>(null) }
    var highlightedGoalId by remember { mutableStateOf<Int?>(null) }

    var showSearch by remember { mutableStateOf(false) }

    val driveStatusMessage by viewModel.driveStatusMessage.collectAsState()
    val googleDriveFiles by viewModel.googleDriveFiles.collectAsState()
    val isFetchingFiles by viewModel.isFetchingFiles.collectAsState()


    var showBackupConfirm by remember(initialAction) { mutableStateOf(initialAction == "ACTION_BACKUP") }
    var cloudBackupStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }
    var showRestoreListDialog by remember { mutableStateOf(false) }
    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(if (language == AppLanguage.BN) "অ্যাপ থেকে বের হবেন?" else "Exit App?") },
            text = { Text(if (language == AppLanguage.BN) "আপনি কি নিশ্চিত যে অ্যাপ থেকে বের হতে চান?" else "Are you sure you want to exit the app?") },
            confirmButton = {
                Button(onClick = { (context as? android.app.Activity)?.finish() }) {
                    Text(if (language == AppLanguage.BN) "হ্যাঁ" else "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text(if (language == AppLanguage.BN) "না" else "No")
                }
            }
        )
    }

    if (showSearch) {
        SearchDialog(
            onDismissRequest = { showSearch = false },
            language = language,
            isDark = isDarkTheme,
            transactions = transactions,
            persons = persons,
            savingsGoals = savingsGoals,
            onNavigateToTransaction = { 
                activeTab = "transactions"
                highlightedTxId = it
                showSearch = false
            },
            onNavigateToPerson = { 
                activeTab = "debts"
                highlightedPersonId = it
                showSearch = false
            },
            onNavigateToGoal = { 
                activeTab = "savings"
                highlightedGoalId = it
                showSearch = false
            }
        )
    }

    // Back handling for overlays and settings
    androidx.activity.compose.BackHandler(
        enabled = true
    ) {
        when {
            selectedPersonDetail != null -> selectedPersonDetail = null
            selectedSavingsGoalDetail != null -> selectedSavingsGoalDetail = null
            activeTab != "dashboard" -> activeTab = "dashboard"
            else -> showExitConfirm = true
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
                                activeTab == "dashboard" -> Translation.get("dashboard", language)
                                activeTab == "transactions" -> Translation.get("transactions", language)
                                activeTab == "debts" -> Translation.get("debts", language)
                                activeTab == "savings" -> if (language == AppLanguage.BN) "সঞ্চয় কার্ড" else "Savings Card"
                                activeTab == "settings" -> Translation.get("settings", language)
                                activeTab == "charts" -> if (language == AppLanguage.BN) "রিপোর্ট চার্ট" else "Report Chart"
                                else -> Translation.get("dashboard", language)
                            }
                            
                            AnimatedContent(
                                targetState = screenTitle,
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn())
                                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                                },
                                label = "TitleSlideTransition"
                            ) { title ->
                                Text(
                                    text = title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { showSearch = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

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
                        .padding(horizontal = 8.dp, vertical = 0.dp)
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
                                .offset(y = (-20).dp)
                                .size(72.dp)
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
                                    .size(58.dp)
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
                                    modifier = Modifier.size(26.dp)
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
                                    onBack = { activeTab = "dashboard" },
                                    onSignInClick = { triggerGoogleSignIn() },
                                    onBackupClick = {
                                        composeCoroutineScope.launch {
                                            val backupData = viewModel.getCurrentDatabaseBackup()
                                            val stats = viewModel.calculateBackupStats(backupData)
                                            cloudBackupStats = stats
                                            showBackupConfirm = true
                                        }
                                    },
                                    onRestoreClick = {
                                        showRestoreListDialog = true
                                        viewModel.listGoogleDriveFiles(context)
                                    }
                                )
                            } else if (tabState == "charts") {
                                ChartsScreen(
                                    language = language,
                                    isDark = isDarkTheme,
                                    transactions = transactions,
                                    persons = persons,
                                    onBack = { activeTab = "dashboard" }
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
                                        pagerState.animateScrollToPage(page)
                                    }
                                }

                                // Sync Pager settled page to activeTab
                                LaunchedEffect(pagerState.settledPage) {
                                    if (!pagerState.isScrollInProgress) {
                                        val tab = when (pagerState.settledPage) {
                                            0 -> "dashboard"
                                            1 -> "transactions"
                                            2 -> "debts"
                                            3 -> "savings"
                                            else -> "dashboard"
                                        }
                                        if (activeTab != tab && (activeTab == "dashboard" || activeTab == "transactions" || activeTab == "debts" || activeTab == "savings")) {
                                            activeTab = tab
                                        }
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
                                            onTimeFilterChange = handleTimeFilterChange,
                                            isGoogleSignedIn = isGoogleSignedIn,
                                            onSignInClick = { triggerGoogleSignIn() },
                                            onBackupClick = {
                                                composeCoroutineScope.launch {
                                                    val backupData = viewModel.getCurrentDatabaseBackup()
                                                    val stats = viewModel.calculateBackupStats(backupData)
                                                    cloudBackupStats = stats
                                                    showBackupConfirm = true
                                                }
                                            },
                                            viewModel = viewModel
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
                        initialPerson = editingPerson,
                        language = language,
                        isDark = isDarkTheme,
                        onDismiss = { 
                            showAddPersonDialog = false
                            editingPerson = null
                        },
                        onConfirm = { name, phone, address, photoUri ->
                            if (editingPerson != null) {
                                viewModel.updatePerson(editingPerson!!.copy(name = name, phone = phone, address = address, photoUri = photoUri))
                            } else {
                                viewModel.addPerson(name, phone, address, photoUri)
                            }
                            showAddPersonDialog = false
                            editingPerson = null
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
                AnimatedVisibility(
                    visible = selectedSavingsGoalDetail != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val goalSnapshot = selectedSavingsGoalDetail
                    if (goalSnapshot != null) {
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
                }

                // Detailed view for transactions with a specific person
                AnimatedVisibility(
                    visible = selectedPersonDetail != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val debtInfoSnapshot = selectedPersonDetail
                    if (debtInfoSnapshot != null) {
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
                            onDeleteTx = { viewModel.deleteTransaction(it) },
                            onEditPerson = {
                                editingPerson = it
                                showAddPersonDialog = true
                            },
                            onDeletePerson = {
                                viewModel.deletePerson(debtInfo.person.id)
                                selectedPersonDetail = null
                            }
                        )
                    }
                }



                if (showBackupConfirm && cloudBackupStats != null) {
                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                    val defaultName = "finance_note_backup_$timestamp.json"

                    BackupStatsDialog(
                        title = if (language == AppLanguage.BN) "ক্লাউড ব্যাকআপ সামারি" else "Cloud Backup Summary",
                        stats = cloudBackupStats!!,
                        language = language,
                        isDark = isDarkTheme,
                        isRestoreMode = false,
                        initialFileName = defaultName,
                        onConfirm = { finalFileName, comment ->
                            showBackupConfirm = false
                            viewModel.backupToGoogleDrive(
                                context = context,
                                customFileName = finalFileName,
                                comment = comment,
                                onSuccess = {
                                    Toast.makeText(context, if (language == AppLanguage.BN) "ড্রাইভ ব্যাকআপ সফল হয়েছে!" else "Drive Backup successful!", Toast.LENGTH_SHORT).show()
                                    viewModel.listGoogleDriveFiles(context)
                                },
                                onError = { err ->
                                    Toast.makeText(context, "${if (language == AppLanguage.BN) "ব্যাকআপ ব্যর্থ হয়েছে: " else "Backup failed: "}$err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onDismiss = { showBackupConfirm = false }
                    )
                }

                if (showRestoreListDialog) {
                    GoogleDriveRestoreListDialog(
                        language = language,
                        isDark = isDarkTheme,
                        isFetching = isFetchingFiles,
                        files = googleDriveFiles,
                        viewModel = viewModel,
                        onDismiss = { showRestoreListDialog = false },
                        onRestoreSuccess = {
                            Toast.makeText(context, if (language == AppLanguage.BN) "ড্রাইভ থেকে রিস্টোর সফল হয়েছে!" else "Drive Restore successful!", Toast.LENGTH_SHORT).show()
                            showRestoreListDialog = false
                        },
                        onDelete = { fileId ->
                            viewModel.deleteGoogleDriveFile(
                                context = context,
                                fileId = fileId,
                                onSuccess = {
                                    Toast.makeText(context, if (language == AppLanguage.BN) "ফাইলটি সফলভাবে ডিলিট করা হয়েছে!" else "File deleted successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, "${if (language == AppLanguage.BN) "ডিলিট ব্যর্থ হয়েছে: " else "Delete failed: "}$err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        onRefresh = {
                            viewModel.listGoogleDriveFiles(context)
                        }
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
    onTimeFilterChange: (String) -> Unit = {},
    isGoogleSignedIn: Boolean = false,
    onSignInClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    viewModel: FinanceViewModel? = null
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel?.let { vm ->
                if (!vm.isNotificationEnabled.value) {
                    vm.toggleNotification(context)
                }
            }
        } else {
            // Permission denied, ensure notification is disabled if it was first launch attempt
            viewModel?.let { vm ->
                if (vm.isNotificationEnabled.value) {
                    vm.toggleNotification(context)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        val hasAskedNotif = prefs.getBoolean("has_asked_notification_permission", false)
        
        if (!hasAskedNotif && Build.VERSION.SDK_INT >= 33) {
            prefs.edit().putBoolean("has_asked_notification_permission", true).apply()
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else if (!hasAskedNotif) {
            // Below Android 13, permission is granted by default, but we should still track that we "asked" or initialized
            prefs.edit().putBoolean("has_asked_notification_permission", true).apply()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card (Fintech Gradient Card styled beautifully with the same indigo-fuchsia gradient)
        item {
            FintechGradientCard(
                gradientColors = GradientsList[0],
                cornerRadius = 24.dp,
                modifier = Modifier
                    .testTag("dashboard_profile_card")
                    .padding(top = 8.dp)
                    .clickable { 
                        if (isGoogleSignedIn) {
                            onNavigate("settings", "")
                        } else {
                            onSignInClick()
                        }
                    }
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
                        if (!isGoogleSignedIn) {
                            Icon(
                                imageVector = Icons.Rounded.Cloud,
                                contentDescription = "Sign in to backup",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (profilePhotoUri != null) {
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
                            text = if (isGoogleSignedIn) {
                                if (profileName.isNotBlank()) profileName else (if (language == AppLanguage.BN) "ব্যবহারকারী" else "User")
                            } else {
                                if (language == AppLanguage.BN) "সাইন-ইন করুন" else "Sign In"
                            },
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isGoogleSignedIn) {
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
                        } else {
                            Text(
                                text = if (language == AppLanguage.BN) "ব্যাকআপ ও এডিটের জন্য গুগল সাইন-ইন করুন" else "Sign in with Google to backup & edit profile",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isGoogleSignedIn) {
                            // Cloud Sync Button
                            IconButton(
                                onClick = { onBackupClick() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudUpload,
                                    contentDescription = if (language == AppLanguage.BN) "ক্লাউড সিঙ্ক" else "Cloud Sync",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Google Sign In Button - shows Sign In icon
                            IconButton(
                                onClick = { onSignInClick() },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Login,
                                    contentDescription = if (language == AppLanguage.BN) "গুগল সাইন-ইন" else "Google Sign-In",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Balance Card (Fintech Gradient Card with sleek styling and beautifully integrated debts/loans cards)
        item {
            FintechGradientCard(
                gradientColors = GradientsList[0], // Sleek Indigo-Violet-Fuchsia Gradient
                cornerRadius = 32.dp,
                padding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
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
                            modifier = Modifier.padding(vertical = 2.dp)
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
                            painter = painterResource(id = R.drawable.ic_pie_chart),
                            contentDescription = "Charts",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trending_up),
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
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
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
                val bgColor by androidx.compose.animation.animateColorAsState(if (isDark) Color(0xFF141724) else Color.White)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
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
    onDelete: (Int) -> Unit,
    isHighlighted: Boolean = false
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val linkedPerson = persons.find { it.id == tx.personId }
    val context = LocalContext.current

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

    if (showDetails) {
        TransactionDetailsDialog(
            tx = tx,
            language = language,
            isDark = isDark,
            linkedPerson = linkedPerson,
            onDismiss = { showDetails = false },
            onDelete = {
                showDetails = false
                showDeleteConfirm = true
            },
            onShare = {
                val shareText = buildString {
                    append(if (language == AppLanguage.BN) "মানি রসিদ\n" else "Money Receipt\n")
                    append("-------------------\n")
                    append(if (language == AppLanguage.BN) "ধরণ: " else "Type: ").append(tx.type).append("\n")
                    append(if (language == AppLanguage.BN) "পরিমাণ: " else "Amount: ").append(formatCurrency(tx.amount, language)).append("\n")
                    append(if (language == AppLanguage.BN) "ক্যাটাগরি: " else "Category: ").append(tx.category).append("\n")
                    if (linkedPerson != null) {
                        append(if (language == AppLanguage.BN) "ব্যক্তি: " else "Person: ").append(linkedPerson.name).append("\n")
                    }
                    if (tx.note.isNotEmpty()) {
                        append(if (language == AppLanguage.BN) "নোট: " else "Note: ").append(tx.note).append("\n")
                    }
                    append(if (language == AppLanguage.BN) "তারিখ: " else "Date: ").append(formatDate(tx.timestamp, language)).append("\n")
                    append("-------------------\n")
                    append(if (language == AppLanguage.BN) "সঞ্চয় অ্যাপ থেকে শেয়ার করা হয়েছে" else "Shared via Sanchay App")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, if (language == AppLanguage.BN) "শেয়ার করুন" else "Share via"))
            }
        )
    }

    val bgColor by androidx.compose.animation.animateColorAsState(if (isHighlighted) (if (isDark) Color(0xFF453A1E) else Color(0xFFFEF3C7)) else (if (isDark) Color(0xFF141724) else Color.White))
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showDetails = true },
                onLongClick = { showDeleteConfirm = true }
            )
            .testTag("tx_item_${tx.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp),
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

// ---------------- TRANSACTION DETAILS DIALOG ----------------
@Composable
fun TransactionDetailsDialog(
    tx: Transaction,
    language: AppLanguage,
    isDark: Boolean,
    linkedPerson: Person?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val bgColor = if (isDark) Color(0xFF1E2235) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subtitleColor = if (isDark) Color.LightGray else Color(0xFF64748B)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.BN) "ট্রানজ্যাকশন মেমো" else "Transaction Memo",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isDark) Color(0xFF141724) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Detail Row Helper
                val DetailRow = @Composable { label: String, value: String ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = subtitleColor, fontSize = 13.sp)
                        Text(text = value, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                val typeStr = when (tx.type) {
                    "INCOME" -> if (language == AppLanguage.BN) "আয়" else "Income"
                    "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয়" else "Expense"
                    "LEND" -> if (language == AppLanguage.BN) "ধার দেওয়া" else "Lend"
                    "BORROW" -> if (language == AppLanguage.BN) "ধার নেওয়া" else "Borrow"
                    "REPAY_PAID" -> if (language == AppLanguage.BN) "পরিশোধ" else "Repay Paid"
                    "REPAY_RECEIVED" -> if (language == AppLanguage.BN) "প্রাপ্তি" else "Repay Received"
                    else -> tx.type
                }
                
                DetailRow(if (language == AppLanguage.BN) "ধরণ:" else "Type:", typeStr)
                DetailRow(if (language == AppLanguage.BN) "পরিমাণ:" else "Amount:", formatCurrency(tx.amount, language))
                DetailRow(if (language == AppLanguage.BN) "ক্যাটাগরি:" else "Category:", tx.category)
                if (linkedPerson != null) {
                    DetailRow(if (language == AppLanguage.BN) "ব্যক্তি:" else "Person:", linkedPerson.name)
                }
                DetailRow(if (language == AppLanguage.BN) "তারিখ:" else "Date:", formatDate(tx.timestamp, language))
                
                if (tx.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = subtitleColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = if (language == AppLanguage.BN) "নোট:" else "Note:", color = subtitleColor, fontSize = 13.sp)
                    Text(text = tx.note, color = textColor, fontSize = 14.sp)
                }
            }
        },
        containerColor = bgColor,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = FintechBlue)
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text(if (language == AppLanguage.BN) "বন্ধ করুন" else "Close", color = FintechBlue)
                }
            }
        }
    )
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
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_income", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalIncome, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_expense", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalExpense, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
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
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_dena", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalDena, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_pawn", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalPawn, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
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
    onDelete: (Int) -> Unit,
    isHighlighted: Boolean = false
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

    val bgColor by androidx.compose.animation.animateColorAsState(if (isHighlighted) (if (isDark) Color(0xFF453A1E) else Color(0xFFFEF3C7)) else (if (isDark) Color(0xFF141724) else Color.White))
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
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
                .padding(horizontal = 22.dp, vertical = 10.dp),
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
            .height(170.dp)
            .combinedClickable(
                onClick = { onGoalClick(goal) },
                onLongClick = { onEditGoal(goal) }
            )
            .testTag("savings_item_${goal.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mastercard Chip and Contactless Icon
            Column(modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 12.dp)) {
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
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp).rotate(90f)
                )
            }


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
                goal.category.uppercase()
            }

            Text(
                text = formattedCategory,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp)
            )

            // Goal Title and Balance in the bottom-left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = goal.title.uppercase(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                
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
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = goal.cardholderName.ifBlank { profileName }.uppercase(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Bottom row: Mastercard-like logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, bottom = 8.dp)
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {

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
                if (isPersonRequired) {
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
    initialPerson: Person? = null,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialPerson?.name ?: "") }
    var phone by remember { mutableStateOf(initialPerson?.phone ?: "") }
    var address by remember { mutableStateOf(initialPerson?.address ?: "") }
    var photoUri by remember { mutableStateOf(initialPerson?.photoUri ?: "") }
    val context = LocalContext.current

    val dialogBg = if (isDark) Color(0xFF141724) else Color.White
    val textColor = if (isDark) Color.White else Color.Black
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
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
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
                                onConfirm(name.trim().uppercase(), trimmedPhone, address.trim(), photoUri.trim())
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
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
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
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
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
                                    onConfirm(title.trim().uppercase(), target, finalSector, colorIndex, cardholderName.trim().uppercase())
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
                Text(
                    text = if (language == AppLanguage.BN) "সঞ্চয় কার্ডের বিস্তারিত" else "Savings Card Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechBlue,
                    modifier = Modifier.align(Alignment.CenterStart)
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
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color.White else Color.Black)
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

                        val bgColor by androidx.compose.animation.animateColorAsState(if (isDark) Color(0xFF141724) else Color.White)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
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
    onDeleteTx: (Int) -> Unit,
    onEditPerson: (Person) -> Unit,
    onDeletePerson: () -> Unit
) {
    val txList by transactionsFlow.collectAsState(initial = emptyList())
    var showActionSheet by remember { mutableStateOf<String?>(null) } // "LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED"
    var actionAmountStr by remember { mutableStateOf("") }
    var actionNoteStr by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("person_detail_overlay"),
        color = MaterialTheme.colorScheme.surface
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

                    Column(modifier = Modifier.weight(1f)) {
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
                    
                    Row {
                        IconButton(onClick = { onEditPerson(personDebt.person) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray)
                        }
                        IconButton(onClick = { onDeletePerson() }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Net Balance Summary Box inside Card
                val (statusText, statusColor, absoluteAmount) = when {
                    personDebt.netBalance > 0 -> Triple(Translation.get("you_get", language), FintechGreen, personDebt.netBalance)
                    personDebt.netBalance < 0 -> Triple(Translation.get("you_owe", language), FintechRed, -personDebt.netBalance)
                    else -> Triple(Translation.get("settled", language), Color.Gray, 0.0)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF141724) else Color(0xFFF3F4F6))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

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

                val overlayTextColor = if (isDark) Color.White else Color.Black
                val overlayLabelColor = if (isDark) Color.Gray else Color(0xFF64748B)

                Dialog(onDismissRequest = { showActionSheet = null }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                color = overlayTextColor,
                                fontSize = 16.sp
                            )

                            OutlinedTextField(
                                value = actionAmountStr,
                                onValueChange = { actionAmountStr = it },
                                label = { Text(Translation.get("amount", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = overlayLabelColor,
                                    focusedTextColor = overlayTextColor,
                                    unfocusedTextColor = overlayTextColor,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = overlayLabelColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = actionNoteStr,
                                onValueChange = { actionNoteStr = it },
                                label = { Text(if (language == AppLanguage.BN) "মন্তব্য যোগ করুন" else "Add Comment") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = overlayLabelColor,
                                    focusedTextColor = overlayTextColor,
                                    unfocusedTextColor = overlayTextColor,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = overlayLabelColor
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
        Column {
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
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
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
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = if (isDark) Color.LightGray else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
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
}

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onBack: () -> Unit,
    onSignInClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    val context = LocalContext.current
    val localCoroutineScope = rememberCoroutineScope()
    val profileName by viewModel.profileName.collectAsState()
    val profileEmail by viewModel.profileEmail.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()
    val profilePhone by viewModel.profilePhone.collectAsState()
    val profileSocial by viewModel.profileSocial.collectAsState()
    val profileAddress by viewModel.profileAddress.collectAsState()

    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
    val googleName by viewModel.googleName.collectAsState()
    val googleEmail by viewModel.googleEmail.collectAsState()
    val driveStatusMessage by viewModel.driveStatusMessage.collectAsState()

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showErrorLogDialog by remember { mutableStateOf(false) }
    var currentLogsText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.initGoogleDrive(context)
    }

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
        if (isGoogleSignedIn) {
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
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
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
                                    if (language == AppLanguage.BN) "দয়া করে নাম লিখুন" else "Please enter your name",
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
        }

        // --- 2. DARK MODE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ডার্ক মোড" else "Dark Mode",
            isDark = isDark,
            icon = Icons.Rounded.NightsStay,
            initiallyExpanded = true
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
                Column {
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

        // --- 3. NOTIFICATION WIDGET CARD ---
        val notificationEnabled by viewModel.isNotificationEnabled.collectAsState()
        val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.toggleNotification(context)
            } else {
                Toast.makeText(context, if (language == AppLanguage.BN) "নটিফিকেশন পারমিশন প্রয়োজন" else "Notification permission required", Toast.LENGTH_SHORT).show()
            }
        }

        SettingCategory(
            title = if (language == AppLanguage.BN) "কুইক একশন উইজেট" else "Quick Action Widget",
            isDark = isDark,
            icon = Icons.Rounded.NotificationsActive,
            initiallyExpanded = true
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(FintechBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartButton,
                        contentDescription = null,
                        tint = FintechBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.BN) "নটিফিকেশন বার উইজেট" else "Notification Bar Widget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "দ্রুত একশন বাটনগুলো নটিফিকেশন বারে দেখান" else "Show quick action buttons in notification bar",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { 
                        if (!notificationEnabled && android.os.Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleNotification(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FintechBlue,
                        uncheckedThumbColor = if (isDark) Color.Gray else Color.White,
                        uncheckedTrackColor = if (isDark) Color(0xFF2A2E42) else Color(0xFFE2E8F0)
                    )
                )
            }
        }

        // --- 4. APP LANGUAGE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপের ভাষা / App Language" else "App Language",
            isDark = isDark,
            icon = Icons.Rounded.Language,
            initiallyExpanded = true
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

        // --- 5. DATA BACKUP & RESTORE CARD ---
        var pendingLocalBackupComment by remember { mutableStateOf("") }
        var pendingLocalRestoreData by remember { mutableStateOf<com.example.data.FinanceBackup?>(null) }
        var pendingLocalRestoreStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }
        var pendingLocalRestoreFileName by remember { mutableStateOf("") }
        var pendingLocalRestoreJson by remember { mutableStateOf("") }
        var showLocalBackupStatsDialog by remember { mutableStateOf(false) }
        var currentDbStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }

        fun getFileNameFromUri(context: android.content.Context, uri: android.net.Uri): String? {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            result = cursor.getString(index)
                        }
                    }
                } finally {
                    cursor?.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != null && cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
            return result
        }

        val createDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.let { outputStream ->
                    viewModel.exportBackupToUri(context, outputStream, pendingLocalBackupComment, {
                        Toast.makeText(context, if (language == AppLanguage.BN) "ব্যাকআপ সফলভাবে সেভ হয়েছে!" else "Backup successfully saved!", Toast.LENGTH_SHORT).show()
                        pendingLocalBackupComment = ""
                    }, { error ->
                        Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }

        val openDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.let { inputStream ->
                    try {
                        val jsonContent = inputStream.use { it.bufferedReader().readText() }
                        val parsed = viewModel.parseBackupJson(jsonContent)
                        if (parsed != null) {
                            pendingLocalRestoreData = parsed
                            pendingLocalRestoreStats = viewModel.calculateBackupStats(parsed)
                            pendingLocalRestoreFileName = getFileNameFromUri(context, uri) ?: "Local Backup"
                            pendingLocalRestoreJson = jsonContent
                        } else {
                            Toast.makeText(context, if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Render Local Backup/Restore summary dialogs if active
        if (showLocalBackupStatsDialog && currentDbStats != null) {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val defaultName = "financenote_backup_$timestamp.json"

            BackupStatsDialog(
                title = if (language == AppLanguage.BN) "লোকাল ব্যাকআপ সামারি" else "Local Backup Summary",
                stats = currentDbStats!!,
                language = language,
                isDark = isDark,
                isRestoreMode = false,
                initialFileName = defaultName,
                onConfirm = { finalFileName, comment ->
                    showLocalBackupStatsDialog = false
                    pendingLocalBackupComment = comment
                    createDocumentLauncher.launch(finalFileName)
                },
                onDismiss = { showLocalBackupStatsDialog = false }
            )
        }

        if (pendingLocalRestoreStats != null && pendingLocalRestoreData != null) {
            BackupStatsDialog(
                title = if (language == AppLanguage.BN) "লোকাল রিস্টোর সামারি" else "Local Restore Summary",
                stats = pendingLocalRestoreStats!!,
                language = language,
                isDark = isDark,
                isRestoreMode = true,
                initialFileName = pendingLocalRestoreFileName,
                onConfirm = { _, _ ->
                    viewModel.importBackup(
                        context = context,
                        json = pendingLocalRestoreJson,
                        fromLocalFile = false,
                        onSuccess = {
                            pendingLocalRestoreStats = null
                            pendingLocalRestoreData = null
                            pendingLocalRestoreJson = ""
                            Toast.makeText(context, if (language == AppLanguage.BN) "ব্যাকআপ সফলভাবে রিস্টোর হয়েছে!" else "Backup successfully restored!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onDismiss = {
                    pendingLocalRestoreStats = null
                    pendingLocalRestoreData = null
                    pendingLocalRestoreJson = ""
                }
            )
        }

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
                                localCoroutineScope.launch {
                                    val backupData = viewModel.getCurrentDatabaseBackup()
                                    val stats = viewModel.calculateBackupStats(backupData)
                                    currentDbStats = stats
                                    showLocalBackupStatsDialog = true
                                }
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
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "application/octet-stream"))
                            },
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
                                try {
                                    val backupFile = java.io.File(context.filesDir, "financenote_backup.json")
                                    if (backupFile.exists()) {
                                        val jsonContent = backupFile.readText()
                                        val parsed = viewModel.parseBackupJson(jsonContent)
                                        if (parsed != null) {
                                            pendingLocalRestoreData = parsed
                                            pendingLocalRestoreStats = viewModel.calculateBackupStats(parsed)
                                            pendingLocalRestoreFileName = "financenote_backup.json"
                                            pendingLocalRestoreJson = jsonContent
                                            showPasteArea = false
                                        } else {
                                            Toast.makeText(context, if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, if (language == AppLanguage.BN) "কোনো সেভ করা ব্যাকআপ পাওয়া যায়নি!" else "No saved backup found!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
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
                                    try {
                                        val parsed = viewModel.parseBackupJson(pasteJsonInput)
                                        if (parsed != null) {
                                            pendingLocalRestoreData = parsed
                                            pendingLocalRestoreStats = viewModel.calculateBackupStats(parsed)
                                            pendingLocalRestoreFileName = if (language == AppLanguage.BN) "পেস্ট করা ব্যাকআপ কোড" else "Pasted Backup Code"
                                            pendingLocalRestoreJson = pasteJsonInput
                                            pasteJsonInput = ""
                                            showPasteArea = false
                                        } else {
                                            Toast.makeText(context, if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
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
                        Column {
                            Text(
                                text = if (isGoogleSignedIn) (googleName.orEmpty().ifBlank { if (language == AppLanguage.BN) "গুগল ইউজার" else "Google User" }) else (if (language == AppLanguage.BN) "লগইন করা নেই" else "Not Signed In"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isGoogleSignedIn) (googleEmail.orEmpty().ifBlank { "drive.user@gmail.com" }) else (if (language == AppLanguage.BN) "ব্যাকআপ রাখতে অনুগ্রহ করে সাইন-ইন করুন" else "Please sign-in to backup your data"),
                                fontSize = 11.sp,
                                color = if (isDark) Color.Gray else Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = if (isGoogleSignedIn) (if (language == AppLanguage.BN) "লগআউট" else "Logout") else (if (language == AppLanguage.BN) "লগইন" else "Login"),
                            color = if (isGoogleSignedIn) FintechRed else FintechBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    if (isGoogleSignedIn) {
                                        showLogoutConfirm = true
                                    } else {
                                        onSignInClick()
                                    }
                                }
                                .padding(4.dp)
                        )
                    }
                    if (!driveStatusMessage.isNullOrEmpty()) {
                        Text(
                            text = driveStatusMessage ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FintechBlue,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "আপনার সমস্ত লেনদেন, সঞ্চয় এবং হিসাবের তথ্য সম্পূর্ণ নিরাপদ রাখতে সরাসরি গুগল ড্রাইভে ব্যাকআপ রাখুন।" 
                            else "To keep all your transaction, savings, and account data safe, backup directly to Google Drive.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isGoogleSignedIn) {
                                    Toast.makeText(context, if (language == AppLanguage.BN) "অনুগ্রহ করে প্রথমে গুগল ড্রাইভে লগইন করুন!" else "Please sign in to Google Drive first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onBackupClick()
                                }
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
                                if (!isGoogleSignedIn) {
                                    Toast.makeText(context, if (language == AppLanguage.BN) "অনুগ্রহ করে প্রথমে গুগল ড্রাইভে লগইন করুন!" else "Please sign in to Google Drive first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    onRestoreClick()
                                }
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

        // --- 6. APP ERROR LOG (SUPPORT) CARD ---
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
                        showErrorLogDialog = true
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

        // --- 7. ABOUT DEVELOPER CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ডেভেলপার প্রোফাইল" else "Developer Profile",
            isDark = isDark,
            icon = Icons.Rounded.Code,
            initiallyExpanded = false
        ) {
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
        }


        // --- 8. APP INFO CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপ ইনফো" else "App Info",
            isDark = isDark,
            icon = Icons.Rounded.Info,
            initiallyExpanded = true
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

        // --- 9. PRIVACY & TERMS CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "প্রাইভেসি ও শর্তাবলী" else "Privacy & Terms",
            isDark = isDark,
            icon = Icons.Rounded.Security,
            initiallyExpanded = true
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
                            showPrivacyDialog = true
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
                            showTermsDialog = true
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

        // --- 10. BOTTOM SPONSORED AD CARD ---
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

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(if (language == AppLanguage.BN) "লগআউট নিশ্চিতকরণ" else "Confirm Logout") },
            text = { Text(if (language == AppLanguage.BN) "আপনি কি নিশ্চিত যে আপনি গুগল ড্রাইভ থেকে লগআউট করতে চান?" else "Are you sure you want to log out of Google Drive?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                    onClick = {
                        showLogoutConfirm = false
                        viewModel.signOutFromGoogle(context) {
                            Toast.makeText(context, if (language == AppLanguage.BN) "গুগল ড্রাইভ থেকে লগআউট সফল হয়েছে!" else "Logged out from Google Drive successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(if (language == AppLanguage.BN) "লগআউট" else "Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "প্রাইভেসি পলিসি" else "Privacy Policy",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "১. তথ্য সংগ্রহ ও ব্যবহার:\nFinance Note অ্যাপটি একটি সম্পূর্ণ অফলাইন-ফার্স্ট অ্যাপ্লিকেশন। আপনার সমস্ত হিসাব, সঞ্চয় এবং লেনদেনের তথ্য শুধুমাত্র আপনার ডিভাইসের লোকাল ডাটাবেজে সংরক্ষিত থাকে। আমরা আমাদের কোনো সার্ভারে আপনার ব্যক্তিগত বা আর্থিক তথ্য সংগ্রহ, সংরক্ষণ বা ট্র্যাক করি না।"
                            else "1. Information Collection & Use:\nFinance Note is a fully offline-first application. All your financial records, savings, and transaction data are stored solely on your local device. We do not collect, store, or track your personal or financial data on any external servers.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "২. গুগল ড্রাইভ ব্যাকআপ:\nঅ্যাপের ক্লাউড ব্যাকআপ ফিচারটি ব্যবহারের জন্য যখন আপনি গুগল ড্রাইভ কানেক্ট করেন, তখন আপনার অনুমতি নিয়ে শুধুমাত্র অ্যাপের তৈরি করা ফাইলটি আপনার নিজস্ব গুগল ড্রাইভে আপলোড করা হয়। এই ব্যাকআপ ফাইলটির উপর আমাদের বা অন্য কোনো তৃতীয় পক্ষের কোনো নিয়ন্ত্রণ বা এক্সেস নেই।"
                            else "2. Google Drive Backup:\nWhen you connect and use the Google Drive backup feature, the app uploads your backup file directly to your personal Google Drive account. Neither we nor any third party has access to or control over this backup file.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "৩. নিরাপত্তা:\nআপনার ফাইন্যান্সিয়াল সিকিউরিটি আমাদের সর্বোচ্চ অগ্রাধিকার। যেহেতু আপনার সমস্ত তথ্য ডিভাইসে অফলাইনে থাকে, তাই আপনার ডিভাইসের নিরাপত্তা বজায় রাখা আপনার দায়িত্ব। আমরা কোনো প্রকার ট্র্যাকিং কুকি বা এনালিটিক্স টুল ব্যবহার করি না।"
                            else "3. Data Security:\nYour financial security is our top priority. Since all records remain offline on your device, maintaining your device security is your responsibility. We do not use any hidden tracking cookies or analytics tools.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(text = if (language == AppLanguage.BN) "ঠিক আছে" else "Close")
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "ব্যবহারের শর্তাবলী" else "Terms of Use",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "১. অ্যাপের ব্যবহার:\nFinance Note অ্যাপটি ব্যক্তিগত ব্যবহারের জন্য তৈরি করা হয়েছে। আপনার কোনো ভুল ডাটা এন্ট্রি বা অসাবধানতাবশত ডাটা ডিলিট হওয়ার জন্য অ্যাপ কর্তৃপক্ষ দায়ী থাকবে না।"
                            else "1. App Usage:\nFinance Note is designed for personal money management. The app and its developers are not responsible for any data loss, accidental deletions, or financial discrepancies resulting from user input errors.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "২. ব্যাকআপের দায়িত্ব:\nআপনার ডাটার নিরাপত্তা এবং ব্যাকআপের সম্পূর্ণ দায়িত্ব আপনার। নিয়মিত ক্লাউড ব্যাকআপ নেওয়ার পরামর্শ দেওয়া হচ্ছে, যাতে ফোন হারিয়ে গেলে বা নষ্ট হলে ডাটা রিস্টোর করা সম্ভব হয়।"
                            else "2. Backup Responsibility:\nYou are solely responsible for backing up your financial data. We highly recommend taking regular backups on Google Drive to prevent data loss in case of hardware failure or phone replacement.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "৩. পরিবর্তন ও আপডেট:\nআমরা অ্যাপের কার্যকারিতা ও নিরাপত্তা বাড়াতে যেকোনো সময় অ্যাপে আপডেট বা পরিবর্তন আনতে পারি।"
                            else "3. Updates & Modifications:\nWe reserve the right to release updates or modify features to improve performance, usability, and app security at any time.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(text = if (language == AppLanguage.BN) "সম্মত আছি" else "I Agree")
                }
            }
        )
    }

    if (showErrorLogDialog) {
        // Load logs text from ErrorLogger
        LaunchedEffect(Unit) {
            currentLogsText = com.example.data.ErrorLogger.getErrorLogs(context)
        }

        AlertDialog(
            onDismissRequest = { showErrorLogDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "সিস্টেম এরর রিপোর্ট" else "System Error Report",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "নিচে আপনার অ্যাপের সাম্প্রতিক এরর লগসমূহ দেওয়া হলো। আপনি এগুলো ডেভেলপারকে ইমেইল বা হোয়াটসঅ্যাপের মাধ্যমে পাঠাতে পারেন।" 
                            else "Below are the recent system error logs from your app. You can send them to the developer via Email or WhatsApp.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(if (isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .border(1.5.dp, if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (currentLogsText.isBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "কোনো এরর লগ পাওয়া যায়নি!" else "No error logs found!",
                                    fontSize = 13.sp,
                                    color = if (isDark) Color.Gray else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = currentLogsText,
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = if (isDark) Color.LightGray else Color(0xFF334155)
                                )
                            }
                        }
                    }
                    
                    if (currentLogsText.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Send Email Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("connect.shariful@gmail.com"))
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Sanchay (Finance Note) Error Log Report")
                                            putExtra(android.content.Intent.EXTRA_TEXT, currentLogsText)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Send Email"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == AppLanguage.BN) "ইমেইল" else "Email", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Send WhatsApp Button
                            Button(
                                onClick = {
                                    try {
                                        val truncatedLogs = if (currentLogsText.length > 2000) {
                                            currentLogsText.takeLast(2000) + "\n(Trimming logs for WhatsApp...)"
                                        } else {
                                            currentLogsText
                                        }
                                        val message = "Hi Developer, here is my Sanchay App Error Report:\n\n$truncatedLogs"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/8801768899599?text=${android.net.Uri.encode(message)}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentLogsText.isNotBlank()) {
                        TextButton(
                            onClick = {
                                com.example.data.ErrorLogger.clearErrorLogs(context)
                                currentLogsText = ""
                                Toast.makeText(context, if (language == AppLanguage.BN) "এরর লগসমূহ মুছে ফেলা হয়েছে!" else "Error logs cleared!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(text = if (language == AppLanguage.BN) "লগ মুছুন" else "Clear Logs", color = FintechRed, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = { showErrorLogDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ঠিক আছে" else "Close",
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }
                }
            }
        )
    }
}



@Composable
fun ChartsScreen(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    onBack: () -> Unit
) {
    val incomeTransactions = transactions.filter { it.type == "INCOME" || it.type == "BORROW" || it.type == "REPAY_RECEIVED" }
    val expenseTransactions = transactions.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }
    
    val incomesByCategory = incomeTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    val expensesByCategory = expenseTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    
    val totalIncome = incomeTransactions.sumOf { it.amount }
    val totalExpense = expenseTransactions.sumOf { it.amount }

    val palette = listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF14B8A6), Color(0xFF06B6D4))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        androidx.activity.compose.BackHandler(onBack = onBack)
        
        Text(
            text = if (language == AppLanguage.BN) "আয় ব্যয়ের চার্ট" else "Income & Expense Chart",
            color = if (isDark) Color.White else Color(0xFF1E293B),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // --- INCOME CHART ---
        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী আয়" else "Income by Category",
            data = incomesByCategory,
            total = totalIncome,
            palette = palette,
            language = language
        )

        // --- EXPENSE CHART ---
        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী ব্যয়" else "Expense by Category",
            data = expensesByCategory,
            total = totalExpense,
            palette = palette.reversed(), // slightly different colors
            language = language
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ChartSection(
    title: String,
    data: Map<String, Double>,
    total: Double,
    palette: List<Color>,
    language: AppLanguage
) {
    FintechGradientCard(
        gradientColors = listOf(Color(0xFF1E222F), Color(0xFF2A2E3D)),
        cornerRadius = 24.dp,
        padding = PaddingValues(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (total == 0.0) {
                Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == AppLanguage.BN) "কোন তথ্য নেই" else "No Data Available",
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                val values = data.values.map { it.toFloat() }
                val labels = data.keys.toList()
                val totalFloat = total.toFloat()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            values.forEachIndexed { index, value ->
                                if (value > 0) {
                                    val sweepAngle = (value / totalFloat) * 360f
                                    drawArc(
                                        color = palette[index % palette.size],
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
                                text = "Total",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = formatCurrency(total, language),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        labels.forEachIndexed { index, label ->
                            val value = data[label] ?: 0.0
                            val percent = ((value / total) * 100).toInt()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(palette[index % palette.size])
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatCurrency(value, language)} ($percent%)",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(isDark: Boolean) {
    val bgColor = if (isDark) Color(0xFF0F121F) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF1D4ED8)
    val subtitleColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF2563EB)
    val footerColor = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF2563EB)

    // Animated loading progress
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            progress += 0.02f
            kotlinx.coroutines.delay(35)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp)
    ) {
        // Center Content
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Splash Logo
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.splash_pie_chart),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Title: Finance Note
            Text(
                text = "Finance Note",
                color = titleColor,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle
            Text(
                text = "Your digital finance tracker. Keep tracking\nand manage your assets always.",
                color = subtitleColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading Bar
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(6.dp)
                    .background(if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Color(0xFF2563EB), CircleShape)
                )
            }
        }

        // Bottom Footer: From VibeStudio
        Text(
            text = "Developed by VibeStudio",
            color = footerColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}



@Composable
fun GoogleDriveRestoreListDialog(
    language: AppLanguage,
    isDark: Boolean,
    isFetching: Boolean,
    files: List<com.example.data.GoogleDriveFile>,
    viewModel: com.example.ui.viewmodel.FinanceViewModel,
    onDismiss: () -> Unit,
    onRestoreSuccess: () -> Unit,
    onDelete: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var fileToDelete by remember { mutableStateOf<com.example.data.GoogleDriveFile?>(null) }

    var pendingCloudRestoreData by remember { mutableStateOf<com.example.data.FinanceBackup?>(null) }
    var pendingCloudRestoreStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }
    var pendingCloudRestoreFileName by remember { mutableStateOf("") }
    var pendingCloudRestoreJson by remember { mutableStateOf("") }
    var isDownloadingByFileId by remember { mutableStateOf<String?>(null) }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp)),
            color = if (isDark) Color(0xFF141724) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "ক্লাউড ব্যাকআপ ফাইলসমূহ" else "Cloud Backup Files",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = FintechBlue)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color.White else Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isFetching) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FintechBlue)
                    }
                } else if (files.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = if (isDark) Color.DarkGray else Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "কোনো ব্যাকআপ ফাইল পাওয়া যায়নি" else "No backup files found",
                                color = if (isDark) Color.Gray else Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(files) { file ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9)
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.InsertDriveFile,
                                        contentDescription = null,
                                        tint = FintechBlue,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        // Nicely formatted name
                                        val displayName = if (file.name.startsWith("finance_note_backup_")) {
                                            val parts = file.name.removePrefix("finance_note_backup_").removeSuffix(".json")
                                            if (parts.length >= 15) { // yyyyMMdd_HHmmss
                                                val dateStr = parts.substring(0, 8)
                                                val timeStr = parts.substring(9)
                                                val yr = dateStr.substring(0, 4)
                                                val mn = dateStr.substring(4, 6)
                                                val dy = dateStr.substring(6, 8)
                                                val hh = timeStr.substring(0, 2)
                                                val mm = timeStr.substring(2, 4)
                                                val ss = timeStr.substring(4, 6)
                                                if (language == AppLanguage.BN) {
                                                    "ব্যাকআপ: $dy-$mn-$yr $hh:$mm:$ss"
                                                } else {
                                                    "Backup: $dy/$mn/$yr $hh:$mm:$ss"
                                                }
                                            } else {
                                                file.name
                                            }
                                        } else {
                                            file.name
                                        }

                                        Text(
                                            text = displayName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (isDark) Color.White else Color(0xFF1E293B)
                                        )

                                        // Size and Date Info
                                        val sizeStr = if (!file.size.isNullOrEmpty()) {
                                            val kb = file.size.toLongOrNull()?.let { it / 1024 } ?: 0
                                            "$kb KB"
                                        } else ""

                                        Text(
                                            text = sizeStr,
                                            fontSize = 11.sp,
                                            color = if (isDark) Color.Gray else Color(0xFF64748B)
                                        )
                                    }

                                    // Action buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                isDownloadingByFileId = file.id
                                                viewModel.downloadGoogleDriveFile(
                                                    context = context,
                                                    fileId = file.id,
                                                    onSuccess = { jsonContent ->
                                                        isDownloadingByFileId = null
                                                        val parsed = viewModel.parseBackupJson(jsonContent)
                                                        if (parsed != null) {
                                                            pendingCloudRestoreData = parsed
                                                            pendingCloudRestoreStats = viewModel.calculateBackupStats(parsed)
                                                            pendingCloudRestoreFileName = file.name
                                                            pendingCloudRestoreJson = jsonContent
                                                        } else {
                                                            Toast.makeText(context, if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    onError = { err ->
                                                        isDownloadingByFileId = null
                                                        Toast.makeText(context, "Download failed: $err", Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            },
                                            enabled = isDownloadingByFileId == null
                                        ) {
                                            if (isDownloadingByFileId == file.id) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = FintechBlue)
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.Restore,
                                                    contentDescription = "Restore",
                                                    tint = FintechBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { fileToDelete = file }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                tint = FintechRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Backup stats dialog for cloud restore
    if (pendingCloudRestoreStats != null && pendingCloudRestoreData != null) {
        BackupStatsDialog(
            title = if (language == AppLanguage.BN) "ক্লাউড রিস্টোর সামারি" else "Cloud Restore Summary",
            stats = pendingCloudRestoreStats!!,
            language = language,
            isDark = isDark,
            isRestoreMode = true,
            initialFileName = pendingCloudRestoreFileName,
            onConfirm = { _, _ ->
                viewModel.importBackup(
                    context = context,
                    json = pendingCloudRestoreJson,
                    fromLocalFile = false,
                    onSuccess = {
                        pendingCloudRestoreStats = null
                        pendingCloudRestoreData = null
                        pendingCloudRestoreJson = ""
                        onRestoreSuccess()
                    },
                    onError = { err ->
                        Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDismiss = {
                pendingCloudRestoreStats = null
                pendingCloudRestoreData = null
                pendingCloudRestoreJson = ""
            }
        )
    }

    // Captcha delete confirmation dialog
    fileToDelete?.let { file ->
        CaptchaDeleteDialog(
            fileName = file.name,
            language = language,
            isDark = isDark,
            onConfirm = {
                onDelete(file.id)
                fileToDelete = null
            },
            onDismiss = { fileToDelete = null }
        )
    }
}
