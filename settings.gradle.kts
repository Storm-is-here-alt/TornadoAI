// settings.gradle.kts
import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Keep Kotlin aligned with AGP 8.5.x (supports Kotlin 2.0.0)
        id("com.android.application") version "8.5.2"
        id("org.jetbrains.kotlin.android") version "2.0.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TornadoAI"
include(":app")