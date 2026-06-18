# Default ProGuard rules for MyApp Base
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.myapp.base.api.User { *; }
-keep class com.myapp.base.api.UsersResponse { *; }
-keep class com.myapp.base.api.HealthResponse { *; }
-keep class com.myapp.base.api.ApiClient { *; }
