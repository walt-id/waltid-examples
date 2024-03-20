package vc

import id.walt.credentials.CredentialBuilder
import id.walt.credentials.CredentialBuilderType
import id.walt.credentials.issuance.Issuer.baseIssue
import id.walt.credentials.verification.Verifier
import id.walt.credentials.verification.models.PolicyRequest.Companion.parsePolicyRequests
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.time.Duration.Companion.days


suspend fun main (){

    DidService.init()

    val entityIdentificationNumber = "12345"
    val issuingAuthorityId = "abc"
    val proofType = "document"
    val proofLocation = "Berlin-Brandenburg"


//  Create issuer key and did
    println("Register the DID with a given key:")
    println("Generating key...")
    val issuerKey = LocalKey.generate(KeyType.Ed25519)
    println("Generated key: "+issuerKey.getKeyId())
    val optionsKey = DidKeyCreateOptions(
        useJwkJcsPub = true
    )
    val issDid = DidService.registerByKey(
        method = "key",
        key = issuerKey,
        options = optionsKey
    )


//  Create holder did
    val options = DidKeyCreateOptions(
        keyType = KeyType.RSA
    )
    val holderDid = DidService.register(options)


//  Custom data
    val myCustomData = mapOf(
        "entityIdentification" to entityIdentificationNumber,
        "issuingAuthority" to issuingAuthorityId,
        "issuingCircumstances" to mapOf(
            "proofType" to proofType,
            "locationType" to "physicalLocation",
            "location" to proofLocation
        )
    ).toJsonObject()


    val jwt = CredentialBuilder(CredentialBuilderType.W3CV2CredentialBuilder).apply {
        addContext("https://www.w3.org/ns/credentials/examples/v2") // [W3CV2 VC context, custom context]
        addType("MyCustomCredential")
        issuerDid = issDid.did
        subjectDid = holderDid.did
        randomCredentialSubjectUUID()
        validFromNow()
        validFor(90.days)
        useCredentialSubject(myCustomData)
    }.buildW3C().baseIssue(
        key = issuerKey,
        did = issDid.did,
        subject = holderDid.did,
        dataOverwrites = mapOf(),
        dataUpdates = mapOf(),
        additionalJwtHeader = emptyMap(),
        additionalJwtOptions = emptyMap(),
    )

    val vpToken = jwt
    println(vpToken)

// configure the validation policies
    val vcPolicies = Json.parseToJsonElement(
        """
       [
          "signature",
          "expired",
          "not-before"
        ] 
    """
    ).jsonArray.parsePolicyRequests()
    val vpPolicies = Json.parseToJsonElement(
        """
        [
          "signature",
          "expired",
          "not-before"
        ]
    """
    ).jsonArray.parsePolicyRequests()
    val specificPolicies = Json.parseToJsonElement(
        """
       {
          "OpenBadgeCredential": [
              {
                "policy": "schema",
                "args": {
                    "type": "object",
                    "required": ["issuer"],
                    "properties": {
                        "issuer": {
                            "type": "object"
                        }
                    }
                }
            }
          ]
       } 
    """
    ).jsonObject.mapValues { it.value.jsonArray.parsePolicyRequests() }

    println("VP Policies: $vpPolicies")
    println("VC Policies: $vcPolicies")
    println("SP Policies: $specificPolicies")

    val r = Verifier.verifyPresentation(
        vpTokenJwt = vpToken, vpPolicies = vpPolicies, globalVcPolicies = vcPolicies, specificCredentialPolicies = specificPolicies, mapOf(
            "presentationSubmission" to JsonObject(emptyMap()), "challenge" to "abc"
        )
    )
}