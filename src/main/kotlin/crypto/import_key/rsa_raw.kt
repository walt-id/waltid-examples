package crypto.import_key

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.LocalKeyMetadata

suspend fun main() {
    println("Importing private JWK...")
    val key = LocalKey.generate(KeyType.RSA, LocalKeyMetadata())
    val privateKeyJsonString = key.jwk
    val privateKeyResult = LocalKey.importRawPublicKey(KeyType.RSA, privateKeyJsonString as ByteArray, LocalKeyMetadata())

    println("Success: "+privateKeyResult)
    println("Getting private key...")



//    println("Private key result: "+privateKeyResult.getOrThrow().jwk)

//
//    println("Importing public JWK...")
//    val key1 = LocalKey.generate(KeyType.RSA, LocalKeyMetadata())
//    val publicKeyJsonString = key1.getPublicKey().jwk.toString()
//    println(publicKeyJsonString)
//    val publicKeyResult = LocalKey.importJWK(publicKeyJsonString)
//    println("Success: "+publicKeyResult.isSuccess)
//    println("Getting public key...")
//    println("Public key result: "+publicKeyResult.getOrThrow().getPublicKey().jwk)

}