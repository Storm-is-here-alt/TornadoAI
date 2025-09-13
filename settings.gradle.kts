// settings.gradle.kts
import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // JitPack only if/when you need it
        maven(url = "https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // keep JitPack here disabled unless needed
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "TornadoAI"
include(":app")