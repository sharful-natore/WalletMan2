package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.FinanceNoteTheme
import com.example.widget.updateAllWidgets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickAddActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        val targetWorkspaceId = intent?.getStringExtra("EXTRA_TARGET_WORKSPACE_ID")
        val initialAction = intent?.action ?: "ACTION_ADD_TRANSACTION"

        setContent {
            val prefs = getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            val isDarkTheme = remember {
                val sysDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                prefs.getBoolean("is_dark_theme", sysDark)
            }

            val defaultGradient = remember { com.example.ui.theme.ThemeGradient(com.example.ui.theme.GradientsList.firstOrNull() ?: listOf(com.example.ui.theme.FintechBlue, com.example.ui.theme.FintechBlue)) }
            FinanceNoteTheme(darkTheme = isDarkTheme) {
                androidx.compose.runtime.CompositionLocalProvider(com.example.ui.screens.LocalActiveThemeGradient provides defaultGradient) {
                    QuickAddDialogScreen(
                        initialAction = initialAction,
                        targetWorkspaceId = targetWorkspaceId,
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialogScreen(
    initialAction: String,
    targetWorkspaceId: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { AppDatabase.getDatabase(context).financeDao() }
    val prefs = remember { context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE) }

    val langStr = remember { prefs.getString("app_language", "BN") ?: "BN" }
    val isBn = langStr == "BN"

    // Load workspaces
    var workspaces by remember { mutableStateOf<List<Workspace>>(emptyList()) }
    var selectedWorkspaceId by remember { 
        mutableStateOf(targetWorkspaceId ?: prefs.getString("active_workspace_id", "default") ?: "default") 
    }

    // Load persons and savings goals
    var persons by remember { mutableStateOf<List<Person>>(emptyList()) }
    var savingsGoals by remember { mutableStateOf<List<SavingsGoal>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val wsList = dao.getAllWorkspacesList()
            workspaces = if (wsList.isEmpty()) {
                listOf(Workspace(id = "default", name = if (isBn) "ব্যক্তিগত" else "Personal"))
            } else wsList

            val pList = dao.getAllPersonsList()
            persons = pList

            val sList = dao.getAllSavingsGoalsList()
            savingsGoals = sList
        }
    }

    // Determine initial selected type
    // Types: "INCOME", "EXPENSE", "DEBT_CREDIT", "SAVINGS"
    var selectedType by remember {
        mutableStateOf(
            when (initialAction) {
                "ACTION_DEBT_CREDIT" -> "DEBT_CREDIT"
                "ACTION_SAVINGS" -> "SAVINGS"
                else -> "EXPENSE"
            }
        )
    }

    // Inputs
    var amountText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    // Subtypes
    var debtSubtype by remember { mutableStateOf("LEND") } // LEND, BORROW, REPAY_PAID, REPAY_RECEIVED
    var selectedPersonId by remember { mutableStateOf<Int?>(null) }
    var newPersonName by remember { mutableStateOf("") }

    var savingsSubtype by remember { mutableStateOf("DEPOSIT") } // DEPOSIT, WITHDRAW
    var selectedGoalId by remember { mutableStateOf<Int?>(null) }

    var isSaving by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Default categories based on type
    val expenseCategories = listOf("খাবার", "কেনাকাটা", "বিল", "পরিবহন", "চিকিৎসা", "বিনোদন", "অন্যান্য")
    val incomeCategories = listOf("বেতন", "ব্যবসা", "ফ্রিল্যান্সিং", "উপহার", "বিনিয়োগ", "অন্যান্য")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(enabled = false) {}, // prevent click-through
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isBn) "তাৎক্ষণিক এন্ট্রি" else "Quick Entry",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val currentWsName = workspaces.find { it.id == selectedWorkspaceId }?.name ?: (if (isBn) "ব্যক্তিগত" else "Personal")
                            Text(
                                text = "${if (isBn) "ওয়ার্কস্পেস" else "Workspace"}: $currentWsName",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Type Chips: Income, Expense, Debt/Credit, Savings
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedType == "EXPENSE",
                            onClick = { 
                                selectedType = "EXPENSE"
                                categoryText = ""
                            },
                            label = { Text(if (isBn) "ব্যয়" else "Expense") },
                            leadingIcon = { Icon(Icons.Rounded.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFFEF4444),
                                selectedLeadingIconColor = Color(0xFFEF4444)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedType == "INCOME",
                            onClick = { 
                                selectedType = "INCOME"
                                categoryText = ""
                            },
                            label = { Text(if (isBn) "আয়" else "Income") },
                            leadingIcon = { Icon(Icons.Rounded.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF10B981),
                                selectedLeadingIconColor = Color(0xFF10B981)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedType == "DEBT_CREDIT",
                            onClick = { selectedType = "DEBT_CREDIT" },
                            label = { Text(if (isBn) "দেনা/পাওনা" else "Debt/Credit") },
                            leadingIcon = { Icon(Icons.Rounded.People, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF8B5CF6),
                                selectedLeadingIconColor = Color(0xFF8B5CF6)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedType == "SAVINGS",
                            onClick = { selectedType = "SAVINGS" },
                            label = { Text(if (isBn) "সঞ্চয়" else "Savings") },
                            leadingIcon = { Icon(Icons.Rounded.Savings, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF06B6D4).copy(alpha = 0.15f),
                                selectedLabelColor = Color(0xFF06B6D4),
                                selectedLeadingIconColor = Color(0xFF06B6D4)
                            )
                        )
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isBn) "পরিমাণ (টাকা)" else "Amount (BDT)") },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(12.dp)
                )

                // Dynamic Inputs based on selectedType
                when (selectedType) {
                    "EXPENSE", "INCOME" -> {
                        val categories = if (selectedType == "EXPENSE") expenseCategories else incomeCategories
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (isBn) "ক্যাটাগরি নির্বাচন করুন:" else "Select Category:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(categories) { cat ->
                                    SuggestionChip(
                                        onClick = { categoryText = cat },
                                        label = { Text(cat, fontSize = 12.sp) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (categoryText == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            labelColor = if (categoryText == cat) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = categoryText,
                                onValueChange = { categoryText = it },
                                label = { Text(if (isBn) "ক্যাটাগরি টাইপ করুন" else "Category Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    "DEBT_CREDIT" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = if (isBn) "লেনদেনের ধরণ:" else "Transaction Type:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Debt Subtypes: LEND, BORROW, REPAY_PAID, REPAY_RECEIVED
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = debtSubtype == "LEND",
                                    onClick = { debtSubtype = "LEND" },
                                    label = { Text(if (isBn) "ধার দেওয়া" else "Lend", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = debtSubtype == "BORROW",
                                    onClick = { debtSubtype = "BORROW" },
                                    label = { Text(if (isBn) "ধার নেওয়া" else "Borrow", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = debtSubtype == "REPAY_PAID",
                                    onClick = { debtSubtype = "REPAY_PAID" },
                                    label = { Text(if (isBn) "দেনা শোধ" else "Repay Paid", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = debtSubtype == "REPAY_RECEIVED",
                                    onClick = { debtSubtype = "REPAY_RECEIVED" },
                                    label = { Text(if (isBn) "পাওনা আদায়" else "Repay Recv", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Select or enter person name
                            val wsPersons = persons.filter { it.workspaceId == selectedWorkspaceId }
                            Text(
                                text = if (isBn) "ব্যক্তি নির্বাচন করুন:" else "Select Person:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (wsPersons.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(wsPersons) { p ->
                                        SuggestionChip(
                                            onClick = { 
                                                selectedPersonId = p.id
                                                newPersonName = p.name
                                            },
                                            label = { Text(p.name, fontSize = 12.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (selectedPersonId == p.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = newPersonName,
                                onValueChange = { 
                                    newPersonName = it 
                                    selectedPersonId = wsPersons.find { p -> p.name.equals(it.trim(), ignoreCase = true) }?.id
                                },
                                label = { Text(if (isBn) "ব্যক্তির নাম" else "Person Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    "SAVINGS" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = savingsSubtype == "DEPOSIT",
                                    onClick = { savingsSubtype = "DEPOSIT" },
                                    label = { Text(if (isBn) "সঞ্চয়ে জমা" else "Deposit") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = savingsSubtype == "WITHDRAW",
                                    onClick = { savingsSubtype = "WITHDRAW" },
                                    label = { Text(if (isBn) "উত্তোলন" else "Withdraw") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val wsGoals = savingsGoals.filter { it.workspaceId == selectedWorkspaceId }
                            Text(
                                text = if (isBn) "লক্ষ্য নির্বাচন করুন:" else "Select Savings Goal:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (wsGoals.isNotEmpty()) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(wsGoals) { goal ->
                                        SuggestionChip(
                                            onClick = { selectedGoalId = goal.id },
                                            label = { Text(goal.title, fontSize = 12.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = if (selectedGoalId == goal.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = if (isBn) "কোনো সঞ্চয় লক্ষ্য পাওয়া যায়নি।" else "No savings goals found.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Optional Note Input
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text(if (isBn) "নোট (ঐচ্ছিক)" else "Note (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isBn) "বাতিল" else "Cancel")
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                Toast.makeText(context, if (isBn) "সঠিক পরিমাণ লিখুন" else "Enter valid amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSaving = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val now = System.currentTimeMillis()

                                    when (selectedType) {
                                        "EXPENSE", "INCOME" -> {
                                            val cat = categoryText.trim().ifEmpty { 
                                                if (selectedType == "EXPENSE") (if (isBn) "অন্যান্য ব্যয়" else "Other Expense")
                                                else (if (isBn) "অন্যান্য আয়" else "Other Income")
                                            }
                                            val tx = Transaction(
                                                amount = amount,
                                                type = selectedType,
                                                category = cat,
                                                timestamp = now,
                                                note = noteText.trim(),
                                                workspaceId = selectedWorkspaceId
                                            )
                                            dao.insertTransaction(tx)
                                        }

                                        "DEBT_CREDIT" -> {
                                            val pName = newPersonName.trim().ifEmpty { 
                                                if (isBn) "অজ্ঞাত ব্যক্তি" else "Unknown Person"
                                            }
                                            var targetPersonId = selectedPersonId
                                            if (targetPersonId == null) {
                                                val newPerson = Person(
                                                    name = pName,
                                                    createdAt = now,
                                                    workspaceId = selectedWorkspaceId
                                                )
                                                targetPersonId = dao.insertPerson(newPerson).toInt()
                                            }

                                            val cat = when (debtSubtype) {
                                                "LEND" -> if (isBn) "ধার দেওয়া" else "Lending"
                                                "BORROW" -> if (isBn) "ধার নেওয়া" else "Borrowing"
                                                "REPAY_PAID" -> if (isBn) "দেনা পরিশোধ" else "Repayment Paid"
                                                else -> if (isBn) "পাওনা আদায়" else "Repayment Received"
                                            }

                                            val tx = Transaction(
                                                amount = amount,
                                                type = debtSubtype,
                                                category = cat,
                                                timestamp = now,
                                                note = noteText.trim(),
                                                personId = targetPersonId,
                                                workspaceId = selectedWorkspaceId
                                            )
                                            dao.insertTransaction(tx)
                                        }

                                        "SAVINGS" -> {
                                            val goalId = selectedGoalId
                                            if (goalId != null) {
                                                val isDeposit = savingsSubtype == "DEPOSIT"
                                                val stx = SavingsTransaction(
                                                    goalId = goalId,
                                                    amount = amount,
                                                    isDeposit = isDeposit,
                                                    timestamp = now,
                                                    note = noteText.trim(),
                                                    workspaceId = selectedWorkspaceId
                                                )
                                                dao.insertSavingsTransaction(stx)

                                                val goal = dao.getSavingsGoalById(goalId)
                                                if (goal != null) {
                                                    val newSaved = if (isDeposit) goal.savedAmount + amount else (goal.savedAmount - amount).coerceAtLeast(0.0)
                                                    dao.insertSavingsGoal(goal.copy(savedAmount = newSaved))
                                                }
                                            }
                                        }
                                    }

                                    // Refresh Widgets
                                    updateAllWidgets(context)

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            context, 
                                            if (isBn) "এন্ট্রি সফলভাবে সংরক্ষিত হয়েছে!" else "Entry saved successfully!", 
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        isSaving = false
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (isBn) "সংরক্ষণ করুন" else "Save Entry")
                        }
                    }
                }
            }
        }
    }
}
