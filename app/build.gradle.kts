plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-kapt")
}

android {

    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.evolve"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
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

    namespace = "com.example.evolve"

    sourceSets {
        getByName("main") {
            res.srcDirs("src/main/res")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation(libs.androidx.espresso.core)
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")

    // JSON parsing
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("io.coil-kt:coil-compose:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // JUnit の依存関係 (Unit テスト用)
    testImplementation("junit:junit:4.13.2")

    // Android Instrumentation テストのための JUnit
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.compose.foundation:foundation:1.5.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}