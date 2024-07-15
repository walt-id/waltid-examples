package waltid;

import id.walt.crypto.keys.Key;
import id.walt.crypto.keys.KeyType;
import id.walt.crypto.keys.jwk.JWKKey;
import id.walt.did.dids.DidService;
import id.walt.did.dids.registrar.DidResult;
import id.walt.did.dids.registrar.dids.DidCreateOptions;
import id.walt.did.dids.registrar.dids.DidJwkCreateOptions;
import id.walt.did.dids.registrar.dids.DidKeyCreateOptions;
import id.walt.did.helpers.WaltidServices;
import kotlin.Result;

import java.util.concurrent.ExecutionException;

public class DidExamples {

    private static final DidService didService = DidService.INSTANCE;

    public static String generateDidSync(Key key) {
        DidCreateOptions options = new DidJwkCreateOptions();

        DidResult result = didService.javaRegisterByKeyBlocking("jwk", key, options);

        System.out.println("(sync) DID: " + result.getDid());
        System.out.println("(sync) DID document: " + result.getDidDocument());

        return result.getDid();
    }

    private static void generateDidAsync(Key key) {
        DidCreateOptions options = new DidKeyCreateOptions();

        didService.javaRegisterByKeyAsync("key", key, options).thenAccept(result -> {
                    System.out.println("(async) DID: " + result.getDid());
                    System.out.println("(async) DID document: " + result.getDidDocument());
                }
        );
    }

    private static void resolveDid(String did) throws ExecutionException, InterruptedException {
        Result<Key> x = didService.resolveToKeyAsync(did).get();
        System.out.println("Resolved: " + x);
    }

    public static void runDidExample() throws ExecutionException, InterruptedException {
        WaltidServices.INSTANCE.minimalInitBlocking();
        var key = JWKKey.Companion.generateBlocking(KeyType.Ed25519, null);

        DidExamples.generateDidAsync(key);
        String did = DidExamples.generateDidSync(key);

        resolveDid(did);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        runDidExample();
    }

}
