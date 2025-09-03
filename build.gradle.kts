plugins {
    val kotlinVersion = "2.1.20"
    kotlin("jvm") version kotlinVersion
    application
}

group = "identity"
version = "0.0.1"

object Versions {
    const val WALTID_VERSION = "0.15.0"
}

application {
    val main = (project.findProperty("mainClass") as String?) ?: "RunAllKt"
    mainClass.set(main)
}

tasks.named<JavaExec>("run").configure {
    mainClass.set((project.findProperty("mainClass") as String?) ?: "RunAllKt")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.waltid.dev/releases")
    maven("https://maven.waltid.dev/snapshots")
}

dependencies {

    // walt.id
    // required dependencies for running the example project
    implementation("id.walt.crypto:waltid-crypto:${Versions.WALTID_VERSION}")
    implementation("id.walt.credentials:waltid-digital-credentials:${Versions.WALTID_VERSION}")
    implementation("id.walt.did:waltid-did:${Versions.WALTID_VERSION}")
    implementation("id.walt.sdjwt:waltid-sdjwt:${Versions.WALTID_VERSION}")
    implementation("id.walt.openid4vc:waltid-openid4vc:${Versions.WALTID_VERSION}")
    implementation("id.walt.policies:waltid-verification-policies:${Versions.WALTID_VERSION}")
    implementation("id.walt.dif-definitions-parser:waltid-dif-definitions-parser:${Versions.WALTID_VERSION}")

    // all walt.id dependencies (not required for this project)
    implementation("id.walt.mdoc-credentials:waltid-mdoc-credentials:${Versions.WALTID_VERSION}")
    implementation("id.walt:waltid-service-commons:${Versions.WALTID_VERSION}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains:annotations:26.0.1")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

// Configure run task to allow dynamic main class selection
tasks.named<JavaExec>("run") {
    if (project.hasProperty("mainClass")) {
        mainClass.set(project.property("mainClass").toString())
    }
}

// Set default main class for application plugin
application {
    mainClass.set("RunAllKt")
}
