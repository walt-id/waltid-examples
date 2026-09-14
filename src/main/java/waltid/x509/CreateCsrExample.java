package waltid.x509;

import java.util.List;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.Pkcs10CertificateSigningRequest;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.builder.X509CertificateDataBuilder;
import id.walt.certificate.x509.extension.SubjectAlternativeNameExtension;
import id.walt.certificate.x509.truststore.InMemoryTrustStore;
import id.walt.certificate.x509.validation.ValidationResult;
import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.algorithms.SignatureAlgorithm;
import id.walt.crypto2.jvm.JavaDigestAlgorithm;
import id.walt.crypto2.jvm.JavaSignatureAlgorithm;
import id.walt.crypto2.jvm.JavaSoftwareKeys;
import id.walt.crypto2.keys.Key;
import kotlin.Unit;

/**
 * Java port of {@code CreateCsrExample.kt}.
 *
 * <p>Builds a PKCS#10 certificate signing request (CSR) for a leaf key, then shows a CA
 * parsing the PEM-encoded CSR, checking its self-signature, and issuing a certificate from it -
 * carrying over the CSR's subject DN, public key and subject alternative names.
 */
public class CreateCsrExample {

    private static final CryptoRuntime cryptoRuntime = JavaSoftwareKeys.defaultRuntime();
    private static final SignatureAlgorithm certSigningAlg =
            JavaSoftwareKeys.toKotlin(JavaSignatureAlgorithm.ecdsa(new JavaDigestAlgorithm("SHA-256"), "DER"));

    public static void main(String[] args) {
        Key leafKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "leaf");
        Pkcs10CertificateSigningRequest csr = createCsr(leafKey);
        System.out.println();

        // A CA typically only sees the PEM-encoded CSR; parse it back and check the CSR is
        // genuinely signed by the key it claims to be for, before issuing anything from it.
        Pkcs10CertificateSigningRequest parsedCsr =
                JavaX509CertificateUtil.getDefault().parseCsrPem(csr.getEncodedPem());
        boolean csrSignatureValid = JavaX509CertificateUtil.getDefault().validateCsrSignature(parsedCsr);
        System.out.println("Parsed CSR signature valid: " + csrSignatureValid);
        System.out.println();

        Key caKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "ca");
        X509Certificate caCert = createSelfSignedRootCa(caKey);
        System.out.println();
        X509Certificate leafCert = issueCertificateFromCsr(caKey, caCert, parsedCsr, leafKey);
        System.out.println();
        validateCertificateChain(leafCert, caCert);
    }

    private static Pkcs10CertificateSigningRequest createCsr(Key subjectKey) {
        Pkcs10CertificateSigningRequest csr = JavaX509CertificateUtil.getDefault().createCsr(
                subjectKey,
                certSigningAlg,
                builder -> {
                    builder.getRequestedCertificate().setSubjectDn("cn=My Leaf Certificate, o=Walt.id, c=AT");
                    SubjectAlternativeNameExtension.Companion.extensionSan(builder.getRequestedCertificate(), san -> {
                        san.addDnsName("leaf.walt.id");
                        san.addEmail("example@walt.id");
                        return Unit.INSTANCE;
                    });
                }
        );
        System.out.println("Created CSR");
        System.out.println("Subject DN: " + csr.getRequestedCertificate().getSubjectDn());
        System.out.println(csr.getEncodedPem());
        return csr;
    }

    private static X509Certificate createSelfSignedRootCa(Key key) {
        X509Certificate cert = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                key,
                certSigningAlg,
                builder -> builder.setSubjectDn("cn=My Root, o=Walt.id, c=AT")
        );
        System.out.println("Created root Ca certificate");
        System.out.println("Subject DN: " + cert.getData().getSubjectDn());
        return cert;
    }

    private static X509Certificate issueCertificateFromCsr(
            Key issuerKey, X509Certificate issuerCert, Pkcs10CertificateSigningRequest csr, Key subjectKey) {
        Pkcs10CertificateSigningRequest.RequestedCertificateData requested = csr.getRequestedCertificate();
        X509Certificate cert = JavaX509CertificateUtil.getDefault().createCertificate(
                issuerKey,
                issuerCert,
                certSigningAlg,
                (X509CertificateDataBuilder builder) -> {
                    builder.setSubjectDn(requested.getSubjectDn());
                    // The crypto2 Key-based issuer path signs the subject public key it is given
                    // directly rather than one reconstructed from the CSR's SubjectPublicKeyInfo
                    // (that reconstruction path only exists for the deprecated legacy-Key issuer
                    // overload) - since the CA in this example already holds the subject's crypto2
                    // key, pass it in here.
                    builder.subjectPublicKey(subjectKey);
                    // carry the CSR's requested extensions (e.g. subject alternative names) onto the issued certificate
                    builder.getExtensions().putAll(requested.getExtensions());
                }
        );
        System.out.println("Issued leaf certificate from CSR");
        System.out.println("Subject DN: " + cert.getData().getSubjectDn());
        System.out.println("Fingerprint: " + cert.getFingerprintSha256Hex());
        return cert;
    }

    private static void validateCertificateChain(X509Certificate leafCert, X509Certificate trustRoot) {
        InMemoryTrustStore trustStore = new InMemoryTrustStore(List.of(trustRoot));
        ValidationResult validationResult =
                JavaX509CertificateUtil.getDefault().validateCertificateChain(List.of(leafCert), trustStore);
        System.out.println("Validation result - isValid: " + validationResult.getValid());
    }
}
