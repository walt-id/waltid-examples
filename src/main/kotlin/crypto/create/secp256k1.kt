package crypto.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    create_secp256k1()
}

suspend fun create_secp256k1() {
    val key = JWKKey.generate(KeyType.secp256k1)
    println(key.jwk)
}
