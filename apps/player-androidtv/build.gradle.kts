plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.playoutedge.player"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.playoutedge.player"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Project modules
    implementation(project(":libs:contracts"))
    implementation(project(":libs:domain"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.android)
    implementation(libs.serialization.json)

    // Media3 / ExoPlayer
    implementation(libs.bundles.media3)

    // Jetpack Compose for TV
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.tv)

    // Ktor Client
    implementation(libs.bundles.ktor.client)

    // DataStore for preferences
    implementation(libs.datastore.preferences)

    // AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
}
