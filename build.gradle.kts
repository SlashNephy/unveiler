plugins {
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.serialization") version "1.8.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("net.dv8tion:JDA:5.0.0-beta.17")
    implementation("io.ktor:ktor-client-java:2.3.5")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.10.0")

    implementation("io.github.microutils:kotlin-logging:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.11")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.unveiler.MainKt")
    }
}
