package vc

import id.walt.credentials.CredentialBuilder
import id.walt.credentials.CredentialBuilderType
import id.walt.credentials.PresentationBuilder
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.days

suspend fun main() {
    create_sign_presentation()
}

suspend fun create_sign_presentation() {
    DidService.minimalInit()

    val myIssuerKey = LocalKey.generate(KeyType.Ed25519)
    val myIssuerDid = DidService.registerByKey("key", myIssuerKey).did

    val mySubjectKey = LocalKey.generate(KeyType.Ed25519)
    val mySubjectDid = DidService.registerByKey("key", mySubjectKey).did

    // Creating The Verifiable Credential
    println("Creating The Verifiable Credential")
    val vc = CredentialBuilder(CredentialBuilderType.W3CV11CredentialBuilder).apply {
        addContext("https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json")

        addType("OpenBadgeCredential")
        credentialId = "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439"

        issuerDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"

        validFromNow()
        validFor(2.days)

        subjectDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"

        useData("name", JsonPrimitive("JFF x vc-edu PlugFest 3 Interoperability"))

        useCredentialSubject(
            mapOf(
                "type" to listOf("AchievementSubject"),
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

    // Signing The Verifiable Credential
    println("Signing The Verifiable Credential")
    val jwtVc: String = vc.signJws(myIssuerKey, myIssuerDid, mySubjectDid)

    // Creating Verifiable Presentation
    println("Creating Verifiable Presentation")
    val vp = PresentationBuilder().apply {
        did = mySubjectDid
        nonce = "20394029340"
        addCredential(JsonPrimitive(jwtVc))
    }
    println("Verifiable Presentation: " + vp.presentationId)

    // Sign Verifiable Presentation as JWT
    println("Sign Verifiable Presentation as JWT")
    val vpSigned = vp.buildAndSign(mySubjectKey)

    println(vpSigned)
}
