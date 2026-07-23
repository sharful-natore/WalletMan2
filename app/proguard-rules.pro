# Project specific ProGuard rules

# General optimizations
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Preserve attributes for debugging and reflection if needed
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Keep all of our data models and entities to prevent serialization/database issues
-keep class com.example.data.** { *; }

# Keep all of our viewmodels to avoid reflection and factory issues
-keep class com.example.ui.viewmodel.** { *; }

# Keep all of our UI custom components
-keep class com.example.ui.components.** { *; }
-keep class com.example.ui.screens.** { *; }

# Room components
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * {
    @androidx.room.Database *;
    @androidx.room.Dao *;
    @androidx.room.Entity *;
}

# Moshi rules
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.JsonClass public *;
}
-keep class *JsonAdapter { *; }

# UCrop
-keep class com.yalantis.ucrop.** { *; }

# Coroutines and kotlinx
-keep class kotlinx.coroutines.** { *; }

# Firebase rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
