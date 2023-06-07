plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("net.dv8tion:JDA:5.0.0-beta.10")
    implementation("io.ktor:ktor-client-java:2.3.1")
    implementation("org.litote.kmongo:kmongo-coroutine-serialization:4.7.2")

    implementation("io.github.microutils:kotlin-logging:2.1.23")
    implementation("ch.qos.logback:logback-classic:1.4.7")
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_17.toString()
                apiVersion = "1.7"
                languageVersion = "1.7"
            }
        }
    }

    sourceSets.all {
        languageSettings {
            progressiveMode = true
            optIn("kotlin.RequiresOptIn")
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes("Main-Class" to "blue.starry.unveiler.MainKt")
    }
}
