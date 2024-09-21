package crypto.key.import.jwk


import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    importSecp256k1Jwk()
}

suspend fun importSecp256k1Jwk() {
    val secp256k1PrivateKey = JWKKey.generate(KeyType.secp256k1)

    println("Importing private Secp256k1 JWK...")
    printImportedJwkStringInfo(secp256k1PrivateKey.jwk.toString())

    println("Importing public Secp256k1 JWK...")
    printImportedJwkStringInfo(secp256k1PrivateKey.getPublicKey().jwk.toString())
}
