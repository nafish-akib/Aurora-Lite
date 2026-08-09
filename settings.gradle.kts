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

rootProject.name = "Aurora"
include(":app")
include(":browser")
include(":home")
include(":engine:api")
include(":engine:webview")
include(":ui:focus")
include(":ui:components")
include(":core")
include(":data")
include(":design")
include(":motion")
include(":Aurora_UI_Compose")

