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
    listOf(
            "ktor-client-js",
            "ktor-client-websockets"
    ).forEach { name ->
        implementation("io.ktor",name,"1.3.2")
    }

    /* depedencies */
    implementation(npm("text-encoding", "0.7.0"))
    implementation(npm("abort-controller", "3.0.0"))
    //These additionals dependencies pop out after adding the ones above
    implementation(npm("utf-8-validate", "5.0.2"))
    implementation(npm("bufferutil", "4.0.1"))
    implementation(npm("fs", "0.0.2"))
}

kotlin.target.browser {
    webpackTask {
        outputFileName = "musicnova.js"
    }
}
