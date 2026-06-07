import java.io.FileInputStream
import java.time.LocalDate
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
}


val keystoreProperties = Properties().apply {
    // Use the correct relative path to your keystore.properties file
    val keystoreFile = rootProject.file(".sign/keystore.properties") // Change path if your file is elsewhere
    if (keystoreFile.exists()) {
        load(FileInputStream(keystoreFile))
    } else {
        println("WARNING: keystore.properties file not found at: ${keystoreFile.absolutePath}")
    }
}

android {
    namespace = "com.paymentoptions.pos"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.paymentoptions.pos"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "3.0"


        val appName = "Daspay"
        base.archivesName.set("$appName-${versionName}-${versionCode}-${LocalDate.now()}")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
           // buildConfigField("String", "CURRENCY", "\"SGD\"")
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
            buildConfigField("String", "CONFIG_BASE_URL", "\"https://api-dev.paymentoptions.com/api/v1/\"")
            versionNameSuffix = "-dev"
        }
        create("staging") {
            dimension = "environment"
           // buildConfigField("String", "CURRENCY", "\"SGD\"")
            buildConfigField("String", "ENVIRONMENT", "\"STAGING\"")
            buildConfigField("String", "CONFIG_BASE_URL", "\"https://api-staging.paymentoptions.com/api/v1/\"")
            versionNameSuffix = "-staging"
        }
        create("production") {
            dimension = "environment"
            //buildConfigField("String", "CURRENCY", "\"SGD\"")
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
            buildConfigField("String", "CONFIG_BASE_URL", "\"https://api.paymentoptions.com/api/v1/\"")
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = keystoreProperties["storeFile"]?.let { rootProject.file(it as String) }
            storePassword = keystoreProperties["storePassword"] as String?
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
        }

        create("release") {
            storeFile = keystoreProperties["storeFile"]?.let { rootProject.file(it as String) }
            storePassword = keystoreProperties["storePassword"] as String?
            keyAlias = keystoreProperties["keyAlias"] as String?
            keyPassword = keystoreProperties["keyPassword"] as String?
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"

            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    // ── Shared CMP module ─────────────────────────────────────────────────────
    implementation(project(":shared"))

    // ── Android app-level deps (platform-specific only) ───────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.androidx.foundation) //Vishal added this dependency
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.lint)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.runtime)

    // MineSec Headless SDK (Android-only payment terminal SDK)
    releaseImplementation(libs.headless.stage)
    debugImplementation(libs.headless.stage)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging.ktx)  // FcmService extends FirebaseMessagingService

    // Biometrics (IsBiometricAvailable.kt still in app)
    implementation(libs.androidx.biometric)

    // OkHttp (UploadMedia.kt uses it directly for S3 PUT)
    implementation(libs.okhttp)

    // Charts (Android-only until vico releases stable CMP support)
    implementation(libs.ycharts)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.mpandroidchart)

    // QR Code (ZXing – Android only; iOS uses CoreImage)
    implementation(libs.core)

    // WorkManager for background retries
    implementation(libs.androidx.work.runtime.ktx)

    // Logging
    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)

    // Accompanist (Android-only)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.swiperefresh)

    // Coil GIF support (Android-only extra)
    implementation(libs.coil.gif)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
