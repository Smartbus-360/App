

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    id("kotlin-kapt")
}
android {
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
}

android {
    namespace = "com.smartbus360.app"
    compileSdk = 35
    bundle {
        language {
            enableSplit = false
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
    }

    defaultConfig {
        applicationId = "com.smartbus360.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 40
        versionName = "2.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

//    buildTypes {
//        release {
//
//            // Add this line to retain debug symbols in the release APK/AAB
////            ndk {
////                debugSymbolLevel = 'FULL' // Options: NONE, SYMBOL_TABLE, FULL
////            }
//            ndk.debugSymbolLevel = "FULL"
//            isMinifyEnabled = true
//
//            // Enables resource shrinking, which is performed by the
//            // Android Gradle plugin.
//            isShrinkResources = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//
//    }

    buildFeatures {
        buildConfig = true  // Enable BuildConfig fields for all build types
    }
    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.smartbus360.com/\"")
            isMinifyEnabled = true
            isShrinkResources = true
            ndk.debugSymbolLevel = "FULL"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            buildConfigField( "String", "API_BASE_URL", "\" https://api.smartbus360.com//\"")
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        // Enable desugaring for backward compatibility
        isCoreLibraryDesugaringEnabled = true // Enable desugaring
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    ndkVersion = "25.2.9519653"

}

dependencies {

    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.common.android)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.swiperefreshlayout)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("io.socket:socket.io-client:2.0.1")
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")
// Socket.IO client
    implementation("org.json:json:20231013")
// For JSON parsing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
//
//    // Koin core for dependency injection
//    implementation (libs.koin.android)
//    // Koin for ViewModel integration
//    implementation (libs.koin.androidx.viewmodel)
    //
    implementation (libs.retrofit)
    implementation (libs.gson)
    implementation (libs.converter.gson)
    implementation (libs.logging.interceptor)

    // Koin Core for Dependency Injection
    implementation ("io.insert-koin:koin-android:3.5.6")

// Koin for Jetpack Compose integration
    implementation (libs.koin.androidx.compose)

    //
    implementation(libs.androidx.navigation.compose.v260)

//// Koin ViewModel
//    implementatisupporton (libs.koin.androidx.viewmodel.v356)
    // Socket.IO client for WebSockets
    implementation(libs.socket.io.client) // Replace with actual version

    // Coroutines for asynchronous programming
    implementation(libs.kotlinx.coroutines.android)

    // OkHttp for networking
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)

    // Jetpack Compose UI
    implementation(libs.ui)
    implementation(libs.material3)

    // Activity Compose integration
    implementation(libs.androidx.activity.compose.v170)

    // Play Services for Location API
    implementation(libs.play.services.location)

    // Google Fonts for Jetpack Compose
    implementation(libs.androidx.ui.text.google.fonts)

    // osmdroid Android library
    implementation(libs.osmdroid.android) // Replace with actual version if needed
    implementation(libs.androidx.material3.v101)

    //OlaMap SDK
          implementation(files("libs/OlaMapSdk-1.5.0.aar"))

//Maplibre
    implementation ("org.maplibre.gl:android-sdk:10.0.2")
    implementation ("org.maplibre.gl:android-plugin-annotation-v9:1.0.0")
    implementation ("org.maplibre.gl:android-plugin-markerview-v9:1.0.0")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation(libs.coil.compose)

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Optional for Kotlin coroutines support
    implementation("androidx.room:room-ktx:2.6.1")

    //swipe referesh web
    //  for dark theme
    // System UI Controller
    implementation(libs.accompanist.systemuicontroller)

    // Swipe Refresh
    implementation(libs.accompanist.swiperefresh) // Check for latest version
    implementation ("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation (libs.androidx.activity.ktx)
    implementation (libs.androidx.ui.v152)
    implementation (libs.androidx.material)
    implementation(libs.androidx.runtime)
    implementation ("androidx.core:core-splashscreen:1.0.1")
//    implementation("com.google.android.play:core:1.10.0") // Updated version
    implementation("com.github.bumptech.glide:glide:4.12.0")
    // Add desugaring library
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation ("com.google.android.play:app-update:2.1.0")

    implementation ("com.google.android.play:review-ktx:2.0.0")

    //implementation("com.google.android.play:core-ktx:1.8.1")
    implementation ("com.google.accompanist:accompanist-pager:0.32.0")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    //QR Scanner Dependencies
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")


    implementation ("com.google.accompanist:accompanist-navigation-animation:0.34.0") // or latest
    implementation(libs.androidx.foundation) // or latest stable
    implementation ("androidx.compose.foundation:foundation:1.4.3" )// or latest stable

}
