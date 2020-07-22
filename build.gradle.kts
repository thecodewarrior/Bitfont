plugins {
    base
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
