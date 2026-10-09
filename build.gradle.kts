// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.47" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Common configuration
ext {
    set("targetSdk", 34)
    set("minSdk", 24)
    set("compileSdk", 34)
    set("kotlinVersion", "1.9.20")
    set("composeVersion", "1.6.0")
}
