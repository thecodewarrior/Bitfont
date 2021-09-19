import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm")
    id("org.jetbrains.dokka")
}

group = "dev.thecodewarrior"

val library_version: String by project
val snapshotVersion = System.getenv("SNAPSHOT_REF")?.let { ref ->
    if(!ref.startsWith("refs/heads/"))
        throw IllegalStateException("SNAPSHOT_REF `$ref` doesn't start with refs/heads/")
    ref.removePrefix("refs/heads/").replace("[^.\\w-]".toRegex(), "-") + "-SNAPSHOT"
}
val prism_version = snapshotVersion ?: library_version
version = prism_version

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    api("com.ibm.icu:icu4j:63.1")
    implementation("it.unimi.dsi:fastutil:8.2.2")
    implementation("org.msgpack:msgpack-core:0.8.16")
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        javaParameters = true
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xjvm-default=all",
            "-Xuse-experimental=kotlin.Experimental",
            "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
        )
    }
}

// ---------------------------------------------------------------------------------------------------------------------
//region // Publishing

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaJavadoc"))
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("Prism")
                description.set("Renderer-agnostic bitmap text renderer")
                url.set("https://github.com/thecodewarrior/Prism")

                licenses {
                    license {
                        name.set("BSD-2-Clause")
                        url.set("https://opensource.org/licenses/BSD-2-Clause")
                    }
                }
                developers {
                    developer {
                        id.set("thecodewarrior")
                        name.set("Pierce Corcoran")
                        email.set("code@thecodewarrior.dev")
                        url.set("https://thecodewarrior.dev")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/thecodewarrior/Prism.git")
                    developerConnection.set("scm:git:ssh://github.com:thecodewarrior/Prism.git")
                    url.set("https://github.com/thecodewarrior/Prism")
                }
            }
        }
    }

    repositories {
        maven {
            name = "ossrh"

            val stagingRepo = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotRepo = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = URI(if(prism_version.endsWith("SNAPSHOT")) snapshotRepo else stagingRepo)
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME") ?: "N/A"
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD") ?: "N/A"
            }
        }
    }
}

signing {
    if(System.getenv("SIGNING_KEY") != null) {
        useInMemoryPgpKeys(
            System.getenv("SIGNING_KEY_ID"),
            System.getenv("SIGNING_KEY"),
            System.getenv("SIGNING_KEY_PASSWORD")
        )
    } else {
        useGpgCmd()
    }

    sign(publishing.publications["maven"])
}

//endregion // Publishing
// ---------------------------------------------------------------------------------------------------------------------
