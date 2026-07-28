package crypto2.key.create

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.keys.EdwardsCurve
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider
import kotlinx.serialization.json.Json

suspend fun main() {
    createEd25519()
}

suspend fun createEd25519() {
    // Initialize CryptoRuntime with software provider
    val runtime = CryptoRuntime(
        softwareProviders = listOf(CryptographySoftwareKeyProvider())
    )

    // Generate Ed25519 key
    val key = runtime.generateSoftwareKey(
        request = GenerateSoftwareKeyRequest(
            id = KeyId("ed25519-key"),
            spec = KeySpec.Edwards(EdwardsCurve.ED25519),
            usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
        )
    )

    // Serialize the key to JSON
    val storedKey = key.storedKey
    val json = Json { prettyPrint = true }
    val serialized = json.encodeToString(
        id.walt.crypto2.keys.StoredKey.Software.serializer(),
        storedKey
    )

    println("Generated Ed25519 key:")
    println(serialized)
    
    runtime.close()
}
