pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT) // <-- allow project-level repos
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TornadoAI"
include(":app")