package waltid.crypto2.signatures;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.algorithms.DigestAlgorithm;
import id.walt.crypto2.algorithms.SignatureAlgorithm;
import id.walt.crypto2.keys.*;
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest;
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider;
import kotlin.collections.CollectionsKt;
import kotlinx.coroutines.future.FutureKt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * Java example: Sign and verify messages with ECDSA-SHA256 using crypto2 library.
 * 
 * This demonstrates:
 * - Signing data with ECDSA
 * - Verifying signatures
 * - Detecting tampered messages
 * - Using capabilities pattern
 */
public class Secp256r1Sign {
    public static void main(String[] args) {
        try {
            signAndVerify().get();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static CompletableFuture<Void> signAndVerify() {
        CryptoRuntime runtime = new CryptoRuntime(
            CollectionsKt.listOf(CryptographySoftwareKeyProvider.Companion.invoke()),
            CollectionsKt.emptyList(),
            null
        );

        // Generate key
        return FutureKt.asCompletableFuture(
            runtime.generateSoftwareKey(
                new GenerateSoftwareKeyRequest(
                    KeyId.Companion.invoke("secp256r1-signing-key"),
                    new KeySpec.Ec(EcCurve.Companion.getP256()),
                    CollectionsKt.setOf(KeyUsage.SIGN, KeyUsage.VERIFY),
                    null
                ),
                null,
                null
            )
        ).thenCompose(key -> {
            // Message to sign
            byte[] message = "Hello from Java with Crypto2!".getBytes(StandardCharsets.UTF_8);
            System.out.println("Message: " + new String(message, StandardCharsets.UTF_8));

            // Get signing capability
            Signer signer = key.getCapabilities().getSigner();
            if (signer == null) {
                throw new IllegalStateException("Key does not support signing");
            }

            // Sign with ECDSA-SHA256
            SignatureAlgorithm algorithm = new SignatureAlgorithm.Ecdsa(
                DigestAlgorithm.Companion.getSHA_256(),
                null
            );

            return FutureKt.asCompletableFuture(signer.sign(message, algorithm, null))
                .thenCompose(signature -> {
                    System.out.println("Signature (" + signature.length + " bytes): " +
                        Base64.getEncoder().encodeToString(signature));

                    // Get verification capability
                    Verifier verifier = key.getCapabilities().getVerifier();
                    if (verifier == null) {
                        throw new IllegalStateException("Key does not support verification");
                    }

                    // Verify signature
                    return FutureKt.asCompletableFuture(
                        verifier.verify(message, signature, algorithm, null)
                    ).thenCompose(isValid -> {
                        System.out.println("Signature valid: " + isValid);

                        // Test with tampered message
                        byte[] tamperedMessage = "Hello from Java with Crypto3!".getBytes(StandardCharsets.UTF_8);
                        return FutureKt.asCompletableFuture(
                            verifier.verify(tamperedMessage, signature, algorithm, null)
                        ).thenAccept(isTamperedValid -> {
                            System.out.println("Tampered message valid: " + isTamperedValid);
                            System.out.println();
                            System.out.println("✅ Java crypto2 signing and verification successful!");

                            // Close runtime
                            FutureKt.asCompletableFuture(runtime.close(null)).join();
                        });
                    });
                });
        });
    }
}
