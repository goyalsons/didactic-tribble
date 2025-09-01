// Root build.gradle.kts

plugins {
    // Android Gradle Plugin (AGP)
    id("com.android.application") version "8.5.2" apply false

    // Kotlin Android plugin
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

// No other blocks needed here. Repositories are defined in settings.gradle.kts via pluginManagement/dependencyResolutionManagement.
