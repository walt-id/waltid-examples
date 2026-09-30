package waltid.x509;

import java.util.List;
import java.util.stream.Collectors;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.X509CertificateTrustStore;
import id.walt.certificate.x509.truststore.CompositeTrustStore;
import id.walt.certificate.x509.truststore.InMemoryTrustStore;
import id.walt.certificate.x509.validation.ValidationResult;

/**
 * Java port of {@code ConfigureTrustStoreExample.kt}.
 *
 * <p>Shows the three ways to supply trust anchors to {@link JavaX509CertificateUtil}: a custom
 * {@link X509CertificateTrustStore} implementation, an {@link InMemoryTrustStore} of anchors, and
 * a {@link CompositeTrustStore} combining them - either configured once via {@code setTrust(...)}
 * or passed per validation call (which fully replaces, not merges, the configured trust store).
 */
public class ConfigureTrustStoreExample {

    private static final String ROOT_CERT_PEM = """
            -----BEGIN CERTIFICATE-----
            MIIB3jCCAYSgAwIBAgIULu5VAXkCuF+V/BRXZhLfwUSsiqwwCgYIKoZIzj0EAwIw
            JDEVMBMGA1UEAwwMV2FsdCBJRCBSb290MQswCQYDVQQGEwJBVDAeFw0yNjA4MTAx
            MjUyNDdaFw00NjA4MDUxMjUyNDdaMCQxFTATBgNVBAMMDFdhbHQgSUQgUm9vdDEL
            MAkGA1UEBhMCQVQwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQbREg0GIX6hBQP
            d3kMad6BC5d6cjb0kNowagy+KgpEE3nd3hRrNqRLa6e7wGewS3G61LaSpGFgE9iT
            1ECuJTeBo4GTMIGQMBIGA1UdEwEB/wQIMAYBAf8CAQAwDgYDVR0PAQH/BAQDAgEG
            MB0GA1UdDgQWBBS5uwPge8/AkJhc5xPe+k1acwcF2jAqBgNVHRIEIzAhgQ5vZmZp
            Y2VAd2FsdC5pZIYPaHR0cHM6Ly93YWx0LmlkMB8GA1UdIwQYMBaAFLm7A+B7z8CQ
            mFznE976TVpzBwXaMAoGCCqGSM49BAMCA0gAMEUCIEpLzmKtA1bv/80hXVEJ3eIi
            S14KNHCfiXkUQxMKjNnuAiEA7JTFpHHYXi2zPTNovOXNP7mov8llZkgbKZwbkEn4
            fkc=
            -----END CERTIFICATE-----""";

    private static final String LEAF_CERT_PEM = """
            -----BEGIN CERTIFICATE-----
            MIICETCCAbegAwIBAgIUMJAkGLbeyDnDaACHF2MwwUs/j1kwCgYIKoZIzj0EAwIw
            JDEVMBMGA1UEAwwMV2FsdCBJRCBSb290MQswCQYDVQQGEwJBVDAeFw0yNjA4MTAx
            MjUyNDdaFw0yNzExMTAxMjUyNDdaMCYxFzAVBgNVBAMMDldhbHQgSUQgbURMIERT
            MQswCQYDVQQGEwJBVDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBtESDQYhfqE
            FA93eQxp3oELl3pyNvSQ2jBqDL4qCkQTed3eFGs2pEtrp7vAZ7BLcbrUtpKkYWAT
            2JPUQK4lN4GjgcQwgcEwHQYDVR0OBBYEFLm7A+B7z8CQmFznE976TVpzBwXaMA4G
            A1UdDwEB/wQEAwIHgDAVBgNVHSUBAf8ECzAJBgcogYxdBQECMCoGA1UdEgQjMCGB
            Dm9mZmljZUB3YWx0Lmlkhg9odHRwczovL3dhbHQuaWQwLAYDVR0fBCUwIzAhoB+g
            HYYbaHR0cHM6Ly9jcmwud2FsdC5pZC9jcmwuZGVyMB8GA1UdIwQYMBaAFLm7A+B7
            z8CQmFznE976TVpzBwXaMAoGCCqGSM49BAMCA0gAMEUCIQD44E8Mukk3WwFeHbB6
            RZZPy85lVEyNqFZs6aNLq2kq4QIgXrURrzy1iLEYmsnna6YYhRrvGaYEjk1GqCn2
            w+skfmw=
            -----END CERTIFICATE-----""";

    public static void main(String[] args) {
        configureTrustStore();
    }

    /** A stub custom trust store implementation. */
    static final class MyTrustStore implements X509CertificateTrustStore {
        @Override
        public List<X509Certificate> findCertificateBySubjectDn(String subjectDn) {
            // my trust store implementation
            return List.of();
        }
    }

    static void configureTrustStore() {
        System.out.println("Configure trust store");

        List<String> trustedCertificatePemList = List.of(ROOT_CERT_PEM);

        // use your own trust store implementation
        X509CertificateTrustStore myTrustStore = new MyTrustStore();

        // use a set of trust anchors
        InMemoryTrustStore trustAnchors = new InMemoryTrustStore(
                trustedCertificatePemList.stream()
                        .map(pem -> JavaX509CertificateUtil.getDefault().parseCertificatePem(pem))
                        .collect(Collectors.toList())
        );

        // trust stores can be combined
        CompositeTrustStore wholeTrust = new CompositeTrustStore(List.of(myTrustStore, trustAnchors));

        // configure the trust store for the certificate util
        JavaX509CertificateUtil utilWithMyTrust =
                JavaX509CertificateUtil.configure(builder -> builder.setTrust(wholeTrust));

        // now you can use the util with your own trust store
        ValidationResult validationResult = utilWithMyTrust.validatePemCertificateChain(LEAF_CERT_PEM);
        System.out.println("Certificate valid: " + validationResult.getValid());
        printLog(validationResult);
        System.out.println();

        ValidationResult validationResultWithoutStore =
                JavaX509CertificateUtil.getDefault().validatePemCertificateChain(LEAF_CERT_PEM);
        System.out.println("Certificate valid (without trust): " + validationResultWithoutStore.getValid());
        printLog(validationResultWithoutStore);
        System.out.println();

        // You don't need to build a whole new util via setTrust() just to scope trust for a single
        // validation call - passing a trust store as the second argument does the same job. It fully
        // REPLACES the util's configured trust store for that call, it is not merged with it. So even
        // on X509CertificateUtil.Default (whose configured trust store is the platform's system CA
        // store on JVM/Android), passing your own anchors here means ONLY those anchors are trusted -
        // the platform's system trust is not silently added on top. This call gives the exact same
        // result as the setTrust()-configured util above:
        ValidationResult validationResultWithOverride =
                JavaX509CertificateUtil.getDefault().validatePemCertificateChain(LEAF_CERT_PEM, trustAnchors);
        System.out.println("Certificate valid (Default util, trust store passed per-call): "
                + validationResultWithOverride.getValid());
        printLog(validationResultWithOverride);
    }

    private static void printLog(ValidationResult result) {
        for (ValidationResult.ValidationLogEntry entry : result.getLog()) {
            System.out.println(entry.getSeverity() + " " + entry.getSubjectDn() + "/"
                    + entry.getValidatorId() + ": '" + entry.getMessage() + "'");
        }
    }
}
