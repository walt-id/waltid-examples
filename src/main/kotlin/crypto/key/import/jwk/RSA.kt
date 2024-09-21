package crypto.key.import.jwk

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    importRSAJwk()
}

suspend fun importRSAJwk() {
    val rsaPrivateKey = JWKKey.generate(KeyType.RSA)

    println("Importing private RSA JWK...")
    printImportedJwkStringInfo(rsaPrivateKey.jwk.toString())

    println("Importing public RSA JWK...")
    printImportedJwkStringInfo(rsaPrivateKey.getPublicKey().jwk.toString())
}
