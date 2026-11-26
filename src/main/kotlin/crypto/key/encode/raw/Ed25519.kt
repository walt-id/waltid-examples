package crypto.key.encode.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.encodeToBase58String

suspend fun main() {
    exportEd25519RawPublicKey()
}

suspend fun exportEd25519RawPublicKey() {
    val keyType = KeyType.Ed25519
    println("Generating key of type: $keyType")
    val privateKey = JWKKey.generate(keyType)

    println("Exporting Raw-encoded $keyType public key as Base58-encoded string...")
    val rawPublicKey= privateKey.getPublicKeyRepresentation()
    println("$keyType Raw-encoded $keyType public key as Base58-encoded string: ${rawPublicKey.encodeToBase58String()}\n")
}
