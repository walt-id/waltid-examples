package crypto.key.import.jwk


import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    importSecp256r1Jwk()
}

suspend fun importSecp256r1Jwk() {
    val secp256r1PrivateKey = JWKKey.generate(KeyType.secp256r1)

    println("Importing private Secp256r1 JWK...")
    printImportedJwkStringInfo(secp256r1PrivateKey.jwk.toString())

    println("Importing public Secp256r1 JWK...")
    printImportedJwkStringInfo(secp256r1PrivateKey.getPublicKey().jwk.toString())
}
