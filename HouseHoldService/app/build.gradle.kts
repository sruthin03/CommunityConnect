plugins {
    //alias(libs.plugins.android.application)
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.householdservice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.householdservice"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation ("com.google.firebase:firebase-auth:21.1.0")
    implementation ("com.google.firebase:firebase-firestore:24.7.1")
    implementation ("com.google.firebase:firebase-auth:22.0.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.play.services.maps)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation ("com.squareup.picasso:picasso:2.8") // For image loading
    implementation ("com.github.bumptech.glide:glide:4.12.0") // Optional, another image loader
    implementation ("com.cloudinary:cloudinary-android:2.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)




    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
