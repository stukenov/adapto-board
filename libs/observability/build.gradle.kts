plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.micrometer.prometheus)
    api(libs.logback)
}
