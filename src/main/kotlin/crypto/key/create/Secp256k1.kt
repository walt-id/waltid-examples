package crypto.key.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    createSecp256k1()
}

suspend fun createSecp256k1() {
    val key = JWKKey.generate(KeyType.secp256k1)
    println(key.jwk)
}
