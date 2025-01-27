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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://github.com/virtusize/virtusize_auth_android/raw/main") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "integration_android"

include(":virtusize")
include(":virtusize-core")
include(":virtusize-auth")
include(":sampleAppCompose")
include(":sampleAppJava")
include(":sampleAppKotlin")
