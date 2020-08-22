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
    implementation(npm("sweetalert2","9.17.1"))
    implementation(npm("@vizuaalog/bulmajs","0.11.0"))
    implementation(npm("@popperjs/core","2.4.4"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.9")
}

kotlin.target {
    browser {
        webpackTask {
            outputFileName = "musicnova.js"
        }

    }
}

