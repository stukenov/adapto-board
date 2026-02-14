pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "playout-edge"

// Apps
include("apps:server")
include("apps:player-androidtv")

// Libs
include("libs:contracts")
include("libs:domain")
include("libs:persistence")
include("libs:auth")
include("libs:storage")
include("libs:overlay")
include("libs:observability")
include("libs:tts")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Kotlin
            version("kotlin", "2.1.10")
            version("coroutines", "1.9.0")
            version("serialization", "1.7.3")

            // Ktor
            version("ktor", "2.3.12")

            // Database
            version("exposed", "0.56.0")
            version("flyway", "10.21.0")
            version("postgresql", "42.7.4")
            version("hikari", "6.0.0")

            // Auth
            version("jwt", "4.4.0")
            version("bcrypt", "0.10.2")

            // Observability
            version("micrometer", "1.14.1")
            version("logback", "1.5.12")

            // Android
            version("agp", "8.9.0")
            version("media3", "1.4.1")
            version("compose", "1.7.5")
            version("composeCompiler", "1.5.15")
            version("datastore", "1.1.1")

            // Testing
            version("junit", "5.11.3")
            version("mockk", "1.13.13")
            version("testcontainers", "1.20.3")

            // Kotlin plugins
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin-android", "org.jetbrains.kotlin.android").versionRef("kotlin")
            plugin("kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("kotlin-compose", "org.jetbrains.kotlin.plugin.compose").versionRef("kotlin")
            plugin("android-application", "com.android.application").versionRef("agp")
            plugin("android-library", "com.android.library").versionRef("agp")

            // Kotlin stdlib
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")
            library("serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("serialization")

            // Ktor Server
            library("ktor-server-core", "io.ktor", "ktor-server-core-jvm").versionRef("ktor")
            library("ktor-server-netty", "io.ktor", "ktor-server-netty-jvm").versionRef("ktor")
            library("ktor-server-content-negotiation", "io.ktor", "ktor-server-content-negotiation-jvm").versionRef("ktor")
            library("ktor-server-auth", "io.ktor", "ktor-server-auth-jvm").versionRef("ktor")
            library("ktor-server-auth-jwt", "io.ktor", "ktor-server-auth-jwt-jvm").versionRef("ktor")
            library("ktor-server-sessions", "io.ktor", "ktor-server-sessions-jvm").versionRef("ktor")
            library("ktor-server-html-builder", "io.ktor", "ktor-server-html-builder-jvm").versionRef("ktor")
            library("ktor-server-status-pages", "io.ktor", "ktor-server-status-pages-jvm").versionRef("ktor")
            library("ktor-server-call-logging", "io.ktor", "ktor-server-call-logging-jvm").versionRef("ktor")
            library("ktor-server-metrics-micrometer", "io.ktor", "ktor-server-metrics-micrometer-jvm").versionRef("ktor")
            library("ktor-server-test-host", "io.ktor", "ktor-server-test-host-jvm").versionRef("ktor")
            library("ktor-serialization-json", "io.ktor", "ktor-serialization-kotlinx-json-jvm").versionRef("ktor")

            // Ktor Client
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor-client-okhttp", "io.ktor", "ktor-client-okhttp").versionRef("ktor")
            library("ktor-client-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")

            // Database
            library("exposed-core", "org.jetbrains.exposed", "exposed-core").versionRef("exposed")
            library("exposed-dao", "org.jetbrains.exposed", "exposed-dao").versionRef("exposed")
            library("exposed-jdbc", "org.jetbrains.exposed", "exposed-jdbc").versionRef("exposed")
            library("exposed-kotlin-datetime", "org.jetbrains.exposed", "exposed-kotlin-datetime").versionRef("exposed")
            library("exposed-json", "org.jetbrains.exposed", "exposed-json").versionRef("exposed")
            library("flyway-core", "org.flywaydb", "flyway-core").versionRef("flyway")
            library("flyway-postgresql", "org.flywaydb", "flyway-database-postgresql").versionRef("flyway")
            library("postgresql", "org.postgresql", "postgresql").versionRef("postgresql")
            library("hikari", "com.zaxxer", "HikariCP").versionRef("hikari")

            // Auth
            library("jwt", "com.auth0", "java-jwt").versionRef("jwt")
            library("bcrypt", "at.favre.lib", "bcrypt").versionRef("bcrypt")

            // Observability
            library("micrometer-prometheus", "io.micrometer", "micrometer-registry-prometheus").versionRef("micrometer")
            library("logback", "ch.qos.logback", "logback-classic").versionRef("logback")

            // Android Media
            library("media3-exoplayer", "androidx.media3", "media3-exoplayer").versionRef("media3")
            library("media3-ui", "androidx.media3", "media3-ui").versionRef("media3")
            library("media3-session", "androidx.media3", "media3-session").versionRef("media3")

            // Android Compose
            library("compose-ui", "androidx.compose.ui", "ui").versionRef("compose")
            library("compose-material", "androidx.compose.material", "material").versionRef("compose")
            library("compose-material3", "androidx.compose.material3", "material3").version("1.3.1")
            library("compose-tv", "androidx.tv", "tv-foundation").version("1.0.0-alpha11")

            // Android DataStore
            library("datastore-preferences", "androidx.datastore", "datastore-preferences").versionRef("datastore")

            // Testing
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            library("testcontainers", "org.testcontainers", "testcontainers").versionRef("testcontainers")
            library("testcontainers-postgresql", "org.testcontainers", "postgresql").versionRef("testcontainers")
            library("testcontainers-junit", "org.testcontainers", "junit-jupiter").versionRef("testcontainers")

            // Bundles
            bundle("ktor-server", listOf(
                "ktor-server-core",
                "ktor-server-netty",
                "ktor-server-content-negotiation",
                "ktor-server-auth",
                "ktor-server-auth-jwt",
                "ktor-server-sessions",
                "ktor-server-html-builder",
                "ktor-server-status-pages",
                "ktor-server-call-logging",
                "ktor-serialization-json"
            ))

            bundle("exposed", listOf(
                "exposed-core",
                "exposed-dao",
                "exposed-jdbc",
                "exposed-kotlin-datetime",
                "exposed-json"
            ))

            bundle("ktor-client", listOf(
                "ktor-client-core",
                "ktor-client-okhttp",
                "ktor-client-content-negotiation",
                "ktor-serialization-json"
            ))

            bundle("media3", listOf(
                "media3-exoplayer",
                "media3-ui",
                "media3-session"
            ))

            bundle("testcontainers", listOf(
                "testcontainers",
                "testcontainers-postgresql",
                "testcontainers-junit"
            ))
        }
    }
}
