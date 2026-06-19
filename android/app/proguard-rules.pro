# Add proguard rules if needed

# Keep security validator classes (defense in depth - separate files)
-keep class com.streamapp.security.** { *; }
-keep class com.streamapp.data.api.SecurityModels.** { *; }

# Keep retrofit and Gson for API calls
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
