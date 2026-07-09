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
    val backupIntent = Intent(context, MainActivity::class.java).apply {
        action = "ACTION_BACKUP"
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

    views.setOnClickPendingIntent(R.id.btn_add_tx, PendingIntent.getActivity(context, 1, txIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.btn_add_person, PendingIntent.getActivity(context, 2, personIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.btn_add_saving, PendingIntent.getActivity(context, 3, savingIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.btn_backup, PendingIntent.getActivity(context, 4, backupIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

    views.setOnClickPendingIntent(R.id.card_income, PendingIntent.getActivity(context, 10, incomeViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.card_expense, PendingIntent.getActivity(context, 11, expenseViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.card_debt, PendingIntent.getActivity(context, 12, debtViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    views.setOnClickPendingIntent(R.id.card_credit, PendingIntent.getActivity(context, 13, creditViewIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

    // Background logic to load data
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val dao = AppDatabase.getDatabase(context).financeDao()
            val allTxs = dao.getAllTransactionsList()
            val income = allTxs.filter { it.type == "INCOME" || it.type == "REPAY_RECEIVED" || it.type == "BORROW" }.sumOf { it.amount }
            val expense = allTxs.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }.sumOf { it.amount }
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

            val df = java.text.DecimalFormat("#,##,##0")
            views.setTextViewText(R.id.tv_income, "৳ ${df.format(balance)}")
            views.setTextViewText(R.id.tv_expense, "৳ ${df.format(expense)}")
            views.setTextViewText(R.id.tv_debt, "৳ ${df.format(totalDena)}")
            views.setTextViewText(R.id.tv_credit, "৳ ${df.format(totalPaona)}")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
