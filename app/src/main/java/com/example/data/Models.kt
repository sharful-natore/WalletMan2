package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "workspaces")
@JsonClass(generateAdapter = true)
data class Workspace(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val budgetIncome: Double = 0.0,
    val budgetExpense: Double = 0.0,
    val budgetSavings: Double = 0.0,
    val profileName: String = "",
    val profileEmail: String = "",
    val profilePhone: String = "",
    val profileSocial: String = "",
    val profileAddress: String = "",
    val profilePhotoUri: String? = null
)

@Entity(tableName = "persons")
@JsonClass(generateAdapter = true)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val workspaceId: String = "default"
)

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED"
    val category: String, // e.g. "Salary", "Food", "Shopping", "Lending", "Borrowing", "Repayment"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val personId: Int? = null, // Linked person if it's a debt/credit related transaction
    val workspaceId: String = "default",
    val subType: String? = "CASH", // "CASH" or "CREDIT"
    val isManualTimestamp: Boolean = false
)

@Entity(tableName = "savings_goals")
@JsonClass(generateAdapter = true)
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val category: String = "General",
    val colorIndex: Int = 0, // Index for choosing distinct fintech gradient card themes
    val cardholderName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val workspaceId: String = "default",
    val displayOrder: Int = 0
)

@Entity(tableName = "savings_transactions")
@JsonClass(generateAdapter = true)
data class SavingsTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val amount: Double,
    val isDeposit: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val workspaceId: String = "default",
    val isManualTimestamp: Boolean = false
)

@JsonClass(generateAdapter = true)
data class FinanceBackup(
    val persons: List<Person>,
    val transactions: List<Transaction>,
    val savingsGoals: List<SavingsGoal>,
    val savingsTransactions: List<SavingsTransaction> = emptyList(),
    val workspaces: List<Workspace> = emptyList(),
    val trashItems: List<TrashItem> = emptyList(),
    val monthlyBudgets: List<MonthlyBudget> = emptyList(),
    val budgetIncome: Double? = 0.0,
    val budgetExpense: Double? = 0.0,
    val budgetSavings: Double? = 0.0,
    val comment: String? = "",
    val createdAt: Long? = null,
    val profileName: String = "",
    val profileEmail: String = "",
    val profilePhone: String = "",
    val profileSocial: String = "",
    val profileAddress: String = "",
    val profilePhotoUri: String? = null,
    val draftTransactions: List<DraftTransaction> = emptyList(),
    val autoEntries: List<AutoEntry> = emptyList(),
    val customGradientsConfigSerialized: String? = null,
    val staticGradientOverridesSerialized: String? = null,
    val chartGradientsSerialized: String? = null,
    val selectedThemeGradientIndex: Int? = null
)

@JsonClass(generateAdapter = true)
data class GoogleTokenResponse(
    val access_token: String,
    val refresh_token: String? = null,
    val expires_in: Long? = null,
    val token_type: String? = null
)

@JsonClass(generateAdapter = true)
data class GoogleUserInfoResponse(
    val name: String? = null,
    val email: String? = null,
    val picture: String? = null
)

@JsonClass(generateAdapter = true)
data class GoogleDriveFile(
    val id: String,
    val name: String,
    val mimeType: String? = null,
    val createdTime: String? = null,
    val size: String? = null
)

@JsonClass(generateAdapter = true)
data class GoogleDriveFilesResponse(
    val files: List<GoogleDriveFile>
)

@Entity(tableName = "trash_items")
@JsonClass(generateAdapter = true)
data class TrashItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalId: Int,
    val itemType: String, // "TRANSACTION", "PERSON", "SAVINGS_GOAL", "SAVINGS_TRANSACTION"
    val itemJson: String,
    val deletedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "draft_transactions")
@JsonClass(generateAdapter = true)
data class DraftTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double? = null,
    val type: String? = null, // "INCOME", "EXPENSE", "LEND", "BORROW" etc. or null
    val category: String? = null,
    val note: String,
    val timestamp: Long = System.currentTimeMillis(),
    val workspaceId: String = "default"
)

@Entity(tableName = "auto_entries")
@JsonClass(generateAdapter = true)
data class AutoEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE", "INCOME", "LEND", "BORROW"
    val category: String,
    val note: String = "",
    val subType: String? = "CASH", // "CASH", "CREDIT"
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val timeOfDay: String = "09:00", // HH:mm format
    val dayOfWeek: Int = 1, // 1 = Monday ... 7 = Sunday
    val dayOfMonth: Int = 1, // 1..31
    val monthOfYear: Int = 1, // 1..12
    val askBeforeAdding: Boolean = true, // true = Ask Before Adding, false = Auto Add
    val isEnabled: Boolean = true,
    val lastExecutedTime: Long = 0L,
    val workspaceId: String = "default"
)

@Entity(tableName = "debt_notification_logs")
data class DebtNotificationLog(
    @PrimaryKey val personId: Int,
    val lastNotifiedAt: Long
)

@Entity(tableName = "monthly_budgets")
@JsonClass(generateAdapter = true)
data class MonthlyBudget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val month: Int,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val savings: Double = 0.0,
    val workspaceId: String = "default"
)

@JsonClass(generateAdapter = true)
data class PersonWithTransactions(
    val person: Person,
    val transactions: List<Transaction>
)

@JsonClass(generateAdapter = true)
data class GoalWithTransactions(
    val goal: SavingsGoal,
    val transactions: List<SavingsTransaction>
)

@JsonClass(generateAdapter = true)
data class DeletedGDriveBackup(
    val fileId: String,
    val fileName: String,
    val backupJson: String
)

data class DraftParseResult(
    val amount: Double?,
    val type: String?,
    val category: String?,
    val cleanedNote: String
)

object DraftParser {
    fun parse(note: String): DraftParseResult {
        if (note.isBlank()) {
            return DraftParseResult(null, null, null, "")
        }

        var amount: Double? = null
        var rawAmountStr = ""
        val digitRegex = Regex("[0-9০-৯]+")
        val matches = digitRegex.findAll(note)
        for (match in matches) {
            val numStr = match.value
            val engNumStr = numStr.map { c ->
                if (c in '০'..'৯') (c - '০' + 48).toChar().toString() else c.toString()
            }.joinToString("")
            val parsed = engNumStr.toDoubleOrNull()
            if (parsed != null && parsed > 0) {
                amount = parsed
                rawAmountStr = numStr
                break
            }
        }

        val noteLower = note.lowercase()
        var type: String? = null
        var category: String? = null

        val isExpenseExplicit = noteLower.contains("ব্যয়") || noteLower.contains("খরচ") || noteLower.contains("expense") || noteLower.contains("cost")
        val isIncomeExplicit = noteLower.contains("আয়") || noteLower.contains("ইনকাম") || noteLower.contains("income") || noteLower.contains("earning")
        val isBorrowExplicit = noteLower.contains("দেনা") || 
                noteLower.contains("ধার নেওয়া") || 
                noteLower.contains("ধার নিলাম") || 
                noteLower.contains("ধার নিছি") || 
                noteLower.contains("ধার নিয়েছি") || 
                noteLower.contains("ধার আনলাম") || 
                noteLower.contains("ধার এনেছি") || 
                noteLower.contains("ধার আনা") || 
                noteLower.contains("কর্জ নিলাম") || 
                noteLower.contains("কর্জ নিছি") || 
                noteLower.contains("কর্জ নিয়েছি") || 
                noteLower.contains("কর্জ নেওয়া") || 
                noteLower.contains("কর্জ") || 
                noteLower.contains("কর্য") || 
                noteLower.contains("পাবে") || 
                noteLower.contains("পাবে আমার কাছে") || 
                noteLower.contains("দিতে হবে") || 
                noteLower.contains("ঋণ নিলাম") || 
                noteLower.contains("ঋণ নেওয়া") || 
                noteLower.contains("ঋণ নিছি") || 
                noteLower.contains("ঋণ নিয়েছি") || 
                noteLower.contains("borrow") || 
                noteLower.contains("debt") || 
                noteLower.contains("payable") || 
                noteLower.contains("borrowed")

        val isLendExplicit = noteLower.contains("পাওনা") || 
                noteLower.contains("পাওনা আছে") || 
                noteLower.contains("ধার দেওয়া") || 
                noteLower.contains("ধার দিলাম") || 
                noteLower.contains("ধার দিয়েছি") || 
                noteLower.contains("ধার দিছি") || 
                noteLower.contains("ধারে দিলাম") || 
                noteLower.contains("ধারে দিয়েছি") || 
                noteLower.contains("কর্জ দিলাম") || 
                noteLower.contains("কর্জ দিয়েছি") || 
                noteLower.contains("কর্জ দিছি") || 
                (noteLower.contains("পাব") && !noteLower.contains("পাবে") && !noteLower.contains("পাবে আমার কাছে")) || 
                noteLower.contains("পাবো") || 
                noteLower.contains("ঋণ দিলাম") || 
                noteLower.contains("ঋণ দেওয়া") || 
                noteLower.contains("ঋণ দিয়েছি") || 
                noteLower.contains("lend") || 
                noteLower.contains("receivable") || 
                noteLower.contains("lent")
        val isSavingsExplicit = noteLower.contains("সঞ্চয়") || noteLower.contains("savings") || noteLower.contains("ডিপোজিট") || noteLower.contains("সংরক্ষণ")
        val isWithdrawalExplicit = noteLower.contains("উত্তোলন") || noteLower.contains("withdraw") || noteLower.contains("উঠালাম") || noteLower.contains("ক্যাশ আউট")

        val hasSalary = noteLower.contains("বেতন") || noteLower.contains("salary")
        val hasBusiness = noteLower.contains("ব্যবসা") || noteLower.contains("business")
        val hasGift = noteLower.contains("উপহার") || noteLower.contains("gift")
        val hasHonorarium = noteLower.contains("সম্মানী") || noteLower.contains("honorarium")

        val hasFood = noteLower.contains("খাবার") || noteLower.contains("চা") || noteLower.contains("ভাত") || noteLower.contains("নাস্তা") || noteLower.contains("কফি") || noteLower.contains("breakfast") || noteLower.contains("lunch") || noteLower.contains("dinner") || noteLower.contains("food") || noteLower.contains("restaurant") || noteLower.contains("রেস্টুরেন্ট") || noteLower.contains("হোটেল") || noteLower.contains("বার্গার") || noteLower.contains("পিৎজা") || noteLower.contains("বিরিয়ানি") || noteLower.contains("আপেল") || noteLower.contains("ফল") || noteLower.contains("কলা") || noteLower.contains("আম") || noteLower.contains("কমলা") || noteLower.contains("আঙ্গুর") || noteLower.contains("apple") || noteLower.contains("fruit")
        val hasGrocery = noteLower.contains("বাজার") || noteLower.contains("গ্রোসারী") || noteLower.contains("grocery") || noteLower.contains("মাছ") || noteLower.contains("মাংস") || noteLower.contains("সবজি") || noteLower.contains("ফল") || noteLower.contains("চাল") || noteLower.contains("ডাল") || noteLower.contains("আটা") || noteLower.contains("ময়দা") || noteLower.contains("ডিম") || noteLower.contains("দুধ") || noteLower.contains("egg") || noteLower.contains("milk")
        val hasTransport = noteLower.contains("গাড়ি") || noteLower.contains("রিকশা") || noteLower.contains("বাস") || noteLower.contains("ভাড়া") || noteLower.contains("ট্যাক্সি") || noteLower.contains("সিএনজি") || noteLower.contains("rent") || noteLower.contains("travel") || noteLower.contains("fare") || noteLower.contains("transport") || noteLower.contains("fuel") || noteLower.contains("petrol") || noteLower.contains("octane") || noteLower.contains("cng") || noteLower.contains("diesel") || noteLower.contains("জ্বালানি") || noteLower.contains("জ্বালানী") || noteLower.contains("পাম্প") || noteLower.contains("uber") || noteLower.contains("pathao") || noteLower.contains("ride") || noteLower.contains("drive") || noteLower.contains("তেল")
        val hasShopping = noteLower.contains("জামা") || noteLower.contains("কাপড়") || noteLower.contains("shopping") || noteLower.contains("কেনাকাটা") || noteLower.contains("জুতা") || noteLower.contains("প্যান্ট") || noteLower.contains("শার্ট") || noteLower.contains("dress") || noteLower.contains("clothes") || noteLower.contains("shoes")
        val hasMedical = noteLower.contains("চিকিৎসা") || noteLower.contains("ঔষধ") || noteLower.contains("ডাক্তার") || noteLower.contains("মেডিকেল") || noteLower.contains("medicine") || noteLower.contains("medical") || noteLower.contains("ফার্মেসি") || noteLower.contains("হাসপাতাল") || noteLower.contains("hospital") || noteLower.contains("pharma") || noteLower.contains("ঔষধপত্র")
        val hasEducation = noteLower.contains("বই") || noteLower.contains("স্কুল") || noteLower.contains("কলেজ") || noteLower.contains("টিউশন") || noteLower.contains("শিক্ষা") || noteLower.contains("education") || noteLower.contains("school") || noteLower.contains("college") || noteLower.contains("fee") || noteLower.contains("ফি")
        val hasBill = noteLower.contains("বিল") || noteLower.contains("বিদ্যুৎ") || noteLower.contains("পানি") || noteLower.contains("গ্যাস") || noteLower.contains("ইন্টারনেট") || noteLower.contains("ওয়াইফাই") || noteLower.contains("wifi") || noteLower.contains("bill") || noteLower.contains("electricity") || noteLower.contains("water") || noteLower.contains("recharge") || noteLower.contains("রিচার্জ") || noteLower.contains("মোবাইল") || noteLower.contains("mobile") || noteLower.contains("net")

        if (isSavingsExplicit) {
            type = "SAVINGS"
            category = "Savings"
        } else if (isWithdrawalExplicit) {
            type = "WITHDRAWAL"
            category = "Withdrawal"
        } else if (isIncomeExplicit || hasSalary || hasBusiness || hasGift || hasHonorarium || noteLower.contains("পেলাম")) {
            type = "INCOME"
            category = when {
                hasSalary -> "Salary"
                hasBusiness -> "Business"
                hasGift -> "Gift"
                hasHonorarium -> "Honorarium"
                else -> "Income"
            }
        } else if (isLendExplicit) {
            type = "LEND"
            category = "Lending"
        } else if (isBorrowExplicit) {
            type = "BORROW"
            category = "Borrowing"
        } else if (isExpenseExplicit || hasFood || hasGrocery || hasTransport || hasShopping || hasMedical || hasEducation || hasBill) {
            type = "EXPENSE"
            category = when {
                hasFood -> "Food"
                hasGrocery -> "Grocery"
                hasTransport -> "Transportation"
                hasShopping -> "Shopping"
                hasMedical -> "Medical"
                hasEducation -> "Education"
                hasBill -> "Utility"
                else -> "Expense"
            }
        } else {
            type = "EXPENSE"
            category = "Other"
        }

        var cleaned = note
        if (rawAmountStr.isNotEmpty()) {
            cleaned = cleaned.replaceFirst(rawAmountStr, "")
        }

        val wordsToStrip = listOf(
            "টাকা", "টাকায়", "টাকা.", "টাকা,", "টি", "টা", "tk", "taka",
            "ব্যয়", "খরচ", "আয়", "ইনকাম", "দেনা", "পাওনা", "সঞ্চয়", "উত্তোলন",
            "expense", "cost", "income", "borrow", "lend", "savings", "withdraw"
        )

        for (w in wordsToStrip) {
            val regex = Regex("(?i)\\b" + Regex.escape(w) + "\\b")
            cleaned = cleaned.replace(regex, "")
        }

        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

        if (cleaned.isBlank()) {
            val noteNoNum = if (rawAmountStr.isNotEmpty()) note.replaceFirst(rawAmountStr, "").trim().replace(Regex("\\s+"), " ") else note
            cleaned = if (noteNoNum.isNotBlank()) noteNoNum else (category ?: note)
        }

        return DraftParseResult(amount, type, category, cleaned)
    }

    fun getCategoryForType(type: String): String {
        return when (type) {
            "INCOME" -> "Salary"
            "EXPENSE" -> "Expense"
            "LEND" -> "Lending"
            "BORROW" -> "Borrowing"
            "SAVINGS" -> "Savings"
            "WITHDRAWAL" -> "Withdrawal"
            else -> "Other"
        }
    }

    fun getCategoryBanglaLabel(cat: String?, type: String?): String {
        val typeLabel = when (type) {
            "INCOME" -> "আয়"
            "EXPENSE" -> "ব্যয়"
            "LEND" -> "পাওনা"
            "BORROW" -> "দেনা"
            "SAVINGS" -> "সঞ্চয়"
            "WITHDRAWAL" -> "উত্তোলন"
            else -> "ব্যয়"
        }

        if (cat.isNullOrBlank()) {
            return typeLabel
        }
        val c = cat.trim()

        if (c.equals("Other", ignoreCase = true) ||
            c.equals("Others", ignoreCase = true) ||
            c.equals("Expense", ignoreCase = true) ||
            c.equals("Income", ignoreCase = true) ||
            c.equals("Lending", ignoreCase = true) ||
            c.equals("Borrowing", ignoreCase = true) ||
            c.equals("Savings", ignoreCase = true) ||
            c.equals("Withdrawal", ignoreCase = true) ||
            c.contains("অন্যান্য", ignoreCase = true)
        ) {
            return typeLabel
        }

        return when {
            c.equals("Salary", ignoreCase = true) || c.contains("বেতন", ignoreCase = true) -> "বেতন"
            c.equals("Business", ignoreCase = true) || c.contains("ব্যবসা", ignoreCase = true) -> "ব্যবসা"
            c.equals("Agriculture", ignoreCase = true) || c.contains("কৃষি", ignoreCase = true) -> "কৃষি"
            c.equals("Gift", ignoreCase = true) || c.contains("উপহার", ignoreCase = true) -> "উপহার"
            c.equals("Sales", ignoreCase = true) || c.contains("বিক্রয়", ignoreCase = true) || c.contains("বিক্রি", ignoreCase = true) -> "বিক্রয়"
            c.equals("Honorarium", ignoreCase = true) || c.contains("সম্মানী", ignoreCase = true) -> "সম্মানী"
            c.equals("Food", ignoreCase = true) || c.contains("খাবার", ignoreCase = true) -> "খাবার"
            c.equals("Grocery", ignoreCase = true) || c.contains("বাজার", ignoreCase = true) -> "বাজার"
            c.equals("Transportation", ignoreCase = true) || c.equals("Transport", ignoreCase = true) || c.contains("যাতায়াত", ignoreCase = true) || c.contains("জ্বালানি", ignoreCase = true) || c.contains("জ্বালানী", ignoreCase = true) -> "যাতায়াত"
            c.equals("Shopping", ignoreCase = true) || c.contains("কেনাকাটা", ignoreCase = true) -> "কেনাকাটা"
            c.equals("Medical", ignoreCase = true) || c.contains("চিকিৎসা", ignoreCase = true) -> "চিকিৎসা"
            c.equals("Education", ignoreCase = true) || c.contains("শিক্ষা", ignoreCase = true) -> "শিক্ষা"
            c.equals("Utility", ignoreCase = true) || c.equals("Bill", ignoreCase = true) || c.contains("বিল", ignoreCase = true) -> "ইউটিলিটি"
            c.equals("Clothing", ignoreCase = true) || c.contains("পোশাক", ignoreCase = true) -> "পোশাক"
            c.equals("Housing", ignoreCase = true) || c.contains("বাসস্থান", ignoreCase = true) -> "বাসস্থান"
            c.equals("Lending", ignoreCase = true) || c.equals("Lend", ignoreCase = true) || c.contains("ধার দেওয়া", ignoreCase = true) -> "ধার দেওয়া"
            c.equals("Borrowing", ignoreCase = true) || c.equals("Borrow", ignoreCase = true) || c.contains("ধার নেওয়া", ignoreCase = true) -> "ধার নেওয়া"
            c.equals("Savings", ignoreCase = true) || c.contains("সঞ্চয়", ignoreCase = true) -> "সঞ্চয়"
            c.equals("Withdrawal", ignoreCase = true) || c.contains("উত্তোলন", ignoreCase = true) -> "উত্তোলন"
            else -> typeLabel
        }
    }
}

data class WorkspaceStats(
    val workspace: Workspace,
    val profileName: String,
    val profilePhoto: String?,
    val income: Double,
    val expense: Double,
    val netOwedToMe: Double,
    val netIOwe: Double,
    val personCount: Int,
    val cardCount: Int
)

