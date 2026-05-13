plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

group = "com.envirowatchsi"
version = "1.0.0"

application {
    mainClass.set("com.envirowatchsi.server.ApiServerKt")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-gson:2.3.12")
    implementation("org.jetbrains.exposed:exposed-core:0.53.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.53.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.53.0")
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")

    implementation(project(":shared"))
}