import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // JitPack as fallback
        maven(url = "https://jitpack.io")
        // SciJava Public (hosts cz.adaptech:tesseract4android)
        maven(url = "https://maven.scijava.org/content/groups/public")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.scijava.org/content/groups/public")
    }
}

rootProject.name = "TornadoAI"
include(":app")