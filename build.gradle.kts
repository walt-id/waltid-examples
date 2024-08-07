val kotlin_version: String by project
val waltid_version: String = "1.0.2408070002-SNAPSHOT"


plugins {
    kotlin("jvm") version "2.0.20-RC"
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

}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    implementation("id.walt.crypto:waltid-crypto:$waltid_version")
    implementation("id.walt.credentials:waltid-verifiable-credentials:$waltid_version")
    implementation("id.walt.did:waltid-did:$waltid_version")
    implementation("id.walt.sdjwt:waltid-sdjwt:$waltid_version")
    implementation("id.walt.openid4vc:waltid-openid4vc:$waltid_version")

    implementation("org.jetbrains:annotations:24.1.0")

    implementation("org.slf4j:slf4j-simple:2.0.13")
}
