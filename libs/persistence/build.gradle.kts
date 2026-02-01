plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":libs:domain"))
    api(project(":libs:contracts"))

    api(libs.bundles.exposed)
    api("org.jetbrains.exposed:exposed-java-time:0.56.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    api(libs.serialization.json)
    api(libs.flyway.core)
    api(libs.flyway.postgresql)
    api(libs.postgresql)
    api(libs.hikari)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bundles.testcontainers)
}
