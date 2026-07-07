package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Person
import com.example.data.SavingsGoal
import com.example.data.SavingsTransaction
import com.example.data.Transaction
import com.example.ui.AppLanguage
import com.example.ui.Translation
import com.example.ui.components.FintechGradientCard
import com.example.ui.components.FrostedGlassCard
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechRed
import com.example.ui.theme.FintechBlue
import com.example.ui.theme.GradientsList
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.PersonDebt
import java.text.DecimalFormat

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
fun SanchayApp(viewModel: FinanceViewModel) {
    val language by viewModel.language.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val profileName by viewModel.profileName.collectAsState()
    val profileEmail by viewModel.profileEmail.collectAsState()
    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()

    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
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

    var timeFilter by remember { mutableStateOf("ALL") } // "ALL", "TODAY", "MONTH"

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
            topBar = {
                Surface(
                    color = FintechBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activeTab == "settings") {
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
                            
                            val screenTitle = when (activeTab) {
                                "dashboard" -> Translation.get("dashboard", language)
                                "transactions" -> Translation.get("transactions", language)
                                "debts" -> Translation.get("debts", language)
                                "savings" -> Translation.get("savings", language)
                                "settings" -> Translation.get("settings", language)
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
                            if (activeTab != "settings") {
                                IconButton(
                                    onClick = { viewModel.toggleLanguage() },
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
                                    onClick = { viewModel.toggleTheme() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.NightsStay,
                                        contentDescription = "Theme Toggle",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { activeTab = "settings" },
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
                        .padding(horizontal = 8.dp, vertical = 6.dp)
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
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                .offset(y = (-30).dp)
                                .size(88.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { showAddTransactionDialog = true }
                                .testTag("fab_add_transaction"),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(78.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF6F7BF7), Color(0xFF38BDF8))
                                        )
                                    )
                                    .border(
                                        width = 5.dp,
                                        color = if (isDarkTheme) Color(0xFF141724) else Color.White,
                                        shape = CircleShape
                                    ),
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
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Tab Render
                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220, delayMillis = 90)) + 
                                 scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                                .togetherWith(fadeOut(animationSpec = tween(90)))
                            },
                            label = "TabTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                "dashboard" -> DashboardScreen(
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
                                    onTimeFilterChange = { timeFilter = it }
                                )
                                "transactions" -> TransactionsScreen(
                                    language = language,
                                    isDark = isDarkTheme,
                                    transactions = transactions,
                                    persons = persons,
                                    onAddTransactionClick = { showAddTransactionDialog = true },
                                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                    filter = transactionFilter,
                                    onFilterChange = { transactionFilter = it },
                                    timeFilter = timeFilter,
                                    onTimeFilterChange = { timeFilter = it }
                                )
                                "debts" -> DebtsScreen(
                                    language = language,
                                    isDark = isDarkTheme,
                                    personDebts = filteredPersonDebts,
                                    onAddPersonClick = { showAddPersonDialog = true },
                                    onPersonClick = { selectedPersonDetail = it },
                                    onDeletePerson = { viewModel.deletePerson(it) },
                                    filter = debtFilter,
                                    onFilterChange = { debtFilter = it },
                                    timeFilter = timeFilter,
                                    onTimeFilterChange = { timeFilter = it }
                                )
                                "savings" -> SavingsScreen(
                                    language = language,
                                    isDark = isDarkTheme,
                                    savingsGoals = savingsGoals,
                                    onAddSavingsGoalClick = { showAddSavingsGoalDialog = true },
                                    onGoalClick = { selectedSavingsGoalDetail = it },
                                    onContributeClick = { goal, isWithdraw -> 
                                        showSavingsContributionDialog = goal
                                        isWithdrawMode = isWithdraw
                                    },
                                    onEditGoal = { goalToEdit = it }
                                )
                                "settings" -> SettingsScreen(
                                    viewModel = viewModel,
                                    language = language,
                                    isDark = isDarkTheme,
                                    onBack = { activeTab = "dashboard" }
                                )
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
                        onConfirm = { amount, type, category, note, personId ->
                            viewModel.addTransaction(amount, type, category, note, personId)
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
                        onConfirm = { title, target, sector, colorIdx ->
                            if (goalToEdit != null) {
                                viewModel.updateSavingsGoal(goalToEdit!!.copy(
                                    title = title,
                                    targetAmount = target,
                                    category = sector,
                                    colorIndex = colorIdx
                                ))
                            } else {
                                viewModel.addSavingsGoal(title, target, sector, colorIdx)
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
                        onConfirm = { amount, isWithdraw ->
                            val finalAmount = if (isWithdraw) -amount else amount
                            viewModel.addSavingsContribution(goal.id, finalAmount)
                            showSavingsContributionDialog = null
                        }
                    )
                }

                // Detailed view for savings transactions
                selectedSavingsGoalDetail?.let { goal ->
                    SavingsGoalDetailOverlay(
                        language = language,
                        isDark = isDarkTheme,
                        goal = goal,
                        transactionsFlow = viewModel.getSavingsTransactions(goal.id),
                        onDismiss = { selectedSavingsGoalDetail = null },
                        onDeleteGoal = { viewModel.deleteSavingsGoal(it) }
                    )
                }

                // Detailed view for transactions with a specific person
                selectedPersonDetail?.let { debtInfo ->
                    PersonDetailOverlay(
                        language = language,
                        isDark = isDarkTheme,
                        personDebt = debtInfo,
                        transactionsFlow = viewModel.getTransactionsByPerson(debtInfo.person.id),
                        onDismiss = { selectedPersonDetail = null },
                        onLendClick = {
                            viewModel.addTransaction(it, "LEND", "Lending", "Lent money to ${debtInfo.person.name}", debtInfo.person.id)
                        },
                        onBorrowClick = {
                            viewModel.addTransaction(it, "BORROW", "Borrowing", "Borrowed money from ${debtInfo.person.name}", debtInfo.person.id)
                        },
                        onRepayPaidClick = {
                            viewModel.addTransaction(it, "REPAY_PAID", "Repay Paid", "Paid back borrowed loan", debtInfo.person.id)
                        },
                        onRepayReceivedClick = {
                            viewModel.addTransaction(it, "REPAY_RECEIVED", "Repay Received", "Received back lent loan", debtInfo.person.id)
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
        modifier = Modifier.fillMaxSize(),
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
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Wallet,
                            contentDescription = null,
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "আয়" else "Income",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatCurrency(income, language),
                            color = Color.White,
                            fontSize = 12.sp,
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
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ব্যয়" else "Expense",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatCurrency(expense, language),
                            color = Color.White,
                            fontSize = 12.sp,
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeFilterChip(selected = timeFilter == "ALL", onClick = { onTimeFilterChange("ALL") }, label = Translation.get("all_time", language))
                TimeFilterChip(selected = timeFilter == "TODAY", onClick = { onTimeFilterChange("TODAY") }, label = Translation.get("today", language))
                TimeFilterChip(selected = timeFilter == "MONTH", onClick = { onTimeFilterChange("MONTH") }, label = Translation.get("this_month", language))
            }
        }

        // New Item: Separate Debt and Owed Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                    // I Owe Card (দেনা)
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clickable { onNavigate("debts", "DENA") }
                        .testTag("dashboard_i_owe_card")
                ) {
                    Text(
                        text = Translation.get("i_owe", language),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(iOwe, language),
                        color = Color.White,
                        fontSize = 24.sp,
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
                        .height(110.dp)
                        .clickable { onNavigate("debts", "PAWN") }
                        .testTag("dashboard_owed_to_me_card")
                ) {
                    Text(
                        text = Translation.get("owed_to_me", language),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatCurrency(owedToMe, language),
                        color = Color.White,
                        fontSize = 24.sp,
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
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1C1E2D) else Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Translation.get("delete_confirm", language),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(Translation.get("cancel", language), color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                onDelete(tx.id)
                                showDeleteConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                        ) {
                            Text(Translation.get("delete", language), color = Color.White)
                        }
                    }
                }
            }
        }
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

                    Text(
                        text = if (linkedPerson != null) "$formattedCategory (${linkedPerson.name})" else formattedCategory,
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
        var list = timeFilteredTransactions
        // Type filter
        when (filter) {
            "INCOME" -> list.filter { it.type == "INCOME" || it.type == "BORROW" || it.type == "REPAY_RECEIVED" }
            "EXPENSE" -> list.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }
            else -> list
        }
    }

    val totalIncome = timeFilteredTransactions.filter { it.type == "INCOME" || it.type == "BORROW" || it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
    val totalExpense = timeFilteredTransactions.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Summary Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        Text(Translation.get("total_income", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalIncome, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        Text(Translation.get("total_expense", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalExpense, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
            }

            // Time Filter Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeFilterChip(selected = timeFilter == "ALL", onClick = { onTimeFilterChange("ALL") }, label = Translation.get("all_time", language))
                TimeFilterChip(selected = timeFilter == "TODAY", onClick = { onTimeFilterChange("TODAY") }, label = Translation.get("today", language))
                TimeFilterChip(selected = timeFilter == "MONTH", onClick = { onTimeFilterChange("MONTH") }, label = Translation.get("this_month", language))
            }

            // Type Filters chip row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    Pair("ALL", "all"),
                    Pair("INCOME", "income"),
                    Pair("EXPENSE", "expense")
                )

                filters.forEach { (type, labelKey) ->
                    val isSelected = filter == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else if (isDark) Color(0xFF1E222F) else Color.White
                            )
                            .clickable { onFilterChange(type) }
                            .padding(vertical = 10.dp),
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
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(txs) { tx ->
                            TransactionRowItem(tx, language, isDark, persons, onDeleteTransaction)
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
            // Summary Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        Text(Translation.get("total_dena", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalDena, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
                FintechGradientCard(
                    gradientColors = GradientsList[0],
                    cornerRadius = 24.dp,
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                        Text(Translation.get("total_pawn", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatCurrency(totalPawn, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
            }

            // Time Filter Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeFilterChip(selected = timeFilter == "ALL", onClick = { onTimeFilterChange("ALL") }, label = Translation.get("all_time", language))
                TimeFilterChip(selected = timeFilter == "TODAY", onClick = { onTimeFilterChange("TODAY") }, label = Translation.get("today", language))
                TimeFilterChip(selected = timeFilter == "MONTH", onClick = { onTimeFilterChange("MONTH") }, label = Translation.get("this_month", language))
            }

            // Filter Tabs
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onFilterChange("ALL") }) { Text(Translation.get("all", language), color = if (filter == "ALL") MaterialTheme.colorScheme.primary else Color.Gray) }
                TextButton(onClick = { onFilterChange("DENA") }) { Text(Translation.get("dena", language), color = if (filter == "DENA") MaterialTheme.colorScheme.primary else Color.Gray) }
                TextButton(onClick = { onFilterChange("PAWN") }) { Text(Translation.get("pawn", language), color = if (filter == "PAWN") MaterialTheme.colorScheme.primary else Color.Gray) }
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
                        PersonDebtRowItem(item, language, isDark, onPersonClick, onDeletePerson)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeFilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
        border = if (selected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun filterTransactionsByTime(transactions: List<Transaction>, timeFilter: String): List<Transaction> {
    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    return when (timeFilter) {
        "TODAY" -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            transactions.filter { it.timestamp >= startOfDay }
        }
        "MONTH" -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            transactions.filter { it.timestamp >= startOfMonth }
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
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1C1E2D) else Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Translation.get("delete_confirm", language),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(Translation.get("cancel", language), color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                onDelete(item.person.id)
                                showDeleteConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                        ) {
                            Text(Translation.get("delete", language), color = Color.White)
                        }
                    }
                }
            }
        }
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
    savingsGoals: List<SavingsGoal>,
    onAddSavingsGoalClick: () -> Unit,
    onGoalClick: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (savingsGoals.isEmpty()) {
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
                        Text(Translation.get("no_savings", language), color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(savingsGoals) { goal ->
                        SavingsGoalCardItem(goal, language, isDark, onGoalClick, onContributeClick, onEditGoal)
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
    onGoalClick: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit
) {
    val gradient = GradientsList[goal.colorIndex % GradientsList.size]

    val progress = if (goal.targetAmount > 0) {
        (goal.savedAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }
    val percentage = (progress * 100).toInt()

    FintechGradientCard(
        gradientColors = gradient,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onGoalClick(goal) },
                onLongClick = { onEditGoal(goal) }
            )
            .testTag("savings_item_${goal.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
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

                Text(
                    text = formattedCategory.uppercase(),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = goal.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Edit Button inside gradient card
            IconButton(
                onClick = { onEditGoal(goal) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Targets and saved amounts
        if (goal.targetAmount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(Translation.get("saved", language), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Text(formatCurrency(goal.savedAmount, language), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(Translation.get("target", language), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    Text(formatCurrency(goal.targetAmount, language), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f)
            )

            Spacer(modifier = Modifier.height(14.dp))
        } else {
            // Only Saved Amount
            Column {
                Text(Translation.get("saved", language), color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                Text(formatCurrency(goal.savedAmount, language), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (goal.targetAmount > 0) {
                    val percentageText = if (language == AppLanguage.BN) {
                        "$percentage% অর্জিত"
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
                        "$percentage% Completed"
                    }

                    Text(
                        text = percentageText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Actions: Deposit & Withdraw (Fixed width ratio for consistency)
            Row(
                modifier = Modifier.fillMaxWidth(0.65f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onContributeClick(goal, false) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.height(30.dp).weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = Translation.get("deposit", language),
                        color = gradient[0],
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onContributeClick(goal, true) }, 
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier.height(30.dp).weight(1f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = Translation.get("withdraw", language),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
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
    onConfirm: (Double, String, String, String, Int?) -> Unit
) {
    val amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

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
                                    onConfirm(amount, type, category, note, selectedPersonId)
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
                            if (name.trim().isNotEmpty()) {
                                onConfirm(name.trim(), phone.trim(), address.trim(), photoUri.trim())
                            } else {
                                Toast.makeText(context, Translation.get("enter_name", language), Toast.LENGTH_SHORT).show()
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
    onConfirm: (String, Double, String, Int) -> Unit
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var targetStr by remember { mutableStateOf(if (initialGoal != null) initialGoal.targetAmount.toString() else "") }
    var sector by remember { mutableStateOf(initialGoal?.category ?: "Emergency") }
    var colorIndex by remember { mutableStateOf(initialGoal?.colorIndex ?: 0) }

    val sectors = listOf("Emergency", "Laptop", "Travel", "Marriage", "Investment")
    var sectorDropdownExpanded by remember { mutableStateOf(false) }
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
                        Translation.get("add_saving", language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
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
                            .testTag("input_saving_title")
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
                                if (title.trim().isNotEmpty()) {
                                    onConfirm(title.trim(), target, sector, colorIndex)
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
    onConfirm: (Double, Boolean) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
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
                                    onConfirm(amount, isWithdraw)
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
    goal: SavingsGoal,
    transactionsFlow: kotlinx.coroutines.flow.Flow<List<SavingsTransaction>>,
    onDismiss: () -> Unit,
    onDeleteGoal: (Int) -> Unit
) {
    val txList by transactionsFlow.collectAsState(initial = emptyList())
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1C1E2D) else Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Translation.get("delete_confirm", language),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text(Translation.get("cancel", language), color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                onDeleteGoal(goal.id)
                                showDeleteConfirm = false
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                        ) {
                            Text(Translation.get("delete", language), color = Color.White)
                        }
                    }
                }
            }
        }
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
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color.White else Color.DarkGray)
                }

                Text(
                    text = "${goal.title} ${Translation.get("history", language)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Color.DarkGray
                )
                
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History Label
            Text(
                text = Translation.get("history", language),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
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
                        Text("No transactions yet", color = Color.Gray)
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
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(amountColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(txIcon, contentDescription = null, tint = amountColor, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(txLabel, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F1724))
                                        val d = java.util.Date(tx.timestamp)
                                        val f = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                                        Text(f.format(d), color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                                Text(
                                    text = "${if (isDeposit) "+" else "-"}${formatCurrency(tx.amount, language)}",
                                    color = amountColor,
                                    fontWeight = FontWeight.ExtraBold
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
    onLendClick: (Double) -> Unit,
    onBorrowClick: (Double) -> Unit,
    onRepayPaidClick: (Double) -> Unit,
    onRepayReceivedClick: (Double) -> Unit,
    onDeleteTx: (Int) -> Unit
) {
    val txList by transactionsFlow.collectAsState(initial = emptyList())
    var showActionSheet by remember { mutableStateOf<String?>(null) } // "LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED"
    var actionAmountStr by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("person_detail_overlay"),
        color = if (isDark) Color(0xFF0B0D14) else Color(0xFFF3F4F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color.White else Color.DarkGray)
                }

                Text(
                    text = Translation.get("details", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Color.DarkGray
                )

                Spacer(modifier = Modifier.width(48.dp)) // symmetry spacer
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(onClick = { showActionSheet = null }, modifier = Modifier.weight(1f)) {
                                    Text(Translation.get("cancel", language), color = Color.Gray)
                                }
                                Button(
                                    onClick = {
                                        val amt = actionAmountStr.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            when (actType) {
                                                "LEND" -> onLendClick(amt)
                                                "BORROW" -> onBorrowClick(amt)
                                                "REPAY_PAID" -> onRepayPaidClick(amt)
                                                "REPAY_RECEIVED" -> onRepayReceivedClick(amt)
                                            }
                                            actionAmountStr = ""
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
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = FintechBlue,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
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

    var nameInput by remember(profileName) { mutableStateOf(profileName) }
    var emailInput by remember(profileEmail) { mutableStateOf(profileEmail) }
    var photoUriInput by remember(profilePhotoUri) { mutableStateOf(profilePhotoUri) }

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
            .background(if (isDark) Color(0xFF0F111A) else Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top Row with Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isDark) Color(0xFF1F2937) else Color.White, CircleShape)
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF374151) else Color(0xFFE2E8F0),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = if (isDark) Color.White else Color(0xFF1E293B)
                )
            }
            Text(
                text = Translation.get("settings", language),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
        }

        // Account Section
        SettingCategory(title = Translation.get("account", language), isDark = isDark) {
            // Profile Info Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUriInput != null) {
                        AsyncImage(
                            model = photoUriInput,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = FintechBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .background(FintechBlue, CircleShape)
                            .border(2.dp, if (isDark) Color(0xFF141724) else Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profileName.ifBlank { Translation.get("name", language) },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = profileEmail.ifBlank { Translation.get("enter_email", language) },
                        fontSize = 14.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                }
            }

            HorizontalDivider(
                color = if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9),
                thickness = 1.dp
            )

            // Edit Profile Fields
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(Translation.get("name", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechBlue,
                        unfocusedBorderColor = if (isDark) Color(0xFF374151) else Color(0xFFCBD5E1),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                )
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(Translation.get("enter_email", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechBlue,
                        unfocusedBorderColor = if (isDark) Color(0xFF374151) else Color(0xFFCBD5E1),
                        focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                        unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                )
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.saveProfile(context, nameInput, emailInput, photoUriInput)
                            android.widget.Toast.makeText(
                                context,
                                Translation.get("profile_saved", language),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(Translation.get("confirm", language), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Appearance Section
        SettingCategory(title = Translation.get("appearance", language), isDark = isDark) {
            SettingItem(
                icon = if (isDark) Icons.Rounded.NightsStay else Icons.Rounded.LightMode,
                iconColor = FintechBlue,
                title = if (isDark) Translation.get("dark_mode", language) else Translation.get("light_mode", language),
                isDark = isDark,
                trailing = {
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleTheme() },
                        colors = SwitchDefaults.colors(checkedThumbColor = FintechBlue)
                    )
                }
            )
            HorizontalDivider(color = if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
            SettingItem(
                icon = Icons.Rounded.Language,
                iconColor = Color(0xFF10B981),
                title = Translation.get("language", language),
                subtitle = if (language == AppLanguage.BN) "বাংলা" else "English",
                isDark = isDark,
                onClick = { viewModel.toggleLanguage() }
            )
        }

        // Data Management Section
        SettingCategory(title = Translation.get("data_management", language), isDark = isDark) {
            SettingItem(
                icon = Icons.Rounded.Backup,
                iconColor = Color(0xFF8B5CF6),
                title = Translation.get("create_backup", language),
                subtitle = Translation.get("backup_instructions", language),
                isDark = isDark,
                onClick = {
                    viewModel.exportBackup(
                        context = context,
                        onSuccess = { json ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Sanchay Backup", json)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, Translation.get("backup_success", language), android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
            HorizontalDivider(color = if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
            SettingItem(
                icon = Icons.Rounded.Restore,
                iconColor = Color(0xFFF59E0B),
                title = Translation.get("restore_backup", language),
                subtitle = Translation.get("restore_clipboard", language),
                isDark = isDark,
                onClick = { showPasteArea = !showPasteArea }
            )

            AnimatedVisibility(visible = showPasteArea) {
                Column(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pasteJsonInput,
                        onValueChange = { pasteJsonInput = it },
                        label = { Text(Translation.get("paste_backup_code", language)) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = if (isDark) Color(0xFF374151) else Color(0xFFCBD5E1),
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
                                        android.widget.Toast.makeText(context, Translation.get("restore_success", language), android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                    ) {
                        Text(Translation.get("confirm", language), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // About Developer Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                    )
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Column {
                        Text(
                            text = Translation.get("dev_name", language),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Translation.get("dev_role", language),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
                Text(
                    text = Translation.get("dev_bio", language),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Kotlin", "Compose", "Android").forEach { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = skill, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:shorifbd24@gmail.com")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Sanchay App Feedback")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Icon(Icons.Rounded.Email, contentDescription = null, tint = Color(0xFF4F46E5))
                    Spacer(Modifier.width(8.dp))
                    Text(Translation.get("contact_dev", language), color = Color(0xFF4F46E5), fontWeight = FontWeight.Bold)
                }
            }
        }

        // App Info Section
        SettingCategory(title = Translation.get("app_info", language), isDark = isDark) {
            SettingItem(
                icon = Icons.Rounded.Info,
                iconColor = Color(0xFF3B82F6),
                title = Translation.get("version", language),
                subtitle = "1.0.4 Professional",
                isDark = isDark
            )
            HorizontalDivider(color = if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
            SettingItem(
                icon = Icons.Rounded.Security,
                iconColor = Color(0xFFEF4444),
                title = Translation.get("privacy_policy", language),
                isDark = isDark,
                onClick = { /* Open Privacy Link */ }
            )
            HorizontalDivider(color = if (isDark) Color(0xFF1F2937) else Color(0xFFF1F5F9))
            SettingItem(
                icon = Icons.Rounded.Star,
                iconColor = Color(0xFFEAB308),
                title = Translation.get("rate_app", language),
                isDark = isDark,
                onClick = { /* Open Play Store */ }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
