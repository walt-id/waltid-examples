package did.register

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidCreateOptions
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions
import id.walt.did.dids.registrar.dids.DidWebCreateOptions

suspend fun main() {
    DidService.minimalInit()

    val options = DidKeyCreateOptions(
        keyType = KeyType.RSA
    )
    val didResult = DidService.register(options)

    

    println("RSA key did result: "+didResult+"\n")




    println("Generating key...")
    val key = LocalKey.generate(KeyType.Ed25519)
    println("Generated key: "+key.getKeyId())
    val optionsKey = DidKeyCreateOptions(
        useJwkJcsPub = true
    )
    val didResultKey = DidService.registerByKey(
        method = "key",
        key = key,
        options = optionsKey
    )
    println("Did result: "+didResultKey)

}