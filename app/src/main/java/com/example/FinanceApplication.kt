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
                    val options = FirebaseOptions.Builder()
                        .setProjectId(BuildConfig.Firestore_Project_ID.ifBlank { "financenote-dc6f8" })
                        .setApplicationId(BuildConfig.Firestore_APP_ID.ifBlank { "1:549900777284:android:b661159d57ed30542bc911" })
                        .setApiKey("AIzaSyCngAmaOYL3jzyZj9JFKrmaYSkaNA5uIHQ")
                        .build()
                    FirebaseApp.initializeApp(appContext, options)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
