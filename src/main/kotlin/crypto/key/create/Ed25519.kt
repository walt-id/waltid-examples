package crypto.key.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    createEd25519()
}

suspend fun createEd25519() {
    val key = JWKKey.generate(KeyType.Ed25519)
    println(key.jwk)
}
