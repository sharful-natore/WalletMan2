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
