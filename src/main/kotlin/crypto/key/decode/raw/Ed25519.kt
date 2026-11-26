package crypto.key.decode.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.decodeBase58

suspend fun main() {
    importEd25519RawPublicKey()
}

suspend fun importEd25519RawPublicKey() {
    val keyType = KeyType.Ed25519
    val rawPublicKeyBase58String =
        "Am4uYrLe9Ukk4S7b7gt7Y8KXe6KDBX8zvMhH1UXEindU"

    println("Importing Raw-encoded $keyType public key from Base58-encoded string: $rawPublicKeyBase58String")
    val publicKey = JWKKey.importRawPublicKey(
        type = keyType,
        rawPublicKey = rawPublicKeyBase58String.decodeBase58(),
    )
    println("Decoded $keyType public key as JWK Object : ${publicKey.exportJWKObject()}")
}
