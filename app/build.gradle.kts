plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.shopsphere"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shopsphere"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.compose.ui:ui-graphics-android:1.6.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

// Navigation component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

// Loading button
    implementation("br.com.simplepass:loading-button-android:2.2.0")

// Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

// Circular image
    implementation("de.hdodenhof:circleimageview:3.1.0")

// Viewpager2 indicator
    implementation("io.github.vejei.viewpagerindicator:viewpagerindicator:1.0.0-alpha.1")

// StepView
    implementation("com.shuhart.stepview:stepview:1.5.1")

// Android Ktx
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")

// Dagger hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

// Google Material Design
    implementation("com.google.android.material:material:1.12.0")

//    Firebase
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.android.gms:play-services-auth:21.1.1")

//    Coroutines with firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

//    Credential Manager
    implementation ("androidx.credentials:credentials:1.2.2")
    implementation ("androidx.credentials:credentials-play-services-auth:1.2.2")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")

//    Razorpay
    implementation ("com.razorpay:checkout:1.6.38")

}

kapt {
    correctErrorTypes = true
}
