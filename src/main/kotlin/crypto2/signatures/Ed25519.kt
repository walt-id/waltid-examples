package crypto2.signatures

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.EdwardsCurve
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
suspend fun main() {
    signAndVerifyEd25519()
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun signAndVerifyEd25519() {
    val runtime = CryptoRuntime(
        softwareProviders = listOf(CryptographySoftwareKeyProvider())
    )

    val key = runtime.generateSoftwareKey(
        request = GenerateSoftwareKeyRequest(
            id = KeyId("ed25519-signing-key"),
            spec = KeySpec.Edwards(EdwardsCurve.ED25519),
            usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
        )
    )

    val message = "Hello, Crypto2!".encodeToByteArray()
    println("Message: ${message.decodeToString()}")

    // EdDsa signature algorithm
    val algorithm = SignatureAlgorithm.EdDsa
    val signer = requireNotNull(key.capabilities.signer) { "Key does not support signing" }
    val signature = signer.sign(message, algorithm)
    
    println("Signature (${signature.size} bytes): ${Base64.Default.encode(signature)}")

    val verifier = requireNotNull(key.capabilities.verifier) { "Key does not support verification" }
    val isValid = verifier.verify(message, signature, algorithm)
    
    println("Signature valid: $isValid")

    runtime.close()
}
