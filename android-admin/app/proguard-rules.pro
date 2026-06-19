# Default rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.streamapp.admin.data.** { *; }

# Keep security-related classes if admin app references them
-keepattributes Signature
-dontwarn okhttp3.**
-dontwarn retrofit2.**
