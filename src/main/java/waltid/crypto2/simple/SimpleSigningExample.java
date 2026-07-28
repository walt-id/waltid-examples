package waltid.crypto2.simple;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.algorithms.SignatureAlgorithm;
import id.walt.crypto2.keys.*;
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest;
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider;
import kotlin.collections.CollectionsKt;
import kotlinx.coroutines.future.FutureKt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

/**
 * Simplified Java example with blocking calls.
 * 
 * This shows a more idiomatic Java approach to using crypto2:
 * - Using .get() to block on CompletableFuture
 * - Focus on the crypto operations rather than coroutine mechanics
 * - Exception handling with try-catch
 */
public class SimpleSigningExample {
    public static void main(String[] args) {
        try {
            simpleSignAndVerify();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void simpleSignAndVerify() throws ExecutionException, InterruptedException {
        // 1. Create runtime
        CryptoRuntime runtime = new CryptoRuntime(
            CollectionsKt.listOf(CryptographySoftwareKeyProvider.Companion.invoke()),
            CollectionsKt.emptyList(),
            null
        );

        try {
            // 2. Generate Ed25519 key (blocking)
            System.out.println("Generating Ed25519 key...");
            SoftwareKey key = FutureKt.asCompletableFuture(
                runtime.generateSoftwareKey(
                    new GenerateSoftwareKeyRequest(
                        KeyId.Companion.invoke("simple-key"),
                        new KeySpec.Edwards(EdwardsCurve.Companion.getED25519()),
                        CollectionsKt.setOf(KeyUsage.SIGN, KeyUsage.VERIFY),
                        null
                    ),
                    null,
                    null
                )
            ).get();
            System.out.println("✓ Key generated: " + key.getId().getValue());

            // 3. Sign a message (blocking)
            byte[] message = "Simple Java example!".getBytes(StandardCharsets.UTF_8);
            System.out.println("\nSigning message: " + new String(message, StandardCharsets.UTF_8));
            
            Signer signer = key.getCapabilities().getSigner();
            if (signer == null) {
                throw new IllegalStateException("Key does not support signing");
            }
            
            byte[] signature = FutureKt.asCompletableFuture(
                signer.sign(message, SignatureAlgorithm.EdDsa, null)
            ).get();
            System.out.println("✓ Signature: " + Base64.getEncoder().encodeToString(signature));

            // 4. Verify signature (blocking)
            System.out.println("\nVerifying signature...");
            Verifier verifier = key.getCapabilities().getVerifier();
            if (verifier == null) {
                throw new IllegalStateException("Key does not support verification");
            }
            
            boolean isValid = FutureKt.asCompletableFuture(
                verifier.verify(message, signature, SignatureAlgorithm.EdDsa, null)
            ).get();
            System.out.println("✓ Signature valid: " + isValid);

            // 5. Test with wrong message
            byte[] wrongMessage = "Different message!".getBytes(StandardCharsets.UTF_8);
            boolean isWrongValid = FutureKt.asCompletableFuture(
                verifier.verify(wrongMessage, signature, SignatureAlgorithm.EdDsa, null)
            ).get();
            System.out.println("✓ Wrong message valid: " + isWrongValid);

            System.out.println("\n✅ Simple Java example completed successfully!");

        } finally {
            // 6. Clean up
            FutureKt.asCompletableFuture(runtime.close(null)).get();
        }
    }
}
