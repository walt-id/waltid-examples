package vc.sdjwt

import id.walt.credentials.vc.vcs.W3CVC
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions
import id.walt.sdjwt.DecoyMode
import id.walt.sdjwt.SDField
import id.walt.sdjwt.SDMap

suspend fun main() {
    signSdJwtVc()
}

suspend fun signSdJwtVc() {
    DidService.minimalInit()

    println("Sign SD-JWT VC:")
    //Create issuer did
    val issuerPrivateKey = JWKKey.generate(KeyType.Ed25519)
    val issuerDidKeyOptions = DidKeyCreateOptions(
        useJwkJcsPub = true,
    )
    val issuerDidResult = DidService.registerByKey(
        method = issuerDidKeyOptions.method,
        key = issuerPrivateKey,
        options = issuerDidKeyOptions,
    )
    println("Generated issuer DID: ${issuerDidResult.did}")


    //Create holder did
    val holderDidResult = DidService.register(
        DidKeyCreateOptions(
            keyType = KeyType.RSA
        )
    )
    println("Generated holder DID: ${holderDidResult.did}")

    //build an OpenBadge verifiable credential
    val vc = W3CVC.build(
        context = listOf(
            "https://www.w3.org/2018/credentials/v1",
            "https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json"
        ),
        type = listOf("VerifiableCredential", "OpenBadgeCredential"),

        "id" to "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439",
        "name" to "JFF x vc-edu PlugFest 3 Interoperability",
        "issuer" to issuerDidResult.did,
        "issuanceDate" to "2023-08-02T08:03:13Z",
        "credentialSubject" to mapOf(
            "type" to listOf("AchievementSubject"),
            "id" to holderDidResult.did,
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
        )
    )
    println("Unsigned VC:\n${vc.toPrettyJson()}")

    //Prepare SD-JWT options for signing:
    val disclosureMap = SDMap(
        fields = mapOf("name" to SDField(true)),
        decoyMode = DecoyMode.RANDOM,
        decoys = 2
    )
    println("Disclosure map: $disclosureMap")

    //SD-JWT VC signature
    println("\nUsing SD-JWT as signature type...")
    val signedSdJwtVc = vc.signSdJwt(
        issuerPrivateKey,
        issuerDidResult.did,
        holderDidResult.did,
        disclosureMap,
        )
    println("Signed SD-JWT VC: $signedSdJwtVc\n")
}