package waltid.crypto2;

import waltid.crypto2.key.create.Ed25519;
import waltid.crypto2.key.create.Secp256r1;
import waltid.crypto2.signatures.Secp256r1Sign;
import waltid.crypto2.simple.SimpleSigningExample;

public class AllCrypto2Examples {

    public static void runAllCrypto2Examples() throws Exception {
        Ed25519.main(new String[0]);
        Secp256r1.main(new String[0]);
        Secp256r1Sign.main(new String[0]);
        SimpleSigningExample.main(new String[0]);
    }
}
