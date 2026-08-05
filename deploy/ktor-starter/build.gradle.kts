plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.rtiqa.backend"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.8")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.8")
    implementation("io.ktor:ktor-server-auth-jvm:2.3.8")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.8")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.8")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.8")
    implementation("io.ktor:ktor-server-websockets-jvm:2.3.8")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm:2.3.8")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    mainClass.set("com.rtiqa.backend.ApplicationKt")
}
