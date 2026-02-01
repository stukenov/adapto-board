plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":libs:contracts"))
    api(project(":libs:domain"))

    api(libs.jwt)
    api(libs.bcrypt)
    api(libs.coroutines.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
