# DayCountApp ProGuard Rules

# Keep Room entities
-keep class com.daycountapp.data.model.** { *; }
-keep class com.daycountapp.data.local.** { *; }

# Keep serialization
-keepattributes *Annotation*
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Compose
-dontwarn androidx.compose.**

# Keep Coil
-dontwarn coil.**
