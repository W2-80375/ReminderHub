import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

configurations.all {
    resolutionStrategy {
        force(libs.androidx.concurrent.futures)
        force(libs.androidx.concurrent.futures.ktx)
    }
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {

    namespace = "com.own.remindme"

    compileSdk = 36

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrEmpty()) {
                storeFile = file(storeFilePath)
            }
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val groqApiKey = localProperties.getProperty("GROQ_API_KEY")
            ?: project.findProperty("GROQ_API_KEY")
            ?: ""

        buildConfigField(
            "String",
            "GROQ_API_KEY",
            "\"$groqApiKey\""
        )
        applicationId = "com.own.remindme"

        minSdk = 24

        targetSdk = 36

        versionCode = 2

        versionName = "1.1"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        release {

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }

    }

    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_17

        targetCompatibility = JavaVersion.VERSION_17

    }

    kotlinOptions {

        jvmTarget = "17"

    }

    buildFeatures {

        compose = true

    }

}

dependencies {

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)

    // Core
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.compose.ui.graphics)

    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.litert.support.api)

    // Navigation
    implementation(libs.navigation.compose)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)

    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.room.runtime)

    implementation(libs.room.ktx)

    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Coil
    implementation(libs.coil.compose)

    // Lottie
    implementation(libs.lottie.compose)

    // Coroutines
    implementation(libs.coroutines.android)

    // WorkManager
    implementation(libs.work.runtime)

    // Splash Screen
    implementation(libs.splashscreen)

    // Icons
    implementation(libs.material.icons)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Unit Test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Test
    androidTestImplementation(platform(libs.androidx.compose.bom))

    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.espresso.core)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)

    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("androidx.compose.material:material-icons-extended")

}