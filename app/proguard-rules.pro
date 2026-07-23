# Project specific ProGuard rules

# General optimizations
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Preserve attributes for debugging and reflection if needed
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Keep all data models to prevent them and their fields from being renamed or stripped
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

# Keep all generated Moshi adapters
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class com.squareup.moshi.** { *; }
-keep class *JsonAdapter { *; }
-keep class com.example.data.*JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Keep Room compiler-generated and database classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Dao { *; }

# Keep Firebase and Firestore related classes to ensure reflection-based mapping works
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.** { *; }

# Keep Google Play Services and sign-in components
-keep class com.google.android.gms.** { *; }
-keepclassmembers class com.google.android.gms.** { *; }

