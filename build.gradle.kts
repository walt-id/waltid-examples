plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

group = "identity"
version = "0.0.1"


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

    // all walt.id dependencies (not required for this project)
    implementation(libs.bundles.waltidNotNeeded)
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
