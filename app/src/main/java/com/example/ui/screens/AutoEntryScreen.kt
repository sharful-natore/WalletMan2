package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AutoEntry
import com.example.ui.AppLanguage
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel

private val IncomeGreen = Color(0xFF10B981)
private val ExpenseRed = Color(0xFFEF4444)
private val WarningAmber = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoEntryScreen(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val autoEntries by viewModel.autoEntries.collectAsStateWithLifecycle()
    var showFormDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<AutoEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<AutoEntry?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (language == AppLanguage.BN) "স্বয়ংক্রিয় লেনদেন (অটো এন্ট্রি)" else "Auto Entry System",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isDark) Color.White else Color.Black
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (isDark) Color.White else Color.Black
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White
                        )
                    )
                },
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editingEntry = null
                            showFormDialog = true
                        },
                        icon = { Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White) },
                        text = {
                            Text(
                                text = if (language == AppLanguage.BN) "নতুন নিয়ম যোগ করুন" else "Add New Rule",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        containerColor = FintechBlue,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FintechBlue.copy(alpha = 0.12f)
                        ),
                        border = BorderStroke(1.dp, FintechBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(FintechBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Autorenew,
                                    contentDescription = null,
                                    tint = FintechBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.BN) {
                                    "যেসব লেনদেন প্রতি দিন, সপ্তাহ, মাস বা বছরে অবশ্যই হয় (যেমন বাসা ভাড়া, বিদ্যুৎ বিল, বেতন) তা অটো এন্ট্রি সেট করে রাখুন।"
                                } else {
                                    "Set recurring transactions (rent, bills, salary) to automatically or manually confirm on schedule."
                                },
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = if (isDark) Color.LightGray else Color(0xFF334155)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (autoEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (language == AppLanguage.BN) "কোনো অটো এন্ট্রি নিয়ম সেট করা নেই" else "No auto entries configured yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.BN) "নিচের '+' বাটনে ট্যাপ করে নতুন নিয়ম যোগ করুন" else "Tap '+' below to add a new recurring entry",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(autoEntries, key = { it.id }) { entry ->
                                AutoEntryCard(
                                    entry = entry,
                                    language = language,
                                    isDark = isDark,
                                    onToggleEnable = {
                                        viewModel.updateAutoEntry(entry.copy(isEnabled = it))
                                    },
                                    onEdit = {
                                        editingEntry = entry
                                        showFormDialog = true
                                    },
                                    onDelete = {
                                        entryToDelete = entry
                                    },
                                    onRunNow = {
                                        viewModel.confirmAutoEntry(context, entry)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        AutoEntryFormDialog(
            entry = editingEntry,
            language = language,
            isDark = isDark,
            onDismiss = { showFormDialog = false },
            onSave = { newOrUpdated ->
                if (editingEntry != null) {
                    viewModel.updateAutoEntry(newOrUpdated)
                } else {
                    viewModel.insertAutoEntry(newOrUpdated)
                }
                showFormDialog = false
            }
        )
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "অটো এন্ট্রি মুছে ফেলতে চান?" else "Delete Auto Entry?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.BN) {
                        "'${entryToDelete?.title}' নিয়মটি স্থায়ীভাবে মুছে যাবে।"
                    } else {
                        "Are you sure you want to delete '${entryToDelete?.title}' rule?"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        entryToDelete?.id?.let { viewModel.deleteAutoEntry(it) }
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text(if (language == AppLanguage.BN) "মুছে ফেলুন" else "Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun AutoEntryCard(
    entry: AutoEntry,
    language: AppLanguage,
    isDark: Boolean,
    onToggleEnable: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRunNow: () -> Unit
) {
    val typeColor = when (entry.type) {
        "INCOME" -> IncomeGreen
        "EXPENSE" -> ExpenseRed
        "LEND" -> FintechBlue
        else -> WarningAmber
    }

    val typeLabel = when (entry.type) {
        "INCOME" -> if (language == AppLanguage.BN) "আয়" else "Income"
        "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয়" else "Expense"
        "LEND" -> if (language == AppLanguage.BN) "ঋণ দেওয়া" else "Lend"
        else -> if (language == AppLanguage.BN) "ঋণ নেওয়া" else "Borrow"
    }

    val frequencyLabel = when (entry.frequency) {
        "DAILY" -> if (language == AppLanguage.BN) "প্রতিদিন" else "Daily"
        "WEEKLY" -> {
            val dayName = when (entry.dayOfWeek) {
                1 -> if (language == AppLanguage.BN) "সোমবার" else "Monday"
                2 -> if (language == AppLanguage.BN) "মঙ্গলবার" else "Tuesday"
                3 -> if (language == AppLanguage.BN) "বুধবার" else "Wednesday"
                4 -> if (language == AppLanguage.BN) "বৃহস্পতিবার" else "Thursday"
                5 -> if (language == AppLanguage.BN) "শুক্রবার" else "Friday"
                6 -> if (language == AppLanguage.BN) "শনিবার" else "Saturday"
                else -> if (language == AppLanguage.BN) "রবিবার" else "Sunday"
            }
            if (language == AppLanguage.BN) "প্রতি সপ্তাহ ($dayName)" else "Weekly ($dayName)"
        }
        "MONTHLY" -> if (language == AppLanguage.BN) "প্রতি মাস (${entry.dayOfMonth} তারিখ)" else "Monthly (Day ${entry.dayOfMonth})"
        else -> if (language == AppLanguage.BN) "প্রতি বছর (${entry.dayOfMonth}/${entry.monthOfYear})" else "Yearly (${entry.dayOfMonth}/${entry.monthOfYear})"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (entry.type) {
                                "INCOME" -> Icons.Rounded.ArrowDownward
                                "EXPENSE" -> Icons.Rounded.ArrowUpward
                                "LEND" -> Icons.Rounded.CallMade
                                else -> Icons.Rounded.CallReceived
                            },
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = entry.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = typeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$typeLabel • ${entry.category}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Switch(
                    checked = entry.isEnabled,
                    onCheckedChange = onToggleEnable,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FintechBlue
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = if (isDark) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "৳${entry.amount.toInt()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = typeColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$frequencyLabel • ${entry.timeOfDay}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (entry.askBeforeAdding) WarningAmber.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (entry.askBeforeAdding) Icons.Rounded.Rule else Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = if (entry.askBeforeAdding) WarningAmber else IncomeGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (entry.askBeforeAdding) {
                                if (language == AppLanguage.BN) "অনুমতি সাপেক্ষে" else "Ask Before Adding"
                            } else {
                                if (language == AppLanguage.BN) "স্বয়ংক্রিয় যোগ" else "Auto Add"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.askBeforeAdding) WarningAmber else IncomeGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRunNow) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = FintechBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "এখনই যুক্ত করুন" else "Run Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechBlue
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AutoEntryFormDialog(
    entry: AutoEntry?,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (AutoEntry) -> Unit
) {
    var title by remember { mutableStateOf(entry?.title ?: "") }
    var amountText by remember { mutableStateOf(entry?.amount?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var type by remember { mutableStateOf(entry?.type ?: "EXPENSE") }
    var category by remember { mutableStateOf(entry?.category ?: "বাসা ভাড়া") }
    var note by remember { mutableStateOf(entry?.note ?: "") }
    var subType by remember { mutableStateOf(entry?.subType ?: "CASH") }
    var frequency by remember { mutableStateOf(entry?.frequency ?: "MONTHLY") }
    var dayOfWeek by remember { mutableStateOf(entry?.dayOfWeek ?: 1) }
    var dayOfMonth by remember { mutableStateOf(entry?.dayOfMonth ?: 1) }
    var monthOfYear by remember { mutableStateOf(entry?.monthOfYear ?: 1) }
    var timeOfDay by remember { mutableStateOf(entry?.timeOfDay ?: "09:00") }
    var askBeforeAdding by remember { mutableStateOf(entry?.askBeforeAdding ?: true) }

    val expenseCategories = listOf("বাসা ভাড়া", "বিদ্যুৎ বিল", "ওয়াইফাই বিল", "মুদি খরচ", "মোবাইল রিচার্জ", "পড়াশোনা", "চিকিৎসা", "পরিবহন", "অন্যান্য")
    val incomeCategories = listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "বাড়ি ভাড়া আয়", "বিনিয়োগ আয়", "অন্যান্য")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF1E1E1E) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (entry == null) {
                        if (language == AppLanguage.BN) "নতুন অটো এন্ট্রি তৈরি করুন" else "Create New Auto Entry"
                    } else {
                        if (language == AppLanguage.BN) "অটো এন্ট্রি সম্পাদনা করুন" else "Edit Auto Entry"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Color.Black
                )

                // Type Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color.Black.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.2f))
                        .padding(4.dp)
                ) {
                    val types = listOf("EXPENSE" to "ব্যয়", "INCOME" to "আয়")
                    types.forEach { (tKey, tLabel) ->
                        val isSelected = type == tKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) (if (tKey == "EXPENSE") ExpenseRed else IncomeGreen) else Color.Transparent)
                                .clickable {
                                    type = tKey
                                    if (tKey == "EXPENSE" && category !in expenseCategories) category = expenseCategories.first()
                                    if (tKey == "INCOME" && category !in incomeCategories) category = incomeCategories.first()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tLabel,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else (if (isDark) Color.LightGray else Color.DarkGray)
                            )
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (language == AppLanguage.BN) "শিরোনাম (যেমন: বাড়ি ভাড়া)" else "Title (e.g., Rent)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (language == AppLanguage.BN) "পরিমাণ (৳)" else "Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category Chips
                Text(
                    text = if (language == AppLanguage.BN) "ক্যাটাগরি নির্বাচন করুন:" else "Select Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                val categories = if (type == "EXPENSE") expenseCategories else incomeCategories
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSel = category == cat
                        FilterChip(
                            selected = isSel,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FintechBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Frequency Selection
                Text(
                    text = if (language == AppLanguage.BN) "পুনরাবৃত্তি সময়সূচি (Frequency):" else "Frequency Schedule:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val freqList = listOf(
                        "DAILY" to "দৈনিক",
                        "WEEKLY" to "সাপ্তাহিক",
                        "MONTHLY" to "মাসিক",
                        "YEARLY" to "বাৎসরিক"
                    )
                    freqList.forEach { (fKey, fLabel) ->
                        val isSel = frequency == fKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) FintechBlue else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.3f)))
                                .clickable { frequency = fKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else (if (isDark) Color.LightGray else Color.DarkGray)
                            )
                        }
                    }
                }

                // Frequency Detail Pickers
                when (frequency) {
                    "WEEKLY" -> {
                        Text(
                            text = if (language == AppLanguage.BN) "সপ্তাহের কোন দিন?" else "Day of Week?",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val days = listOf(1 to "সোম", 2 to "মঙ্গল", 3 to "বুধ", 4 to "বৃহঃ", 5 to "শুক্র", 6 to "শনি", 7 to "রবি")
                            items(days) { (dNum, dName) ->
                                val isSel = dayOfWeek == dNum
                                FilterChip(
                                    selected = isSel,
                                    onClick = { dayOfWeek = dNum },
                                    label = { Text(dName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    "MONTHLY" -> {
                        OutlinedTextField(
                            value = dayOfMonth.toString(),
                            onValueChange = {
                                val d = it.toIntOrNull() ?: 1
                                dayOfMonth = d.coerceIn(1, 31)
                            },
                            label = { Text(if (language == AppLanguage.BN) "মাসের কত তারিখ (১ - ৩১)?" else "Day of Month (1 - 31)?") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    "YEARLY" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dayOfMonth.toString(),
                                onValueChange = {
                                    val d = it.toIntOrNull() ?: 1
                                    dayOfMonth = d.coerceIn(1, 31)
                                },
                                label = { Text("তারিখ (১-৩১)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = monthOfYear.toString(),
                                onValueChange = {
                                    val m = it.toIntOrNull() ?: 1
                                    monthOfYear = m.coerceIn(1, 12)
                                },
                                label = { Text("মাস (১-১২)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Ask Before Adding Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.LightGray.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.BN) "অনুমতি চেয়ে নোটিফিকেশন দিন (Ask Before Adding)" else "Ask Before Adding",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (askBeforeAdding) {
                                    if (language == AppLanguage.BN) "নির্দিষ্ট দিনে ড্যাশবোর্ডে ব্যানার আসবে, আপনি নিশ্চিত করলে তবেই যুক্ত হবে।" else "Dashboard banner will prompt for confirmation."
                                } else {
                                    if (language == AppLanguage.BN) "কোনো ড্রাফট ব্যানার ছাড়াই নির্ধারিত দিনে সরাসরি লেনদেনে যুক্ত হবে।" else "Transaction will be automatically added on schedule."
                                },
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = askBeforeAdding,
                            onCheckedChange = { askBeforeAdding = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = WarningAmber)
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (title.isBlank()) return@Button
                            if (amt <= 0.0) return@Button

                            val newEntry = (entry ?: AutoEntry(
                                title = title.trim(),
                                amount = amt,
                                type = type,
                                category = category,
                                note = note.trim(),
                                subType = subType,
                                frequency = frequency,
                                dayOfWeek = dayOfWeek,
                                dayOfMonth = dayOfMonth,
                                monthOfYear = monthOfYear,
                                timeOfDay = timeOfDay,
                                askBeforeAdding = askBeforeAdding
                            )).copy(
                                title = title.trim(),
                                amount = amt,
                                type = type,
                                category = category,
                                note = note.trim(),
                                subType = subType,
                                frequency = frequency,
                                dayOfWeek = dayOfWeek,
                                dayOfMonth = dayOfMonth,
                                monthOfYear = monthOfYear,
                                timeOfDay = timeOfDay,
                                askBeforeAdding = askBeforeAdding
                            )
                            onSave(newEntry)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                    ) {
                        Text(if (language == AppLanguage.BN) "সংরক্ষণ করুন" else "Save Rule", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
