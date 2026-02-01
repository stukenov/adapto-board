# Playout Edge Player ProGuard rules

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep contracts package (shared DTOs)
-keep class com.playoutedge.contracts.** { *; }

# Keep domain models
-keep class com.playoutedge.domain.** { *; }
