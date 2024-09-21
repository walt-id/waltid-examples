package crypto.key.import.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    importRSARawPublicKey()
}

suspend fun importRSARawPublicKey() {
    val rsaPrivateKey = JWKKey.generate(KeyType.RSA)

    println("Importing Raw-encoded RSA public key...")
    printImportedRawPublicKeyInfo(rsaPrivateKey.getPublicKeyRepresentation(), KeyType.RSA)
}
