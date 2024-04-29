package crypto.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    create_rsa()
}

suspend fun create_rsa() {
    val key = JWKKey.generate(KeyType.RSA)
    println(key.jwk)
}
