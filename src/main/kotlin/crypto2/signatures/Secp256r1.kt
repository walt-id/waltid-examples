package crypto2.signatures

import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.DigestAlgorithm
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.EcCurve
import id.walt.crypto2.keys.KeyId
import id.walt.crypto2.keys.KeySpec
import id.walt.crypto2.keys.KeyUsage
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
suspend fun main() {
    signAndVerifySecp256r1()
}

@OptIn(ExperimentalEncodingApi::class)
suspend fun signAndVerifySecp256r1() {
    val runtime = CryptoRuntime(
        softwareProviders = listOf(CryptographySoftwareKeyProvider())
    )

    // Generate key
    val key = runtime.generateSoftwareKey(
        request = GenerateSoftwareKeyRequest(
            id = KeyId("secp256r1-signing-key"),
            spec = KeySpec.Ec(EcCurve.P256),
            usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
        )
    )

    // Data to sign
    val message = "Hello, Crypto2!".encodeToByteArray()
    println("Message: ${message.decodeToString()}")

    // Sign with ECDSA-SHA256
    val algorithm = SignatureAlgorithm.Ecdsa(digest = DigestAlgorithm.SHA_256)
    val signer = requireNotNull(key.capabilities.signer) { "Key does not support signing" }
    val signature = signer.sign(message, algorithm)
    
    println("Signature (${signature.size} bytes): ${Base64.Default.encode(signature)}")

    // Verify signature
    val verifier = requireNotNull(key.capabilities.verifier) { "Key does not support verification" }
    val isValid = verifier.verify(message, signature, algorithm)
    
    println("Signature valid: $isValid")

    // Test with tampered message
    val tamperedMessage = "Hello, Crypto3!".encodeToByteArray()
    val isTamperedValid = verifier.verify(tamperedMessage, signature, algorithm)
    
    println("Tampered message valid: $isTamperedValid")

    runtime.close()
}
