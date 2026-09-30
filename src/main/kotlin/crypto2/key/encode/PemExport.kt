package crypto2.key.encode

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.keys.EcCurve
import id.walt.crypto2.keys.EncodedKey
import id.walt.crypto2.keys.KeyEncodingFormat
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.keys.encodePem
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider

suspend fun main() {
    exportToPem()
}

suspend fun exportToPem() {
    println("=== PEM Export Demo ===\n")

    val runtime = CryptoRuntime(
        softwareProviders = listOf(CryptographySoftwareKeyProvider())
    )

    // Generate secp256r1 key
    val key = runtime.generateSoftwareKey(
        request = GenerateSoftwareKeyRequest(
            id = KeyId("pem-export-key"),
            spec = KeySpec.Ec(EcCurve.P256),
            usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
        )
    )

    // Export public key to SPKI PEM
    println("1. Exporting public key to SPKI PEM...")
    val publicKeyExporter = requireNotNull(key.capabilities.publicKeyExporter) {
        "Key does not support public key export"
    }
    val publicKey = publicKeyExporter.exportPublicKey(KeyEncodingFormat.SPKI_DER) as EncodedKey.SpkiDer
    val publicPem = publicKey.encodePem()
    println(publicPem)
    println()

    // Export private key to PKCS8 PEM
    println("2. Exporting private key to PKCS8 PEM...")
    val privateKeyExporter = requireNotNull(key.capabilities.privateKeyExporter) {
        "Key does not support private key export"
    }
    val privateKey = privateKeyExporter.exportPrivateKey(KeyEncodingFormat.PKCS8_DER) as EncodedKey.Pkcs8Der
    val privatePem = privateKey.encodePem()
    println(privatePem)

    runtime.close()
}
