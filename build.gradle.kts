import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", "1.3.0"))
    }
}

plugins {
    base
    kotlin("jvm") version "1.3.10" apply false
}

allprojects {
    group = "games.thecodewarrior.bitfont"
    version = "1.0"

    repositories {
        jcenter()
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}
