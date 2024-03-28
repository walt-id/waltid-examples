package did.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions

suspend fun main() {
    jwk_method()
}
suspend fun jwk_method(){
    DidService.minimalInit()
    val options = DidJwkCreateOptions(
        keyType = KeyType.Ed25519
    )
    val didResult = DidService.register(options)
    println("Key did result: "+didResult+"\n")



    println("Register the DID with a given key:")
    println("Generating key...")
    val key = LocalKey.generate(KeyType.RSA)
    println("Generated key: "+key.getKeyId())
    val didResultKey = DidService.registerByKey(
        method = "jwk",
        key = key,
    )
    println("Did result: "+didResultKey)

}