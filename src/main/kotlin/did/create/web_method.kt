package did.register

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions
import id.walt.did.dids.registrar.dids.DidWebCreateOptions

suspend fun main() {
    DidService.minimalInit()
    val options = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path= "/wallet-api/registry/1111",
        keyType = KeyType.Ed25519
    )
    val didResult = DidService.register(options = options)
    println("Did result: "+didResult+"\n")



    println("Generating key...")
    val key = LocalKey.generate(KeyType.Ed25519)
    println("Generated key: "+key.getKeyId())
    val optionsKey = DidWebCreateOptions(
        domain = "wallet.walt-test.cloud",
        path= "/wallet-api/registry/1111",
        keyType = KeyType.Ed25519
    )
    val didResultKey = DidService.registerByKey(
        method = "key",
        key = key,
        options = optionsKey
    )
    println("Did result: "+didResultKey)


}