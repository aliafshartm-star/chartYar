# ============ Tapsell Mediation ============
-keep class ir.tapsell.mediation.** { *; }
-keep interface ir.tapsell.mediation.** { *; }
-keep class ir.tapsell.plus.** { *; }
-keep interface ir.tapsell.plus.** { *; }
-keep class ir.tapsell.sdk.** { *; }
-keep interface ir.tapsell.sdk.** { *; }
-keepclassmembers class ir.tapsell.** { *; }
-dontwarn ir.tapsell.**
-dontwarn ir.tapsell.mediation.**

# ============ Google Play Services (Tapsell) ============
-keep class com.google.android.gms.ads.identifier.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# ============ Room ============
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# ============ ML Kit ============
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ============ Coil ============
-dontwarn coil.**

# ============ DataStore ============
-dontwarn androidx.datastore.**

# ============ WorkManager ============
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ============ Tapsell Adapter ============
-keep class ir.tapsell.mediation.adapter.** { *; }
-dontwarn ir.tapsell.mediation.adapter.**

# ============ Retrofit / OkHttp ============
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ============ حفظ اطلاعات خطا برای دیباگ ============
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*

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