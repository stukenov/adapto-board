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
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // UI Testing with Playwright
    testImplementation("com.microsoft.playwright:playwright:1.49.0")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.playoutedge.server.ApplicationKt"
    }
}

tasks.test {
    useJUnitPlatform {
        // Exclude UI tests by default (require RUN_UI_TESTS=true)
        excludeTags("ui")
    }
    // Run tests sequentially to avoid database container race conditions
    maxParallelForks = 1
}

// Separate task for UI tests
tasks.register<Test>("uiTest") {
    description = "Runs UI tests with Playwright"
    group = "verification"

    // Set the classpath and test classes
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    useJUnitPlatform {
        includeTags("ui")
    }

    // Pass environment variable to enable UI tests
    environment("RUN_UI_TESTS", "true")
}
