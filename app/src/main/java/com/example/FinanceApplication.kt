package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class FinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ensureFirebaseInitialized(this)
    }

    companion object {
        fun ensureFirebaseInitialized(context: android.content.Context) {
            try {
                val appContext = context.applicationContext ?: context
                if (FirebaseApp.getApps(appContext).isEmpty()) {
                    try {
                        FirebaseApp.initializeApp(appContext)
                    } catch (e: Exception) {
                        val options = FirebaseOptions.Builder()
                            .setProjectId("financenote-dc6f8")
                            .setApplicationId("1:549900777284:android:b661159d57ed30542bc911")
                            .setApiKey("AIzaSyCngAmaOYL3jzyZj9JFKrmaYSkaNA5uIHQ")
                            .setDatabaseUrl("https://financenote-dc6f8-default-rtdb.firebaseio.com")
                            .setStorageBucket("financenote-dc6f8.firebasestorage.app")
                            .build()
                        FirebaseApp.initializeApp(appContext, options)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
