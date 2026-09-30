package waltid;

import static waltid.CustomKeyExample.runCustomKeyExample;
import static waltid.DidExamples.runDidExample;
import static waltid.KeysExamples.runKeyExample;
import static waltid.VcExamples.runVcExample;
import static waltid.crypto2.AllCrypto2Examples.runAllCrypto2Examples;
import static waltid.x509.AllX509Examples.runAllX509Examples;

public class RunAll {
    public static void main(String[] args) throws Exception {
        runKeyExample();
        runAllCrypto2Examples();
        runDidExample();
        runVcExample();
        runCustomKeyExample();
        runAllX509Examples();
    }
}
