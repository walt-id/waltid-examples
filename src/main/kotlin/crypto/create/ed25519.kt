package crypto.create

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.LocalKeyMetadata

suspend fun main() {
    create_ed25519()
}
suspend fun create_ed25519(){
    val key = LocalKey.generate(KeyType.Ed25519, LocalKeyMetadata())
    println(key.jwk)
}