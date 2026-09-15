package waltid.x509;

public class AllX509Examples {

    public static void runAllX509Examples() throws Exception {
        CreateCsrExample.main(new String[0]);
        ConfigureTrustStoreExample.main(new String[0]);
        SignCertificateExample.main(new String[0]);
        IsoMdlOnboardingExample.main(new String[0]);
        WrpacRelyingPartyExample.main(new String[0]);
        WrprcRelyingPartyExample.main(new String[0]);
    }

    public static void main(String[] args) throws Exception {
        AllX509Examples.runAllX509Examples();
    }
}
