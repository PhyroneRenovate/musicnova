plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

group = "eu.musicnova"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.slf4j","slf4j-api","1.7.30")
}
tasks {
    shadowJar {
        classifier = null
        version = null
        baseName = "agent"
        manifest{
            attributes["Can-Redefine-Classes"] = "true"
            attributes["Can-Retransform-Classes"] = "true"
            val mainClass = "eu.musicnova.lazyloadagend.AgentMain"
            attributes["Agent-Class"] = mainClass
            attributes["Premain-Class"] = mainClass
        }
    }
}