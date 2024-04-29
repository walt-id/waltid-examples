package crypto.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    create_ed25519()
}

suspend fun create_ed25519() {
    val key = JWKKey.generate(KeyType.Ed25519)
    println(key.jwk)
}
