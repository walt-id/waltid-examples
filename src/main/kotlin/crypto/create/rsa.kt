package crypto.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.LocalKeyMetadata

suspend fun main() {
    create_rsa()
}

suspend fun create_rsa() {
    val key = LocalKey.generate(KeyType.RSA, LocalKeyMetadata())
    println(key.jwk)
}