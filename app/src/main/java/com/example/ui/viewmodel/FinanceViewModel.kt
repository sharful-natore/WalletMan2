package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
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
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.InputStream

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

    private val _isDarkTheme = MutableStateFlow(false) // Default to light theme as requested
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Profile Settings States
    private val _profileName = MutableStateFlow("Shariful Islam")
    val profileName: StateFlow<String> = _profileName.asStateFlow()

    private val _profileEmail = MutableStateFlow("connect.shariful@gmail.com")
    val profileEmail: StateFlow<String> = _profileEmail.asStateFlow()

    private val _profilePhotoUri = MutableStateFlow<String?>(null)
    val profilePhotoUri: StateFlow<String?> = _profilePhotoUri.asStateFlow()

    private val _profilePhone = MutableStateFlow("01768899599")
    val profilePhone: StateFlow<String> = _profilePhone.asStateFlow()

    private val _profileSocial = MutableStateFlow("connect.shariful@gmail.com")
    val profileSocial: StateFlow<String> = _profileSocial.asStateFlow()

    private val _profileAddress = MutableStateFlow("Parkol, Baraigram, Natore")
    val profileAddress: StateFlow<String> = _profileAddress.asStateFlow()

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
    fun toggleLanguage(context: Context) {
        val newLang = if (_language.value == AppLanguage.BN) AppLanguage.EN else AppLanguage.BN
        _language.value = newLang
        context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language", newLang.name)
            .apply()
    }

    fun toggleTheme(context: Context) {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_dark_theme", newTheme)
            .apply()
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
        personId: Int?,
        timestamp: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    personId = personId,
                    timestamp = timestamp
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

    fun addSavingsGoal(title: String, targetAmount: Double, category: String, colorIndex: Int, cardholderName: String = "") {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoal(
                    title = title,
                    targetAmount = targetAmount,
                    savedAmount = 0.0,
                    category = category,
                    colorIndex = colorIndex,
                    cardholderName = cardholderName
                )
            )
        }
    }

    fun getSavingsTransactions(goalId: Int): kotlinx.coroutines.flow.Flow<List<SavingsTransaction>> {
        return repository.getSavingsTransactionsByGoal(goalId)
    }

    fun addSavingsContribution(id: Int, contribution: Double, note: String = "") {
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
                        note = note
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

    private fun saveImageToInternalStorage(context: Context, uriString: String): String? {
        return try {
            if (!uriString.startsWith("content://")) return uriString
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
            null
        }
    }

    // Profile Settings Helpers
    fun loadProfile(context: Context) {
        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        _profileName.value = prefs.getString("user_name", "Shariful Islam") ?: "Shariful Islam"
        _profileEmail.value = prefs.getString("user_email", "connect.shariful@gmail.com") ?: "connect.shariful@gmail.com"
        _profilePhotoUri.value = prefs.getString("user_photo", null)
        _profilePhone.value = prefs.getString("user_phone", "01768899599") ?: "01768899599"
        _profileSocial.value = prefs.getString("user_social", "connect.shariful@gmail.com") ?: "connect.shariful@gmail.com"
        _profileAddress.value = prefs.getString("user_address", "Parkol, Baraigram, Natore") ?: "Parkol, Baraigram, Natore"
        
        // Load language and theme, defaulting language to BN and theme to false (Light)
        val savedLangStr = prefs.getString("app_language", AppLanguage.BN.name) ?: AppLanguage.BN.name
        _language.value = try { AppLanguage.valueOf(savedLangStr) } catch (e: Exception) { AppLanguage.BN }
        _isDarkTheme.value = prefs.getBoolean("is_dark_theme", false)
    }

    fun saveProfile(context: Context, name: String, email: String, photoUri: String? = null, phone: String = "", social: String = "", address: String = "") {
        val finalPhotoUri = photoUri?.let { saveImageToInternalStorage(context, it) } ?: photoUri
        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_photo", finalPhotoUri)
            .putString("user_phone", phone)
            .putString("user_social", social)
            .putString("user_address", address)
            .apply()
        _profileName.value = name
        _profileEmail.value = email
        _profilePhotoUri.value = finalPhotoUri
        _profilePhone.value = phone
        _profileSocial.value = social
        _profileAddress.value = address
    }

    // Backup & Restore operations
    fun exportBackupToUri(context: Context, outputStream: java.io.OutputStream, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupData = repository.getBackupData()
                val json = backupAdapter.indent("  ").toJson(backupData)
                outputStream.use { it.write(json.toByteArray()) }
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

    fun exportBackup(context: Context, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupData = repository.getBackupData()
                val json = backupAdapter.indent("  ").toJson(backupData)
                
                // Write to local private file financenote_backup.json
                val backupFile = File(context.filesDir, "financenote_backup.json")
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
                    val backupFile = File(context.filesDir, "financenote_backup.json")
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
