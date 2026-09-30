# ============ ChartYar ProGuard Rules ============

# حفظ کلاس‌های اصلی اپ
-keep class com.chartyar.app.** { *; }
-keepclassmembers class com.chartyar.app.** { *; }

# حفظ اطلاعات Debug
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ============ Room Database ============
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# ============ ML Kit ============
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ============ Jetpack Compose ============
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }

# ============ Kotlin ============
-keepclassmembers class kotlin.Metadata { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ============ Kotlin Reflection ============
-keep class kotlin.reflect.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-dontwarn kotlin.reflect.**

# ============ JSON ============
-keep class org.json.** { *; }

# ============ Firebase (اگه استفاده می‌شه) ============
-keepattributes *Annotation*
-keepattributes Signature
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============ Security ============
-keep class com.chartyar.app.security.** { *; }

# ============ Data Classes ============
-keep class com.chartyar.app.data.** { *; }
-keep class com.chartyar.app.analysis.** { *; }

# ============ حذف لاگ‌ها در Release ============
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# ============ حذف کدهای Debug ============
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(java.lang.Object);
    public static void checkNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
}

# ============ حفظ Enum ============
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============ حفظ Parcelable ============
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ============ حفظ Serializable ============
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ============ حفظ WebView ============
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# ============ حذف هشدارها ============
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn com.squareup.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============ حفظ سازندگان ============
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ============ حفظ ViewModel ============
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ============ حفظ DataStore ============
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# ============ حفظ Coil ============
-keep class coil.** { *; }
-dontwarn coil.**

# ============ R8 Rules جدید ============
-allowaccessmodification
-repackageclasses ''

# ============ حفظ R ============
-keepclassmembers class **.R$* {
    public static <fields>;
}