package did.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidWebCreateOptions

suspend fun main() {
    web_method()
}

suspend fun web_method() {
    DidService.minimalInit()
    val options = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
        keyType = KeyType.Ed25519
    )
    val didResult = DidService.register(options = options)
    println("Did result: $didResult\n")


    println("Register the DID with a given key:")
    println("Generating key...")
    val key = JWKKey.generate(KeyType.RSA)
    println("Generated key: " + key.getKeyId())
    val optionsKey = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path = "/wallet-api/registry/1111",
    )
    val didResultKey = DidService.registerByKey(
        method = "web",
        key = key,
        options = optionsKey
    )
    println("DID result: $didResultKey")

}
