package waltid.x509;

import java.util.List;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.extension.BasicConstraintsExtension;
import id.walt.certificate.x509.profile.EtsiPidProviderX509CertificateProfile;
import id.walt.certificate.x509.validation.ValidationResult;
import id.walt.certificate.x509.validation.validator.X509CertificateValidityValidator;
import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.algorithms.SignatureAlgorithm;
import id.walt.crypto2.jvm.JavaDigestAlgorithm;
import id.walt.crypto2.jvm.JavaSignatureAlgorithm;
import id.walt.crypto2.jvm.JavaSoftwareKeys;
import id.walt.crypto2.keys.Key;
import kotlin.Unit;

/**
 * Creating and validating PID Provider certificates - ETSI TS 119 412-6 clause 4.
 *
 * <p>A PID Provider's private key signs EUDI Wallet PID attestations (SD-JWT VC / mdoc). This
 * example shows both shapes a PID Provider certificate can take: issued under a separate CA, and
 * self-signed - a LoTE (ETSI TS 119 602) PID Provider list accepts either an end-entity or a CA
 * certificate as its trust anchor, so both are valid.
 */
public class PidProviderExample {

    private static final CryptoRuntime cryptoRuntime = JavaSoftwareKeys.defaultRuntime();
    private static final SignatureAlgorithm signingAlg =
            JavaSoftwareKeys.toKotlin(JavaSignatureAlgorithm.ecdsa(new JavaDigestAlgorithm("SHA-256"), "DER"));

    private static final JavaX509CertificateUtil pidProviderCertUtil = JavaX509CertificateUtil.configure(
            builder -> builder.addValidators(
                    EtsiPidProviderX509CertificateProfile.INSTANCE,
                    new X509CertificateValidityValidator(true) // allowValidityInFuture = true
            )
    );

    public static void main(String[] args) {
        issueCaIssuedCertificate();
        System.out.println();
        issueSelfSignedCertificate();
    }

    private static void issueCaIssuedCertificate() {
        Key rootKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "pid-provider-root");
        X509Certificate rootCert = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                rootKey,
                signingAlg,
                builder -> {
                    builder.setSubjectDn("CN=Example Root CA,O=Walt.id,OrganizationIdentifier=VATAT-U12345678,C=AT");
                    BasicConstraintsExtension.Companion.extensionBasicConstraints(builder, bc -> {
                        bc.setCritical(true);
                        bc.setCA(true);
                        return Unit.INSTANCE;
                    });
                }
        );
        System.out.println("Created root CA: " + rootCert.getData().getSubjectDn());
        System.out.println(rootCert.getEncodedPem());

        Key pidProviderKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "pid-provider-ca-issued");
        X509Certificate pidProviderCert = JavaX509CertificateUtil.getDefault().createCertificate(
                rootKey,
                rootCert,
                signingAlg,
                builder -> EtsiPidProviderX509CertificateProfile.INSTANCE.profileEtsiPidProviderCertificate(
                        builder,
                        pidProviderKey,                        // subjectKey
                        "CN=Example PID Provider,O=Walt.id,OrganizationIdentifier=VATAT-U87654321,C=AT", // subjectDn
                        List.of("0.4.0.194112.1.1"),           // certificatePolicyOids
                        "https://ca.example.com/root.crt",     // caIssuerUri
                        null                                    // ocspResponderUri
                )
        );
        System.out.println("Issued CA-issued PID Provider cert: " + pidProviderCert.getData().getSubjectDn());
        System.out.println(pidProviderCert.getEncodedPem());

        ValidationResult result = pidProviderCertUtil.validateCertificateChain(List.of(pidProviderCert), rootCert);
        System.out.println("CA-issued PID Provider cert valid: " + result.getValid());
        for (ValidationResult.ValidationLogEntry entry : result.getLog()) {
            if (entry.getSeverity() == ValidationResult.Severity.ERROR) {
                System.out.println("  ERROR " + entry.getValidatorId() + ": " + entry.getMessage());
            }
        }
    }

    private static void issueSelfSignedCertificate() {
        Key pidProviderKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "pid-provider-self-signed");
        X509Certificate pidProviderCert = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                pidProviderKey,
                signingAlg,
                builder -> EtsiPidProviderX509CertificateProfile.INSTANCE.profileEtsiPidProviderCertificate(
                        builder,
                        null,                                   // subjectKey - null = self-signed
                        "CN=Example Self-Signed PID Provider,O=Walt.id,OrganizationIdentifier=VATAT-U11223344,C=AT", // subjectDn
                        List.of("0.4.0.194112.1.1"),            // certificatePolicyOids
                        null,                                    // caIssuerUri
                        null                                     // ocspResponderUri
                )
        );
        System.out.println("Issued self-signed PID Provider cert: " + pidProviderCert.getData().getSubjectDn());
        System.out.println(pidProviderCert.getEncodedPem());

        ValidationResult result =
                pidProviderCertUtil.validateCertificateChain(List.of(pidProviderCert), pidProviderCert);
        System.out.println("Self-signed PID Provider cert valid: " + result.getValid());
        for (ValidationResult.ValidationLogEntry entry : result.getLog()) {
            if (entry.getSeverity() == ValidationResult.Severity.ERROR) {
                System.out.println("  ERROR " + entry.getValidatorId() + ": " + entry.getMessage());
            }
        }
    }
}
