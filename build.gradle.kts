// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) version "8.8.1" apply false
    alias(libs.plugins.kotlin.android)  apply false
    alias(libs.plugins.kotlin.compose)  apply false
    alias(libs.plugins.kotlin.serialization) apply false

    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.datadoghq.dd-sdk-android-gradle-plugin") version "1.16.0" apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false

    // Compose Multiplatform – JetBrains runtime + resources plugin
    alias(libs.plugins.jetbrains.compose) apply false
}

