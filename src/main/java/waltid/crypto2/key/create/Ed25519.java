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
 * Java example: Generate an Ed25519 key using crypto2 library.
 * 
 * This demonstrates:
 * - Creating CryptoRuntime from Java
 * - Using Kotlin suspend functions via CompletableFuture
 * - Generating Edwards curve keys
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
        // Initialize CryptoRuntime with software provider
        CryptoRuntime runtime = new CryptoRuntime(
            CollectionsKt.listOf(CryptographySoftwareKeyProvider.Companion.invoke()),
            CollectionsKt.emptyList(),
            null
        );

        // Generate Ed25519 key (using CompletableFuture for Kotlin suspend function)
        return FutureKt.asCompletableFuture(
            runtime.generateSoftwareKey(
                new GenerateSoftwareKeyRequest(
                    KeyId.Companion.invoke("ed25519-key"),
                    new KeySpec.Edwards(EdwardsCurve.Companion.getED25519()),
                    CollectionsKt.setOf(KeyUsage.SIGN, KeyUsage.VERIFY),
                    null
                ),
                null,
                null
            )
        ).thenAccept(key -> {
            // Get the stored key
            StoredKey.Software storedKey = key.getStoredKey();

            // Serialize to JSON
            Json json = Json.Default;

            String serialized = json.encodeToString(
                StoredKey.Software.Companion.serializer(),
                storedKey
            );

            System.out.println("Generated Ed25519 key:");
            System.out.println(serialized);

            // Close runtime
            FutureKt.asCompletableFuture(runtime.close(null)).join();
        });
    }
}
