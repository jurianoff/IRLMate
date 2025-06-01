val TWITCH_CLIENT_ID: String by project

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.jurianoff.irlmate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jurianoff.irlmate"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "0.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TWITCH_CLIENT_ID", "\"$TWITCH_CLIENT_ID\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // 1.5.3 jest kompatybilny z Compose BOM 2025.05.00 i Kotlin 2.0.x
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    /* ---------- Compose BOM ---------- */
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.material:material-icons-extended")

    /* ---------- Accompanist ---------- */
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.34.0")

    /* ---------- Coil (GIF/WebP animowane) ---------- */
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    /* ---------- Pozostałe biblioteki ---------- */
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.pusher:pusher-java-client:2.4.0")
    implementation("androidx.webkit:webkit:1.9.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.android.material:material:1.11.0")

    /* ---------- Testy ---------- */
    testImplementation(libs.junit)
    testImplementation("org.json:json:20240303")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    /* ---------- Debug ---------- */
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
