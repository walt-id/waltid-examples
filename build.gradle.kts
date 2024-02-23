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
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
    testImplementation("io.kotest:kotest-assertions-json:5.7.2")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    // https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
    implementation("com.nimbusds:nimbus-jose-jwt:9.38-rc3")

    implementation("com.github.multiformats:java-multibase:v1.1.1")


    implementation("id.walt.crypto:waltid-crypto:$waltid_version")
    implementation("id.walt.credentials:waltid-verifiable-credentials:$waltid_version")
    implementation("id.walt.did:waltid-did:$waltid_version")
    implementation("id.walt:waltid-sdjwt-jvm:$waltid_version")
    implementation("id.walt:waltid-openid4vc:$waltid_version")


}
