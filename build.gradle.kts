val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val waltid_version: String= "0.1.0"

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
}

group = "identity"
version = "0.0.1"

application {
    mainClass.set("identity.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.walt.id/repository/waltid/") }
    maven { url = uri("https://jitpack.io") }

}

dependencies {

    implementation("id.walt.crypto:waltid-crypto:$waltid_version")
    implementation("id.walt.credentials:waltid-verifiable-credentials:$waltid_version")
    implementation("id.walt.did:waltid-did:$waltid_version")
    implementation("id.walt:waltid-sdjwt:$waltid_version")
    implementation("id.walt:waltid-openid4vc:$waltid_version")

}
