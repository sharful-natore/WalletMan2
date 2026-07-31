package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.DraftTransaction
import com.example.data.Transaction
import com.example.util.SmsParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("is_sms_auto_parse_enabled", true)
        if (!isEnabled) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val isDirectEntry = prefs.getBoolean("is_sms_auto_direct_entry", false)
        val activeWorkspaceId = prefs.getString("current_workspace_id", "default") ?: "default"

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.financeDao()

                for (sms in messages) {
                    val parsed = SmsParser.parse(sms.originatingAddress, sms.messageBody, sms.timestampMillis)
                    if (parsed != null) {
                        if (isDirectEntry) {
                            val transaction = Transaction(
                                amount = parsed.amount,
                                type = if (parsed.isIncome) "INCOME" else "EXPENSE",
                                category = parsed.category,
                                timestamp = parsed.timestamp,
                                note = parsed.summaryNote,
                                workspaceId = activeWorkspaceId,
                                subType = if (parsed.provider.contains("Bank", ignoreCase = true)) "BANK" else "MOBILE_MONEY"
                            )
                            dao.insertTransaction(transaction)
                        } else {
                            val draft = DraftTransaction(
                                amount = parsed.amount,
                                type = if (parsed.isIncome) "INCOME" else "EXPENSE",
                                category = parsed.category,
                                note = parsed.summaryNote,
                                timestamp = parsed.timestamp,
                                workspaceId = activeWorkspaceId
                            )
                            dao.insertDraftTransaction(draft)
                        }

                        showNotification(context, parsed, isDirectEntry)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, parsed: com.example.util.ParsedFinancialSms, isDirectEntry: Boolean) {
        val channelId = "sms_auto_entry_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SMS Auto Entry Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for auto-parsed financial SMS transactions"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountStr = String.format("%.2f", parsed.amount)
        val title = if (isDirectEntry) {
            if (parsed.isIncome) "আয় নোটিফিকেশন (SMS Auto)" else "ব্যয় নোটিফিকেশন (SMS Auto)"
        } else {
            if (parsed.isIncome) "নতুন আয় এসএমএস (খসড়া)" else "নতুন ব্যয় এসএমএস (খসড়া)"
        }

        val content = if (isDirectEntry) {
            "${parsed.provider}: ৳$amountStr অটো এন্ট্রি করা হয়েছে।"
        } else {
            "${parsed.provider}: ৳$amountStr খসড়ায় যুক্ত করা হয়েছে। রিভিউ করে সেভ করুন।"
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${parsed.summaryNote}\n$content"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
    }
}
