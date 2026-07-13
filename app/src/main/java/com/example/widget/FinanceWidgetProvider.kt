package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FinanceWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    val views = RemoteViews(context.packageName, R.layout.widget_finance)

    val txIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_ADD_TRANSACTION"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val personIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_DEBT_CREDIT"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val savingIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_SAVINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val incomeViewIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_VIEW_INCOME"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val expenseViewIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_VIEW_EXPENSE"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val debtViewIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_VIEW_DEBT"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val creditViewIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_VIEW_CREDIT"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val profileIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_REFRESH_WIDGET"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val syncIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_SYNC_TRIGGER"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    views.setOnClickPendingIntent(R.id.btn_add_tx, PendingIntent.getActivity(context, 1, txIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.btn_add_person, PendingIntent.getActivity(context, 2, personIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.btn_add_saving, PendingIntent.getActivity(context, 3, savingIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.btn_sync, PendingIntent.getActivity(context, 4, syncIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    
    views.setOnClickPendingIntent(R.id.card_income, PendingIntent.getActivity(context, 10, incomeViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.card_expense, PendingIntent.getActivity(context, 11, expenseViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.card_debt, PendingIntent.getActivity(context, 12, debtViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.card_credit, PendingIntent.getActivity(context, 13, creditViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.card_profile, PendingIntent.getActivity(context, 14, profileIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

    // Background logic to load data
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val dao = AppDatabase.getDatabase(context).financeDao()
            val allTxs = dao.getAllTransactionsList()
            val income = allTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = allTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val balance = income - expense

            val persons = dao.getAllPersonsList()
            var totalDena = 0.0
            var totalPaona = 0.0
            for (p in persons) {
                val pTxs = allTxs.filter { it.personId == p.id }
                val lent = pTxs.filter { it.type == "LEND" }.sumOf { it.amount }
                val borrowed = pTxs.filter { it.type == "BORROW" }.sumOf { it.amount }
                val repaidPaid = pTxs.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                val repaidReceived = pTxs.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                val net = (lent + repaidPaid) - (borrowed + repaidReceived)
                if (net > 0) totalPaona += net
                if (net < 0) totalDena += -net
            }

            // Load user settings and profile details
            val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            val langStr = prefs.getString("app_language", "BN") ?: "BN"
            val isBn = langStr == "BN"

            val rawName = prefs.getString("user_name", "Shariful Islam") ?: "Shariful Islam"
            val rawEmail = prefs.getString("user_email", "connect.shariful@gmail.com") ?: "connect.shariful@gmail.com"

            val gPrefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
            val isGoogleSignedIn = !gPrefs.getString("google_email", null).isNullOrEmpty()

            val googleName = gPrefs.getString("google_name", null)
            val googleEmail = gPrefs.getString("google_email", null)

            val profileIntent = Intent(context, MainActivity::class.java).apply {
                action = "ACTION_REFRESH_WIDGET"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            views.setOnClickPendingIntent(R.id.card_profile, PendingIntent.getActivity(context, 14, profileIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

            val profileName = if (isGoogleSignedIn) (googleName ?: rawName) else rawName

            // Load profile photo if available
            val photoUri = prefs.getString("user_photo", null) ?: gPrefs.getString("google_photo_url", null)
            
            if (!photoUri.isNullOrEmpty()) {
                try {
                    val bitmap = if (photoUri.startsWith("http")) {
                        val client = okhttp3.OkHttpClient()
                        val request = okhttp3.Request.Builder().url(photoUri).build()
                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            val bytes = response.body?.bytes()
                            if (bytes != null) android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) else null
                        } else null
                    } else if (photoUri.startsWith("content://")) {
                        val contentUri = android.net.Uri.parse(photoUri)
                        context.contentResolver.openInputStream(contentUri)?.use {
                            android.graphics.BitmapFactory.decodeStream(it)
                        }
                    } else {
                        android.graphics.BitmapFactory.decodeFile(photoUri)
                    }

                    if (bitmap != null) {
                        val circled = getCircledBitmap(bitmap)
                        views.setImageViewBitmap(R.id.iv_avatar, circled)
                        views.setViewVisibility(R.id.iv_avatar, android.view.View.VISIBLE)
                        views.setViewVisibility(R.id.iv_default_logo, android.view.View.GONE)
                        views.setViewVisibility(R.id.tv_avatar_initials, android.view.View.GONE)
                    } else {
                        showInitials(views, profileName)
                    }
                } catch (e: Exception) {
                    showInitials(views, profileName)
                }
            } else {
                showInitials(views, profileName)
            }

            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val welcomeText = if (isBn) {
                when (hour) {
                    in 5..11 -> "শুভ সকাল,"
                    in 12..15 -> "শুভ দুপুর,"
                    in 16..17 -> "শুভ বিকাল,"
                    in 18..19 -> "শুভ সন্ধ্যা,"
                    else -> "শুভ রাত্রি,"
                }
            } else {
                when (hour) {
                    in 5..11 -> "Good morning,"
                    in 12..16 -> "Good afternoon,"
                    in 17..20 -> "Good evening,"
                    else -> "Good night,"
                }
            }
            views.setTextViewText(R.id.tv_welcome_label, welcomeText)

            val displayName = if (isGoogleSignedIn) {
                if (profileName.isNotBlank()) profileName else (if (isBn) "ব্যবহারকারী" else "User")
            } else {
                if (isBn) "সাইন-ইন করুন" else "Sign In"
            }
            views.setTextViewText(R.id.tv_profile_name, displayName)

            // Gregorian, Bengali, and Arabic Dates
            val now = java.util.Calendar.getInstance()
            val englishDateStr = java.text.SimpleDateFormat("EEEE, d MMMM yyyy", java.util.Locale(if (isBn) "bn" else "en")).format(now.time)
            val bengaliDateStr = getBengaliDate(now, isBn)
            val arabicDateStr = getIslamicDate(now, isBn)

            views.setTextViewText(R.id.tv_date_english, englishDateStr)
            views.setTextViewText(R.id.tv_date_bengali, bengaliDateStr)
            views.setTextViewText(R.id.tv_date_arabic, arabicDateStr)

            // Dynamic card headers based on language
            val labelIncome = if (isBn) "আয়" else "Income"
            val labelExpense = if (isBn) "ব্যয়" else "Expense"
            val labelDebt = if (isBn) "দেনা" else "Debt"
            val labelCredit = if (isBn) "পাওনা" else "Credit"

            views.setTextViewText(R.id.lbl_income, labelIncome)
            views.setTextViewText(R.id.lbl_expense, labelExpense)
            views.setTextViewText(R.id.lbl_debt, labelDebt)
            views.setTextViewText(R.id.lbl_credit, labelCredit)

            val df = java.text.DecimalFormat("#,##,##0")
            views.setTextViewText(R.id.tv_income, "৳ ${df.format(income)}")
            views.setTextViewText(R.id.tv_expense, "৳ ${df.format(expense)}")
            views.setTextViewText(R.id.tv_debt, "৳ ${df.format(totalDena)}")
            views.setTextViewText(R.id.tv_credit, "৳ ${df.format(totalPaona)}")

            views.setBoolean(R.id.tv_income, "setSelected", true)
            views.setBoolean(R.id.tv_expense, "setSelected", true)
            views.setBoolean(R.id.tv_debt, "setSelected", true)
            views.setBoolean(R.id.tv_credit, "setSelected", true)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun showInitials(views: RemoteViews, profileName: String) {
    val initials = if (profileName.isNotBlank()) {
        profileName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .uppercase()
    } else ""

    if (initials.isNotEmpty()) {
        views.setViewVisibility(R.id.iv_avatar, android.view.View.GONE)
        views.setViewVisibility(R.id.iv_default_logo, android.view.View.GONE)
        views.setViewVisibility(R.id.tv_avatar_initials, android.view.View.VISIBLE)
        views.setTextViewText(R.id.tv_avatar_initials, initials)
    } else {
        views.setViewVisibility(R.id.iv_avatar, android.view.View.GONE)
        views.setViewVisibility(R.id.iv_default_logo, android.view.View.VISIBLE)
        views.setViewVisibility(R.id.tv_avatar_initials, android.view.View.GONE)
    }
}

private fun getCircledBitmap(bitmap: android.graphics.Bitmap): android.graphics.Bitmap {
    val size = Math.min(bitmap.width, bitmap.height)
    val output = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint()
    paint.isAntiAlias = true
    
    val r = size / 2f
    val borderSize = (size * 0.05f).toInt().coerceAtLeast(4) // Solid thick white border
    
    canvas.drawARGB(0, 0, 0, 0)
    
    // Draw solid circle of original image
    paint.style = android.graphics.Paint.Style.FILL
    canvas.drawCircle(r, r, r - borderSize, paint)
    
    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    // Draw cropped original bitmap
    val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
    val dstRect = android.graphics.RectF(borderSize.toFloat(), borderSize.toFloat(), (size - borderSize).toFloat(), (size - borderSize).toFloat())
    canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
    
    // Draw white border
    paint.xfermode = null
    paint.style = android.graphics.Paint.Style.STROKE
    paint.color = android.graphics.Color.WHITE
    paint.strokeWidth = borderSize.toFloat()
    canvas.drawCircle(r, r, r - borderSize / 2f, paint)
    
    return output
}

fun updateAllWidgets(context: Context) {
    try {
        val intent = Intent(context, FinanceWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(
            android.content.ComponentName(context, FinanceWidgetProvider::class.java)
        )
        if (ids.isNotEmpty()) {
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun convertNumberToBengali(numStr: String): String {
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return numStr.map { c ->
        if (c in '0'..'9') bnDigits[c - '0'] else c
    }.joinToString("")
}

private fun getBengaliDate(cal: java.util.Calendar, isBn: Boolean): String {
    val year = cal.get(java.util.Calendar.YEAR)
    val month = cal.get(java.util.Calendar.MONTH) // 0-indexed
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    
    val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    val dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR)
    
    val april14Day = if (isLeap) 105 else 104
    var bengaliYear = year - 593
    if (dayOfYear < april14Day) {
        bengaliYear = year - 594
    }
    
    val bMonthIndex: Int
    val bDay: Int
    
    when (month) {
        0 -> { // January
            if (day < 14) {
                bMonthIndex = 8 // Poush
                bDay = day + 17
            } else {
                bMonthIndex = 9 // Magh
                bDay = day - 13
            }
        }
        1 -> { // February
            if (day < 13) {
                bMonthIndex = 9 // Magh
                bDay = day + 18
            } else {
                bMonthIndex = 10 // Falgun
                bDay = day - 12
            }
        }
        2 -> { // March
            val prevMonthDays = if (isLeap) 30 else 29
            if (day < 15) {
                bMonthIndex = 10 // Falgun
                bDay = day + (prevMonthDays - 12)
            } else {
                bMonthIndex = 11 // Chaitra
                bDay = day - 14
            }
        }
        3 -> { // April
            if (day < 14) {
                bMonthIndex = 11 // Chaitra
                bDay = day + 17
            } else {
                bMonthIndex = 0 // Boishakh
                bDay = day - 13
            }
        }
        4 -> { // May
            if (day < 15) {
                bMonthIndex = 0 // Boishakh
                bDay = day + 17
            } else {
                bMonthIndex = 1 // Jyaistha
                bDay = day - 14
            }
        }
        5 -> { // June
            if (day < 15) {
                bMonthIndex = 1 // Jyaistha
                bDay = day + 17
            } else {
                bMonthIndex = 2 // Ashadha
                bDay = day - 14
            }
        }
        6 -> { // July
            if (day < 16) {
                bMonthIndex = 2 // Ashadha
                bDay = day + 16
            } else {
                bMonthIndex = 3 // Shravana
                bDay = day - 15
            }
        }
        7 -> { // August
            if (day < 16) {
                bMonthIndex = 3 // Shravana
                bDay = day + 16
            } else {
                bMonthIndex = 4 // Bhadra
                bDay = day - 15
            }
        }
        8 -> { // September
            if (day < 16) {
                bMonthIndex = 4 // Bhadra
                bDay = day + 16
            } else {
                bMonthIndex = 5 // Ashwin
                bDay = day - 15
            }
        }
        9 -> { // October
            if (day < 16) {
                bMonthIndex = 5 // Ashwin
                bDay = day + 15
            } else {
                bMonthIndex = 6 // Kartik
                bDay = day - 15
            }
        }
        10 -> { // November
            if (day < 15) {
                bMonthIndex = 6 // Kartik
                bDay = day + 16
            } else {
                bMonthIndex = 7 // Agrahayan
                bDay = day - 14
            }
        }
        11 -> { // December
            if (day < 15) {
                bMonthIndex = 7 // Agrahayan
                bDay = day + 16
            } else {
                bMonthIndex = 8 // Poush
                bDay = day - 14
            }
        }
        else -> {
            bMonthIndex = 0
            bDay = 1
        }
    }
    
    val bnMonths = listOf("বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন", "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র")
    val enMonths = listOf("Boishakh", "Jyaistha", "Ashadha", "Shravana", "Bhadra", "Ashwin", "Kartik", "Agrahayan", "Poush", "Magh", "Falgun", "Chaitra")
    
    val monthName = if (isBn) bnMonths[bMonthIndex] else enMonths[bMonthIndex]
    
    return if (isBn) {
        val bnDay = convertNumberToBengali(bDay.toString())
        val bnYear = convertNumberToBengali(bengaliYear.toString())
        "$bnDay $monthName, $bnYear বঙ্গাব্দ"
    } else {
        "$bDay $monthName, $bengaliYear SB"
    }
}

private fun getIslamicDate(cal: java.util.Calendar, isBn: Boolean): String {
    try {
        val islamic = android.icu.util.IslamicCalendar()
        islamic.time = cal.time
        val hYear = islamic.get(android.icu.util.Calendar.YEAR)
        val hMonth = islamic.get(android.icu.util.Calendar.MONTH)
        val hDay = islamic.get(android.icu.util.Calendar.DAY_OF_MONTH)
        
        val arMonths = listOf(
            "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি",
            "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
        )
        val enArMonths = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' ath-Thani", "Jumada al-Awwal", "Jumada ath-Thani",
            "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        
        val monthName = if (isBn) arMonths[hMonth] else enArMonths[hMonth]
        
        return if (isBn) {
            val bnDay = convertNumberToBengali(hDay.toString())
            val bnYear = convertNumberToBengali(hYear.toString())
            "$bnDay $monthName, $bnYear হিজরি"
        } else {
            "$hDay $monthName, $hYear AH"
        }
    } catch (e: Exception) {
        return if (isBn) "১ রমজান, ১৪৪৭ হিজরি" else "1 Ramadan, 1447 AH"
    }
}
