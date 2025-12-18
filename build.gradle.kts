import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.docta.dds"
version = "0.0.1"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.ktor)
}

application {
    mainClass = "com.docta.dds.ApplicationKt"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.http.redirect)
    implementation(libs.ktor.server.status.pages)
    // Ktor Client
    implementation(libs.ktor.client.cio.jvm)
    implementation(libs.ktor.client.logging)
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    // Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    // dRPC
    implementation(libs.drpc)
    // Utilities
    implementation(libs.kotlinx.datetime)
    implementation(libs.logback.classic)
    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.koin.test)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}
