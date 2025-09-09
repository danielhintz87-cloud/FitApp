# ===================================
# FITAPP PROGUARD/R8 OPTIMIZATION RULES
# ===================================

# --- Essential Attributes for Reflection & Serialization ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# --- Remove Debugging Information (Production) ---
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** isLoggable(...);
}

# --- Kotlin Coroutines (Targeted) ---
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.channels.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }

# --- Room Database (Specific) ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# --- FitApp Room Entities (Required for Schema) ---
-keep class com.example.fitapp.data.db.** { *; }
-keepclassmembers class com.example.fitapp.data.db.** {
    <init>(...);
    <fields>;
}

# --- Jetpack Compose (Optimized) ---
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.platform.** { *; }

# --- Retrofit & Networking (Targeted) ---
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# --- Moshi JSON Serialization ---
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# --- Kotlinx Serialization (Optimized) ---
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

# --- Google AI / Gemini (Critical) ---
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# --- FitApp AI Module (Core Components) ---
-keep class com.example.fitapp.ai.** { *; }
-keep class com.example.fitapp.application.** { *; }
-keep class com.example.fitapp.domain.** { *; }
-keep class com.example.fitapp.infrastructure.** { *; }

# --- ML Kit & CameraX (Essential) ---
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.lifecycle.** { *; }

# --- WorkManager (Background Tasks) ---
-keep class androidx.work.impl.WorkManagerImpl { *; }
-keep class androidx.work.impl.background.systemjob.SystemJobService { *; }
-keep class com.example.fitapp.services.** { *; }

# --- Voice Recognition & Reflection ---
-keep class android.speech.** { *; }
-keep class com.example.fitapp.services.VoiceCommandManager { *; }
-keepclassmembers class com.example.fitapp.services.VoiceCommandManager {
    <methods>;
}

# --- Navigation & Gesture System ---
-keep class com.example.fitapp.ui.navigation.** { *; }
-keepclassmembers class com.example.fitapp.ui.navigation.** {
    <methods>;
}

# --- Memory Management & Performance ---
-keep class com.example.fitapp.util.MemoryLeakPrevention { *; }
-keep class com.example.fitapp.util.performance.** { *; }

# --- Coil Image Loading ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- Android Resources ---
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# --- Hilt Dependency Injection (Critical for Runtime) ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Provide* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keep class **Hilt* { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.Provides class * { *; }
-keep @javax.inject.Inject class * { *; }
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <methods>;
}

# --- Prevent Obfuscation of Entry Points ---
-keep public class com.example.fitapp.MainActivity { *; }
-keep public class com.example.fitapp.FitAppApplication { *; }
-keep public class **Hilt_FitAppApplication { *; }

# --- Missing R8 Rules (Generated) ---
-dontwarn com.example.fitapp.Hilt_FitAppApplication
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.element.Modifier
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.SimpleTypeVisitor8
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options$GpuBackend
-dontwarn org.tensorflow.lite.gpu.GpuDelegateFactory$Options

# --- Enum Classes (Preserve) ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Parcelable Classes ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
