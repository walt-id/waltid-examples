package crypto.key.encode.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.encodeToBase58String

suspend fun main() {
    exportSecp256k1RawPublicKey()
}

suspend fun exportSecp256k1RawPublicKey() {
    val keyType = KeyType.secp256k1
    println("Generating key of type: $keyType")
    val privateKey = JWKKey.generate(keyType)

    println("Exporting Raw-encoded $keyType public key as Base58-encoded string...")
    val rawPublicKey= privateKey.getPublicKeyRepresentation()
    println("$keyType Raw-encoded $keyType public key as Base58-encoded string: ${rawPublicKey.encodeToBase58String()}\n")
}
