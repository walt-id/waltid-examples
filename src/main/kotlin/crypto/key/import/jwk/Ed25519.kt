package crypto.key.import.jwk


import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    importEd25519Jwk()
}

suspend fun importEd25519Jwk() {
    val ed25519PrivateKey = JWKKey.generate(KeyType.Ed25519)

    println("Importing private Ed25519 JWK...")
    printImportedJwkStringInfo(ed25519PrivateKey.jwk.toString())

    println("Importing public Ed25519 JWK...")
    printImportedJwkStringInfo(ed25519PrivateKey.getPublicKey().jwk.toString())
}
