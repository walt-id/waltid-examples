val kotlin_version: String by project
val waltid_version: String = "1.0.2405211840-SNAPSHOT"


plugins {
    kotlin("jvm") version "1.9.24"
}

group = "identity"
version = "0.0.1"

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://maven.waltid.dev/snapshots") }

//    maven { url = uri("https://jitpack.io") }

}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("id.walt.crypto:waltid-crypto:$waltid_version")
    implementation("id.walt.credentials:waltid-verifiable-credentials:$waltid_version")
    implementation("id.walt.did:waltid-did:$waltid_version")
    implementation("id.walt:waltid-sdjwt:$waltid_version")
    implementation("id.walt:waltid-openid4vc:$waltid_version")

}
