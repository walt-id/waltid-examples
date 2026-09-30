plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "identity"
version = "0.0.1"

kotlin {
    jvmToolchain(21)
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
    implementation(libs.bundles.waltid)
    implementation(libs.waltid.trust.registry)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime.compat) {
        version { strictly(libs.versions.kotlinx.datetime.compat.get()) }
    }
    // all walt.id dependencies (not required for this project)
    implementation(libs.bundles.waltidNotNeeded)

    // crypto2 (new library)
    implementation(libs.waltid.crypto2)
    implementation(libs.waltid.crypto2.java)
}

// Configure run task to allow dynamic main class selection
tasks.named<JavaExec>("run") {
    if (project.hasProperty("mainClass")) {
        mainClass.set(project.property("mainClass").toString())
    }
}

tasks.register<JavaExec>("runTrustListFormats") {
    group = "application"
    description = "Load and verify every trust-list format supported by waltid-trust-registry"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("trustregistry.TrustListFormatsKt")
}

tasks.register<JavaExec>("validateTrustListUrls") {
    group = "verification"
    description = "Validate all public trust-list URLs advertised by the Enterprise API"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("trustregistry.TrustListUrlsKt")
}

// Set default main class for application plugin
application {
    mainClass.set("RunAllKt")
}
