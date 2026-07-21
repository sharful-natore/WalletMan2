package com.example.ui.viewmodel

import com.example.BuildConfig
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import android.Manifest
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Person
import com.example.data.Transaction
import com.example.data.SavingsGoal
import com.example.data.SavingsTransaction
import com.example.data.FinanceRepository
import com.example.data.FinanceBackup
import com.example.data.AppDatabase
import com.example.data.GoogleTokenResponse
import com.example.data.GoogleUserInfoResponse
import com.example.data.GoogleDriveFile
import com.example.data.GoogleDriveFilesResponse
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.ui.AppLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.InputStream

data class CustomNotification(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val message: String,
    val isSuccess: Boolean = true,
    val type: String = "INFO" // "SUCCESS", "ERROR", "SIGN_IN", "SIGN_OUT", "SYNC", "RESTORE", "BACKUP"
)

data class PersonDebt(
    val person: Person,
    val netBalance: Double, // positive = owed to me (pawn), negative = I owe (dena)
    val totalLent: Double,
    val totalBorrowed: Double,
    val totalRepaidPaid: Double,
    val totalRepaidReceived: Double
)

data class BackupStats(
    val totalIncome: Double,
    val totalExpense: Double,
    val totalOwedToMe: Double,
    val totalIOwe: Double,
    val totalPersons: Int,
    val totalCards: Int,
    val comment: String = "",
    val createdAt: Long? = null,
    val workspaces: List<com.example.data.Workspace> = emptyList()
)

class FinanceViewModel(private val repository: FinanceRepository, application: Application) : AndroidViewModel(application) {

    val updateManager = UpdateManager()

    // Preferences & UI State
    private val _language = MutableStateFlow(AppLanguage.BN) // Default to Bengali
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false) // Default to light theme as requested
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _selectedThemeGradientIndex = MutableStateFlow(0)
    val selectedThemeGradientIndex: StateFlow<Int> = _selectedThemeGradientIndex.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true) // Default to enabled
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    // Profile Settings States
    private val _profileName = MutableStateFlow("")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow("")
    val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profilePhotoUri = MutableStateFlow<String?>(null)
    val profilePhotoUri: StateFlow<String?> = _profilePhotoUri.asStateFlow()

    private val _profilePhone = MutableStateFlow("")
    val profilePhone: StateFlow<String> = _profilePhone.asStateFlow()

    private val _profileSocial = MutableStateFlow("")
    val profileSocial: StateFlow<String> = _profileSocial.asStateFlow()

    private val _profileAddress = MutableStateFlow("")
    val profileAddress: StateFlow<String> = _profileAddress.asStateFlow()

    // Moshi JSON adapter configuration
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(FinanceBackup::class.java)

    private val personAdapter = moshi.adapter(Person::class.java)
    private val transactionAdapter = moshi.adapter(Transaction::class.java)
    private val savingsGoalAdapter = moshi.adapter(SavingsGoal::class.java)
    private val savingsTransactionAdapter = moshi.adapter(SavingsTransaction::class.java)

    private val personWithTxAdapter = moshi.adapter(com.example.data.PersonWithTransactions::class.java)
    private val goalWithTxAdapter = moshi.adapter(com.example.data.GoalWithTransactions::class.java)
    private val deletedBackupAdapter = moshi.adapter(com.example.data.DeletedGDriveBackup::class.java)

    
    val allTrashItems = repository.allTrashItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun restoreTrashItem(item: com.example.data.TrashItem) {
        viewModelScope.launch {
            when (item.itemType) {
                "PERSON" -> {
                    personAdapter.fromJson(item.itemJson)?.let { repository.insertPerson(it) }
                }
                "PERSON_WITH_TXS" -> {
                    personWithTxAdapter.fromJson(item.itemJson)?.let { pWithTx ->
                        repository.insertPerson(pWithTx.person)
                        pWithTx.transactions.forEach { tx ->
                            repository.insertTransaction(tx)
                        }
                    }
                }
                "TRANSACTION" -> {
                    transactionAdapter.fromJson(item.itemJson)?.let { repository.insertTransaction(it) }
                }
                "SAVINGS_GOAL" -> {
                    savingsGoalAdapter.fromJson(item.itemJson)?.let { repository.insertSavingsGoal(it) }
                }
                "SAVINGS_GOAL_WITH_TXS" -> {
                    goalWithTxAdapter.fromJson(item.itemJson)?.let { gWithTx ->
                        repository.insertSavingsGoal(gWithTx.goal)
                        gWithTx.transactions.forEach { tx ->
                            repository.insertSavingsTransaction(tx)
                        }
                    }
                }
                "SAVINGS_TRANSACTION" -> {
                    savingsTransactionAdapter.fromJson(item.itemJson)?.let { repository.insertSavingsTransaction(it) }
                }
                "WORKSPACE" -> {
                    backupAdapter.fromJson(item.itemJson)?.let { backup ->
                        backup.workspaces.forEach { repository.insertWorkspace(it) }
                        backup.persons.forEach { repository.insertPerson(it) }
                        backup.transactions.forEach { repository.insertTransaction(it) }
                        backup.savingsGoals.forEach { repository.insertSavingsGoal(it) }
                        backup.savingsTransactions.forEach { repository.insertSavingsTransaction(it) }
                    }
                }
                "GDRIVE_BACKUP" -> {
                    deletedBackupAdapter.fromJson(item.itemJson)?.let { deletedBackup ->
                        val backup = backupAdapter.fromJson(deletedBackup.backupJson)
                        if (backup != null) {
                            restoreFullBackup(backup)
                        }
                    }
                }
            }
            if (item.itemType != "GDRIVE_BACKUP") {
                repository.deleteTrashItemById(item.id)
                com.example.widget.updateAllWidgets(getApplication())
                onLocalDatabaseChanged()
            }
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "আইটেম সফলভাবে রিস্টোর করা হয়েছে" else "Item restored successfully", isSuccess = true, type = "SUCCESS")
        }
    }
    
    fun permanentDeleteTrashItem(id: Int) {
        viewModelScope.launch {
            repository.deleteTrashItemById(id)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "আইটেম স্থায়ীভাবে মুছে ফেলা হয়েছে" else "Item permanently deleted", isSuccess = true, type = "SUCCESS")
        }
    }
    
    fun cleanUpOldTrash() {
        viewModelScope.launch {
            // 30 days ago
            val threshold = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
            repository.deleteOldTrashItems(threshold)
        }
    }


    // Database Streams
    private val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)

    // Workspaces List
    val workspaces: StateFlow<List<com.example.data.Workspace>> = repository.allWorkspaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentWorkspaceId = MutableStateFlow(
        prefs.getString("active_workspace_id", "default") ?: "default"
    )
    val currentWorkspaceId: StateFlow<String> = _currentWorkspaceId.asStateFlow()

    private val _budgetIncome = MutableStateFlow(0.0)
    val budgetIncome: StateFlow<Double> = _budgetIncome.asStateFlow()

    private val _budgetExpense = MutableStateFlow(0.0)
    val budgetExpense: StateFlow<Double> = _budgetExpense.asStateFlow()

    private val _budgetSavings = MutableStateFlow(0.0)
    val budgetSavings: StateFlow<Double> = _budgetSavings.asStateFlow()

    val monthlyBudgets: StateFlow<List<com.example.data.MonthlyBudget>> = _currentWorkspaceId
        .flatMapLatest { workspaceId -> repository.getAllMonthlyBudgets(workspaceId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMonthlyBudget(year: Int, month: Int, income: Double?, expense: Double?, savings: Double?) {
        viewModelScope.launch {
            val workspaceId = _currentWorkspaceId.value
            val existing = repository.getMonthlyBudget(year, month, workspaceId)
            val newBudget = existing?.copy(
                income = income ?: existing.income,
                expense = expense ?: existing.expense,
                savings = savings ?: existing.savings
            ) ?: com.example.data.MonthlyBudget(
                year = year,
                month = month,
                income = income ?: 0.0,
                expense = expense ?: 0.0,
                savings = savings ?: 0.0,
                workspaceId = workspaceId
            )
            repository.insertMonthlyBudget(newBudget)
            onLocalDatabaseChanged()
        }
    }

    fun deleteMonthlyBudget(year: Int, month: Int) {
        viewModelScope.launch {
            repository.deleteMonthlyBudget(year, month, _currentWorkspaceId.value)
            onLocalDatabaseChanged()
        }
    }

    fun loadBudgets() {
        // No-op, now handled by collecting currentWorkspace flow
    }

    fun setBudgetIncome(amount: Double) {
        viewModelScope.launch {
            val workspaceId = _currentWorkspaceId.value
            val existing = repository.getWorkspaceById(workspaceId) ?: com.example.data.Workspace(id = workspaceId, name = "ব্যক্তিগত")
            repository.insertWorkspace(existing.copy(budgetIncome = amount))
            _budgetIncome.value = amount
            recordDatabaseMutation("UPDATE", "Budget", "Income", amount)
            uploadToFirestore()
        }
    }

    fun setBudgetExpense(amount: Double) {
        viewModelScope.launch {
            val workspaceId = _currentWorkspaceId.value
            val existing = repository.getWorkspaceById(workspaceId) ?: com.example.data.Workspace(id = workspaceId, name = "ব্যক্তিগত")
            repository.insertWorkspace(existing.copy(budgetExpense = amount))
            _budgetExpense.value = amount
            recordDatabaseMutation("UPDATE", "Budget", "Expense", amount)
            uploadToFirestore()
        }
    }

    fun setBudgetSavings(amount: Double) {
        viewModelScope.launch {
            val workspaceId = _currentWorkspaceId.value
            val existing = repository.getWorkspaceById(workspaceId) ?: com.example.data.Workspace(id = workspaceId, name = "ব্যক্তিগত")
            repository.insertWorkspace(existing.copy(budgetSavings = amount))
            _budgetSavings.value = amount
            recordDatabaseMutation("UPDATE", "Budget", "Savings", amount)
            uploadToFirestore()
        }
    }

    val currentWorkspace: StateFlow<com.example.data.Workspace> = combine(workspaces, currentWorkspaceId) { list, activeId ->
        list.find { it.id == activeId } ?: list.firstOrNull() ?: com.example.data.Workspace(id = "default", name = "ব্যক্তিগত")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.Workspace(id = "default", name = "ব্যক্তিগত"))

    val workspaceStatsList: StateFlow<List<com.example.data.WorkspaceStats>> = combine(
        workspaces,
        repository.allPersons,
        repository.allTransactions,
        repository.allSavingsGoals,
        _currentWorkspaceId
    ) { workspaceList, allPersons, allTransactions, allSavingsGoals, _ ->
        val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        workspaceList.map { workspace ->
            val wsId = workspace.id
            val pName = workspace.profileName
            val pPhoto = workspace.profilePhotoUri
            
            val wsTransactions = allTransactions.filter { it.workspaceId == wsId }
            val wsPersons = allPersons.filter { it.workspaceId == wsId }
            val wsGoals = allSavingsGoals.filter { it.workspaceId == wsId }
            
            val income = wsTransactions.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
            val expense = wsTransactions.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }
            
            var owedToMe = 0.0
            var iOwe = 0.0
            wsPersons.forEach { person ->
                val personTx = wsTransactions.filter { it.personId == person.id }
                val lent = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
                val borrowed = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                val repaidPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                val repaidReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                
                val net = (lent + repaidPaid) - (borrowed + repaidReceived)
                if (net > 0) {
                    owedToMe += net
                } else if (net < 0) {
                    iOwe += -net
                }
            }
            
            com.example.data.WorkspaceStats(
                workspace = workspace,
                profileName = pName.ifBlank { workspace.name },
                profilePhoto = pPhoto,
                income = income,
                expense = expense,
                netOwedToMe = owedToMe,
                netIOwe = iOwe,
                personCount = wsPersons.size,
                cardCount = wsGoals.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getProfileKey(baseKey: String, workspaceId: String): String {
        return if (workspaceId == "default") baseKey else "${baseKey}_${workspaceId}"
    }

    fun selectWorkspace(workspaceId: String) {
        val oldId = _currentWorkspaceId.value
        _currentWorkspaceId.value = workspaceId
        prefs.edit().putString("active_workspace_id", workspaceId).apply()
        loadProfile(getApplication())
        loadBudgets()
        com.example.widget.updateAllWidgets(getApplication())
        
        if (oldId != workspaceId) {
            triggerCustomNotification(
                if (_language.value == com.example.ui.AppLanguage.BN) 
                    "ওয়ার্কস্পেস পরিবর্তন করা হয়েছে" 
                else "Workspace changed", 
                isSuccess = true, 
                type = "SUCCESS"
            )
        }
    }

    fun createWorkspace(name: String) {
        viewModelScope.launch {
            val id = "ws_${System.currentTimeMillis()}"
            repository.insertWorkspace(com.example.data.Workspace(id = id, name = name))
            selectWorkspace(id)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ওয়ার্কস্পেস তৈরি করা হয়েছে" else "Workspace created", isSuccess = true, type = "SUCCESS")
        }
    }

    fun editWorkspace(workspaceId: String, name: String) {
        viewModelScope.launch {
            val existing = workspaces.value.find { it.id == workspaceId }
            if (existing != null) {
                repository.insertWorkspace(existing.copy(name = name))
                // trigger refresh by updating flow slightly
                _currentWorkspaceId.value = _currentWorkspaceId.value
                com.example.widget.updateAllWidgets(getApplication())
                triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ওয়ার্কস্পেস নাম পরিবর্তন করা হয়েছে" else "Workspace updated", isSuccess = true, type = "SUCCESS")
            }
        }
    }

    fun deleteWorkspace(workspaceId: String) {
        viewModelScope.launch {
            val workspace = repository.allWorkspaces.first().find { it.id == workspaceId }
            if (workspace != null) {
                val fullBackup = repository.getBackupData()
                
                val backupData = com.example.data.FinanceBackup(
                    persons = fullBackup.persons.filter { it.workspaceId == workspaceId },
                    transactions = fullBackup.transactions.filter { it.workspaceId == workspaceId },
                    savingsGoals = fullBackup.savingsGoals.filter { it.workspaceId == workspaceId },
                    savingsTransactions = fullBackup.savingsTransactions.filter { it.workspaceId == workspaceId },
                    workspaces = listOf(workspace)
                )

                
                val json = backupAdapter.toJson(backupData)
                repository.insertTrashItem(com.example.data.TrashItem(
                    originalId = workspaceId.hashCode(),
                    itemType = "WORKSPACE",
                    itemJson = json,
                    deletedAt = System.currentTimeMillis()
                ))
            }
            
            repository.deleteWorkspace(workspaceId)
            if (_currentWorkspaceId.value == workspaceId) {
                selectWorkspace("default")
            }
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ওয়ার্কস্পেস মুছে ফেলা হয়েছে" else "Workspace deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    val persons: StateFlow<List<Person>> = combine(repository.allPersons, currentWorkspaceId) { list, activeId ->
        list.filter { it.workspaceId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = combine(repository.allTransactions, currentWorkspaceId) { list, activeId ->
        list.filter { it.workspaceId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = combine(repository.allSavingsGoals, currentWorkspaceId) { list, activeId ->
        list.filter { it.workspaceId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsTransactions: StateFlow<List<com.example.data.SavingsTransaction>> = combine(repository.allSavingsTransactions, currentWorkspaceId) { list, activeId ->
        list.filter { it.workspaceId == activeId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personDebts: StateFlow<List<PersonDebt>> = combine(persons, transactions) { personList, txList ->
        personList.map { person ->
            val personTx = txList.filter { it.personId == person.id }
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Metrics State
    val totalIncome: StateFlow<Double> = transactions
        .combine(personDebts) { txList, debts ->
            // In Accounting (Accrual Basis):
            // Revenue = Cash Income + Sales on Credit (LEND with CREDIT subtype)
            txList.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactions
        .combine(personDebts) { txList, debts ->
            // In Accounting (Accrual Basis):
            // Expenses = Cash Expenses + Purchases on Credit (BORROW with CREDIT subtype)
            txList.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOwedToMe: StateFlow<Double> = personDebts
        .combine(transactions) { debts, _ ->
            debts.filter { it.netBalance > 0 }.sumOf { it.netBalance }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIOwe: StateFlow<Double> = personDebts
        .combine(transactions) { debts, _ ->
            debts.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = combine(transactions, savingsTransactions) { txList, savingsTxList ->
        // Total Balance (Cash in Hand/Wallet) formula:
        // Cash Inflow: INCOME (Cash) + BORROW (Cash) + REPAY_RECEIVED (Debt collected) + Savings Withdrawals
        // Cash Outflow: EXPENSE (Cash) + LEND (Cash given) + REPAY_PAID (Debt paid back) + Savings Deposits
        
        val cashIncome = txList.filter { it.type == "INCOME" }.sumOf { it.amount }
        val cashBorrowed = txList.filter { it.type == "BORROW" && it.subType != "CREDIT" }.sumOf { it.amount }
        val repaidReceived = txList.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
        val savingsWithdrawals = savingsTxList.filter { !it.isDeposit }.sumOf { it.amount }

        val cashExpense = txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val cashLent = txList.filter { it.type == "LEND" && it.subType != "CREDIT" }.sumOf { it.amount }
        val repaidPaid = txList.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
        val savingsDeposits = savingsTxList.filter { it.isDeposit }.sumOf { it.amount }

        (cashIncome + cashBorrowed + repaidReceived + savingsWithdrawals) - 
        (cashExpense + cashLent + repaidPaid + savingsDeposits)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSavings: StateFlow<Double> = savingsGoals
        .map { goals -> goals.fold(0.0) { acc, goal -> acc + goal.savedAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netWorth: StateFlow<Double> = combine(totalBalance, totalSavings, totalOwedToMe, totalIOwe) { balance, savings, owed, owe ->
        // Accounting Equation: Equity (Net Worth) = Assets - Liabilities
        // Assets = Cash (Balance) + Savings + Accounts Receivable (Owed to Me)
        // Liabilities = Accounts Payable (I Owe)
        (balance + savings + owed) - owe
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User Actions / Intents
    fun toggleLanguage(context: Context) {
        val newLang = if (_language.value == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN
        _language.value = newLang
        context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language", newLang.name)
            .apply()
        com.example.widget.updateAllWidgets(context)
    }

    fun toggleTheme(context: Context) {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_dark_theme", newTheme)
            .apply()
    }

    fun selectThemeGradientIndex(context: Context, index: Int) {
        _selectedThemeGradientIndex.value = index
        context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("selected_theme_gradient_index", index)
            .apply()
    }

    fun toggleNotification(context: Context) {
        val currentState = _isNotificationEnabled.value
        val newState = !currentState
        
        if (newState) {
            // Check permission if turning ON
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // Cannot enable without permission
                    _isNotificationEnabled.value = false
                    context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("notification_enabled", false)
                        .apply()
                    return
                }
            }
        }
        
        _isNotificationEnabled.value = newState
        context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("notification_enabled", newState)
            .apply()

        val intent = Intent(context, com.example.widget.FinanceNotificationService::class.java)
        if (newState) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) { e.printStackTrace() }
        } else {
            context.stopService(intent)
        }
    }


    fun addPerson(name: String, phone: String, address: String, photoUri: String) {
        viewModelScope.launch {
            repository.insertPerson(Person(name = name, phone = phone, address = address, photoUri = photoUri, workspaceId = _currentWorkspaceId.value))
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            recordDatabaseMutation("ADD", name, "PERSON", 0.0)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ব্যক্তি সফলভাবে যুক্ত করা হয়েছে" else "Person added successfully", isSuccess = true, type = "SUCCESS")
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            repository.updatePerson(person)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            recordDatabaseMutation("EDIT", person.name, "PERSON", 0.0)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ব্যক্তি সফলভাবে আপডেট করা হয়েছে" else "Person updated successfully", isSuccess = true, type = "SUCCESS")
        }
    }

    fun movePerson(personId: Int, targetWorkspaceId: String) {
        viewModelScope.launch {
            val person = repository.getPersonById(personId)
            if (person != null) {
                repository.updatePerson(person.copy(workspaceId = targetWorkspaceId))
                val transactions = repository.getTransactionsByPersonList(personId)
                transactions.forEach { tx ->
                    repository.updateTransaction(tx.copy(workspaceId = targetWorkspaceId))
                }
                onLocalDatabaseChanged()
                com.example.widget.updateAllWidgets(getApplication())
                triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ব্যক্তি অন্য ওয়ার্কস্পেসে মুভ করা হয়েছে" else "Person moved to another workspace", isSuccess = true, type = "SUCCESS")
            }
        }
    }

    fun deletePersons(ids: List<Int>) {
        viewModelScope.launch {
            var lastDeletedName = ""
            ids.forEach { id ->
                val p = repository.getPersonById(id)
                if (p != null) {
                    lastDeletedName = p.name
                    val txs = repository.getTransactionsByPersonList(id)
                    val pWithTx = com.example.data.PersonWithTransactions(p, txs)
                    repository.insertTrashItem(com.example.data.TrashItem(
                        originalId = id, 
                        itemType = "PERSON_WITH_TXS", 
                        itemJson = personWithTxAdapter.toJson(pWithTx)
                    ))
                }
                repository.deletePerson(id)
            }
            if (lastDeletedName.isNotEmpty()) {
                val msg = if (ids.size > 1) "$lastDeletedName + ${ids.size - 1}" else lastDeletedName
                recordDatabaseMutation("DELETE", msg, "PERSON", 0.0)
            }
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "নির্বাচিত ব্যক্তিদের মুছে ফেলা হয়েছে" else "Selected persons deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    fun movePersons(personIds: List<Int>, targetWorkspaceId: String) {
        viewModelScope.launch {
            personIds.forEach { personId ->
                val person = repository.getPersonById(personId)
                if (person != null) {
                    repository.updatePerson(person.copy(workspaceId = targetWorkspaceId))
                    val transactions = repository.getTransactionsByPersonList(personId)
                    transactions.forEach { tx ->
                        repository.updateTransaction(tx.copy(workspaceId = targetWorkspaceId))
                    }
                }
            }
            onLocalDatabaseChanged()
            com.example.widget.updateAllWidgets(getApplication())
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "নির্বাচিত ব্যক্তিদের অন্য ওয়ার্কস্পেসে মুভ করা হয়েছে" else "Selected persons moved to another workspace", isSuccess = true, type = "SUCCESS")
        }
    }

    fun deletePerson(id: Int) {
        viewModelScope.launch {
            val p = repository.getPersonById(id)
            if (p != null) {
                val txs = repository.getTransactionsByPersonList(id)
                val pWithTx = com.example.data.PersonWithTransactions(p, txs)
                repository.insertTrashItem(com.example.data.TrashItem(
                    originalId = id, 
                    itemType = "PERSON_WITH_TXS", 
                    itemJson = personWithTxAdapter.toJson(pWithTx)
                ))
                recordDatabaseMutation("DELETE", p.name, "PERSON", 0.0)
            }
            repository.deletePerson(id)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ব্যক্তি মুছে ফেলা হয়েছে" else "Person deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String,
        personId: Int?,
        timestamp: Long = System.currentTimeMillis(),
        subType: String? = "CASH"
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    personId = personId,
                    timestamp = timestamp,
                    workspaceId = _currentWorkspaceId.value,
                    subType = subType
                )

            )
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            val personName = personId?.let { pid -> repository.allPersons.first().find { it.id == pid }?.name }
            val finalName = personName ?: if (type == "INCOME") (if (_language.value == com.example.ui.AppLanguage.BN) "আয়" else "Income")
            else if (type == "EXPENSE") (if (_language.value == com.example.ui.AppLanguage.BN) "ব্যয়" else "Expense")
            else if (note.isBlank()) category else note
            val finalCategory = "${type.uppercase()} - $category"
            recordDatabaseMutation("ADD", finalName, finalCategory, amount)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "লেনদেন সফলভাবে সংরক্ষণ করা হয়েছে" else "Transaction saved", isSuccess = true, type = "SUCCESS")
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            val personName = transaction.personId?.let { pid -> repository.allPersons.first().find { it.id == pid }?.name }
            val finalName = personName ?: if (transaction.type == "INCOME") (if (_language.value == com.example.ui.AppLanguage.BN) "আয়" else "Income")
            else if (transaction.type == "EXPENSE") (if (_language.value == com.example.ui.AppLanguage.BN) "ব্যয়" else "Expense")
            else if (transaction.note.isBlank()) transaction.category else transaction.note
            val finalCategory = "${transaction.type.uppercase()} - ${transaction.category}"
            recordDatabaseMutation("EDIT", finalName, finalCategory, transaction.amount)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "লেনদেন আপডেট করা হয়েছে" else "Transaction updated", isSuccess = true, type = "SUCCESS")
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            val t = repository.getTransactionById(id)
            if (t != null) {
                repository.insertTrashItem(com.example.data.TrashItem(originalId = id, itemType = "TRANSACTION", itemJson = transactionAdapter.toJson(t)))
                val personName = t.personId?.let { pid -> repository.allPersons.first().find { it.id == pid }?.name }
                val finalName = personName ?: if (t.type == "INCOME") (if (_language.value == com.example.ui.AppLanguage.BN) "আয়" else "Income")
                else if (t.type == "EXPENSE") (if (_language.value == com.example.ui.AppLanguage.BN) "ব্যয়" else "Expense")
                else if (t.note.isBlank()) t.category else t.note
                val finalCategory = "${t.type.uppercase()} - ${t.category}"
                recordDatabaseMutation("DELETE", finalName, finalCategory, t.amount)
            }
            repository.deleteTransaction(id)
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "লেনদেন মুছে ফেলা হয়েছে" else "Transaction deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    fun getTransactionsByPerson(personId: Int): kotlinx.coroutines.flow.Flow<List<Transaction>> {
        return repository.getTransactionsByPerson(personId)
    }

    fun addSavingsGoal(title: String, targetAmount: Double, category: String, colorIndex: Int, cardholderName: String = "") {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoal(
                    title = title,
                    targetAmount = targetAmount,
                    savedAmount = 0.0,
                    category = category,
                    colorIndex = colorIndex,
                    cardholderName = cardholderName,
                    workspaceId = _currentWorkspaceId.value
                )
            )
            onLocalDatabaseChanged()
            recordDatabaseMutation("ADD", title, "SAVINGS_GOAL", targetAmount)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "সঞ্চয় লক্ষ্য তৈরি করা হয়েছে" else "Savings goal created", isSuccess = true, type = "SUCCESS")
        }
    }

    fun getSavingsTransactions(goalId: Int): kotlinx.coroutines.flow.Flow<List<SavingsTransaction>> {
        return repository.getSavingsTransactionsByGoal(goalId)
    }

    fun addSavingsContribution(id: Int, contribution: Double, note: String = "", timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            // Find current savings goal and update it
            val currentList = savingsGoals.value
            val goal = currentList.find { it.id == id }
            if (goal != null) {
                val updated = goal.copy(savedAmount = goal.savedAmount + contribution)
                repository.insertSavingsGoal(updated)
                
                // Add savings transaction
                val isDeposit = contribution >= 0
                val absoluteAmount = kotlin.math.abs(contribution)
                repository.insertSavingsTransaction(
                    SavingsTransaction(
                        goalId = id,
                        amount = absoluteAmount,
                        isDeposit = isDeposit,
                        note = note,
                        timestamp = timestamp,
                        workspaceId = _currentWorkspaceId.value
                    )
                )

                onLocalDatabaseChanged()
                val finalName = if (note.isBlank()) goal.title else note
                recordDatabaseMutation("ADD", finalName, "SAVINGS_CONTRIBUTION", absoluteAmount)
            }
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "সঞ্চয় যুক্ত করা হয়েছে" else "Contribution added", isSuccess = true, type = "SUCCESS")
        }
    }

    fun deleteSavingsGoal(id: Int) {
        viewModelScope.launch {
            val g = repository.getSavingsGoalById(id)
            if (g != null) {
                val txs = repository.getSavingsTransactionsByGoalList(id)
                val gWithTx = com.example.data.GoalWithTransactions(g, txs)
                repository.insertTrashItem(com.example.data.TrashItem(
                    originalId = id, 
                    itemType = "SAVINGS_GOAL_WITH_TXS", 
                    itemJson = goalWithTxAdapter.toJson(gWithTx)
                ))
                recordDatabaseMutation("DELETE", g.title, "SAVINGS_GOAL", g.targetAmount)
            }
            repository.deleteSavingsGoal(id)
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "সঞ্চয় লক্ষ্য মুছে ফেলা হয়েছে" else "Savings goal deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    fun updateSavingsTransaction(oldTx: SavingsTransaction, newTx: SavingsTransaction) {
        viewModelScope.launch {
            repository.updateSavingsTransaction(newTx)
            val currentList = savingsGoals.value
            val goal = currentList.find { it.id == oldTx.goalId }
            if (goal != null) {
                val oldContribution = if (oldTx.isDeposit) oldTx.amount else -oldTx.amount
                val newContribution = if (newTx.isDeposit) newTx.amount else -newTx.amount
                val difference = newContribution - oldContribution
                val updated = goal.copy(savedAmount = goal.savedAmount + difference)
                repository.insertSavingsGoal(updated)
                onLocalDatabaseChanged()
                val finalName = if (newTx.note.isBlank()) goal.title else newTx.note
                recordDatabaseMutation("EDIT", finalName, "SAVINGS_CONTRIBUTION", newTx.amount)
            }
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "সঞ্চয় লেনদেন আপডেট করা হয়েছে" else "Savings transaction updated", isSuccess = true, type = "SUCCESS")
        }
    }

    fun deleteSavingsTransaction(tx: SavingsTransaction) {
        viewModelScope.launch {
            repository.insertTrashItem(com.example.data.TrashItem(originalId = tx.id, itemType = "SAVINGS_TRANSACTION", itemJson = savingsTransactionAdapter.toJson(tx)))
            repository.deleteSavingsTransaction(tx.id)
            val currentList = savingsGoals.value
            val goal = currentList.find { it.id == tx.goalId }
            if (goal != null) {
                val delta = if (tx.isDeposit) -tx.amount else tx.amount
                val updated = goal.copy(savedAmount = goal.savedAmount + delta)
                repository.insertSavingsGoal(updated)
                val finalName = if (tx.note.isBlank()) goal.title else tx.note
                recordDatabaseMutation("DELETE", finalName, "SAVINGS_CONTRIBUTION", tx.amount)
            }
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "সঞ্চয় লেনদেন মুছে ফেলা হয়েছে" else "Savings transaction deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.insertSavingsGoal(goal)
            onLocalDatabaseChanged()
            recordDatabaseMutation("EDIT", goal.title, "SAVINGS_GOAL", goal.targetAmount)
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "সঞ্চয় লক্ষ্য আপডেট করা হয়েছে" else "Savings goal updated", isSuccess = true, type = "SUCCESS")
        }
    }

    fun moveSavingsGoal(goalId: Int, targetWorkspaceId: String) {
        viewModelScope.launch {
            val goal = repository.getSavingsGoalById(goalId)
            if (goal != null) {
                repository.insertSavingsGoal(goal.copy(workspaceId = targetWorkspaceId))
                val transactions = repository.getSavingsTransactionsByGoalList(goalId)
                transactions.forEach { tx ->
                    repository.updateSavingsTransaction(tx.copy(workspaceId = targetWorkspaceId))
                }
                onLocalDatabaseChanged()
                triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "সঞ্চয় কার্ড অন্য ওয়ার্কস্পেসে মুভ করা হয়েছে" else "Savings card moved to another workspace", isSuccess = true, type = "SUCCESS")
            }
        }
    }

    fun deleteTransactions(ids: List<Int>) {
        viewModelScope.launch {
            var lastDeletedNote = ""
            var lastDeletedAmount = 0.0
            var lastDeletedCategory = "TRANSACTIONS"
            ids.forEach { id ->
                val t = repository.getTransactionById(id)
                if (t != null) {
                    lastDeletedNote = if (t.note.isBlank()) t.category else t.note
                    lastDeletedAmount = t.amount
                    lastDeletedCategory = "${t.type.uppercase()} - ${t.category}"
                    repository.insertTrashItem(com.example.data.TrashItem(originalId = id, itemType = "TRANSACTION", itemJson = transactionAdapter.toJson(t)))
                }
                repository.deleteTransaction(id)
            }
            if (lastDeletedNote.isNotEmpty()) {
                val msg = if (ids.size > 1) "$lastDeletedNote + ${ids.size - 1}" else lastDeletedNote
                recordDatabaseMutation("DELETE", msg, lastDeletedCategory, lastDeletedAmount)
            }
            com.example.widget.updateAllWidgets(getApplication())
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "${ids.size}টি লেনদেন মুছে ফেলা হয়েছে" else "${ids.size} transactions deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    fun deleteSavingsGoals(ids: List<Int>) {
        viewModelScope.launch {
            var lastDeletedTitle = ""
            var lastDeletedTargetAmount = 0.0
            ids.forEach { id ->
                val g = repository.getSavingsGoalById(id)
                if (g != null) {
                    lastDeletedTitle = g.title
                    lastDeletedTargetAmount = g.targetAmount
                    val txs = repository.getSavingsTransactionsByGoalList(id)
                    val gWithTx = com.example.data.GoalWithTransactions(g, txs)
                    repository.insertTrashItem(com.example.data.TrashItem(
                        originalId = id, 
                        itemType = "SAVINGS_GOAL_WITH_TXS", 
                        itemJson = goalWithTxAdapter.toJson(gWithTx)
                    ))
                }
                repository.deleteSavingsGoal(id)
            }
            if (lastDeletedTitle.isNotEmpty()) {
                val msg = if (ids.size > 1) "$lastDeletedTitle + ${ids.size - 1}" else lastDeletedTitle
                recordDatabaseMutation("DELETE", msg, "SAVINGS_GOAL", lastDeletedTargetAmount)
            }
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "${ids.size}টি সঞ্চয় কার্ড মুছে ফেলা হয়েছে" else "${ids.size} savings cards deleted", isSuccess = true, type = "SUCCESS")
        }
    }

    fun moveSavingsGoals(ids: List<Int>, targetWorkspaceId: String) {
        viewModelScope.launch {
            ids.forEach { goalId ->
                val goal = repository.getSavingsGoalById(goalId)
                if (goal != null) {
                    repository.insertSavingsGoal(goal.copy(workspaceId = targetWorkspaceId))
                    val transactions = repository.getSavingsTransactionsByGoalList(goalId)
                    transactions.forEach { tx ->
                        repository.updateSavingsTransaction(tx.copy(workspaceId = targetWorkspaceId))
                    }
                }
            }
            onLocalDatabaseChanged()
            triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "${ids.size}টি সঞ্চয় কার্ড অন্য ওয়ার্কস্পেসে মুভ করা হয়েছে" else "${ids.size} savings cards moved", isSuccess = true, type = "SUCCESS")
        }
    }

    private suspend fun restoreFullBackup(backup: FinanceBackup) {
        repository.restoreBackupData(backup)
        
        // Restore budgets for each workspace if available
        backup.workspaces.forEach { ws ->
            // Budget is already in the workspace entity now, so repository.restoreBackupData(backup) 
            // should have handled it via financeDao.insertWorkspaces(backup.workspaces)
        }

        if (backup.profileName.isNotBlank() || backup.profileEmail.isNotBlank()) {
            saveProfile(getApplication(),
                name = backup.profileName.ifBlank { _profileName.value },
                email = backup.profileEmail.ifBlank { _profileEmail.value },
                photoUri = backup.profilePhotoUri ?: _profilePhotoUri.value,
                phone = backup.profilePhone.ifBlank { _profilePhone.value },
                social = backup.profileSocial.ifBlank { _profileSocial.value },
                address = backup.profileAddress.ifBlank { _profileAddress.value }
            )
        }
        com.example.widget.updateAllWidgets(getApplication())
        onLocalDatabaseChanged()
    }

    suspend fun restoreSelectiveBackup(backup: FinanceBackup, workspaceIds: List<String>) {
        val currentData = repository.getBackupData()
        
        val preservedPersons = currentData.persons.filter { it.workspaceId !in workspaceIds }
        val preservedTransactions = currentData.transactions.filter { it.workspaceId !in workspaceIds }
        val preservedGoals = currentData.savingsGoals.filter { it.workspaceId !in workspaceIds }
        val preservedSavingsTxs = currentData.savingsTransactions.filter { it.workspaceId !in workspaceIds }
        val preservedWorkspaces = currentData.workspaces.filter { it.id !in workspaceIds }
        val preservedBudgets = currentData.monthlyBudgets.filter { it.workspaceId !in workspaceIds }
        
        val incomingPersons = backup.persons.filter { it.workspaceId in workspaceIds }
        val incomingTransactions = backup.transactions.filter { it.workspaceId in workspaceIds }
        val incomingGoals = backup.savingsGoals.filter { it.workspaceId in workspaceIds }
        val incomingSavingsTxs = backup.savingsTransactions.filter { it.workspaceId in workspaceIds }
        val incomingWorkspaces = backup.workspaces.filter { it.id in workspaceIds }
        val incomingBudgets = backup.monthlyBudgets.filter { it.workspaceId in workspaceIds }
        
        val combinedBackup = FinanceBackup(
            persons = preservedPersons + incomingPersons,
            transactions = preservedTransactions + incomingTransactions,
            savingsGoals = preservedGoals + incomingGoals,
            savingsTransactions = preservedSavingsTxs + incomingSavingsTxs,
            workspaces = preservedWorkspaces + incomingWorkspaces,
            monthlyBudgets = preservedBudgets + incomingBudgets,
            comment = backup.comment,
            createdAt = backup.createdAt,
            profileName = currentData.profileName,
            profileEmail = currentData.profileEmail,
            profilePhone = currentData.profilePhone,
            profileSocial = currentData.profileSocial,
            profileAddress = currentData.profileAddress,
            profilePhotoUri = currentData.profilePhotoUri
        )
        
        repository.restoreBackupData(combinedBackup)
        com.example.widget.updateAllWidgets(getApplication())
        onLocalDatabaseChanged()
    }

    private fun saveImageToInternalStorage(context: Context, uriString: String): String? {
        return try {
            if (!uriString.startsWith("content://") && !uriString.startsWith("file://")) {
                return uriString
            }
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            if (uriString.startsWith("file://")) {
                uriString.substring(7)
            } else {
                null
            }
        }
    }

    fun verifyNotificationState(context: Context) {
        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        val notifEnabledInPrefs = prefs.getBoolean("notification_enabled", true)
        
        var isActuallyEnabled = notifEnabledInPrefs

        // Check if system notifications are enabled
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                isActuallyEnabled = false
            }
        }

        // If enabled but not running, try to start it
        if (isActuallyEnabled && !com.example.widget.FinanceNotificationService.isRunning) {
            val intent = Intent(context, com.example.widget.FinanceNotificationService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) { 
                e.printStackTrace() 
                isActuallyEnabled = false
            }
        } else if (!isActuallyEnabled && com.example.widget.FinanceNotificationService.isRunning) {
            val intent = Intent(context, com.example.widget.FinanceNotificationService::class.java)
            context.stopService(intent)
        }

        _isNotificationEnabled.value = isActuallyEnabled
    }

    // Profile Settings Helpers
    fun loadProfile(context: Context) {
        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        val wsId = _currentWorkspaceId.value
        _profileName.value = prefs.getString(getProfileKey("user_name", wsId), "") ?: ""
        _profileEmail.value = prefs.getString(getProfileKey("user_email", wsId), "") ?: ""
        _profilePhotoUri.value = prefs.getString(getProfileKey("user_photo", wsId), null)
        _profilePhone.value = prefs.getString(getProfileKey("user_phone", wsId), "") ?: ""
        _profileSocial.value = prefs.getString(getProfileKey("user_social", wsId), "") ?: ""
        _profileAddress.value = prefs.getString(getProfileKey("user_address", wsId), "") ?: ""
        
        val lastSync = prefs.getLong("last_firestore_sync_time", 0L)
        _lastSyncTime.value = if (lastSync == 0L) null else lastSync

        // Load language and theme, defaulting language to BN and theme to false (Light)
        val savedLangStr = prefs.getString("app_language", AppLanguage.BN.name) ?: AppLanguage.BN.name
        _language.value = try { AppLanguage.valueOf(savedLangStr) } catch (e: Exception) { AppLanguage.BN }
        _isDarkTheme.value = prefs.getBoolean("is_dark_theme", false)
        _selectedThemeGradientIndex.value = prefs.getInt("selected_theme_gradient_index", 0)

        // Load notification setting
        var notifEnabled = prefs.getBoolean("notification_enabled", true)
        
        // Ensure it's disabled if permission is missing (Android 13+)
        if (notifEnabled && android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifEnabled = false
            }
        }
        
        _isNotificationEnabled.value = notifEnabled
        
        // Also initialize Google Drive state to ensure persistence
        initGoogleDrive(context)

        startRealtimeSync()

        // Update home screen widgets immediately on start to show fresh data
        com.example.widget.updateAllWidgets(context)

        if (notifEnabled) {
            val intent = Intent(context, com.example.widget.FinanceNotificationService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun saveProfile(context: Context, name: String, email: String, photoUri: String? = null, phone: String = "", social: String = "", address: String = "") {
        viewModelScope.launch {
            val wsId = _currentWorkspaceId.value
            val existing = repository.getWorkspaceById(wsId) ?: com.example.data.Workspace(id = wsId, name = "ব্যক্তিগত")
            val finalPhotoUri = photoUri?.let { saveImageToInternalStorage(context, it) } ?: photoUri
            
            repository.insertWorkspace(existing.copy(
                profileName = name,
                profileEmail = email,
                profilePhone = phone,
                profileSocial = social,
                profileAddress = address,
                profilePhotoUri = finalPhotoUri
            ))
            
            _profileName.value = name
            _profileEmail.value = email
            _profilePhotoUri.value = finalPhotoUri
            _profilePhone.value = phone
            _profileSocial.value = social
            _profileAddress.value = address
            
            com.example.widget.updateAllWidgets(context)
            uploadToFirestore()
        }
    }

    // Firestore Sync States
    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _isNetworkActive = MutableStateFlow(false)
    val isNetworkActive: StateFlow<Boolean> = _isNetworkActive.asStateFlow()

    private val _firestoreSyncStatus = MutableStateFlow<String?>(null)
    val firestoreSyncStatus: StateFlow<String?> = _firestoreSyncStatus.asStateFlow()

    private val _showCloudDataFoundDialog = MutableStateFlow(false)
    val showCloudDataFoundDialog: StateFlow<Boolean> = _showCloudDataFoundDialog.asStateFlow()

    private val _pendingCloudData = MutableStateFlow<FinanceBackup?>(null)
    val pendingCloudData: StateFlow<FinanceBackup?> = _pendingCloudData.asStateFlow()

    fun dismissCloudDataFoundDialog() {
        _showCloudDataFoundDialog.value = false
        _pendingCloudData.value = null
    }

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _lastMutationAction = MutableStateFlow<String?>(null)
    val lastMutationAction: StateFlow<String?> = _lastMutationAction.asStateFlow()

    private val _lastMutationName = MutableStateFlow<String?>(null)
    val lastMutationName: StateFlow<String?> = _lastMutationName.asStateFlow()

    private val _lastMutationCategory = MutableStateFlow<String?>(null)
    val lastMutationCategory: StateFlow<String?> = _lastMutationCategory.asStateFlow()

    private val _lastMutationAmount = MutableStateFlow<Double?>(null)
    val lastMutationAmount: StateFlow<Double?> = _lastMutationAmount.asStateFlow()

    fun recordDatabaseMutation(action: String, name: String, category: String, amount: Double) {
        val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("last_mutation_action", action)
            .putString("last_mutation_name", name)
            .putString("last_mutation_category", category)
            .putFloat("last_mutation_amount", amount.toFloat())
            .apply()
        _lastMutationAction.value = action
        _lastMutationName.value = name
        _lastMutationCategory.value = category
        _lastMutationAmount.value = amount
    }

    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    init {
        _isNetworkActive.value = isNetworkAvailable(getApplication())
        registerNetworkCallback()
        
        viewModelScope.launch {
            currentWorkspace.collect { ws ->
                _budgetIncome.value = ws.budgetIncome
                _budgetExpense.value = ws.budgetExpense
                _budgetSavings.value = ws.budgetSavings
                
                _profileName.value = ws.profileName
                _profileEmail.value = ws.profileEmail
                _profilePhone.value = ws.profilePhone
                _profileSocial.value = ws.profileSocial
                _profileAddress.value = ws.profileAddress
                _profilePhotoUri.value = ws.profilePhotoUri

                // Keep SharedPreferences in perfect sync with the Workspace Database record!
                val wsId = ws.id
                val cachedPrefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                cachedPrefs.edit().apply {
                    putString(getProfileKey("user_name", wsId), ws.profileName)
                    putString(getProfileKey("user_email", wsId), ws.profileEmail)
                    putString(getProfileKey("user_photo", wsId), ws.profilePhotoUri)
                    putString(getProfileKey("user_phone", wsId), ws.profilePhone)
                    putString(getProfileKey("user_social", wsId), ws.profileSocial)
                    putString(getProfileKey("user_address", wsId), ws.profileAddress)
                    apply()
                }

                // Immediately refresh all home screen widgets with the updated workspace profile!
                com.example.widget.updateAllWidgets(getApplication())
            }
        }

        val cachedPrefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        _lastMutationAction.value = cachedPrefs.getString("last_mutation_action", null)
        _lastMutationName.value = cachedPrefs.getString("last_mutation_name", null)
        _lastMutationCategory.value = cachedPrefs.getString("last_mutation_category", null)
        _lastMutationAmount.value = if (cachedPrefs.contains("last_mutation_amount")) cachedPrefs.getFloat("last_mutation_amount", 0f).toDouble() else null

        viewModelScope.launch {
            try {
                val list = repository.allWorkspaces.first()
                if (list.isEmpty()) {
                    repository.insertWorkspace(com.example.data.Workspace(id = "default", name = "ব্যক্তিগত"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            checkDebtReminders()
        }
    }

    private fun registerNetworkCallback() {
        try {
            val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (connectivityManager != null) {
                val request = android.net.NetworkRequest.Builder()
                    .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        super.onAvailable(network)
                        _isNetworkActive.value = true
                        // Internet is back! If there are unsaved changes, trigger sync
                        if (_hasUnsavedChanges.value && _isGoogleSignedIn.value) {
                            uploadToFirestore()
                        }
                    }
                    override fun onLost(network: android.net.Network) {
                        super.onLost(network)
                        _isNetworkActive.value = false
                    }
                }
                connectivityManager.registerNetworkCallback(request, networkCallback!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unregisterNetworkCallback() {
        try {
            val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (connectivityManager != null && networkCallback != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }

    private fun updateSyncSuccess(context: Context, isUpload: Boolean) {
        val now = System.currentTimeMillis()
        _lastSyncTime.value = now
        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_firestore_sync_time", now).apply()
        
        val isBn = _language.value == AppLanguage.BN
        val title = if (isBn) "ক্লাউড সিঙ্ক সম্পন্ন" else "Cloud Sync Completed"
        val message = if (isBn) {
            if (isUpload) "আপনার তথ্য ক্লাউডে সফলভাবে সংরক্ষিত হয়েছে!" else "নতুন তথ্য ক্লাউড থেকে সফলভাবে রিস্টোর হয়েছে!"
        } else {
            if (isUpload) "Your data has been successfully saved to the cloud!" else "New data has been successfully restored from the cloud!"
        }
        triggerCustomNotification(message, isSuccess = true, type = "SYNC")
    }

    private var firestore: com.google.firebase.firestore.FirebaseFirestore? = null

    private fun getFirestore(context: Context): com.google.firebase.firestore.FirebaseFirestore {
        if (firestore == null) {
            try {
                if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setProjectId(BuildConfig.Firestore_Project_ID.ifBlank { "financenote-dc6f8" })
                        .setApplicationId(BuildConfig.Firestore_APP_ID.ifBlank { "1:549900777284:android:b661159d57ed30542bc911" })
                        .setApiKey("AIzaSyCngAmaOYL3jzyZj9JFKrmaYSkaNA5uIHQ")
                        .build()
                    com.google.firebase.FirebaseApp.initializeApp(context, options)
                }
                firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        return firestore ?: com.google.firebase.firestore.FirebaseFirestore.getInstance()
    }

    private suspend fun getFullBackupData(): FinanceBackup {
        return repository.getBackupData()
    }

    fun checkUnsavedChanges() {
        val email = _googleEmail.value
        if (!_isGoogleSignedIn.value || email.isNullOrBlank()) {
            _hasUnsavedChanges.value = false
            return
        }
        viewModelScope.launch {
            try {
                val currentData = getFullBackupData()
                val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                val cachedJson = prefs.getString("firestore_cached_data_$email", null)
                if (cachedJson == null) {
                    val hasData = currentData.transactions.isNotEmpty() ||
                            currentData.persons.isNotEmpty() ||
                            currentData.savingsGoals.isNotEmpty()
                    _hasUnsavedChanges.value = hasData
                } else {
                    val cachedData = try { backupAdapter.fromJson(cachedJson) } catch (e: Exception) { null }
                    if (cachedData == null) {
                        _hasUnsavedChanges.value = true
                    } else {
                        val isDifferent = currentData.transactions.size != cachedData.transactions.size ||
                                currentData.persons.size != cachedData.persons.size ||
                                currentData.savingsGoals.size != cachedData.savingsGoals.size ||
                                currentData.savingsTransactions.size != cachedData.savingsTransactions.size ||
                                backupAdapter.toJson(currentData) != backupAdapter.toJson(cachedData)
                        _hasUnsavedChanges.value = isDifferent
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun onLocalDatabaseChanged() {
        if (_isGoogleSignedIn.value && !_googleEmail.value.isNullOrBlank()) {
            checkUnsavedChanges()
            uploadToFirestore()
        } else {
            _hasUnsavedChanges.value = false
            _firestoreSyncStatus.value = null
        }
        com.example.widget.updateAllWidgets(getApplication())
    }

    private var uploadJob: kotlinx.coroutines.Job? = null
    
    fun uploadToFirestore(onComplete: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        val email = _googleEmail.value
        if (email.isNullOrBlank() || !_isGoogleSignedIn.value) {
            _firestoreSyncStatus.value = "Sign-in required"
            onError?.invoke("Not signed in to Google")
            return
        }
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Debounce rapid edits
            
            try {
                val backupData = getFullBackupData()
                val json = backupAdapter.toJson(backupData)
                
                val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                val cachedJson = prefs.getString("firestore_cached_data_$email", null)
                
                if (json == cachedJson) {
                    _hasUnsavedChanges.value = false
                    _firestoreSyncStatus.value = null
                    onComplete?.invoke()
                    return@launch
                }

                _firestoreSyncStatus.value = "Syncing..."
                val encryptedJson = BackupEncryptionHelper.encrypt(json)
                val db = getFirestore(getApplication())
                val data = mapOf(
                    "backupJson" to encryptedJson,
                    "updatedAt" to System.currentTimeMillis()
                )

                db.collection("users").document(email).set(data)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            try {
                                val currentData = getFullBackupData()
                                val currentJson = backupAdapter.toJson(currentData)
                                val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                                prefs.edit().putString("firestore_cached_data_$email", currentJson).apply()
                            } catch (e: Exception) { e.printStackTrace() }
                            _hasUnsavedChanges.value = false
                            _firestoreSyncStatus.value = "Synced"
                            updateSyncSuccess(getApplication(), true)
                        }
                    }
                    .addOnFailureListener { e ->
                        _firestoreSyncStatus.value = "Failed"
                        onError?.invoke(e.localizedMessage ?: "Unknown Firestore error")
                    }
            } catch (e: Exception) {
                _firestoreSyncStatus.value = "Error"
                onError?.invoke(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun pullFromFirestore(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = _googleEmail.value
        if (email.isNullOrBlank() || !_isGoogleSignedIn.value) {
            _firestoreSyncStatus.value = "Sign-in required"
            onError("Not signed in to Google")
            return
        }
        viewModelScope.launch {
            _firestoreSyncStatus.value = "Downloading..."
            try {
                val db = getFirestore(getApplication())
                db.collection("users").document(email).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val json = document.getString("backupJson") ?: ""
                            if (json.isNotEmpty()) {
                                viewModelScope.launch {
                                    try {
                                        val decryptedJson = BackupEncryptionHelper.decrypt(json)
                                        val backupData = backupAdapter.fromJson(decryptedJson)
                                        if (backupData != null) {
                                            restoreFullBackup(backupData)
                                            com.example.widget.updateAllWidgets(getApplication())
                                            try {
                                                val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                                                prefs.edit().putString("firestore_cached_data_$email", decryptedJson).apply()
                                            } catch (e: Exception) { e.printStackTrace() }
                                            _hasUnsavedChanges.value = false
                                            _firestoreSyncStatus.value = "Synced"
                                            updateSyncSuccess(getApplication(), false)
                                            onSuccess()
                                        } else {
                                            onError("Invalid backup format")
                                        }
                                    } catch (e: Exception) {
                                        onError(e.localizedMessage ?: "Restore failed")
                                    }
                                }
                            } else {
                                onError("No backup data found on server")
                            }
                        } else {
                            onError("No Firestore document found")
                        }
                    }
                    .addOnFailureListener { e ->
                        _firestoreSyncStatus.value = "Failed"
                        onError(e.localizedMessage ?: "Download failed")
                    }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Initialization failed")
            }
        }
    }

    private var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun startRealtimeSync() {
        firestoreListener?.remove()
        val email = _googleEmail.value
        if (email.isNullOrBlank() || !_isGoogleSignedIn.value) {
            _firestoreSyncStatus.value = null
            return
        }
        try {
            val db = getFirestore(getApplication())
            firestoreListener = db.collection("users").document(email)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        error.printStackTrace()
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val remoteJson = snapshot.getString("backupJson") ?: ""
                        if (remoteJson.isNotEmpty()) {
                            viewModelScope.launch {
                                try {
                                    val currentLocalData = getFullBackupData()
                                    val prefs = getApplication<Application>().getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                                    val cachedJson = prefs.getString("firestore_cached_data_$email", null)

                                    var hasUnsaved = false
                                    if (cachedJson == null) {
                                        hasUnsaved = currentLocalData.transactions.isNotEmpty() ||
                                            currentLocalData.persons.isNotEmpty() ||
                                            currentLocalData.savingsGoals.isNotEmpty()
                                    } else {
                                        val cachedData = try { backupAdapter.fromJson(cachedJson) } catch (e: Exception) { null }
                                        if (cachedData == null) {
                                            hasUnsaved = true
                                        } else {
                                            hasUnsaved = currentLocalData.transactions.size != cachedData.transactions.size ||
                                                    currentLocalData.persons.size != cachedData.persons.size ||
                                                    currentLocalData.savingsGoals.size != cachedData.savingsGoals.size ||
                                                    currentLocalData.savingsTransactions.size != cachedData.savingsTransactions.size ||
                                                    backupAdapter.toJson(currentLocalData) != backupAdapter.toJson(cachedData)
                                        }
                                    }

                                    if (hasUnsaved) {
                                        _hasUnsavedChanges.value = true
                                        
                                        // Show dialog ONLY if we have a real conflict (local has data and remote is different)
                                        // But only if we don't have a cached version yet (first sync after sign-in)
                                        if (cachedJson == null) {
                                            val decryptedJson = BackupEncryptionHelper.decrypt(remoteJson)
                                            val remoteData = try { backupAdapter.fromJson(decryptedJson) } catch (e: Exception) { null }
                                            if (remoteData != null) {
                                                _pendingCloudData.value = remoteData
                                                _showCloudDataFoundDialog.value = true
                                            }
                                        }
                                        return@launch // Do not overwrite local data if we have offline changes
                                    }

                                    val decryptedJson = BackupEncryptionHelper.decrypt(remoteJson)
                                    val remoteData = backupAdapter.fromJson(decryptedJson)
                                    if (remoteData != null) {
                                        val localTxCount = currentLocalData.transactions.size
                                        val remoteTxCount = remoteData.transactions.size
                                        
                                        val currentJson = backupAdapter.toJson(currentLocalData)
                                        if (currentJson != decryptedJson) {
                                            // Data is different, check if remote is newer or just different
                                            // For simplicity, if remote is different and local is same as last cached, we restore
                                            if (cachedJson == null || cachedJson == backupAdapter.toJson(currentLocalData)) {
                                                restoreFullBackup(remoteData)
                                                com.example.widget.updateAllWidgets(getApplication())
                                                _firestoreSyncStatus.value = "Synced"
                                            } else {
                                                // Local has changes that are not in cloud yet
                                                _hasUnsavedChanges.value = true
                                            }
                                        } else {
                                            // No change needed
                                            _firestoreSyncStatus.value = null // Hide sync status if nothing changed
                                        }
                                        
                                        try {
                                            prefs.edit().putString("firestore_cached_data_$email", decryptedJson).apply()
                                        } catch (e: Exception) { e.printStackTrace() }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } else {
                        // Document doesn't exist on server yet (new signed-in user), upload current local DB as initial backup
                        uploadToFirestore()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRealtimeSync() {
        firestoreListener?.remove()
        firestoreListener = null
        _firestoreSyncStatus.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRealtimeSync()
        unregisterNetworkCallback()
    }

    // Backup & Restore operations
    fun parseBackupJson(json: String): FinanceBackup? {
        return try {
            val decryptedJson = BackupEncryptionHelper.decrypt(json)
            backupAdapter.fromJson(decryptedJson)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCurrentDatabaseBackup(): FinanceBackup {
        return repository.getBackupData()
    }

    fun calculateBackupStats(backup: FinanceBackup): BackupStats {
        val personsList = backup.persons
        val txList = backup.transactions
        val savingsGoals = backup.savingsGoals

        val income = txList.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }

        // Person debts calculation
        val debts = personsList.map { person ->
            val personTx = txList.filter { it.personId == person.id }
            val lent = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
            val borrowed = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
            val repaidPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
            val repaidReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
            val net = (lent + repaidPaid) - (borrowed + repaidReceived)
            net
        }

        val totalOwedToMe = debts.filter { it > 0 }.sum()
        val totalIOwe = debts.filter { it < 0 }.sumOf { -it }

        return BackupStats(
            totalIncome = income,
            totalExpense = expense,
            totalOwedToMe = totalOwedToMe,
            totalIOwe = totalIOwe,
            totalPersons = personsList.size,
            totalCards = savingsGoals.size,
            comment = backup.comment ?: "",
            createdAt = backup.createdAt,
            workspaces = backup.workspaces
        )
    }

    fun exportBackupToUri(context: Context, outputStream: java.io.OutputStream, comment: String = "", workspaceIds: List<String>? = null, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val fullBackup = repository.getBackupData()
                val baseData = if (workspaceIds != null && workspaceIds.isNotEmpty()) {
                    fullBackup.copy(
                        persons = fullBackup.persons.filter { it.workspaceId in workspaceIds },
                        transactions = fullBackup.transactions.filter { it.workspaceId in workspaceIds },
                        savingsGoals = fullBackup.savingsGoals.filter { it.workspaceId in workspaceIds },
                        savingsTransactions = fullBackup.savingsTransactions.filter { it.workspaceId in workspaceIds },
                        workspaces = fullBackup.workspaces.filter { it.id in workspaceIds },
                        monthlyBudgets = fullBackup.monthlyBudgets.filter { it.workspaceId in workspaceIds },
                        comment = comment,
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    fullBackup.copy(comment = comment, createdAt = System.currentTimeMillis())
                }
                
                val backupData = baseData

                
                val json = backupAdapter.indent("  ").toJson(backupData)
                val encryptedJson = BackupEncryptionHelper.encrypt(json)
                outputStream.use { it.write(encryptedJson.toByteArray()) }
                val isBn = _language.value == AppLanguage.BN
                val msg = if (isBn) "আপনার ডাটা সফলভাবে এনক্রিপ্ট করে ব্যাকআপ ফাইল তৈরি করা হয়েছে!" else "Your data has been successfully encrypted and backup file created!"
                triggerCustomNotification(msg, isSuccess = true, type = "BACKUP")
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun importBackupFromUri(context: Context, inputStream: java.io.InputStream, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonContent = inputStream.use { it.bufferedReader().readText() }
                val decryptedJson = BackupEncryptionHelper.decrypt(jsonContent)
                val backupData = backupAdapter.fromJson(decryptedJson)
                if (backupData != null) {
                    restoreFullBackup(backupData)
                    com.example.widget.updateAllWidgets(getApplication())
                    val isBn = _language.value == AppLanguage.BN
                    val msg = if (isBn) "ব্যাকআপ ফাইল থেকে আপনার ডাটা সফলভাবে রিস্টোর করা হয়েছে!" else "Your data has been successfully restored from the backup file!"
                    triggerCustomNotification(msg, isSuccess = true, type = "RESTORE")
                    onSuccess()
                } else {
                    throw Exception("Invalid backup data format")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun exportBackup(context: Context, comment: String = "", workspaceIds: List<String>? = null, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val fullBackup = repository.getBackupData()
                val baseData = if (workspaceIds != null && workspaceIds.isNotEmpty()) {
                    fullBackup.copy(
                        persons = fullBackup.persons.filter { it.workspaceId in workspaceIds },
                        transactions = fullBackup.transactions.filter { it.workspaceId in workspaceIds },
                        savingsGoals = fullBackup.savingsGoals.filter { it.workspaceId in workspaceIds },
                        savingsTransactions = fullBackup.savingsTransactions.filter { it.workspaceId in workspaceIds },
                        workspaces = fullBackup.workspaces.filter { it.id in workspaceIds },
                        monthlyBudgets = fullBackup.monthlyBudgets.filter { it.workspaceId in workspaceIds },
                        comment = comment,
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    fullBackup.copy(comment = comment, createdAt = System.currentTimeMillis())
                }
                
                val backupData = baseData

                
                val json = backupAdapter.indent("  ").toJson(backupData)
                val encryptedJson = BackupEncryptionHelper.encrypt(json)
                
                // Write to local private file financenote_backup.json
                val backupFile = File(context.filesDir, "financenote_backup.json")
                backupFile.writeText(encryptedJson)
                
                val isBn = _language.value == AppLanguage.BN
                val msg = if (isBn) "লোকাল স্টোরেজে ডাটা ব্যাকআপ সফলভাবে সংরক্ষিত হয়েছে!" else "Local storage data backup successfully saved!"
                triggerCustomNotification(msg, isSuccess = true, type = "BACKUP")
                onSuccess(encryptedJson)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun importBackup(context: Context, json: String?, fromLocalFile: Boolean, workspaceIds: List<String>? = null, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val jsonContent = if (fromLocalFile) {
                    val backupFile = File(context.filesDir, "financenote_backup.json")
                    if (backupFile.exists()) {
                        backupFile.readText()
                    } else {
                        throw Exception("No local backup file found")
                    }
                } else {
                    json ?: throw Exception("JSON backup code is empty")
                }
                
                val decryptedJson = BackupEncryptionHelper.decrypt(jsonContent)
                val backupData = backupAdapter.fromJson(decryptedJson)
                if (backupData != null) {
                    if (workspaceIds != null && workspaceIds.isNotEmpty()) {
                        restoreSelectiveBackup(backupData, workspaceIds)
                    } else {
                        restoreFullBackup(backupData)
                    }
                    com.example.widget.updateAllWidgets(getApplication())
                    val isBn = _language.value == AppLanguage.BN
                    val msg = if (isBn) "ব্যাকআপ ডাটা সফলভাবে রিস্টোর করা হয়েছে!" else "Backup data successfully restored!"
                    triggerCustomNotification(msg, isSuccess = true, type = "RESTORE")
                    onSuccess()
                } else {
                    throw Exception("Invalid backup data format")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Google Sign-In & Drive Backup properties
    private val _googleName = MutableStateFlow<String?>(null)
    val googleName: StateFlow<String?> = _googleName.asStateFlow()

    private val _googleEmail = MutableStateFlow<String?>(null)
    val googleEmail: StateFlow<String?> = _googleEmail.asStateFlow()

    private val _googlePhotoUrl = MutableStateFlow<String?>(null)
    val googlePhotoUrl: StateFlow<String?> = _googlePhotoUrl.asStateFlow()

    private val _isGoogleSignedIn = MutableStateFlow(false)
    val isGoogleSignedIn: StateFlow<Boolean> = _isGoogleSignedIn.asStateFlow()

    private val _driveStatusMessage = MutableStateFlow<String?>(null)
    val driveStatusMessage: StateFlow<String?> = _driveStatusMessage.asStateFlow()

    private val _googleDriveFiles = MutableStateFlow<List<GoogleDriveFile>>(emptyList())
    val googleDriveFiles: StateFlow<List<GoogleDriveFile>> = _googleDriveFiles.asStateFlow()

    private val _isFetchingFiles = MutableStateFlow(false)
    val isFetchingFiles: StateFlow<Boolean> = _isFetchingFiles.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastGDriveBackupTime = MutableStateFlow<Long?>(null)
    val lastGDriveBackupTime: StateFlow<Long?> = _lastGDriveBackupTime.asStateFlow()

    private val _autoBackupIntervalDays = MutableStateFlow(-1)
    val autoBackupIntervalDays: StateFlow<Int> = _autoBackupIntervalDays.asStateFlow()

    private val _customNotifications = MutableStateFlow<List<CustomNotification>>(emptyList())
    val customNotifications: StateFlow<List<CustomNotification>> = _customNotifications.asStateFlow()

    fun triggerCustomNotification(message: String, isSuccess: Boolean = true, type: String = "INFO") {
        val newNotification = CustomNotification(message = message, isSuccess = isSuccess, type = type)
        _customNotifications.value = _customNotifications.value + newNotification
        
        // Auto dismiss after 4 seconds
        viewModelScope.launch {
            kotlinx.coroutines.delay(4000)
            dismissCustomNotification(newNotification.id)
        }
    }

    fun dismissCustomNotification(id: Long) {
        _customNotifications.value = _customNotifications.value.filter { it.id != id }
    }

    fun getTrashItemSummary(item: com.example.data.TrashItem, language: com.example.ui.AppLanguage): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
        return try {
            when (item.itemType) {
                "TRANSACTION" -> {
                    transactionAdapter.fromJson(item.itemJson)?.let { t ->
                        val amountLabel = if (language == com.example.ui.AppLanguage.BN) "পরিমাণ: " else "Amount: "
                        val typeLabel = if (language == com.example.ui.AppLanguage.BN) "ধরণ: " else "Type: "
                        val dateLabel = if (language == com.example.ui.AppLanguage.BN) "তারিখ: " else "Date: "
                        
                        val typeText = when(t.type) {
                            "INCOME" -> if (language == com.example.ui.AppLanguage.BN) "আয়" else "Income"
                            "EXPENSE" -> if (language == com.example.ui.AppLanguage.BN) "ব্যয়" else "Expense"
                            "LEND" -> if (language == com.example.ui.AppLanguage.BN) "দেনা (পাওনা হবে)" else "Lend"
                            "BORROW" -> if (language == com.example.ui.AppLanguage.BN) "পাওনা (দেনা হবে)" else "Borrow"
                            "REPAY_PAID" -> if (language == com.example.ui.AppLanguage.BN) "পরিশোধ (প্রদান)" else "Repay Paid"
                            "REPAY_RECEIVED" -> if (language == com.example.ui.AppLanguage.BN) "পরিশোধ (প্রাপ্ত)" else "Repay Received"
                            else -> t.type
                        }
                        
                        val notePart = if (t.note.isNotBlank()) " | ${t.note}" else ""
                        val dateText = sdf.format(java.util.Date(t.timestamp))
                        
                        "$typeLabel$typeText\n$amountLabel${t.amount}\n$dateLabel$dateText$notePart"
                    } ?: item.itemJson
                }
                "PERSON_WITH_TXS" -> {
                    personWithTxAdapter.fromJson(item.itemJson)?.let { pWithTx ->
                        val p = pWithTx.person
                        val txCount = pWithTx.transactions.size
                        val net = pWithTx.transactions.sumOf { tx ->
                            when (tx.type) {
                                "LEND", "REPAY_PAID" -> tx.amount
                                "BORROW", "REPAY_RECEIVED" -> -tx.amount
                                else -> 0.0
                            }
                        }
                        val status = if (net > 0) 
                            (if (language == com.example.ui.AppLanguage.BN) "পাওনা: " else "Receivable: ") + net
                        else if (net < 0)
                            (if (language == com.example.ui.AppLanguage.BN) "দেনা: " else "Payable: ") + (-net)
                        else (if (language == com.example.ui.AppLanguage.BN) "ব্যালেন্স: ০" else "Balance: 0")
                        
                        val txText = if (language == com.example.ui.AppLanguage.BN) " টি লেনদেন" else " transactions"
                        "${p.name} | $status | $txCount$txText"
                    } ?: item.itemJson
                }
                "PERSON" -> {
                    personAdapter.fromJson(item.itemJson)?.let { p ->
                        p.name + (if (p.phone.isNotBlank()) " (${p.phone})" else "")
                    } ?: item.itemJson
                }
                "SAVINGS_GOAL" -> {
                    savingsGoalAdapter.fromJson(item.itemJson)?.let { g ->
                        val target = if (language == com.example.ui.AppLanguage.BN) "লক্ষ্য: " else "Target: "
                        "${g.title} | $target${g.targetAmount}"
                    } ?: item.itemJson
                }
                "SAVINGS_GOAL_WITH_TXS" -> {
                    goalWithTxAdapter.fromJson(item.itemJson)?.let { gWithTx ->
                        val g = gWithTx.goal
                        val txCount = gWithTx.transactions.size
                        val current = gWithTx.transactions.sumOf { it.amount }
                        val target = if (language == com.example.ui.AppLanguage.BN) "লক্ষ্য: " else "Target: "
                        val saved = if (language == com.example.ui.AppLanguage.BN) "জমা: " else "Saved: "
                        val txText = if (language == com.example.ui.AppLanguage.BN) " টি লেনদেন" else " transactions"
                        "${g.title} | $saved$current / $target${g.targetAmount} | $txCount$txText"
                    } ?: item.itemJson
                }
                "GDRIVE_BACKUP" -> {
                    deletedBackupAdapter.fromJson(item.itemJson)?.let { deletedBackup ->
                        backupAdapter.fromJson(deletedBackup.backupJson)?.let { b ->
                            val pCount = b.persons.size
                            val txCount = b.transactions.size
                            val gCount = b.savingsGoals.size
                            val pText = if (language == com.example.ui.AppLanguage.BN) "ব্যক্তি" else "persons"
                            val txText = if (language == com.example.ui.AppLanguage.BN) "লেনদেন" else "transactions"
                            val gText = if (language == com.example.ui.AppLanguage.BN) "লক্ষ্য" else "goals"
                            "$txText: $txCount, $pText: $pCount, $gText: $gCount"
                        }
                    } ?: item.itemJson
                }
                "WORKSPACE" -> {
                    backupAdapter.fromJson(item.itemJson)?.let { backup ->
                        val ws = backup.workspaces.firstOrNull()
                        if (language == com.example.ui.AppLanguage.BN) {
                            "ওয়ার্কস্পেস: ${ws?.name ?: "অজানা"}"
                        } else {
                            "Workspace: ${ws?.name ?: "Unknown"}"
                        }
                    } ?: item.itemJson
                }
                "SAVINGS_TRANSACTION" -> {
                    savingsTransactionAdapter.fromJson(item.itemJson)?.let { st ->
                        val amountLabel = if (language == com.example.ui.AppLanguage.BN) "পরিমাণ: " else "Amount: "
                        val typeLabel = if (language == com.example.ui.AppLanguage.BN) "ধরণ: " else "Type: "
                        val dateLabel = if (language == com.example.ui.AppLanguage.BN) "তারিখ: " else "Date: "
                        
                        val typeText = if (st.isDeposit) 
                            (if (language == com.example.ui.AppLanguage.BN) "জমা" else "Deposit")
                        else 
                            (if (language == com.example.ui.AppLanguage.BN) "উত্তোলন" else "Withdrawal")

                        val dateText = sdf.format(java.util.Date(st.timestamp))
                        val notePart = if (st.note.isNotBlank()) " | ${st.note}" else ""

                        "$typeLabel$typeText\n$amountLabel${st.amount}\n$dateLabel$dateText$notePart"
                    } ?: item.itemJson
                }
                else -> item.itemJson
            }
        } catch (e: Exception) {
            item.itemJson
        }
    }

    fun clearAllNotifications() {
        _customNotifications.value = emptyList()
    }

    private val client = OkHttpClient()

    // Initialize Google State from Shared Prefs
    fun initGoogleDrive(context: Context) {
        val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
        val email = prefs.getString("google_email", null)
        val name = prefs.getString("google_name", null)
        val photoUrl = prefs.getString("google_photo_url", null)

        val lastBackup = prefs.getLong("last_gdrive_backup_time", 0L)
        _lastGDriveBackupTime.value = if (lastBackup == 0L) null else lastBackup
        _autoBackupIntervalDays.value = prefs.getInt("auto_backup_interval_days", -1)

        if (!email.isNullOrEmpty()) {
            _googleEmail.value = email
            _googleName.value = name
            _googlePhotoUrl.value = photoUrl
            _isGoogleSignedIn.value = true
            
            // Trigger auto-backup check
            checkAndTriggerAutoBackup(context)
            checkUnsavedChanges()
        } else {
            _googleEmail.value = null
            _googleName.value = null
            _googlePhotoUrl.value = null
            _isGoogleSignedIn.value = false
        }
        com.example.widget.updateAllWidgets(context)
    }

    fun setAutoBackupIntervalDays(context: Context, days: Int) {
        val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("auto_backup_interval_days", days).apply()
        _autoBackupIntervalDays.value = days
        
        checkAndTriggerAutoBackup(context)
    }

    fun checkAndTriggerAutoBackup(context: Context) {
        val intervalDays = _autoBackupIntervalDays.value
        if (intervalDays <= 0) return // -1 or 0 means Never/off
        if (!_isGoogleSignedIn.value) return // must be signed in
        if (_isSyncing.value) return // already syncing

        val lastBackup = _lastGDriveBackupTime.value ?: 0L
        val currentTime = System.currentTimeMillis()
        val intervalMillis = intervalDays * 24L * 60L * 60L * 1000L

        if (currentTime - lastBackup >= intervalMillis) {
            backupToGoogleDrive(
                context = context,
                customFileName = "finance_note_auto_backup_${java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())}.json",
                comment = "Automatic scheduled backup",
                onSuccess = {},
                onError = {}
            )
        }
    }

    fun handleGoogleSignInSuccess(
        context: Context,
        account: com.google.android.gms.auth.api.signin.GoogleSignInAccount,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val email = account.email
                if (email == null) {
                    throw Exception("Google account email not found")
                }
                
                // Select "default" workspace on Google Sign-In as requested
                selectWorkspace("default")

                val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("google_email", email)
                    .putString("google_name", account.displayName ?: "Google User")
                    .putString("google_photo_url", account.photoUrl?.toString())
                    .putString("last_google_email", email)
                    .apply()
                
                _googleEmail.value = email
                _googleName.value = account.displayName ?: "Google User"
                _googlePhotoUrl.value = account.photoUrl?.toString()
                _isGoogleSignedIn.value = true
                
                // Update profile states as well so they match the Google account immediately
                saveProfile(getApplication(),
                    name = account.displayName ?: "Google User",
                    email = email,
                    photoUri = account.photoUrl?.toString(),
                    phone = _profilePhone.value,
                    social = _profileSocial.value,
                    address = _profileAddress.value
                )






                
                // Proceed normally with automatic realtime sync
                startRealtimeSync()
                checkAndTriggerAutoBackup(context)
                onSuccess()
            } catch (e: Exception) {
                _driveStatusMessage.value = "Sign-In Failed: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private suspend fun getValidAccessToken(context: Context): String? {
        return kotlinx.coroutines.Dispatchers.IO.let { d ->
            kotlinx.coroutines.withContext(d) {
                try {
                    val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
                    val email = prefs.getString("google_email", null) ?: return@withContext null
                    val account = android.accounts.Account(email, "com.google")
                    val scope = "oauth2:https://www.googleapis.com/auth/drive.file"
                    com.google.android.gms.auth.GoogleAuthUtil.getToken(context, account, scope)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    fun backupToGoogleDrive(context: Context, customFileName: String? = null, comment: String = "", workspaceIds: List<String>? = null, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                _driveStatusMessage.value = "Starting cloud backup..."
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                // 1. Get database data JSON string with comment and createdAt metadata
                val fullBackup = repository.getBackupData()
                val baseData = if (workspaceIds != null && workspaceIds.isNotEmpty()) {
                    fullBackup.copy(
                        persons = fullBackup.persons.filter { it.workspaceId in workspaceIds },
                        transactions = fullBackup.transactions.filter { it.workspaceId in workspaceIds },
                        savingsGoals = fullBackup.savingsGoals.filter { it.workspaceId in workspaceIds },
                        savingsTransactions = fullBackup.savingsTransactions.filter { it.workspaceId in workspaceIds },
                        workspaces = fullBackup.workspaces.filter { it.id in workspaceIds },
                        monthlyBudgets = fullBackup.monthlyBudgets.filter { it.workspaceId in workspaceIds },
                        comment = comment,
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    fullBackup.copy(comment = comment, createdAt = System.currentTimeMillis())
                }

                val backupData = baseData


                val json = backupAdapter.indent("  ").toJson(backupData)
                val encryptedJson = BackupEncryptionHelper.encrypt(json)

                // 2. Create timestamp and fileName
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val fileName = customFileName ?: "finance_note_backup_$timestamp.json"

                _driveStatusMessage.value = "Creating cloud backup file..."
                val boundary = "BackupBoundary"
                val multipartBody = buildString {
                    append("--$boundary\r\n")
                    append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                    append("{\"name\": \"$fileName\", \"mimeType\": \"application/json\"}\r\n")
                    append("--$boundary\r\n")
                    append("Content-Type: application/json\r\n\r\n")
                    append(encryptedJson)
                    append("\r\n--$boundary--\r\n")
                }

                val requestBody = multipartBody.toRequestBody("multipart/related; boundary=$boundary".toMediaType())
                val createRequest = Request.Builder()
                    .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                    .header("Authorization", "Bearer $accessToken")
                    .post(requestBody)
                    .build()

                val createResponse = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(createRequest).execute()
                    }
                }

                if (createResponse.isSuccessful) {
                    _driveStatusMessage.value = "Backup successfully created: $fileName"
                    val now = System.currentTimeMillis()
                    val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putLong("last_gdrive_backup_time", now).apply()
                    _lastGDriveBackupTime.value = now
                    onSuccess()
                } else {
                    val errBody = createResponse.body?.string() ?: ""
                    throw Exception("Failed to create backup on Google Drive: $errBody")
                }
            } catch (e: Exception) {
                _driveStatusMessage.value = "Backup Failed: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "Unknown backup error")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun listGoogleDriveFiles(context: Context, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isFetchingFiles.value = true
            try {
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                val request = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files?q=name%20contains%20'finance_note_backup'%20and%20trashed=false&fields=files(id,name,mimeType,createdTime,size)&orderBy=createdTime%20desc")
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val response = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(request).execute()
                    }
                }

                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val filesResponse = moshi.adapter(GoogleDriveFilesResponse::class.java).fromJson(body)
                    _googleDriveFiles.value = filesResponse?.files ?: emptyList()
                    onSuccess()
                } else {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("Failed to list files: $errBody")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            } finally {
                _isFetchingFiles.value = false
            }
        }
    }



    fun deleteGoogleDriveFile(context: Context, fileId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                // Fetch metadata for file name
                val metaRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId?fields=name")
                    .header("Authorization", "Bearer $accessToken")
                    .get()
                    .build()
                
                var fileName = "Deleted Backup"
                var backupJson = ""

                kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        try {
                            val metaResponse = client.newCall(metaRequest).execute()
                            if (metaResponse.isSuccessful) {
                                val metaStr = metaResponse.body?.string() ?: "{}"
                                val metaJsonObj = org.json.JSONObject(metaStr)
                                fileName = metaJsonObj.optString("name", "Deleted Backup")
                            }
                            
                            val downloadRequest = Request.Builder()
                                .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                                .header("Authorization", "Bearer $accessToken")
                                .get()
                                .build()
                            val dlResponse = client.newCall(downloadRequest).execute()
                            if (dlResponse.isSuccessful) {
                                backupJson = dlResponse.body?.string() ?: ""
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }

                if (backupJson.isNotEmpty()) {
                    val deletedBackup = com.example.data.DeletedGDriveBackup(fileId, fileName, backupJson)
                    repository.insertTrashItem(com.example.data.TrashItem(
                        originalId = 0,
                        itemType = "GDRIVE_BACKUP",
                        itemJson = deletedBackupAdapter.toJson(deletedBackup)
                    ))
                }

                val request = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId")
                    .delete()
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val response = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(request).execute()
                    }
                }

                if (response.isSuccessful) {
                    listGoogleDriveFiles(context)
                    triggerCustomNotification(if (_language.value == com.example.ui.AppLanguage.BN) "ফাইলটি ট্র্যাশে সরানো হয়েছে" else "File moved to trash", isSuccess = true, type = "SUCCESS")
                    onSuccess()
                } else {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("Failed to delete file: $errBody")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    fun downloadGoogleDriveFile(context: Context, fileId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                _driveStatusMessage.value = "Downloading selected backup file..."
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                val downloadRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val downloadResponse = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(downloadRequest).execute()
                    }
                }

                if (downloadResponse.isSuccessful) {
                    val jsonContent = downloadResponse.body?.string() ?: ""
                    onSuccess(jsonContent)
                } else {
                    val errBody = downloadResponse.body?.string() ?: ""
                    throw Exception("Failed to download file from Google Drive: $errBody")
                }
            } catch (e: Exception) {
                _driveStatusMessage.value = "Download Failed: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "Unknown restore error")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun restoreFromGoogleDriveFile(context: Context, fileId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _driveStatusMessage.value = "Downloading selected backup file..."
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                val downloadRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val downloadResponse = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(downloadRequest).execute()
                    }
                }

                if (downloadResponse.isSuccessful) {
                    val jsonContent = downloadResponse.body?.string() ?: ""
                    _driveStatusMessage.value = "Restoring database content..."
                    val decryptedJson = BackupEncryptionHelper.decrypt(jsonContent)
                    val backupData = backupAdapter.fromJson(decryptedJson)
                    if (backupData != null) {
                       restoreFullBackup(backupData)
                       com.example.widget.updateAllWidgets(getApplication())
                       _driveStatusMessage.value = "Restore successfully completed!"
                       onSuccess()
                    } else {
                       throw Exception("Downloaded backup file format is invalid")
                    }
                } else {
                    val errBody = downloadResponse.body?.string() ?: ""
                    throw Exception("Failed to download file from Google Drive: $errBody")
                }
            } catch (e: Exception) {
                _driveStatusMessage.value = "Restore Failed: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "Unknown restore error")
            }
        }
    }

    fun restoreFromGoogleDrive(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _driveStatusMessage.value = "Searching for backup on cloud..."
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                val searchRequest = Request.Builder()
                    .url("https://www.googleapis.com/drive/v3/files?q=name%20contains%20'finance_note_backup'%20and%20trashed=false&fields=files(id,name)&orderBy=createdTime%20desc")
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val searchResponse = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(searchRequest).execute()
                    }
                }

                var existingFileId: String? = null
                if (searchResponse.isSuccessful) {
                    val searchBody = searchResponse.body?.string() ?: ""
                    val listType = com.squareup.moshi.Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
                    val mapAdapter = moshi.adapter<Map<String, Any>>(listType)
                    val parsed = mapAdapter.fromJson(searchBody)
                    val filesList = parsed?.get("files") as? List<*>
                    if (!filesList.isNullOrEmpty()) {
                        val firstFile = filesList[0] as? Map<*, *>
                        existingFileId = firstFile?.get("id") as? String
                    }
                }

                if (existingFileId == null) {
                    throw Exception("No backup file found on Google Drive!")
                }

                restoreFromGoogleDriveFile(context, existingFileId, onSuccess, onError)
            } catch (e: Exception) {
                _driveStatusMessage.value = "Restore Failed: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "Unknown restore error")
            }
        }
    }

    fun confirmCloudSync(context: Context, backupLocalFirst: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            val dataToRestore = _pendingCloudData.value ?: return@launch
            
            if (backupLocalFirst) {
                // Create a backup file in Google Drive with timestamp
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                backupToGoogleDrive(
                    context = context,
                    customFileName = "finance_note_backup_pre_sync_$timestamp",
                    comment = "Auto-backup before cloud sync",
                    onSuccess = {
                        // After successful backup, restore from cloud
                        viewModelScope.launch {
                            restoreFullBackup(dataToRestore)
                            com.example.widget.updateAllWidgets(context)
                            val email = _googleEmail.value
                            if (email != null) {
                                try {
                                    val decryptedJson = backupAdapter.toJson(dataToRestore)
                                    context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                                        .edit().putString("firestore_cached_data_$email", decryptedJson).apply()
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                            startRealtimeSync()
                            dismissCloudDataFoundDialog()
                            onComplete()
                        }
                    },
                    onError = {
                        // If backup fails, we should probably warn or still proceed depending on risk
                        // For now let's just proceed to restore as requested
                        viewModelScope.launch {
                            restoreFullBackup(dataToRestore)
                            com.example.widget.updateAllWidgets(context)
                            startRealtimeSync()
                            dismissCloudDataFoundDialog()
                            onComplete()
                        }
                    }
                )
            } else {
                com.example.widget.updateAllWidgets(context)
                val email = _googleEmail.value
                if (email != null) {
                    try {
                        val decryptedJson = backupAdapter.toJson(dataToRestore)
                        context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
                            .edit().putString("firestore_cached_data_$email", decryptedJson).apply()
                    } catch (e: Exception) { e.printStackTrace() }
                }
                startRealtimeSync()
                dismissCloudDataFoundDialog()
                onComplete()
            }
        }
    }

    fun skipCloudSyncAndOverwrite(context: Context) {
        viewModelScope.launch {
            uploadToFirestore(
                onComplete = {
                    startRealtimeSync()
                    dismissCloudDataFoundDialog()
                },
                onError = {
                    startRealtimeSync()
                    dismissCloudDataFoundDialog()
                }
            )
        }
    }

    fun performAutoBackupAndSignOut(context: Context, profileName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val safeName = profileName.replace(" ", "_").ifBlank { "User" }
                val fileName = "Auto_backup_on_signout_${timestamp}_${safeName}.json".replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                val comment = "Auto backup on signout"

                // 1. Local Storage Auto Backup
                val backupData = repository.getBackupData().copy(comment = comment, createdAt = System.currentTimeMillis())
                val json = backupAdapter.indent("  ").toJson(backupData)
                val encryptedJson = BackupEncryptionHelper.encrypt(json)

                // Save to app external files dir (accessible local storage on phone)
                val externalDir = context.getExternalFilesDir(null) ?: context.filesDir
                val backupFile = File(externalDir, fileName)
                backupFile.writeText(encryptedJson)

                // Also write to standard internal financenote_backup.json for fallback restore
                val standardBackupFile = File(context.filesDir, "financenote_backup.json")
                standardBackupFile.writeText(encryptedJson)

                // 2. Google Drive Auto Backup (if signed in)
                if (_isGoogleSignedIn.value) {
                    backupToGoogleDrive(
                        context = context,
                        customFileName = fileName,
                        comment = comment,
                        onSuccess = {
                            signOutFromGoogle(context, onSuccess)
                        },
                        onError = {
                            signOutFromGoogle(context, onSuccess)
                        }
                    )
                } else {
                    signOutFromGoogle(context, onSuccess)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                signOutFromGoogle(context, onSuccess)
            }
        }
    }

    fun signOutFromGoogle(context: Context, onSuccess: () -> Unit) {
        val googlePrefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
        val currentEmail = googlePrefs.getString("google_email", null)
        googlePrefs.edit()
            .remove("google_email")
            .remove("google_name")
            .remove("google_photo_url")
            .apply()
        
        if (!currentEmail.isNullOrBlank()) {
            googlePrefs.edit().putString("last_google_email", currentEmail).apply()
        }
        
        // Clear main profile data as well
        val profilePrefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        profilePrefs.edit()
            .remove("user_name")
            .remove("user_email")
            .remove("user_photo")
            .remove("user_phone")
            .remove("user_social")
            .remove("user_address")
            .apply()

        _profileName.value = ""
        _profileEmail.value = ""
        _profilePhotoUri.value = null
        _profilePhone.value = ""
        _profileSocial.value = ""
        _profileAddress.value = ""

        _googleEmail.value = null
        _googleName.value = null
        _googlePhotoUrl.value = null
        _isGoogleSignedIn.value = false
        _driveStatusMessage.value = "Signed Out"
        com.example.widget.updateAllWidgets(context)
        stopRealtimeSync()
        onSuccess()
    }

    fun clearAllDataLocal(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                restoreFullBackup(FinanceBackup(emptyList(), emptyList(), emptyList(), emptyList()))
                com.example.widget.updateAllWidgets(getApplication())
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun checkDebtReminders() {
        viewModelScope.launch {
            val allPersons = repository.allPersons.first()
            val allTransactions = repository.allTransactions.first()
            val language = _language.value
            
            val eligiblePersons = allPersons.filter { person ->
                val personTx = allTransactions.filter { it.personId == person.id }
                if (personTx.isEmpty()) return@filter false
                
                val oldestTx = personTx.minByOrNull { it.timestamp } ?: return@filter false
                val daysOld = (System.currentTimeMillis() - oldestTx.timestamp) / (1000L * 60 * 60 * 24)
                
                if (daysOld < 7) return@filter false
                
                val lent = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
                val borrowed = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                val repaidPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                val repaidReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                val net = (lent + repaidPaid) - (borrowed + repaidReceived)
                
                if (kotlin.math.abs(net) < 0.01) return@filter false
                
                val log = repository.getDebtNotificationLog(person.id)
                val lastNotified = log?.lastNotifiedAt ?: 0L
                val daysSinceLastNotify = (System.currentTimeMillis() - lastNotified) / (1000L * 60 * 60 * 24)
                
                daysSinceLastNotify >= 3
            }
            
            if (eligiblePersons.isNotEmpty()) {
                val personToNotify = eligiblePersons.random()
                
                val personTx = allTransactions.filter { it.personId == personToNotify.id }
                val lent = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
                val borrowed = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                val repaidPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                val repaidReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                val net = (lent + repaidPaid) - (borrowed + repaidReceived)
                
                val message = if (net > 0) {
                    if (language == AppLanguage.BN) {
                        "${personToNotify.name} এর কাছে ${String.format("%.2f", net)} টাকা পাওনা আছে, আদায় করুন।"
                    } else {
                        "You have a receivable of ${String.format("%.2f", net)} from ${personToNotify.name}, please collect."
                    }
                } else {
                    if (language == AppLanguage.BN) {
                        "${personToNotify.name} এর কাছে ${String.format("%.2f", -net)} টাকা দেনা আছে, পরিশোধ করুন।"
                    } else {
                        "You owe ${String.format("%.2f", -net)} to ${personToNotify.name}, please pay."
                    }
                }
                
                sendDebtSystemNotification(message)
                repository.insertDebtNotificationLog(com.example.data.DebtNotificationLog(personToNotify.id, System.currentTimeMillis()))
            }
        }
    }

    private fun sendDebtSystemNotification(message: String) {
        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "debt_reminders_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                if (_language.value == AppLanguage.BN) "পাওনা/দেনা রিমাইন্ডার" else "Debt Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = "ACTION_DEBT_CREDIT"
        }
        val pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_pie_chart)
            .setContentTitle(if (_language.value == AppLanguage.BN) "টাকা আদায়/পরিশোধের রিমাইন্ডার" else "Debt/Credit Reminder")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

class FinanceViewModelFactory(private val repository: FinanceRepository, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
