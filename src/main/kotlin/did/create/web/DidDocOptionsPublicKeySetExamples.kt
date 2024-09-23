package did.create.web

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.JsonUtils.toJsonElement
import id.walt.did.dids.DidService
import id.walt.did.dids.document.models.service.RegisteredServiceType
import id.walt.did.dids.document.models.service.ServiceEndpointObject
import id.walt.did.dids.document.models.service.ServiceEndpointURL
import id.walt.did.dids.registrar.dids.DidDocConfig
import id.walt.did.dids.registrar.dids.DidWebCreateOptions
import id.walt.did.dids.registrar.dids.ServiceConfiguration
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

internal suspend fun createDidWebDidDocOptionsFromPublicKeySetExamples() {
    //create from public key set
    println("Register did:web with public key set")
    val publicKeySet = setOf(
        JWKKey.generate(KeyType.RSA).getPublicKey(),
        JWKKey.generate(KeyType.secp256k1).getPublicKey(),
        JWKKey.generate(KeyType.secp256r1).getPublicKey(),
    )
    var didDocConfig = DidDocConfig.buildFromPublicKeySet(
        publicKeySet = publicKeySet,
    )
    var webCreateOptions = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
        didDocConfig = didDocConfig,
    )
    var didResult = DidService.register(webCreateOptions)
    println("DID: ${didResult.did}")
    println("DID Document: ${didResult.didDocument.toJsonObject()}")

    //create from public key set but also add service endpoints
    println("Register did:web with public key set and service endpoints")
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
    didDocConfig = DidDocConfig.buildFromPublicKeySet(
        publicKeySet = publicKeySet,
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

    //create from public key set, add service endpoints and custom properties
    //in the root portion of the did document
    println("Register did:web with public key set, service endpoints and add custom properties to root portion of did document")
    val rootDocCustomProperties = mapOf(
        "custom-property1" to "custom-property1-value".toJsonElement(),
        "custom-property2" to buildJsonObject {
            put("nested-key1", "nested-value1".toJsonElement())
        }.toJsonElement(),
        "custom-property3" to buildJsonArray {
            add("array-value1".toJsonElement())
            add("array-value2".toJsonElement())
        }
    )
    didDocConfig = DidDocConfig.buildFromPublicKeySet(
        publicKeySet = publicKeySet,
        serviceConfigurationSet = setOf(
            didCommServiceConfig,
            credentialRegistryServiceConfig,
        ),
        rootCustomProperties = rootDocCustomProperties,
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

suspend fun main() {
    DidService.minimalInit()
    createDidWebDidDocOptionsFromPublicKeySetExamples()
}