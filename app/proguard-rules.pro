# ============ Tapsell ProGuard Rules ============
-keep class ir.tapsell.** { *; }
-keepclassmembers class ir.tapsell.** { *; }
-dontwarn ir.tapsell.**

# ============ AndroidX ============
-keep class androidx.** { *; }
-dontwarn androidx.**

# ============ Kotlin ============
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.** { *; }
-dontwarn kotlin.**

# ============ Room ============
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ============ Retrofit / OkHttp (اختیاری) ============
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ============ ML Kit ============
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ============ Coil ============
-dontwarn coil.**

# ============ DataStore ============
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ============ WorkManager ============
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ============ Tapsell Adapter ============
-keep class ir.tapsell.mediation.adapter.** { *; }
-dontwarn ir.tapsell.mediation.adapter.**

# ============ حفظ نام کلاس‌های Compose ============
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============ اطلاعات خطا برای دیباگ ============
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============ حفظ Serializable و Parcelable ============
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}