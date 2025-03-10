plugins {
    kotlin("jvm") version "2.0.21"
}

group = "identity"
version = "0.0.1"

object Versions {
    const val KOTLIN_VERSION = "2.0.21" // also change 1 plugin
    const val WALTID_VERSION = "0.12.0"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.waltid.dev/releases")
}

dependencies {

    // walt.id
    // required dependencies for running the example project
    implementation("id.walt.crypto:waltid-crypto:${Versions.WALTID_VERSION}")
    implementation("id.walt.credentials:waltid-verifiable-credentials:${Versions.WALTID_VERSION}")
    implementation("id.walt.did:waltid-did:${Versions.WALTID_VERSION}")
    implementation("id.walt.sdjwt:waltid-sdjwt:${Versions.WALTID_VERSION}")
    implementation("id.walt.openid4vc:waltid-openid4vc:${Versions.WALTID_VERSION}")
    implementation("id.walt.policies:waltid-verification-policies:${Versions.WALTID_VERSION}")
    implementation("id.walt.dif-definitions-parser:waltid-dif-definitions-parser:${Versions.WALTID_VERSION}")

    // all walt.id dependencies (not required for this project)
    implementation("id.walt.mdoc-credentials:waltid-mdoc-credentials:${Versions.WALTID_VERSION}")
    implementation("id.walt:waltid-service-commons:${Versions.WALTID_VERSION}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains:annotations:24.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}
