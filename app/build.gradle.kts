plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.mychelin_page"
    compileSdk = 35
    buildFeatures{
        viewBinding=true
    }

    defaultConfig {
        applicationId = "com.example.mychelin_page"
        minSdk = 30
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    implementation ("com.squareup.retrofit2:retrofit:2.9.0'")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")


    implementation ("com.google.android.gms:play-services-location:21.3.0")
    implementation ("com.naver.maps:map-sdk:3.19.1") // Use the latest version

//    // Firebase 추가
//    implementation ("com.google.firebase:firebase-bom:32.7.0"))
    implementation (libs.firebase.messaging.ktx)
    implementation (libs.androidx.work.runtime.ktx)

//    implementation("org.locationtech.proj4j:proj4j:1.2.3")
//    implementation("org.osgeo.proj4j:proj4j:1.5.1")
//    implementation (libs.proj4j)

    // 스플래시 스크린
    implementation ("androidx.core:core-splashscreen:1.0.1")
    // Glide for image loading
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
}