package crypto2.key

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.EdwardsCurve
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.keys.SoftwareKey
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
suspend fun main() {
    keySerializationAndRestoration()
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun keySerializationAndRestoration() {
    println("=== Key Serialization and Restoration Demo ===\n")

    // Step 1: Generate a key
    println("1. Generating Ed25519 key...")
    val runtime = CryptoRuntime(
        softwareProviders = listOf(CryptographySoftwareKeyProvider())
    )

    val originalKey = runtime.generateSoftwareKey(
        request = GenerateSoftwareKeyRequest(
            id = KeyId("demo-key"),
            spec = KeySpec.Edwards(EdwardsCurve.ED25519),
            usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
        )
    )

    // Step 2: Sign with original key
    println("2. Signing message with original key...")
    val message = "Test message for crypto2".encodeToByteArray()
    val signer = requireNotNull(originalKey.capabilities.signer)
    val signature = signer.sign(message, SignatureAlgorithm.EdDsa)
    println("   Signature: ${Base64.Default.encode(signature)}\n")

    // Step 3: Serialize the key
    println("3. Serializing key to JSON...")
    val json = Json { prettyPrint = true }
    val serialized = json.encodeToString(SoftwareKey.serializer(), originalKey)
    println("   Serialized key length: ${serialized.length} characters\n")
    // SECURITY WARNING: This contains private key material in production!
    println("   Serialized key (first 200 chars):")
    println("   ${serialized.take(200)}...\n")

    // Step 4: Deserialize the key (non-operational)
    println("4. Deserializing key (returns non-operational handle)...")
    val deserializedKey = json.decodeFromString<SoftwareKey>(serialized)
    println("   Deserialized key ID: ${deserializedKey.id.value}")
    println("   Has signing capability? ${deserializedKey.capabilities.signer != null}")
    println("   (Expected: false - capabilities are null until restored)\n")

    // Step 5: Restore the key with runtime
    println("5. Restoring key with CryptoRuntime...")
    val restoredKey = runtime.restore(deserializedKey)
    println("   Has signing capability? ${restoredKey.capabilities.signer != null}")
    println("   (Expected: true - capabilities restored)\n")

    // Step 6: Verify signature with restored key
    println("6. Verifying original signature with restored key...")
    val verifier = requireNotNull(restoredKey.capabilities.verifier)
    val isValid = verifier.verify(message, signature, SignatureAlgorithm.EdDsa)
    println("   Signature valid: $isValid\n")

    println("✅ Key successfully serialized, deserialized, and restored!")

    runtime.close()
}
