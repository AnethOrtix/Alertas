plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

//con verde, blanco negro para el frontend
android {
    namespace = "com.example.alertas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.alertas"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // Para trabajar con BLE y RxAndroidBle
    implementation("com.polidea.rxandroidble3:rxandroidble:1.17.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")  // Para RxJava
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")  // Para RxAndroid
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8") // Ensure RxJava is included
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation ("com.polidea.rxandroidble3:rxandroidble:1.17.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0") // Or latest version
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidthings)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}