package waltid;

import java.util.concurrent.ExecutionException;

import static waltid.CustomKeyExample.runCustomKeyExample;
import static waltid.DidExamples.runDidExample;
import static waltid.KeysExamples.runKeyExample;
import static waltid.VcExamples.runVcExample;

public class RunAll {
    public static void main(String[] args) throws Exception {
        runKeyExample();
        runDidExample();
        runVcExample();
        runCustomKeyExample();
    }
}
