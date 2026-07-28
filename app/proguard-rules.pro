# Rtiqa Production ProGuard / R8 Rules

# Keep Domain Data Models and Entities
-keep class com.rtiqa.core.domain.model.** { *; }
-keep class com.rtiqa.core.database.entity.** { *; }
-keep class com.rtiqa.core.network.api.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json *;
}

# Coroutines
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { *; }

# DataStore & Security
-keep class androidx.datastore.** { *; }
-keep class androidx.security.crypto.** { *; }
