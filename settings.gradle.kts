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
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "dagger.hilt.android.plugin") {
                useVersion("2.50")
            }
            if (requested.id.id == "org.jetbrains.kotlin.kapt") {
                useVersion("1.9.21")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/TeeVeeCorp/telly-partner-sdk")
            credentials {
                username = System.getenv("gh_username") ?: "default_username"
                password = System.getenv("gh_password") ?: "default_password"
            }
        }
    }
}

rootProject.name = "Telly_CES_Fallback"
include(":app")
 