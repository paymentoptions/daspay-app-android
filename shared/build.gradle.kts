import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    // Compose Multiplatform: JetBrains runtime + compiler
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    // kotlinx.serialization for all models
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.paymentoptions.pos.shared"
        compileSdk = 36
        minSdk = 24

        withHostTestBuilder {}

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    val xcfName = "sharedKit"
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    // Workaround for https://youtrack.jetbrains.com/issue/CMP-5683
    // syncComposeResourcesForIos task fails when run from command line or IDE
    // as it expects Xcode environment variables.
    tasks.matching { it.name == "syncComposeResourcesForIos" }.configureEach {
        onlyIf {
            val isXcode = System.getenv("SDK_NAME") != null
            if (!isXcode) {
                logger.info("Skipping $name because it's not running from Xcode")
            }
            isXcode
        }
    }

    sourceSets {

        // ── Common (shared across Android + iOS) ─────────────────────────────
        commonMain {
            dependencies {
                // Kotlin stdlib
                implementation(libs.kotlin.stdlib)

                // Coroutines
                implementation(libs.kotlinx.coroutines.core)

                // Compose Multiplatform UI — api() so app-module screens can compile
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.ui)
                api(compose.materialIconsExtended)
                implementation(compose.components.resources)   // fonts, drawables
                implementation(compose.components.uiToolingPreview)

                // Navigation (JetBrains CMP fork of androidx.navigation:navigation-compose)
                api(libs.jetbrains.navigation.compose)

                // Ktor multiplatform HTTP client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.auth)

                // Multiplatform Settings (replaces EncryptedSharedPreferences / NSUserDefaults)
                implementation(libs.multiplatform.settings)

                // Kotlinx — api() so app code can use Json{} and datetime directly
                api(libs.kotlinx.serialization.json)
                api(libs.kotlinx.datetime)

                // Coil 3 – api() so app image components can use AsyncImage
                api(libs.coil.compose)
                implementation(libs.coil.network.ktor2)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        // ── Android-specific ─────────────────────────────────────────────────
        androidMain {
            dependencies {
                // Ktor engine for Android
                implementation(libs.ktor.client.okhttp)

                // Coroutines on Android
                implementation(libs.kotlinx.coroutines.android)

                // Activity Compose (needed to host shared UI in MainActivity)
                implementation(libs.androidx.activity.compose)

                // Encrypted SharedPreferences backing for AppStorage on Android
                implementation("androidx.security:security-crypto:1.1.0-alpha06")

                // Biometric for FingerprintAutoLogin
                implementation(libs.androidx.biometric)

                // Firebase (Android only)
                implementation(libs.firebase.messaging.ktx)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }

        // ── iOS-specific ─────────────────────────────────────────────────────
        iosMain {
            dependencies {
                // Ktor engine for iOS (uses NSURLSession / Darwin)
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}
