plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.playoutedge.server.ApplicationKt")
}

dependencies {
    // Project modules
    implementation(project(":libs:contracts"))
    implementation(project(":libs:domain"))
    implementation(project(":libs:persistence"))
    implementation(project(":libs:auth"))
    implementation(project(":libs:storage"))
    implementation(project(":libs:overlay"))
    implementation(project(":libs:observability"))

    // Ktor Server
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.server.metrics.micrometer)

    // Serialization
    implementation(libs.serialization.json)

    // Database
    implementation(libs.bundles.exposed)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)
    implementation(libs.postgresql)
    implementation(libs.hikari)

    // Auth
    implementation(libs.jwt)
    implementation(libs.bcrypt)

    // Observability
    implementation(libs.micrometer.prometheus)
    implementation(libs.logback)

    // Coroutines
    implementation(libs.coroutines.core)

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.bundles.testcontainers)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.playoutedge.server.ApplicationKt"
    }
}
