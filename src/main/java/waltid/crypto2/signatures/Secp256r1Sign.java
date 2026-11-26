package waltid.crypto2.signatures;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.jvm.JavaDigestAlgorithm;
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
import java.util.concurrent.CompletableFuture;

/**
 * Java example: Sign and verify messages with ECDSA-SHA256 using crypto2 library.
 *
 * This demonstrates:
 * - Signing data with ECDSA
 * - Verifying signatures
 * - Detecting tampered messages
 * - Using the capabilities pattern via the waltid-crypto2-java bridge
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
        CryptoRuntime runtime = JavaSoftwareKeys.defaultRuntime();

        JavaGenerateSoftwareKeyRequest request = JavaGenerateSoftwareKeyRequest.of(
            "secp256r1-signing-key",
            JavaKeySpec.ec("P-256"),
            Set.of(KeyUsage.SIGN, KeyUsage.VERIFY)
        );

        return JavaSoftwareKeys.generate(runtime, request).thenCompose(key -> {
            byte[] message = "Hello from Java with Crypto2!".getBytes(StandardCharsets.UTF_8);
            System.out.println("Message: " + new String(message, StandardCharsets.UTF_8));

            JavaSigner signer = key.signer();
            if (signer == null) {
                throw new IllegalStateException("Key does not support signing");
            }

            JavaSignatureAlgorithm algorithm = JavaSignatureAlgorithm.ecdsa(
                new JavaDigestAlgorithm("SHA-256"),
                "IEEE_P1363"
            );

            return signer.sign(message, algorithm).thenCompose(signature -> {
                System.out.println("Signature (" + signature.length + " bytes): " +
                    Base64.getEncoder().encodeToString(signature));

                JavaVerifier verifier = key.verifier();
                if (verifier == null) {
                    throw new IllegalStateException("Key does not support verification");
                }

                return verifier.verify(message, signature, algorithm).thenCompose(isValid -> {
                    System.out.println("Signature valid: " + isValid);

                    byte[] tamperedMessage = "Hello from Java with Crypto3!".getBytes(StandardCharsets.UTF_8);
                    return verifier.verify(tamperedMessage, signature, algorithm).thenCompose(isTamperedValid -> {
                        System.out.println("Tampered message valid: " + isTamperedValid);
                        System.out.println();
                        System.out.println("Java crypto2 signing and verification successful!");

                        return JavaSoftwareKeys.close(runtime);
                    });
                });
            });
        }).toCompletableFuture();
    }
}
