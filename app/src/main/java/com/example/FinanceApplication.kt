package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class FinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
    }

    companion object {
        fun ensureFirebaseInitialized(context: Context): FirebaseApp? {
            val appContext = context.applicationContext ?: context
            if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                return try { FirebaseApp.getInstance() } catch (e: Exception) { null }
            }

            try {
                val app = FirebaseApp.initializeApp(appContext)
                if (app != null && FirebaseApp.getApps(appContext).isNotEmpty()) {
                    return app
                }
            } catch (e: Exception) {
                Log.e("FinanceApp", "Auto Firebase initialization failed", e)
            }

            return try {
                val options = FirebaseOptions.Builder()
                    .setProjectId("financenote-dc6f8")
                    .setApplicationId("1:549900777284:android:b661159d57ed30542bc911")
                    .setApiKey("AIzaSyCngAmaOYL3jzyZj9JFKrmaYSkaNA5uIHQ")
                    .setDatabaseUrl("https://financenote-dc6f8-default-rtdb.firebaseio.com")
                    .setStorageBucket("financenote-dc6f8.firebasestorage.app")
                    .setGcmSenderId("549900777284")
                    .build()
                FirebaseApp.initializeApp(appContext, options)
            } catch (e: Exception) {
                Log.e("FinanceApp", "Explicit Firebase initialization failed", e)
                if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                    try { FirebaseApp.getInstance() } catch (ex: Exception) { null }
                } else null
            }
        }
    }
}
