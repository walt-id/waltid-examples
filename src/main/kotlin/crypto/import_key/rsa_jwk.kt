package crypto.import_key

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    import_rsa_jwk()
}

suspend fun import_rsa_jwk() {
    println("Importing private JWK...")
    val key = JWKKey.generate(KeyType.RSA)
    val privateKeyJsonString = key.jwk.toString()
    val privateKeyResult = JWKKey.importJWK(privateKeyJsonString)
    println("Success: " + privateKeyResult.isSuccess)
    println("Getting private key...")
    println("Private key result: " + privateKeyResult.getOrThrow().jwk)


    println("Importing public JWK...")
    val key1 = JWKKey.generate(KeyType.RSA)
    val publicKeyJsonString = key1.getPublicKey().jwk.toString()
    println(publicKeyJsonString)
    val publicKeyResult = JWKKey.importJWK(publicKeyJsonString)
    println("Success: " + publicKeyResult.isSuccess)
    println("Getting public key...")
    println("Public key result: " + publicKeyResult.getOrThrow().getPublicKey().jwk)

}
