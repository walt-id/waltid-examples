package waltid.x509;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.jvm.JavaGenerateSoftwareKeyRequest;
import id.walt.crypto2.jvm.JavaKeySpec;
import id.walt.crypto2.jvm.JavaSoftwareKeys;
import id.walt.crypto2.keys.Key;
import id.walt.crypto2.keys.KeyUsage;

/** Shared helper for generating EC P-256 crypto2 software keys, used by the x509 examples. */
final class Crypto2Keys {
    private Crypto2Keys() {
    }

    static Key generateEcP256Key(CryptoRuntime runtime, String id) {
        JavaGenerateSoftwareKeyRequest request = JavaGenerateSoftwareKeyRequest.of(
                id,
                JavaKeySpec.ec("P-256"),
                Set.of(KeyUsage.SIGN, KeyUsage.VERIFY)
        );
        try {
            return JavaSoftwareKeys.generateKey(runtime, request).toCompletableFuture().get();
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to generate key: " + id, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while generating key: " + id, e);
        }
    }
}
