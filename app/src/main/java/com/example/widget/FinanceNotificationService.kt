package com.example.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FinanceNotificationService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(101, createNotification(0.0, 0.0, 0.0, 0.0))
        
        scope.launch {
            while (isActive) {
                updateNotification()
                delay(60000) // Update every minute
            }
        }
    }

    private suspend fun updateNotification() {
        try {
            val dao = AppDatabase.getDatabase(this).financeDao()
            val allTxs = dao.getAllTransactionsList()
            val income = allTxs.filter { it.type == "INCOME" || it.type == "REPAY_RECEIVED" || it.type == "BORROW" }.sumOf { it.amount }
            val expense = allTxs.filter { it.type == "EXPENSE" || it.type == "LEND" || it.type == "REPAY_PAID" }.sumOf { it.amount }

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

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(101, createNotification(income, expense, totalDena, totalPaona))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotification(income: Double, expense: Double, debt: Double, credit: Double): Notification {
        val views = RemoteViews(packageName, R.layout.notification_finance)

        views.setTextViewText(R.id.tv_notif_income, "৳ $income")
        views.setTextViewText(R.id.tv_notif_expense, "৳ $expense")
        views.setTextViewText(R.id.tv_notif_debt, "৳ $debt")
        views.setTextViewText(R.id.tv_notif_credit, "৳ $credit")

        val txIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_ADD_TRANSACTION"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val personIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_ADD_PERSON"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val savingIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_ADD_SAVING"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        views.setOnClickPendingIntent(R.id.btn_notif_tx, PendingIntent.getActivity(this, 1, txIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_notif_person, PendingIntent.getActivity(this, 2, personIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_notif_saving, PendingIntent.getActivity(this, 3, savingIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

        return NotificationCompat.Builder(this, "finance_notif_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure to use a valid icon
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "finance_notif_channel",
                "Finance Widget",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing finance widget"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
