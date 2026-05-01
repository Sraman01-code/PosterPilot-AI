# PosterPilot AI — ProGuard / R8 rules.
#
# Minification is currently OFF for the release build (see app/build.gradle.kts)
# because v0.1 ships to internal testing only. These rules are kept here so that
# when minification is enabled in a future release, Retrofit / Gson / Room
# reflection paths continue to work.

# --- Retrofit ---------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# --- OkHttp -----------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**

# --- Gson -------------------------------------------------------------------
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.opengraphlabs.posterpilot.data.remote.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Room -------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.** { *; }
-keep class com.opengraphlabs.posterpilot.data.local.history.** { *; }

# --- Kotlin coroutines ------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
