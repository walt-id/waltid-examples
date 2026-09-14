package waltid.crypto2.simple;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.jvm.JavaGenerateSoftwareKeyRequest;
import id.walt.crypto2.jvm.JavaKeySpec;
import id.walt.crypto2.jvm.JavaSignatureAlgorithm;
import id.walt.crypto2.jvm.JavaSigner;
import id.walt.crypto2.jvm.JavaSoftwareKey;
import id.walt.crypto2.jvm.JavaSoftwareKeys;
import id.walt.crypto2.jvm.JavaVerifier;
import id.walt.crypto2.keys.KeyUsage;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Simplified Java example with blocking calls.
 *
 * This shows a more idiomatic Java approach to using crypto2:
 * - Using .get() to block on CompletionStage/CompletableFuture
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
        CryptoRuntime runtime = JavaSoftwareKeys.defaultRuntime();

        try {
            // 2. Generate Ed25519 key (blocking)
            System.out.println("Generating Ed25519 key...");
            JavaGenerateSoftwareKeyRequest request = JavaGenerateSoftwareKeyRequest.of(
                "simple-key",
                JavaKeySpec.edwards("Ed25519"),
                Set.of(KeyUsage.SIGN, KeyUsage.VERIFY)
            );
            JavaSoftwareKey key = JavaSoftwareKeys.generate(runtime, request).toCompletableFuture().get();
            System.out.println("✓ Key generated: " + key.id());

            // 3. Sign a message (blocking)
            byte[] message = "Simple Java example!".getBytes(StandardCharsets.UTF_8);
            System.out.println("\nSigning message: " + new String(message, StandardCharsets.UTF_8));

            JavaSigner signer = key.signer();
            if (signer == null) {
                throw new IllegalStateException("Key does not support signing");
            }

            JavaSignatureAlgorithm algorithm = JavaSignatureAlgorithm.edDsa();

            byte[] signature = signer.sign(message, algorithm).toCompletableFuture().get();
            System.out.println("✓ Signature: " + Base64.getEncoder().encodeToString(signature));

            // 4. Verify signature (blocking)
            System.out.println("\nVerifying signature...");
            JavaVerifier verifier = key.verifier();
            if (verifier == null) {
                throw new IllegalStateException("Key does not support verification");
            }

            boolean isValid = verifier.verify(message, signature, algorithm).toCompletableFuture().get();
            System.out.println("✓ Signature valid: " + isValid);

            // 5. Test with wrong message
            byte[] wrongMessage = "Different message!".getBytes(StandardCharsets.UTF_8);
            boolean isWrongValid = verifier.verify(wrongMessage, signature, algorithm).toCompletableFuture().get();
            System.out.println("✓ Wrong message valid: " + isWrongValid);

            System.out.println("\n✅ Simple Java example completed successfully!");

        } finally {
            // 6. Clean up
            JavaSoftwareKeys.close(runtime).toCompletableFuture().get();
        }
    }
}
