import org.gradle.internal.os.OperatingSystem

plugins {
    java
    kotlin("jvm")
}

val lwjglNatives = when(OperatingSystem.current()) {
    OperatingSystem.LINUX -> "natives-linux"
    OperatingSystem.MAC_OS -> "natives-macos"
    OperatingSystem.WINDOWS -> "natives-windows"
    else -> ""
}
val lwjglVersion = "3.2.3"
val jimguiVersion = "v0.9"

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-dev") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(project(":bitfont"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("it.unimi.dsi:fastutil:8.2.2")
    implementation("com.ibm.icu:icu4j:63.1")
    implementation("org.msgpack:msgpack-core:0.8.16")

    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-nuklear:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-glfw:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-nfd:$lwjglVersion")
    implementation("org.ice1000.jimgui:core:$jimguiVersion") // basic functionality

    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nuklear:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-glfw:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-nfd:$lwjglVersion:$lwjglNatives")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=enable",
                "-Xuse-experimental=kotlin.Experimental",
                "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
        )
    }
}
