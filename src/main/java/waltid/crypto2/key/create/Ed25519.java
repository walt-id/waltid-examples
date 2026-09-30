package waltid.crypto2.key.create;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.jvm.JavaGenerateSoftwareKeyRequest;
import id.walt.crypto2.jvm.JavaKeySpec;
import id.walt.crypto2.jvm.JavaSoftwareKeys;
import id.walt.crypto2.keys.KeyUsage;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Java example: Generate an Ed25519 key using crypto2 library.
 *
 * This demonstrates:
 * - Creating CryptoRuntime from Java
 * - Generating Edwards curve keys via the waltid-crypto2-java bridge
 * - Serializing keys to JSON
 */
public class Ed25519 {
    public static void main(String[] args) {
        try {
            createEd25519().get();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static CompletableFuture<Void> createEd25519() {
        CryptoRuntime runtime = JavaSoftwareKeys.defaultRuntime();

        JavaGenerateSoftwareKeyRequest request = JavaGenerateSoftwareKeyRequest.of(
            "ed25519-key",
            JavaKeySpec.edwards("Ed25519"),
            Set.of(KeyUsage.SIGN, KeyUsage.VERIFY)
        );

        CompletionStage<Void> result = JavaSoftwareKeys.generate(runtime, request)
            .thenAccept(key -> {
                System.out.println("Generated Ed25519 key:");
                System.out.println(key.storedKeyJson());
            })
            .thenCompose(unused -> JavaSoftwareKeys.close(runtime));

        return result.toCompletableFuture();
    }
}
