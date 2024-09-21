package did.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.DidResult
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions

suspend fun main() {
    createDidJwk()
}


suspend fun createDidJwk() {
    DidService.minimalInit()
    var didResult: DidResult

    println("Register did:jwk")
    val options = DidJwkCreateOptions(
        keyType = KeyType.secp256k1
    )
    didResult = DidService.register(options)
    printDidResult(didResult)

    println("Register did:jwk by providing key")
    val key = JWKKey.generate(KeyType.RSA)
    println("Generated key: ${key.exportJWK()}")
    didResult = DidService.registerByKey(
        method = options.method,
        key = key,
        options = options,
    )
    printDidResult(didResult)
}
