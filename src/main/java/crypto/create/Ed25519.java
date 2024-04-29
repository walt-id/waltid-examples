package crypto.create;

import id.walt.crypto.keys.KeyType;
import id.walt.crypto.keys.jwk.JWKKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Ed25519 {

    public static void signBlocking() {
        System.out.println("Generating key synchronous...");
        JWKKey k = JWKKey.Companion.generateBlocking(KeyType.Ed25519, null);
        System.out.println("Signing with key synchronous...");
        var signed = (byte[]) k.signRawBlocking("< this is my plaintext>".getBytes(StandardCharsets.UTF_8));

        System.out.println("Signed synchronous: " + Arrays.toString(signed));
    }

    @SuppressWarnings("CodeBlock2Expr")
    public static void signAsync() {
        System.out.println("Generating key asynchronous...");

        // join Futures to make sure they execute even when program terminates earlier
        JWKKey.Companion.generateAsync(KeyType.Ed25519, null).thenAccept(key -> {
            System.out.println("Signing with key asynchronous...");
            key.signRawAsync("< this is my plaintext>".getBytes(StandardCharsets.UTF_8)).thenAccept(signed -> {
                System.out.println("Signed asynchronous: " + Arrays.toString((byte[]) signed));
            });
        });
    }

    public static void main(String[] args) {
        Ed25519.signAsync();
        Ed25519.signBlocking();
    }
}
