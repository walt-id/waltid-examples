package crypto2.key.create

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider
import kotlinx.serialization.json.Json

suspend fun main() {
    createRSA()
}

suspend fun createRSA() {
    val runtime = CryptoRuntime(
        softwareProviders = listOf(CryptographySoftwareKeyProvider())
    )

    val key = runtime.generateSoftwareKey(
        request = GenerateSoftwareKeyRequest(
            id = KeyId("rsa-key"),
            spec = KeySpec.Rsa(bits = 2048),
            usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
        )
    )

    val json = Json { prettyPrint = true }
    val serialized = json.encodeToString(
        id.walt.crypto2.keys.StoredKey.Software.serializer(),
        key.storedKey
    )

    println("Generated RSA 2048 key:")
    println(serialized)
    
    runtime.close()
}
