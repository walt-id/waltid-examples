package crypto2.key.create

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.keys.EcCurve
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider
import kotlinx.serialization.json.Json

suspend fun main() {
    createSecp256r1()
}

suspend fun createSecp256r1() {
    val runtime = CryptoRuntime(
        softwareProviders = listOf(CryptographySoftwareKeyProvider())
    )

    val key = runtime.generateSoftwareKey(
        request = GenerateSoftwareKeyRequest(
            id = KeyId("secp256r1-key"),
            spec = KeySpec.Ec(EcCurve.P256),
            usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
        )
    )

    val json = Json { prettyPrint = true }
    val serialized = json.encodeToString(
        id.walt.crypto2.keys.StoredKey.Software.serializer(),
        key.storedKey
    )

    println("Generated secp256r1 (P-256) key:")
    println(serialized)
    
    runtime.close()
}
