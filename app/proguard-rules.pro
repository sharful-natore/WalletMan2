# Project specific ProGuard rules

# General attributes preservation for reflection, generics, annotations
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Preserve Kotlin reflection used by Moshi KotlinJsonAdapterFactory
-keep class kotlin.reflect.** { *; }
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class com.squareup.moshi.** { *; }
-keep class com.squareup.moshi.kotlin.reflect.** { *; }
-keepclassmembers class com.squareup.moshi.kotlin.reflect.** { *; }

# Keep all Moshi JSON Adapters & Annotated classes
-keep class *JsonAdapter { *; }
-keep class com.example.**.*JsonAdapter { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Keep all classes and members in com.example
-keep class com.example.** { *; }
-keepclassmembers class com.example.** { *; }

# Keep all data models & data classes in com.example
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }
-keep class com.example.ui.viewmodel.** { *; }
-keepclassmembers class com.example.ui.viewmodel.** { *; }

# Keep BackupEncryptionHelper
-keep class com.example.ui.viewmodel.BackupEncryptionHelper { *; }

# Keep Room compiler-generated and database classes
-keep class androidx.room.** { *; }
-keepclassmembers class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Dao { *; }

# Keep Firebase and Firestore related classes
-keep class com.google.firebase.** { *; }
-keepclassmembers class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Google Play Services and sign-in components
-keep class com.google.android.gms.** { *; }
-keepclassmembers class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep Sherpa ONNX JNI components
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keepclassmembers class com.k2fsa.sherpa.onnx.** { *; }
