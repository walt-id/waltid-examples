package vc

import id.walt.credentials.CredentialBuilder
import id.walt.credentials.CredentialBuilderType
import id.walt.crypto.utils.JsonUtils.toJsonObject
import kotlin.time.Duration.Companion.days

fun main() {
    build()
}

fun build() {
    val entityIdentificationNumber = "12345"
    val issuingAuthorityId = "abc"
    val proofType = "document"
    val proofLocation = "Berlin-Brandenburg"

    val credentialBuilder = CredentialBuilderType.W3CV2CredentialBuilder
    val credentialSubject = mapOf(
        "entityIdentification" to entityIdentificationNumber,
        "issuingAuthority" to issuingAuthorityId,
        "issuingCircumstances" to mapOf(
            "proofType" to proofType,
            "locationType" to "physicalLocation",
            "location" to proofLocation
        )
    ).toJsonObject()

    val w3cCredential = CredentialBuilder(credentialBuilder).apply {
        addContext("https://www.w3.org/ns/credentials/examples/v2") // [W3CV2 VC context, custom context]
        addType("MyCustomCredential") // [VerifiableCredential, MyCustomCredential]
        randomCredentialSubjectUUID() // automatically generate
        issuerDid = "did:key:abc"     // possibly later overridden by data mapping during issuance
        subjectDid = "did:key:xyz"    // possibly later overridden by data mapping during issuance
        validFromNow()                // set validFrom per current time
        validFor(90.days)
        useStatusList2021Revocation("https://university.example/credentials/status/3", 94567)

        useCredentialSubject(credentialSubject)
    }.buildW3C()

    println(w3cCredential.toPrettyJson())
}

