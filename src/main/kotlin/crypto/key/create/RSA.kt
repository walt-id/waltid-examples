package crypto.key.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    createRSA()
}

suspend fun createRSA() {
    val key = JWKKey.generate(KeyType.RSA)
    println(key.jwk)
}
