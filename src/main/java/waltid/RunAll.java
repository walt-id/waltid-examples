package waltid;

import static waltid.CustomKeyExample.runCustomKeyExample;
import static waltid.DidExamples.runDidExample;
import static waltid.KeysExamples.runKeyExample;
import static waltid.VcExamples.runVcExample;
import static waltid.x509.AllX509Examples.runAllX509Examples;

public class RunAll {
    public static void main(String[] args) throws Exception {
        runKeyExample();
        runDidExample();
        runVcExample();
        runCustomKeyExample();
        runAllX509Examples();
    }
}
