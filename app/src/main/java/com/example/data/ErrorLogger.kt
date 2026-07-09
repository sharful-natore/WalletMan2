package com.example.data

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ErrorLogger {
    private const val FILE_NAME = "error_logs.txt"
    private const val TAG = "ErrorLogger"

    fun logError(context: Context, source: String, message: String, throwable: Throwable? = null) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val exceptionDetails = throwable?.let {
                val sw = java.io.StringWriter()
                val pw = java.io.PrintWriter(sw)
                it.printStackTrace(pw)
                sw.toString()
            } ?: ""

            val logEntry = """
=========================================
TIME: $timestamp
SOURCE: $source
ERROR: $message
${if (exceptionDetails.isNotEmpty()) "STACKTRACE:\n$exceptionDetails" else ""}=========================================

"""

            // Also print to Logcat
            Log.e(TAG, "[$source] $message", throwable)

            // Write to local file
            val file = File(context.filesDir, FILE_NAME)
            file.appendText(logEntry)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log error to file", e)
        }
    }

    fun getErrorLogs(context: Context): String {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.readText()
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read error logs", e)
            "Error reading log file: ${e.localizedMessage}"
        }
    }

    fun clearErrorLogs(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear error logs", e)
            false
        }
    }

    // Initialize global uncaught exception handler
    fun registerUncaughtExceptionHandler(context: Context) {
        val appContext = context.applicationContext
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logError(appContext, "Uncaught Crash (Thread: ${thread.name})", throwable.localizedMessage ?: "Unknown Fatal Crash", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
