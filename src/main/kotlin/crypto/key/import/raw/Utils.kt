package crypto.key.import.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

internal suspend fun printImportedRawPublicKeyInfo(
    publicKeyBytes: ByteArray,
    keyType: KeyType,
) {
    val key = JWKKey.importRawPublicKey(keyType, publicKeyBytes, null)
    println("Decoded ${key.keyType} public key: ${key.exportJWK()}")
}
