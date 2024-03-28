package crypto.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.LocalKeyMetadata

suspend fun main() {
    create_secp256k1()
}
suspend fun create_secp256k1(){
    val key = LocalKey.generate(KeyType.secp256k1, LocalKeyMetadata())
    println(key.jwk)
}