package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Person
import com.example.data.SavingsGoal
import com.example.data.SavingsTransaction
import com.example.data.Transaction
import com.example.ui.AppLanguage

data class SettingSearchModel(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val subtitleBn: String,
    val subtitleEn: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    onDismissRequest: () -> Unit,
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    savingsGoals: List<SavingsGoal>,
    savingsTransactions: List<SavingsTransaction>,
    onNavigateToTransaction: (Int) -> Unit,
    onNavigateToPerson: (Int) -> Unit,
    onNavigateToGoal: (Int) -> Unit,
    onNavigateToSavingsTransaction: () -> Unit,
    onNavigateToSettings: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // 1. Persons (ব্যক্তিবর্গ ও দেনা-পাওনা) - at the top
    val filteredPersons = remember(searchQuery, persons) {
        if (searchQuery.isBlank()) emptyList()
        else persons.filter { 
            it.name.contains(searchQuery, true) || 
            it.phone.contains(searchQuery, true) ||
            (it.address ?: "").contains(searchQuery, true)
        }
    }

    val personNetMap = remember(transactions, persons) {
        val map = mutableMapOf<Int, Double>()
        persons.forEach { person ->
            val personTx = transactions.filter { it.personId == person.id }
            val lent = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
            val borrowed = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
            val repaidPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
            val repaidReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
            map[person.id] = (lent + repaidPaid) - (borrowed + repaidReceived)
        }
        map
    }

    // 2. Transactions (লেনদেনসমূহ)
    val filteredTransactions = remember(searchQuery, transactions) {
        if (searchQuery.isBlank()) emptyList()
        else transactions.filter { 
            it.category.contains(searchQuery, true) || 
            it.note.contains(searchQuery, true) 
        }
    }

    // 3. Savings Goals (সঞ্চয় লক্ষ্য / সঞ্চয় কার্ড)
    val filteredGoals = remember(searchQuery, savingsGoals) {
        if (searchQuery.isBlank()) emptyList()
        else savingsGoals.filter { goal -> goal.title.contains(searchQuery, true) }
    }

    // 4. Savings Transactions (সঞ্চয় কার্ড এন্ট্রি)
    val filteredSavingsTransactions = remember(searchQuery, savingsTransactions, savingsGoals) {
        if (searchQuery.isBlank()) emptyList()
        else savingsTransactions.filter { st ->
            val goal = savingsGoals.find { it.id == st.goalId }
            st.note.contains(searchQuery, true) || 
            (goal?.title ?: "").contains(searchQuery, true) ||
            st.amount.toString().contains(searchQuery)
        }
    }

    // 5. App Settings (অ্যাপের বিভিন্ন সেটিংস)
    val allSettings = remember {
        listOf(
            SettingSearchModel("backup", "ব্যাকআপ ও রিস্টোর", "Backup & Restore", "ডাটা ব্যাকআপ ও ক্লাউড সিঙ্ক", "Cloud sync and local backups", Icons.Rounded.CloudUpload),
            SettingSearchModel("theme", "থিম ও কালার", "Theme & Colors", "ডার্ক মোড ও কাস্টম গ্রেডিয়েন্ট", "Dark mode and custom gradients", Icons.Rounded.Palette),
            SettingSearchModel("language", "ভাষা পরিবর্তন", "Language", "বাংলা অথবা ইংরেজি ভাষা", "Switch between Bengali and English", Icons.Rounded.Language),
            SettingSearchModel("workspace", "ওয়ার্কস্পেস ব্যবস্থাপনা", "Workspaces", "একাধিক অ্যাকাউন্ট পরিচালনা", "Manage multiple workspaces", Icons.Rounded.Work),
            SettingSearchModel("notification", "নোটিফিকেশন", "Notifications", "দৈনিক রিমাইন্ডার ও অ্যালার্ট", "Daily reminders and alerts", Icons.Rounded.Notifications),
            SettingSearchModel("security", "সিকিউরিটি ও পিন লক", "Security & Pin", "অ্যাপ লক ও পাসকোড", "App lock and passcode protection", Icons.Rounded.Lock),
            SettingSearchModel("profile", "প্রোফাইল তথ্য", "Profile", "নাম, ছবি ও ইমেইল", "Name, photo and contact info", Icons.Rounded.Person)
        )
    }

    val filteredSettings = remember(searchQuery, allSettings) {
        if (searchQuery.isBlank()) emptyList()
        else allSettings.filter { 
            it.titleBn.contains(searchQuery, true) || 
            it.titleEn.contains(searchQuery, true) ||
            it.subtitleBn.contains(searchQuery, true) ||
            it.subtitleEn.contains(searchQuery, true)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .widthIn(max = 600.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131724) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.3f else 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.BN) "স্মার্ট অনুসন্ধান" else "Smart Search",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "সব তথ্য এক জায়গায় খুঁজুন" else "Find everything instantly",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = onDismissRequest,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isDark) Color(0xFF1E2235) else Color(0xFFF1F5F9)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = if (isDark) Color.White else Color(0xFF475569)
                            )
                        }
                    }

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text(
                                text = if (language == AppLanguage.BN) "ব্যক্তি, ক্যাটাগরি, নোট বা সেটিংস খুঁজুন..." else "Search person, category, notes, settings...",
                                fontSize = 14.sp
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Rounded.Search, 
                                contentDescription = null, 
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Clear",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color(0xFF181C2A) else Color(0xFFF8FAFC),
                            unfocusedContainerColor = if (isDark) Color(0xFF181C2A) else Color(0xFFF8FAFC),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDark) Color(0xFF2E354F) else Color(0xFFE2E8F0)
                        ),
                        singleLine = true
                    )

                    // Results content in exact requested order:
                    // 1. Persons (top, with due/receivable)
                    // 2. Transactions
                    // 3. Savings Goals (cards)
                    // 4. Savings Transactions (entries)
                    // 5. App Settings
                    val hasResults = filteredPersons.isNotEmpty() || filteredTransactions.isNotEmpty() || 
                                     filteredGoals.isNotEmpty() || filteredSavingsTransactions.isNotEmpty() || 
                                     filteredSettings.isNotEmpty()

                    if (searchQuery.isNotBlank()) {
                        if (hasResults) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. Persons Section (Top)
                                if (filteredPersons.isNotEmpty()) {
                                    item {
                                        CategoryHeader(
                                            title = if (language == AppLanguage.BN) "ব্যক্তিবর্গ ও দেনা-পাওনা" else "Contacts & Debts",
                                            count = filteredPersons.size,
                                            isDark = isDark
                                        )
                                    }
                                    items(filteredPersons) { person ->
                                        val net = personNetMap[person.id] ?: 0.0
                                        PersonSearchItem(
                                            person = person,
                                            netBalance = net,
                                            language = language,
                                            isDark = isDark,
                                            searchQuery = searchQuery,
                                            onSelect = { onNavigateToPerson(person.id) }
                                        )
                                    }
                                }

                                // 2. Transactions Section
                                if (filteredTransactions.isNotEmpty()) {
                                    item {
                                        CategoryHeader(
                                            title = if (language == AppLanguage.BN) "লেনদেনসমূহ" else "Transactions",
                                            count = filteredTransactions.size,
                                            isDark = isDark
                                        )
                                    }
                                    items(filteredTransactions) { tx ->
                                        TransactionSearchItem(
                                            tx = tx,
                                            language = language,
                                            isDark = isDark,
                                            searchQuery = searchQuery,
                                            onSelect = { onNavigateToTransaction(tx.id) }
                                        )
                                    }
                                }

                                // 3. Savings Goals (সঞ্চয় কার্ড) Section
                                if (filteredGoals.isNotEmpty()) {
                                    item {
                                        CategoryHeader(
                                            title = if (language == AppLanguage.BN) "সঞ্চয় কার্ড (লক্ষ্য)" else "Savings Cards",
                                            count = filteredGoals.size,
                                            isDark = isDark
                                        )
                                    }
                                    items(filteredGoals) { goal ->
                                        GoalSearchItem(
                                            goal = goal,
                                            language = language,
                                            isDark = isDark,
                                            searchQuery = searchQuery,
                                            onSelect = { onNavigateToGoal(goal.id) }
                                        )
                                    }
                                }

                                // 4. Savings Transactions (সঞ্চয় কার্ড এন্ট্রি) Section
                                if (filteredSavingsTransactions.isNotEmpty()) {
                                    item {
                                        CategoryHeader(
                                            title = if (language == AppLanguage.BN) "সঞ্চয় কার্ড এন্ট্রি" else "Savings Entries",
                                            count = filteredSavingsTransactions.size,
                                            isDark = isDark
                                        )
                                    }
                                    items(filteredSavingsTransactions) { st ->
                                        val goal = savingsGoals.find { it.id == st.goalId }
                                        SavingsTransactionSearchItem(
                                            st = st,
                                            goalTitle = goal?.title ?: (if (language == AppLanguage.BN) "সঞ্চয় কার্ড" else "Savings Goal"),
                                            language = language,
                                            isDark = isDark,
                                            searchQuery = searchQuery,
                                            onSelect = { onNavigateToSavingsTransaction() }
                                        )
                                    }
                                }

                                // 5. App Settings Section
                                if (filteredSettings.isNotEmpty()) {
                                    item {
                                        CategoryHeader(
                                            title = if (language == AppLanguage.BN) "অ্যাপের বিভিন্ন সেটিংস" else "App Settings",
                                            count = filteredSettings.size,
                                            isDark = isDark
                                        )
                                    }
                                    items(filteredSettings) { setting ->
                                        SettingSearchItem(
                                            setting = setting,
                                            language = language,
                                            isDark = isDark,
                                            searchQuery = searchQuery,
                                            onSelect = { onNavigateToSettings(setting.id) }
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "🔍",
                                        fontSize = 40.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BN) "কোনো ফলাফল পাওয়া যায়নি" else "No matching results found",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (language == AppLanguage.BN) "অন্য কোনো কিওয়ার্ড দিয়ে চেষ্টা করুন" else "Try searching for another keyword",
                                        color = Color.Gray.copy(alpha = 0.7f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Empty/Type to Search state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💡",
                                    fontSize = 44.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) "খুঁজতে টাইপ করুন..." else "Type to search...",
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) "যেমন: ব্যক্তির নাম, বেতন, খাবার, বা সেটিংস" else "e.g., person name, salary, food, or settings",
                                    color = Color.Gray.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String, count: Int, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp
        )
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF1E293B) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = count.toString(),
                color = if (isDark) Color.White else MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionSearchItem(
    tx: Transaction,
    language: AppLanguage,
    isDark: Boolean,
    searchQuery: String,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E2235) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                val (color, icon) = when (tx.type) {
                    "INCOME" -> Pair(Color(0xFF10B981), Icons.AutoMirrored.Rounded.TrendingUp)
                    "EXPENSE" -> Pair(Color(0xFFEF4444), Icons.AutoMirrored.Rounded.TrendingDown)
                    "LEND" -> Pair(Color(0xFF3B82F6), Icons.Rounded.ArrowUpward)
                    "BORROW" -> Pair(Color(0xFFFBBF24), Icons.Rounded.ArrowDownward)
                    else -> Pair(Color.Gray, Icons.AutoMirrored.Rounded.CompareArrows)
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val banglaCat = when (tx.category) {
                        "Salary" -> "বেতন"
                        "Business" -> "ব্যবসা"
                        "Agriculture" -> "কৃষি"
                        "Gift" -> "উপহার"
                        "Sales" -> "বিক্রয়"
                        "Honorarium" -> "সম্মানী"
                        "Food" -> "খাবার"
                        "Housing" -> "বাসস্থান"
                        "Transport" -> "যাতায়াত"
                        "Shopping" -> "কেনাকাটা"
                        "Medical" -> "চিকিৎসা"
                        "Education" -> "শিক্ষা"
                        "Clothing" -> "পোশাক"
                        "Others" -> "অন্যান্য"
                        else -> tx.category
                    }
                    val highlightColor = if (isDark) Color(0xFFFFB300) else Color(0xFFF57C00)
                    Text(
                        text = highlightMatch(if (language == AppLanguage.BN) banglaCat else tx.category, searchQuery, highlightColor),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                    if (tx.note.isNotBlank()) {
                        Text(
                            text = highlightMatch(tx.note, searchQuery, highlightColor),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Text(
                text = "৳${tx.amount}",
                color = if (tx.type == "INCOME" || tx.type == "REPAY_RECEIVED") Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun PersonSearchItem(
    person: Person,
    netBalance: Double,
    language: AppLanguage,
    isDark: Boolean,
    searchQuery: String,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E2235) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Person, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary, 
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val highlightColor = if (isDark) Color(0xFFFFB300) else Color(0xFFF57C00)
                    Text(
                        text = highlightMatch(person.name, searchQuery, highlightColor),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                    val phoneText = if (person.phone.isNotBlank()) {
                        highlightMatch(person.phone, searchQuery, highlightColor)
                    } else {
                        androidx.compose.ui.text.AnnotatedString(if (language == AppLanguage.BN) "ফোন নম্বর নেই" else "No phone")
                    }
                    Text(
                        text = phoneText,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Total due or receivable display with person name
            val dueText = if (language == AppLanguage.BN) {
                if (netBalance > 0) "পাওনা: ৳$netBalance"
                else if (netBalance < 0) "দেনা: ৳${-netBalance}"
                else "হিসাব সমান"
            } else {
                if (netBalance > 0) "Receivable: ৳$netBalance"
                else if (netBalance < 0) "Payable: ৳${-netBalance}"
                else "Settled"
            }
            val dueColor = if (netBalance > 0) Color(0xFF10B981) else if (netBalance < 0) Color(0xFFEF4444) else Color.Gray

            Text(
                text = dueText,
                color = dueColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun GoalSearchItem(
    goal: SavingsGoal,
    language: AppLanguage,
    isDark: Boolean,
    searchQuery: String,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E2235) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFBBF24).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AccountBalance, 
                        contentDescription = null, 
                        tint = Color(0xFFFBBF24), 
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val highlightColor = if (isDark) Color(0xFFFFB300) else Color(0xFFF57C00)
                    Text(
                        text = highlightMatch(goal.title, searchQuery, highlightColor),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "টার্গেট: ৳${goal.targetAmount}" else "Target: ৳${goal.targetAmount}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = "৳${goal.savedAmount}",
                color = Color(0xFF10B981),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun SavingsTransactionSearchItem(
    st: SavingsTransaction,
    goalTitle: String,
    language: AppLanguage,
    isDark: Boolean,
    searchQuery: String,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E2235) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Savings, 
                        contentDescription = null, 
                        tint = Color(0xFF3B82F6), 
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val highlightColor = if (isDark) Color(0xFFFFB300) else Color(0xFFF57C00)
                    Text(
                        text = highlightMatch(goalTitle, searchQuery, highlightColor),
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                    if (st.note.isNotBlank()) {
                        Text(
                            text = highlightMatch(st.note, searchQuery, highlightColor),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = if (st.isDeposit) (if (language == AppLanguage.BN) "জমাকৃত" else "Deposit") else (if (language == AppLanguage.BN) "উত্তোলন" else "Withdrawal"),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            Text(
                text = (if (st.isDeposit) "+ " else "- ") + "৳${st.amount}",
                color = if (st.isDeposit) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun SettingSearchItem(
    setting: SettingSearchModel,
    language: AppLanguage,
    isDark: Boolean,
    searchQuery: String,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E2235) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    setting.icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.secondary, 
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val highlightColor = if (isDark) Color(0xFFFFB300) else Color(0xFFF57C00)
                val title = if (language == AppLanguage.BN) setting.titleBn else setting.titleEn
                val subtitle = if (language == AppLanguage.BN) setting.subtitleBn else setting.subtitleEn
                Text(
                    text = highlightMatch(title, searchQuery, highlightColor),
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E293B),
                    fontSize = 14.sp
                )
                Text(
                    text = highlightMatch(subtitle, searchQuery, highlightColor),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
