# Default ProGuard rules for MyApp Admin
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.myapp.admin.api.LoginRequest { *; }
-keep class com.myapp.admin.api.LoginResponse { *; }
-keep class com.myapp.admin.api.StatsResponse { *; }
-keep class com.myapp.admin.api.ErrorResponse { *; }
-keep class com.myapp.admin.api.ErrorResponse$ErrorDetail { *; }
-keep class com.myapp.admin.api.ApiClient { *; }
