package vc

import id.walt.credentials.vc.vcs.W3CVC
import id.walt.credentials.verification.Verifier
import id.walt.credentials.verification.models.PolicyRequest
import id.walt.credentials.verification.policies.JwtSignaturePolicy
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions
import id.walt.sdjwt.DecoyMode
import id.walt.sdjwt.SDField
import id.walt.sdjwt.SDMap

suspend fun main() {
    sign_vc()
}

suspend fun sign_vc() {

    DidService.init()

//  Create issuer key and did
    println("Register the DID with a given key:")
    println("Generating key...")
    val myIssuerKey = JWKKey.generate(KeyType.Ed25519)
    println("Generated key: " + myIssuerKey.getKeyId())
    val optionsKey = DidKeyCreateOptions(
        useJwkJcsPub = true
    )
    val myIssuerDid = DidService.registerByKey(
        method = "key",
        key = myIssuerKey,
        options = optionsKey
    )


//  Create holder did
    val options = DidKeyCreateOptions(
        keyType = KeyType.RSA
    )
    val mySubjectDid = DidService.register(options)

    val vc = W3CVC.build(
        context = listOf(
            "https://www.w3.org/2018/credentials/v1",
            "https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json"
        ),
        type = listOf("VerifiableCredential", "OpenBadgeCredential"),

        "id" to "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439",
        "name" to "JFF x vc-edu PlugFest 3 Interoperability",
        "issuer" to myIssuerDid.did,
        "issuanceDate" to "2023-08-02T08:03:13Z",
        "credentialSubject" to mapOf(
            "type" to listOf("AchievementSubject"),
            "id" to mySubjectDid.did,
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
    //JWT signature
    println("\nUsing JWT as signature type..")
    val signedJWT = vc.signJws(myIssuerKey, myIssuerDid.did, null, mySubjectDid.did)
    println("JWS: " + signedJWT + "\n")
    val results = Verifier.verifyCredential(
        signedJWT,
        listOf(
            PolicyRequest(JwtSignaturePolicy())
        )
    )
    results.forEach { verification ->
        println("[${verification.request.policy.name}] -> Success=${verification.isSuccess()}, Result=${verification.result}")
    }


    println("\nUsing SD-JWT as signature type..")
    // Prepare SD-JWT options for signing:
    val disclosureMap = SDMap(
        fields = mapOf("name" to SDField(true)),
        decoyMode = DecoyMode.RANDOM,
        decoys = 2
    )
    val signedSDJWT: String = vc.signSdJwt(myIssuerKey, myIssuerDid.did, mySubjectDid.did, disclosureMap)
    println("JWS: " + signedSDJWT + "\n")
    val results1 = Verifier.verifyCredential(
        signedSDJWT,
        listOf(
            PolicyRequest(JwtSignaturePolicy())
        )
    )
    results1.forEach { verification ->
        println("[${verification.request.policy.name}] -> Success=${verification.isSuccess()}, Result=${verification.result}")
    }

}
