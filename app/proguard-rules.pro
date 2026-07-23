# Project specific ProGuard rules

# Firebase and Compose have their own rules embedded in libraries.
# Keeping broad rules like below prevents optimization.
# -keep class com.google.firebase.** { *; }
# -keep class androidx.compose.** { *; }

# General optimizations
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively

# Preserve attributes for debugging and reflection if needed
-keepattributes *Annotation*,Signature
-keepattributes SourceFile,LineNumberTable

# Keep Room database and entity classes from being renamed or stripped
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.example.data.** { *; }
-dontwarn androidx.room.**

# Keep serializable and data model classes
-keep class * implements java.io.Serializable { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
