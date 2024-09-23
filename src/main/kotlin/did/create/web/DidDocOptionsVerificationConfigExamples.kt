package did.create.web

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.did.dids.DidService
import id.walt.did.dids.document.models.service.RegisteredServiceType
import id.walt.did.dids.document.models.service.ServiceEndpointObject
import id.walt.did.dids.document.models.service.ServiceEndpointURL
import id.walt.did.dids.document.models.verification.relationship.VerificationRelationshipType
import id.walt.did.dids.registrar.dids.DidDocConfig
import id.walt.did.dids.registrar.dids.DidWebCreateOptions
import id.walt.did.dids.registrar.dids.ServiceConfiguration
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

internal suspend fun createDidWebDidDocOptionsFromPublicKeySetVerificationConfigurationExamples() {
    println("Register did:web by assigning public keys to authentication and key agreement verification relationships")
    val authenticationPublicKeySet = setOf(
        JWKKey.generate(KeyType.Ed25519).getPublicKey(),
        JWKKey.generate(KeyType.secp256k1).getPublicKey(),
        JWKKey.generate(KeyType.secp256r1).getPublicKey(),
    )
    val keyAgreementPublicKeySet = setOf(
        JWKKey.generate(KeyType.RSA).getPublicKey(),
        JWKKey.generate(KeyType.secp256k1).getPublicKey(),
    )
    var didDocConfig = DidDocConfig.buildFromPublicKeySetVerificationConfiguration(
        verificationKeySetConfiguration = mapOf(
            VerificationRelationshipType.Authentication to authenticationPublicKeySet,
            VerificationRelationshipType.KeyAgreement to keyAgreementPublicKeySet,
        )
    )
    var webCreateOptions = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
        didDocConfig = didDocConfig,
    )
    var didResult = DidService.register(webCreateOptions)
    println("DID: ${didResult.did}")
    println("DID Document: ${didResult.didDocument.toJsonObject()}")
    //add services and capability invocation
    println("Register did:web by assigning public keys to authentication, key agreement, capability invocation verification relationships and also add service endpoints")
    val capabilityInvocationPublicKeySet = setOf(
        JWKKey.generate(KeyType.secp256r1).getPublicKey(),
        JWKKey.generate(KeyType.secp256k1).getPublicKey(),
    )
    val didCommServiceConfig = ServiceConfiguration(
        type = RegisteredServiceType.DIDCommMessaging.toString(),
        serviceEndpoint = setOf(
            ServiceEndpointObject(
                buildJsonObject {
                    put("uri", "http://example.com/path".toJsonElement())
                    put("accept", buildJsonArray {
                        add("didcomm/v2".toJsonElement())
                        add("didcomm/aip2;env=rfc587".toJsonElement())
                    })
                    put("routingKeys", buildJsonArray {
                        add("did:example:somemediator#somekey".toJsonElement())
                    })
                }
            )
        )
    )
    val credentialRegistryServiceConfig = ServiceConfiguration(
        type = RegisteredServiceType.CredentialRegistry.toString(),
        serviceEndpoint = setOf(
            ServiceEndpointURL("https://ssi.eecc.de/api/registry/vcs/some-credential-subject-id")
        )
    )
    didDocConfig = DidDocConfig.buildFromPublicKeySetVerificationConfiguration(
        verificationKeySetConfiguration = mapOf(
            VerificationRelationshipType.Authentication to authenticationPublicKeySet,
            VerificationRelationshipType.KeyAgreement to keyAgreementPublicKeySet,
            VerificationRelationshipType.CapabilityInvocation to capabilityInvocationPublicKeySet,
        ),
        serviceConfigurationSet = setOf(
            didCommServiceConfig,
            credentialRegistryServiceConfig,
        )
    )
    webCreateOptions = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
        didDocConfig = didDocConfig,
    )
    didResult = DidService.register(webCreateOptions)
    println("DID: ${didResult.did}")
    println("DID Document: ${didResult.didDocument.toJsonObject()}")
}