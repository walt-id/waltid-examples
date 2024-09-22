package crypto.key.decode.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.decodeBase58

suspend fun main() {
    importSecp256k1RawPublicKey()
}

suspend fun importSecp256k1RawPublicKey() {
    val keyType = KeyType.secp256k1
    val rawPublicKeyBase58String =
        "23Bhk2tEJm6waWvv7bpgCrWaVd6LJgg5FVsYhHYxvXPp9"

    println("Importing Raw-encoded $keyType public key from Base58-encoded string: $rawPublicKeyBase58String")
    val publicKey = JWKKey.importRawPublicKey(
        type = keyType,
        rawPublicKey = rawPublicKeyBase58String.decodeBase58(),
    )
    println("Decoded $keyType public key as JWK Object : ${publicKey.exportJWKObject()}")
}
