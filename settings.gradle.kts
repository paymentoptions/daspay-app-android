pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // JetBrains Compose Multiplatform plugin repository
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        // JetBrains Compose Multiplatform libraries
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        // JetBrains Navigation Compose (CMP)
        maven("https://androidx.dev/storage/compose-compiler/repository/")

        // MineSec's maven registry
        maven {
            val MINESEC_REGISTRY_LOGIN: String? by settings
            val MINESEC_REGISTRY_TOKEN: String? by settings

            requireNotNull(MINESEC_REGISTRY_LOGIN) {
                """
                    Please set your MineSec Github credential in `gradle.properties`.
                    On local machine,
                    ** DO NOT **
                    ** DO NOT **
                    ** DO NOT **
                    Do not put it in the project's file. (and accidentally commit and push)
                    ** DO **
                    Do set it in your machine's global (~/.gradle/gradle.properties)
                """.trimIndent()
            }
            requireNotNull(MINESEC_REGISTRY_TOKEN)
            println("MS GPR: $MINESEC_REGISTRY_LOGIN")

            name = "MineSecMavenClientRegistry"
            url = uri("https://maven.pkg.github.com/theminesec/ms-registry-client")
            credentials {
                username = MINESEC_REGISTRY_LOGIN
                password = "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"
            }
        }

    }
}

rootProject.name = "PaymentOptionsPOS"
include(":app")
include(":shared")
