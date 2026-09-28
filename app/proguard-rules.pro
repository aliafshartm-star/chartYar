# ChartYar ProGuard Rules

# حفظ کلاس‌های اصلی
-keep class com.chartyar.app.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Compose
-dontwarn androidx.compose.**

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Kotlin Metadata
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes *Annotation*

# حفظ نام‌های سازنده
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# حذف لاگ‌ها در Release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# JSON
-keep class org.json.** { *; }