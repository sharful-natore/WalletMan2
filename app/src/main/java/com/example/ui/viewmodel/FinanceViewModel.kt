package com.example.ui.viewmodel

import com.example.BuildConfig
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import android.Manifest
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

    private val _isNotificationEnabled = MutableStateFlow(true) // Default to enabled
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

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
            repository.insertPerson(Person(name = name, phone = phone, address = address, photoUri = photoUri))
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            repository.updatePerson(person)
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

        // Load notification setting
        var notifEnabled = prefs.getBoolean("notification_enabled", true)
        
        // Ensure it's disabled if permission is missing (Android 13+)
        if (notifEnabled && android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifEnabled = false
            }
        }
        
        _isNotificationEnabled.value = notifEnabled

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

    private val client = OkHttpClient()

    // Initialize Google State from Shared Prefs
    fun initGoogleDrive(context: Context) {
        val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
        val refreshToken = prefs.getString("google_refresh_token", null)
        val email = prefs.getString("google_email", null)
        val name = prefs.getString("google_name", null)
        val photoUrl = prefs.getString("google_photo_url", null)

        if (!refreshToken.isNullOrEmpty()) {
            _googleEmail.value = email
            _googleName.value = name
            _googlePhotoUrl.value = photoUrl
            _isGoogleSignedIn.value = true
        } else {
            _googleEmail.value = null
            _googleName.value = null
            _googlePhotoUrl.value = null
            _isGoogleSignedIn.value = false
        }
    }

    fun exchangeCodeForTokens(
        context: Context,
        authCode: String,
        clientId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _driveStatusMessage.value = "Authenticating with Google..."
                val formBody = FormBody.Builder()
                    .add("code", authCode)
                    .add("client_id", clientId)
                    .add("redirect_uri", "http://localhost")
                    .add("grant_type", "authorization_code")
                    .build()

                val request = Request.Builder()
                    .url("https://oauth2.googleapis.com/token")
                    .post(formBody)
                    .build()

                val response = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(request).execute()
                    }
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val tokenResponse = moshi.adapter(GoogleTokenResponse::class.java).fromJson(responseBody)
                    if (tokenResponse != null) {
                        val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
                        val edit = prefs.edit()
                            .putString("google_access_token", tokenResponse.access_token)
                            .putLong("google_access_token_expires_at", System.currentTimeMillis() + (tokenResponse.expires_in ?: 3600) * 1000)
                        
                        if (tokenResponse.refresh_token != null) {
                            edit.putString("google_refresh_token", tokenResponse.refresh_token)
                        }
                        edit.apply()

                        // Fetch user info with the new access token
                        fetchGoogleUserInfo(context, tokenResponse.access_token, { name, email ->
                            _isGoogleSignedIn.value = true
                            _driveStatusMessage.value = "Successfully Signed In!"
                            onSuccess()
                        }, { err ->
                            _isGoogleSignedIn.value = true
                            _driveStatusMessage.value = "Signed In (Unable to fetch user info)"
                            onSuccess()
                        })
                    } else {
                        throw Exception("Failed to parse token response")
                    }
                } else {
                    val errBody = response.body?.string() ?: ""
                    throw Exception("Google server returned error: $errBody")
                }
            } catch (e: Exception) {
                _driveStatusMessage.value = "Sign-In Failed: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "Unknown authentication error")
            }
        }
    }

    private fun fetchGoogleUserInfo(
        context: Context,
        accessToken: String,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = Request.Builder()
                    .url("https://www.googleapis.com/oauth2/v3/userinfo")
                    .header("Authorization", "Bearer $accessToken")
                    .build()

                val response = kotlinx.coroutines.Dispatchers.IO.let { d ->
                    kotlinx.coroutines.withContext(d) {
                        client.newCall(request).execute()
                    }
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val userInfo = moshi.adapter(GoogleUserInfoResponse::class.java).fromJson(responseBody)
                    if (userInfo != null) {
                        val name = userInfo.name ?: "Google User"
                        val email = userInfo.email ?: "drive.user@gmail.com"
                        val picture = userInfo.picture

                        val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
                        val editor = prefs.edit()
                            .putString("google_name", name)
                            .putString("google_email", email)
                        if (!picture.isNullOrEmpty()) {
                            editor.putString("google_photo_url", picture)
                        } else {
                            editor.remove("google_photo_url")
                        }
                        editor.apply()

                        _googleName.value = name
                        _googleEmail.value = email
                        _googlePhotoUrl.value = picture
                        onSuccess(name, email)
                    } else {
                        throw Exception("Failed to parse user info")
                    }
                } else {
                    throw Exception("Failed to fetch user info")
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private suspend fun getValidAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
        val accessToken = prefs.getString("google_access_token", null)
        val expiresAt = prefs.getLong("google_access_token_expires_at", 0)
        val refreshToken = prefs.getString("google_refresh_token", null)

        if (refreshToken.isNullOrEmpty()) {
            return null
        }

        // If access token exists and is valid for at least another 5 minutes, use it
        if (!accessToken.isNullOrEmpty() && expiresAt > System.currentTimeMillis() + 300 * 1000) {
            return accessToken
        }

       // Otherwise, refresh the access token!
        return try {
            val finalClientId = if (BuildConfig.GOOGLE_CLIENT_ID.isNotEmpty()) BuildConfig.GOOGLE_CLIENT_ID else "767284176898-t1aj175l4h6gg73514kjsq9v28bg8hgg.apps.googleusercontent.com"
            val formBody = FormBody.Builder()
                .add("client_id", finalClientId)
                .add("refresh_token", refreshToken)
                .add("grant_type", "refresh_token")
                .build()
            val request = Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(formBody)
                .build()

            val response = kotlinx.coroutines.Dispatchers.IO.let { d ->
                kotlinx.coroutines.withContext(d) {
                    client.newCall(request).execute()
                }
            }

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val tokenResponse = moshi.adapter(GoogleTokenResponse::class.java).fromJson(responseBody)
                if (tokenResponse != null) {
                    prefs.edit()
                        .putString("google_access_token", tokenResponse.access_token)
                        .putLong("google_access_token_expires_at", System.currentTimeMillis() + (tokenResponse.expires_in ?: 3600) * 1000)
                        .apply()
                    tokenResponse.access_token
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun backupToGoogleDrive(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _driveStatusMessage.value = "Starting cloud backup..."
                val accessToken = getValidAccessToken(context)
                if (accessToken == null) {
                    _isGoogleSignedIn.value = false
                    throw Exception("Not signed in to Google or session expired")
                }

                // 1. Get database data JSON string
                val backupData = repository.getBackupData()
                val json = backupAdapter.indent("  ").toJson(backupData)

                // 2. Create timestamp and fileName
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val fileName = "finance_note_backup_$timestamp.json"

                _driveStatusMessage.value = "Creating cloud backup file..."
                val boundary = "BackupBoundary"
                val multipartBody = buildString {
                    append("--$boundary\r\n")
                    append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
                    append("{\"name\": \"$fileName\", \"mimeType\": \"application/json\"}\r\n")
                    append("--$boundary\r\n")
                    append("Content-Type: application/json\r\n\r\n")
                    append(json)
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
                    onSuccess()
                } else {
                    val errBody = createResponse.body?.string() ?: ""
                    throw Exception("Failed to create backup on Google Drive: $errBody")
                }
            } catch (e: Exception) {
                _driveStatusMessage.value = "Backup Failed: ${e.localizedMessage}"
                onError(e.localizedMessage ?: "Unknown backup error")
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
                    val backupData = backupAdapter.fromJson(jsonContent)
                    if (backupData != null) {
                       repository.restoreBackupData(backupData)
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

    fun signOutFromGoogle(context: Context, onSuccess: () -> Unit) {
        val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        
        _googleEmail.value = null
        _googleName.value = null
        _googlePhotoUrl.value = null
        _isGoogleSignedIn.value = false
        _driveStatusMessage.value = "Signed Out"
        onSuccess()
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
