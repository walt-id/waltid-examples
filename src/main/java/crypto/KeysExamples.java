package crypto;

import id.walt.crypto.keys.Key;
import id.walt.crypto.keys.KeyType;
import id.walt.crypto.keys.jwk.JWKKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import kotlin.Result;

public class KeysExamples {

    private static final byte[] plaintext = "< this is my plaintext>".getBytes(StandardCharsets.UTF_8);

    // The following two functions generate a new (local) JWK Key and use it to sign raw data,
    // with the first function demonstrating the Java Blocking API, and the second function
    // demonstrating the Java (async) CompletableFuture API.
    // Replace KeyType.Ed25519 with a different KeyType if desired.

    public static void signBlocking() {
        System.out.println("Generating key synchronous...");
        JWKKey k = JWKKey.Companion.generateBlocking(KeyType.Ed25519, null);
        System.out.println("Sync generated key: " + k);
        System.out.println("Signing with key synchronous...");
        var signed = (byte[]) k.signRawBlocking(plaintext);

        System.out.println("Signed synchronous: " + Arrays.toString(signed));

        verifyAsync(k, signed, plaintext, "Test sync verification");

        System.out.println("Test a verification failure...");
        byte[] invalid = new byte[signed.length + 1];
        System.arraycopy(signed, 0, invalid, 0, signed.length);
        invalid[signed.length] = Integer.valueOf(1).byteValue();

        verifyAsync(k, signed, invalid, "Test verification failure");
    }

    public static void signAsync() {
        System.out.println("Generating key asynchronous...");

        // join Futures to make sure they execute even when the program terminates earlier
        JWKKey.Companion.generateAsync(KeyType.Ed25519, null).thenAccept(key -> {
            System.out.println("Async generated key: " + key);
            System.out.println("Signing with key asynchronous...");
            key.signRawAsync(plaintext).thenAccept(signed -> {
                System.out.println("Signed asynchronous: " + Arrays.toString((byte[]) signed));

                verifyAsync(key, (byte[]) signed, plaintext, "Test async verification");
            });
        });
    }

    public static void verifyAsync(Key key, byte[] signed, byte[] plaintext, String message) {
        key.getPublicKeyAsync().thenAccept(publicKey -> {
            publicKey.verifyRawAsync(signed, plaintext).thenAccept(result -> {
                System.out.println("Verification result (" + message + "): " + result);
            }).join();
        }).join();
    }

    public static String exportKey() {
        // Other KeyTypes:
        var key2 = JWKKey.Companion.generateBlocking(KeyType.secp256r1, null);
        System.out.println("Export key...");
        String jwkExport = key2.exportJWKBlocking();
        System.out.println("Different KeyType, exported: " + jwkExport);
        return jwkExport;
    }

    public static void importKey(String jwk) throws ExecutionException, InterruptedException {
        System.out.println("Import key...");
        Result<JWKKey> keyImport = JWKKey.Companion.importJWKAsync(jwk).get();
        System.out.println("Import result: " + keyImport);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        KeysExamples.signAsync();
        KeysExamples.signBlocking();

        var jwk = exportKey();
        importKey(jwk);
    }
}
