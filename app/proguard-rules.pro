# Retrofit and OkHttp Rules
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson Rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Keep our domain / model classes intact for GSON deserialization
-keep class com.mobile.tugasrancangmoka.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Glide Rules (Image caching & loading library)
-keep public class * extends com.bumptech.glide.module.AppGlideModule {
    public <init>();
}
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule {
    public <init>();
}
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Lottie Animation Rules
-keep class com.airbnb.lottie.** { *; }

# MPAndroidChart Rules
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Strip Logcat calls in release builds for cleaner execution and security
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}