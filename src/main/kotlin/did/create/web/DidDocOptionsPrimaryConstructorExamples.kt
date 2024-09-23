package did.create.web

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.did.dids.DidService
import id.walt.did.dids.document.models.verification.relationship.VerificationRelationshipType
import id.walt.did.dids.registrar.dids.DidDocConfig
import id.walt.did.dids.registrar.dids.DidWebCreateOptions
import id.walt.did.dids.registrar.dids.VerificationMethodConfiguration
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

internal suspend fun createDidWebDidDocOptionsPrimaryConstructorExamples() {
    println("Register did:web by assigning public keys to all verification relationships and also add custom properties to each verification method produced")
    val pubKey = JWKKey.generate(KeyType.RSA).getPublicKey()
    val customVerificationMethodProperties = mapOf(
        "custom-property1" to "custom-property1-value".toJsonElement(),
        "custom-property2" to buildJsonObject {
            put("nested-key1", "nested-value1".toJsonElement())
        }.toJsonElement(),
        "custom-property3" to buildJsonArray {
            add("array-value1".toJsonElement())
            add("array-value2".toJsonElement())
        }
    )
    val verificationConfigurationMap = VerificationRelationshipType.entries.associateWith {
        setOf(
            VerificationMethodConfiguration(
                publicKeyId = pubKey.getKeyId(),
                customProperties = customVerificationMethodProperties,
            )
        )
    }
    val didDocConfig = DidDocConfig(
        publicKeyMap = mapOf(pubKey.getKeyId() to pubKey),
        verificationConfigurationMap = verificationConfigurationMap,
    )
    val webCreateOptions = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
        didDocConfig = didDocConfig,
    )
    val didResult = DidService.register(webCreateOptions)
    println("DID: ${didResult.did}")
    println("DID Document: ${didResult.didDocument.toJsonObject()}")
}

suspend fun main() {
    DidService.minimalInit()
    createDidWebDidDocOptionsPrimaryConstructorExamples()
}