plugins {
    id("org.jetbrains.kotlin.js")
}

group = "eu.musicnova"

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/kotlin/kotlin-eap/")
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":musicnova-shared"))
    implementation(npm("sweetalert2"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
}

kotlin.target.browser {
    webpackTask {
        outputFileName = "musicnova.js"
    }
}
