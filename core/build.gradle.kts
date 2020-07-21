import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    id("maven")
}

repositories {
    jcenter()
}

dependencies {
    "api"(kotlin("stdlib-jdk8", "1.3.0"))
    "api"("com.ibm.icu:icu4j:63.1")
    "implementation"("it.unimi.dsi:fastutil:8.2.2")
    "implementation"("org.msgpack:msgpack-core:0.8.16")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xjvm-default=enable",
        "-Xuse-experimental=kotlin.Experimental",
        "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
    )
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

tasks["jar"].dependsOn.add(sourcesJar)

artifacts {
    this.add("archives", sourcesJar)
}
