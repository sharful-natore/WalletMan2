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
import android.content.pm.ServiceInfo
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
    companion object {
        var isRunning = false
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()
    }
    
    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= 34) {
                try {
                    startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to normal if specialUse fails (might need different type)
                    startForeground(101, notification)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground(101, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val views = RemoteViews(packageName, R.layout.notification_finance)

        val mainAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val mainAppPendingIntent = PendingIntent.getActivity(this, 0, mainAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val txIntent = Intent(this, com.example.QuickAddActivity::class.java).apply {
            action = "ACTION_ADD_TRANSACTION"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val debtIntent = Intent(this, com.example.QuickAddActivity::class.java).apply {
            action = "ACTION_DEBT_CREDIT"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val savingsIntent = Intent(this, com.example.QuickAddActivity::class.java).apply {
            action = "ACTION_SAVINGS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val backupIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_BACKUP"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val dashboardIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_DASHBOARD"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val settingsIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_SETTINGS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        views.setOnClickPendingIntent(R.id.btn_notif_tx, PendingIntent.getActivity(this, 1, txIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_notif_debt, PendingIntent.getActivity(this, 2, debtIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_notif_savings, PendingIntent.getActivity(this, 3, savingsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_notif_backup, PendingIntent.getActivity(this, 4, backupIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_notif_dashboard, PendingIntent.getActivity(this, 5, dashboardIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_notif_settings, PendingIntent.getActivity(this, 7, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

        try {
            val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val currentTime = timeFormat.format(java.util.Date())
            views.setTextViewText(R.id.tv_notif_time, currentTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val prefs = getSharedPreferences("financenote_prefs", android.content.Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("is_dark_theme", false)
        
        return NotificationCompat.Builder(this, "finance_notif_channel")
            .setSmallIcon(R.drawable.ic_pie_chart) 
            .setCustomContentView(views)
            .setCustomBigContentView(views)
            .setContentIntent(mainAppPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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
