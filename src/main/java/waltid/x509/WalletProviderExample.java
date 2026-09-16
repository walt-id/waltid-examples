package waltid.x509;

import java.util.List;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.extension.BasicConstraintsExtension;
import id.walt.certificate.x509.profile.EtsiWalletProviderX509CertificateProfile;
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
 * Creating and validating a Wallet Provider certificate - ETSI TS 119 412-6 clause 5.1 (WAL-5.1-01).
 *
 * <p>A Wallet Provider's private key signs Wallet Instance/Unit Attestations (WIA/WUA). Identical
 * in shape to {@link PidProviderExample} - the only difference is the QcType OID carried
 * (id-etsi-qct-wal instead of id-etsi-qct-pid). See {@link PidProviderExample} for the self-signed
 * variant, which works the same way here too.
 */
public class WalletProviderExample {

    private static final CryptoRuntime cryptoRuntime = JavaSoftwareKeys.defaultRuntime();
    private static final SignatureAlgorithm signingAlg =
            JavaSoftwareKeys.toKotlin(JavaSignatureAlgorithm.ecdsa(new JavaDigestAlgorithm("SHA-256"), "DER"));

    private static final JavaX509CertificateUtil walletProviderCertUtil = JavaX509CertificateUtil.configure(
            builder -> builder.addValidators(
                    EtsiWalletProviderX509CertificateProfile.INSTANCE,
                    new X509CertificateValidityValidator(true) // allowValidityInFuture = true
            )
    );

    public static void main(String[] args) {
        Key rootKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "wallet-provider-root");
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

        Key walletProviderKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "wallet-provider");
        X509Certificate walletProviderCert = JavaX509CertificateUtil.getDefault().createCertificate(
                rootKey,
                rootCert,
                signingAlg,
                builder -> EtsiWalletProviderX509CertificateProfile.INSTANCE.profileWalletProviderCertificate(
                        builder,
                        walletProviderKey,                     // subjectKey
                        "CN=Example Wallet Provider,O=Walt.id,OrganizationIdentifier=VATAT-U11223344,C=AT", // subjectDn
                        List.of("0.4.0.194112.1.2"),           // certificatePolicyOids
                        "https://ca.example.com/root.crt",     // caIssuerUri
                        null                                    // ocspResponderUri
                )
        );
        System.out.println("Issued Wallet Provider cert: " + walletProviderCert.getData().getSubjectDn());
        System.out.println(walletProviderCert.getEncodedPem());

        ValidationResult result =
                walletProviderCertUtil.validateCertificateChain(List.of(walletProviderCert), rootCert);
        System.out.println("Wallet Provider cert valid: " + result.getValid());
        for (ValidationResult.ValidationLogEntry entry : result.getLog()) {
            if (entry.getSeverity() == ValidationResult.Severity.ERROR) {
                System.out.println("  ERROR " + entry.getValidatorId() + ": " + entry.getMessage());
            }
        }
    }
}
