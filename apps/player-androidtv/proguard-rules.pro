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

# SLF4J - ignore missing bindings (Ktor uses SLF4J)
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**

# Ktor
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep player data classes for serialization
-keep class com.playoutedge.player.network.** { *; }
-keep class com.playoutedge.player.config.** { *; }
-keep class com.playoutedge.player.playlist.** { *; }
-keep class com.playoutedge.player.telemetry.** { *; }
-keep class com.playoutedge.player.overlay.** { *; }
-keep class com.playoutedge.player.enrollment.** { *; }
