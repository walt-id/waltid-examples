package crypto.import_key

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    import_rsa_raw()
}

suspend fun import_rsa_raw() {
    println("Importing private JWK...")
    val key = JWKKey.generate(KeyType.RSA)
    val privateKeyJsonString = key.jwk
    val privateKeyResult =
        JWKKey.importRawPublicKey(KeyType.RSA, privateKeyJsonString as ByteArray, null)

    println("Success: " + privateKeyResult)
    println("Getting private key...")

}
