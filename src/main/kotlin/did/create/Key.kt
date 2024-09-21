package did.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.DidResult
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions

suspend fun main() {
    createDidKey()
}

suspend fun createDidKey() {
    DidService.minimalInit()
    var didResult: DidResult

    println("Register did:key")
    val options = DidKeyCreateOptions(
        keyType = KeyType.RSA,
        useJwkJcsPub = true,
    )
    didResult = DidService.register(options)
    printDidResult(didResult)

    println("Register did:key by providing key")
    val key = JWKKey.generate(KeyType.secp256r1)
    println("Generated key: ${key.exportJWK()}")
    didResult = DidService.registerByKey(
        method = options.method,
        key = key,
        options = options,
    )
    printDidResult(didResult)
}
