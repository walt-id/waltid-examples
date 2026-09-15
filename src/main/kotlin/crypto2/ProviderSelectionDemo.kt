package crypto2

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.keys.EcCurve
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.ProviderSelection
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider

suspend fun main() {
    providerSelectionDemo()
}

suspend fun providerSelectionDemo() {
    println("=== Provider Selection Demo ===\n")

    // Create runtime with software provider
    val provider = CryptographySoftwareKeyProvider()
    val runtime = CryptoRuntime(
        softwareProviders = listOf(provider)
    )

    val request = GenerateSoftwareKeyRequest(
        id = KeyId("provider-demo-key"),
        spec = KeySpec.Ec(EcCurve.P256),
        usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
    )

    // 1. Automatic selection (default)
    println("1. Automatic provider selection:")
    val key1 = runtime.generateSoftwareKey(request.copy(id = KeyId("auto-key")))
    println("   ✓ Key generated with automatic selection\n")

    // 2. Explicit provider selection
    println("2. Explicit provider selection:")
    val key2 = runtime.generateSoftwareKey(
        request.copy(id = KeyId("explicit-key")),
        selection = ProviderSelection.Only(provider.id)
    )
    println("   ✓ Key generated with provider: ${provider.id.value}\n")

    // 3. Fallback list (only one provider in this example)
    println("3. Fallback provider list:")
    val key3 = runtime.generateSoftwareKey(
        request.copy(id = KeyId("fallback-key")),
        selection = ProviderSelection.FirstAvailable(listOf(provider.id))
    )
    println("   ✓ Key generated with fallback list\n")

    // 4. Demonstrate failure with non-existent provider
    println("4. Attempting to use non-existent provider:")
    try {
        runtime.generateSoftwareKey(
            request.copy(id = KeyId("fail-key")),
            selection = ProviderSelection.Only(id.walt.crypto2.keys.ProviderId("non-existent"))
        )
        println("   ✗ Should have failed!")
    } catch (e: Exception) {
        println("   ✓ Failed as expected: ${e.message}\n")
    }

    println("Summary:")
    println("  - Automatic selection: Uses first compatible provider")
    println("  - Explicit selection: No fallback, fails if provider unavailable")
    println("  - Fallback list: Tries providers in order")

    runtime.close()
}
