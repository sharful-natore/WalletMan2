package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Person
import com.example.data.Transaction
import com.example.data.SavingsGoal
import com.example.data.SavingsTransaction
import com.example.data.FinanceRepository
import com.example.data.FinanceBackup
import com.example.ui.AppLanguage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class PersonDebt(
    val person: Person,
    val netBalance: Double, // positive = owed to me (pawn), negative = I owe (dena)
    val totalLent: Double,
    val totalBorrowed: Double,
    val totalRepaidPaid: Double,
    val totalRepaidReceived: Double
)

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // Preferences & UI State
    private val _language = MutableStateFlow(AppLanguage.BN) // Default to Bengali
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true) // Default to dark theme for premium fintech feel
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Profile Settings States
    private val _profileName = MutableStateFlow("Rahad Ahmed")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow("shorifbd24@gmail.com")
    val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profilePhotoUri = MutableStateFlow<String?>(null)
    val profilePhotoUri: StateFlow<String?> = _profilePhotoUri.asStateFlow()

    // Moshi JSON adapter configuration
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(FinanceBackup::class.java)

    // Database Streams
    val persons: StateFlow<List<Person>> = repository.allPersons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Streams for Debts & Credits
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
            // INCOME is general cash-in.
            txList.filter { it.type == "INCOME" }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactions
        .combine(personDebts) { txList, debts ->
            // EXPENSE is general cash-out.
            txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOwedToMe: StateFlow<Double> = personDebts
        .combine(transactions) { debts, _ ->
            debts.filter { it.netBalance > 0 }.sumOf { it.netBalance }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIOwe: StateFlow<Double> = personDebts
        .combine(transactions) { debts, _ ->
            debts.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = combine(transactions, totalOwedToMe, totalIOwe) { txList, _, _ ->
        val income = txList.filter { it.type == "INCOME" }.sumOf { it.amount }
        val borrowed = txList.filter { it.type == "BORROW" }.sumOf { it.amount }
        val repaidReceived = txList.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }

        val expense = txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val lent = txList.filter { it.type == "LEND" }.sumOf { it.amount }
        val repaidPaid = txList.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }

        (income + borrowed + repaidReceived) - (expense + lent + repaidPaid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User Actions / Intents
    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun addPerson(name: String, phone: String, address: String, photoUri: String) {
        viewModelScope.launch {
            repository.insertPerson(Person(name = name, phone = phone, address = address, photoUri = photoUri))
        }
    }

    fun deletePerson(id: Int) {
        viewModelScope.launch {
            repository.deletePerson(id)
        }
    }

    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String,
        personId: Int?
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    personId = personId,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun getTransactionsByPerson(personId: Int): kotlinx.coroutines.flow.Flow<List<Transaction>> {
        return repository.getTransactionsByPerson(personId)
    }

    fun addSavingsGoal(title: String, targetAmount: Double, category: String, colorIndex: Int) {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoal(
                    title = title,
                    targetAmount = targetAmount,
                    savedAmount = 0.0,
                    category = category,
                    colorIndex = colorIndex
                )
            )
        }
    }

    fun getSavingsTransactions(goalId: Int): kotlinx.coroutines.flow.Flow<List<SavingsTransaction>> {
        return repository.getSavingsTransactionsByGoal(goalId)
    }

    fun addSavingsContribution(id: Int, contribution: Double) {
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
                        isDeposit = isDeposit
                    )
                )
            }
        }
    }

    fun deleteSavingsGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.insertSavingsGoal(goal)
        }
    }

    // Profile Settings Helpers
    fun loadProfile(context: Context) {
        val prefs = context.getSharedPreferences("sanchay_prefs", Context.MODE_PRIVATE)
        _profileName.value = prefs.getString("user_name", "Rahad Ahmed") ?: "Rahad Ahmed"
        _profileEmail.value = prefs.getString("user_email", "shorifbd24@gmail.com") ?: "shorifbd24@gmail.com"
        _profilePhotoUri.value = prefs.getString("user_photo", null)
    }

    fun saveProfile(context: Context, name: String, email: String, photoUri: String? = null) {
        val prefs = context.getSharedPreferences("sanchay_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_photo", photoUri)
            .apply()
        _profileName.value = name
        _profileEmail.value = email
        _profilePhotoUri.value = photoUri
    }

    // Backup & Restore operations
    fun exportBackup(context: Context, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupData = repository.getBackupData()
                val json = backupAdapter.indent("  ").toJson(backupData)
                
                // Write to local private file sanchay_backup.json
                val backupFile = File(context.filesDir, "sanchay_backup.json")
                backupFile.writeText(json)
                
                onSuccess(json)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun importBackup(context: Context, json: String?, fromLocalFile: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonContent = if (fromLocalFile) {
                    val backupFile = File(context.filesDir, "sanchay_backup.json")
                    if (backupFile.exists()) {
                        backupFile.readText()
                    } else {
                        throw Exception("No local backup file found")
                    }
                } else {
                    json ?: throw Exception("JSON backup code is empty")
                }
                
                val backupData = backupAdapter.fromJson(jsonContent)
                if (backupData != null) {
                    repository.restoreBackupData(backupData)
                    onSuccess()
                } else {
                    throw Exception("Invalid backup data format")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
