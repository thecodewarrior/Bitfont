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
    group = "dev.thecodewarrior.bitfont"
    version = "0.2"

    repositories {
        jcenter()
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}
