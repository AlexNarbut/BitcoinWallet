enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
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
    }
}

rootProject.name = "BitcoinApp"
include(":app")

include(":core")
include(":core:common")
include(":core:common-extens")
include(":core:mempool-api")
include(":core:uikit")

include(":feature:transaction:api")
include(":feature:transaction:data")

include(":feature:wallet:api")
include(":feature:wallet:data")
include(":feature:wallet:current-state")
include(":feature:wallet:send-coins")
