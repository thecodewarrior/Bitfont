import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    "compile"(kotlin("stdlib-jdk8", "1.3.0"))
    "compile"("it.unimi.dsi:fastutil:8.2.2")
    "compile"("com.ibm.icu:icu4j:63.1")
    "compile"("org.msgpack:msgpack-core:0.8.16")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.ExperimentalUnsignedTypes", "-Xjvm-default=enable")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

tasks["jar"].dependsOn.add(sourcesJar)
