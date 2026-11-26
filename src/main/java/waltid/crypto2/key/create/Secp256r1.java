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
 * Java example: Generate a secp256r1 (P-256) ECDSA key using crypto2 library.
 */
public class Secp256r1 {
    public static void main(String[] args) {
        try {
            createSecp256r1().get();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static CompletableFuture<Void> createSecp256r1() {
        CryptoRuntime runtime = JavaSoftwareKeys.defaultRuntime();

        JavaGenerateSoftwareKeyRequest request = JavaGenerateSoftwareKeyRequest.of(
            "secp256r1-key",
            JavaKeySpec.ec("P-256"),
            Set.of(KeyUsage.SIGN, KeyUsage.VERIFY)
        );

        CompletionStage<Void> result = JavaSoftwareKeys.generate(runtime, request)
            .thenAccept(key -> {
                System.out.println("Generated secp256r1 (P-256) key:");
                System.out.println(key.storedKeyJson());
            })
            .thenCompose(unused -> JavaSoftwareKeys.close(runtime));

        return result.toCompletableFuture();
    }
}
