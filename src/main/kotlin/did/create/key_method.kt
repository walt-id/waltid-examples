package did.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions

suspend fun main() {
    key_method()
}

suspend fun key_method() {
    DidService.minimalInit()
    val options = DidKeyCreateOptions(
        keyType = KeyType.RSA
    )
    val didResult = DidService.register(options)
    println("Did result: $didResult\n")


    println("Register the DID with a given key:")
    println("Generating key...")
    val key = LocalKey.generate(KeyType.secp256k1)
    println("Generated key: " + key.getKeyId())
    val optionsKey = DidKeyCreateOptions(
        useJwkJcsPub = true
    )
    val didResultKey = DidService.registerByKey(
        method = "key",
        key = key,
        options = optionsKey
    )
    println("DID result: $didResultKey")

}