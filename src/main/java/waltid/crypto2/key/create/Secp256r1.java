package waltid.crypto2.key.create;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.keys.*;
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest;
import id.walt.crypto2.providers.cryptography.CryptographySoftwareKeyProvider;
import kotlin.collections.CollectionsKt;
import kotlinx.coroutines.future.FutureKt;
import kotlinx.serialization.json.Json;

import java.util.concurrent.CompletableFuture;

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
        CryptoRuntime runtime = new CryptoRuntime(
            CollectionsKt.listOf(CryptographySoftwareKeyProvider.Companion.invoke()),
            CollectionsKt.emptyList(),
            null
        );

        return FutureKt.asCompletableFuture(
            runtime.generateSoftwareKey(
                new GenerateSoftwareKeyRequest(
                    KeyId.Companion.invoke("secp256r1-key"),
                    new KeySpec.Ec(EcCurve.Companion.getP256()),
                    CollectionsKt.setOf(KeyUsage.SIGN, KeyUsage.VERIFY),
                    null
                ),
                null,
                null
            )
        ).thenAccept(key -> {
            Json json = Json.Default;

            String serialized = json.encodeToString(
                StoredKey.Software.Companion.serializer(),
                key.getStoredKey()
            );

            System.out.println("Generated secp256r1 (P-256) key:");
            System.out.println(serialized);

            FutureKt.asCompletableFuture(runtime.close(null)).join();
        });
    }
}
