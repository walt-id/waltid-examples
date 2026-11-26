package did.create.web

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidWebCreateOptions

internal suspend fun simpleRegister() {
    println("Register did:web")
    val options = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
        keyType = KeyType.Ed25519,
    )
    val didResult = DidService.register(options)
    println("DID: ${didResult.did}")
    println("DID Document: ${didResult.didDocument.toJsonObject()}")
}

internal suspend fun simpleRegisterByKey() {
    println("Register did:web by providing key")
    val key = JWKKey.generate(KeyType.Ed25519)
    println("Generated key: ${key.exportJWK()}")
    val options = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
    )
    val didResult = DidService.registerByKey(
        method = options.method,
        key = key,
        options = options,
    )
    println("DID: ${didResult.did}")
    println("DID Document: ${didResult.didDocument.toJsonObject()}")
}