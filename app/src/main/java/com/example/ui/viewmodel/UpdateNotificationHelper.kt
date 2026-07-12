package com.example.ui.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object UpdateNotificationHelper {
    private const val CHANNEL_ID = "app_update_channel"
    private const val NOTIFICATION_ID = 202

    fun showUpdateNotification(context: Context, language: String, version: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (language == "BN") "অ্যাপ আপডেট" else "App Update"
            val descriptionText = if (language == "BN") "নতুন ভার্সন এভেইলএবল হলে জানাবে" else "Notify when a new version is available"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_SHOW_UPDATE"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (language == "BN") "নতুন আপডেট পাওয়া যাচ্ছে!" else "New Update Available!"
        val message = if (language == "BN") 
            "ফাইন্যান্স নোট এর নতুন ভার্সন ($version) এখন এভেইলএবল। এখনই আপডেট করুন।" 
            else "A new version of Finance Note ($version) is now available. Update now."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pie_chart) // Using existing icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
