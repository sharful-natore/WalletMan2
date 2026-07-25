# Project specific ProGuard rules

# Firebase and Compose have their own rules embedded in libraries.
# Keeping broad rules like below prevents optimization.
# -keep class com.google.firebase.** { *; }
# -keep class androidx.compose.** { *; }

# General optimizations
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Preserve attributes for debugging and reflection
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Keep all data models, Room entities, DAOs, and database classes
-keep class com.example.data.** { *; }
-keep interface com.example.data.** { *; }
-dontwarn com.example.data.**

# Keep ViewModels and UI state
-keep class com.example.ui.** { *; }
-keep interface com.example.ui.** { *; }

# Keep Moshi and models
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}

# Keep Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep UCrop
-keep class com.yalantis.ucrop.** { *; }

# Keep Coroutines
-keepclassmembers class * {
    *** volatile ***;
}
