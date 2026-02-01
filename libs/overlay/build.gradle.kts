plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":libs:contracts"))
    api(project(":libs:domain"))

    api(libs.serialization.json)
    api(libs.coroutines.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
}
