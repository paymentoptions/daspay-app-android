import java.io.FileInputStream
import java.time.LocalDate
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    kotlin("plugin.serialization") version "2.0.21"
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    //implementation(platform(libs.androidx.compose.bom.v20250500)) //Vishal commented this dependency
    //implementation(platform(libs.androidx.compose.bom)) //Vishal commented this dependency
    implementation(libs.androidx.foundation) //Vishal added this dependency
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.lint)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    //RetroFit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)


//    Minesec - using local AAR since remote registry is unavailable
    releaseImplementation(libs.headless.stage)
    debugImplementation(libs.headless.stage)

//    releaseImplementation(files("libs/minehades-stage-1.10.105.12.61.aar"))
//    debugImplementation(files("libs/minehades-stage-1.10.105.12.61.aar"))
//    releaseImplementation(files("libs/headless-stage-1.2.17-stage-release.aar"))
//    debugImplementation(files("libs/headless-stage-1.2.17-stage-release.aar"))




    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    //ycharts
    implementation(libs.ycharts)

    //System UI Color
    implementation(libs.accompanist.systemuicontroller)

    //Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.gif)

    implementation(libs.kotlinx.datetime)

    // QR Code Generation (ZXing)
    implementation(libs.core)

    // For logging API requests and responses
    implementation(libs.logging.interceptor)

    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation(platform(libs.log4j.bom))
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // WorkManager for background task scheduling
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation(libs.accompanist.swiperefresh)
}
