package did.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.DidResult
import id.walt.did.dids.registrar.dids.DidCheqdCreateOptions

suspend fun main() {
    createDidCheqd()
}

suspend fun createDidCheqd() {
    DidService.minimalInit()
    var didResult: DidResult

    println("Register did:cheqd")
    val options = DidCheqdCreateOptions(
        network = "testnet"
    )
    didResult = DidService.register(options)
    printDidResult(didResult)

    println("Register did:cheqd by providing key")
    val key = JWKKey.generate(KeyType.Ed25519)
    println("Generated key: ${key.exportJWK()}")
    didResult = DidService.registerByKey(
        method = options.method,
        key = key,
        options = options,
    )
    printDidResult(didResult)
}