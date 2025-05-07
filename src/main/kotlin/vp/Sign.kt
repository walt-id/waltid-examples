package vp

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.w3c.CredentialBuilder
import id.walt.w3c.CredentialBuilderType
import id.walt.w3c.PresentationBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.days

suspend fun main() {
    signVP()
}

suspend fun signVP() {
    DidService.minimalInit()

    println("Sign VP:")
    //Create issuer did
    val issuerPrivateKey = JWKKey.generate(KeyType.Ed25519)
    val issuerDid = DidService.registerByKey("key", issuerPrivateKey).did
    println("Generated issuer DID: $issuerDid")

    //Create holder did
    val holderPrivateKey = JWKKey.generate(KeyType.Ed25519)
    val holderDid = DidService.registerByKey("key", holderPrivateKey).did
    println("Generated holder DID: $holderDid")

    println("Creating The Verifiable Credential")
    val vc = CredentialBuilder(CredentialBuilderType.W3CV11CredentialBuilder).apply {
        addContext("https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json")
        addType("OpenBadgeCredential")
        credentialId = "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439"
        this.issuerDid = issuerDid
        validFromNow()
        validFor(2.days)
        useData("name", JsonPrimitive("JFF x vc-edu PlugFest 3 Interoperability"))
        useCredentialSubject(
            mapOf(
                "type" to listOf("AchievementSubject"),
                "id" to holderDid,
                "achievement" to mapOf(
                    "id" to "urn:uuid:ac254bd5-8fad-4bb1-9d29-efd938536926",
                    "type" to listOf("Achievement"),
                    "name" to "JFF x vc-edu PlugFest 3 Interoperability",
                    "description" to "This wallet supports the use of W3C Verifiable Credentials and has demonstrated interoperability during the presentation request workflow during JFF x VC-EDU PlugFest 3.",
                    "criteria" to mapOf(
                        "type" to "Criteria",
                        "narrative" to "Wallet solutions providers earned this badge by demonstrating interoperability during the presentation request workflow. This includes successfully receiving a presentation request, allowing the holder to select at least two types of verifiable credentials to create a verifiable presentation, returning the presentation to the requestor, and passing verification of the presentation and the included credentials."
                    ),
                    "image" to mapOf(
                        "id" to "https://w3c-ccg.github.io/vc-ed/plugfest-3-2023/images/JFF-VC-EDU-PLUGFEST3-badge-image.png",
                        "type" to "Image"
                    )
                )
            ).toJsonObject()
        )
    }.buildW3C()
    println("Unsigned VC:\n${vc.toPrettyJson()}")

    //JWT VC signature
    println("\nUsing JWT as signature type...")
    val signedJwtVc = vc.signJws(
        issuerKey = issuerPrivateKey,
        issuerId = issuerDid,
        issuerKid = issuerPrivateKey.getKeyId(),
        subjectDid = holderDid,
    )
    println("Signed JWT VC: $signedJwtVc\n")

    val verifierPrivateKey = JWKKey.generate(KeyType.Ed25519)
    val verifierDid = DidService.registerByKey("key", verifierPrivateKey).did
    println("Generated verifier DID: $verifierDid")

    // Creating Verifiable Presentation
    println("Creating Verifiable Presentation...")
    val vp = PresentationBuilder().apply {
        did = holderDid
        nonce = "20394029340"
        audience = verifierDid
        addCredential(signedJwtVc.toJsonElement())
    }
    // Sign Verifiable Presentation as JWT
    val vpSigned = vp.buildAndSign(holderPrivateKey)
    println("Signed VP: $vpSigned")
}
