# --- Core Kotlin / Coroutines ---
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# --- Room ---
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# --- Jetpack Compose ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Material3 ---
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

# --- Nutrition API Integration ---
-keep class retrofit2.** { *; }
-keep class com.squareup.moshi.** { *; }
-keepattributes *Annotation*, Signature

# --- WorkManager ---
-keep class androidx.work.impl.WorkManagerImpl { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-dontwarn androidx.work.**

# --- Google Generative AI / Gemini ---
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# --- Prevent R class issues ---
-keep class **.R
-keepclassmembers class **.R$* { *; }

# --- CameraX ---
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# --- ML Kit ---
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# --- FitApp specific entities ---
-keep class com.example.fitapp.data.db.** { *; }
-keep class com.example.fitapp.ai.** { *; }

# --- Serialization ---
-keepattributes SerializedName
-keepclassmembers,allowobfuscation class * {
  @kotlinx.serialization.SerialName <fields>;
}
-keep,includedescriptorclasses class com.example.fitapp.**$$serializer { *; }
-keepclassmembers class com.example.fitapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.fitapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- OkHttp ---
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# --- Coil ---
-keep class coil.** { *; }
-dontwarn coil.**
