// Top-level build file
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    }
}

// Repo untuk dependency project sudah didefinisikan terpusat di settings.gradle.kts
// (dependencyResolutionManagement), jadi TIDAK boleh ada allprojects { repositories {} }
// di sini — bentrok dengan RepositoriesMode.FAIL_ON_PROJECT_REPOS.
