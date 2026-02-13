plugins {
    kotlin("jvm") version "2.2.21"
}

group = "gg.nippon.squad"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.lavalink.dev/releases")
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:6.1.2")

    // Lavalink Client
    implementation("dev.arbjerg:lavalink-client:3.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    //Logging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.13")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.21")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}