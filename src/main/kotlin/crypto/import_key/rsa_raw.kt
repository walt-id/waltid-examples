package crypto.import_key

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.LocalKeyMetadata

suspend fun main() {
    import_rsa_raw()
}

suspend fun import_rsa_raw() {
    println("Importing private JWK...")
    val key = LocalKey.generate(KeyType.RSA, LocalKeyMetadata())
    val privateKeyJsonString = key.jwk
    val privateKeyResult =
        LocalKey.importRawPublicKey(KeyType.RSA, privateKeyJsonString as ByteArray, LocalKeyMetadata())

    println("Success: " + privateKeyResult)
    println("Getting private key...")

}