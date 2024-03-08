package crypto.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.LocalKeyMetadata

suspend fun main() {
    val key = LocalKey.generate(KeyType.RSA, LocalKeyMetadata())
    println(key.jwk)
}