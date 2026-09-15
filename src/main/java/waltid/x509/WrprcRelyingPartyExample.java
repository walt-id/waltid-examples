package waltid.x509;

import java.util.List;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.extension.BasicConstraintsExtension;
import id.walt.certificate.x509.profile.EtsiWrprcX509CertificateProfile;
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
 * Creating and validating a Wallet Relying Party Registration Certificate (WRPRC) - ETSI TS 119 475.
 *
 * <p><b>{@link EtsiWrprcX509CertificateProfile} is a draft.</b> Unlike every other profile shown in
 * these examples, there is no reference implementation to cross-check WRPRC against, and the
 * encoding of its defining feature - the Relying Party's <em>registered intended use</em> (what
 * attribute scope it's authorized to request, and why) - isn't confirmed; it likely needs a new
 * custom X.509 extension. This example only exercises the baseline end-entity certificate shape
 * the profile currently implements, and deliberately prints the {@code WARNING} log entry
 * {@link EtsiWrprcX509CertificateProfile#validate} always emits, to make that gap visible rather
 * than silently passing a certificate a real WRPRC issuer might reject.
 */
public class WrprcRelyingPartyExample {

    private static final CryptoRuntime cryptoRuntime = JavaSoftwareKeys.defaultRuntime();
    private static final SignatureAlgorithm signingAlg =
            JavaSoftwareKeys.toKotlin(JavaSignatureAlgorithm.ecdsa(new JavaDigestAlgorithm("SHA-256"), "DER"));

    private static final JavaX509CertificateUtil wrprcCertUtil = JavaX509CertificateUtil.configure(
            builder -> builder.addValidators(
                    EtsiWrprcX509CertificateProfile.INSTANCE,
                    new X509CertificateValidityValidator(true) // allowValidityInFuture = true
            )
    );

    public static void main(String[] args) {
        Key rootKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "wrprc-root");
        X509Certificate rootCert = createRootCa(rootKey);
        System.out.println();

        Key relyingPartyKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "wrprc-subject");
        X509Certificate wrprcCert = JavaX509CertificateUtil.getDefault().createCertificate(
                rootKey,
                rootCert,
                signingAlg,
                builder -> EtsiWrprcX509CertificateProfile.INSTANCE.profileWrpRegistrationCertificate(
                        builder,
                        relyingPartyKey,                     // subjectKey
                        "CN=Example Relying Party,O=Walt.id,OrganizationIdentifier=VATAT-U55667788,C=AT", // subjectDn
                        List.of("0.4.0.194118.1.2"),          // certificatePolicyOids - WRPRC-specific OIDs unconfirmed
                        "https://ca.example.com/root.crt",    // caIssuerUri
                        null                                   // ocspResponderUri
                )
        );
        System.out.println("Issued WRPRC: " + wrprcCert.getData().getSubjectDn());

        ValidationResult result = wrprcCertUtil.validateCertificateChain(List.of(wrprcCert), rootCert);
        System.out.println("WRPRC valid: " + result.getValid());
        System.out.println("Validation log:");
        for (ValidationResult.ValidationLogEntry entry : result.getLog()) {
            System.out.println("  " + entry.getSeverity() + " " + entry.getValidatorId() + ": " + entry.getMessage());
        }
    }

    private static X509Certificate createRootCa(Key key) {
        X509Certificate cert = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                key,
                signingAlg,
                builder -> {
                    builder.setSubjectDn("CN=Example WRPRC Registrar,O=Walt.id,OrganizationIdentifier=VATAT-U99999999,C=AT");
                    BasicConstraintsExtension.Companion.extensionBasicConstraints(builder, bc -> {
                        bc.setCritical(true);
                        bc.setCA(true);
                        return Unit.INSTANCE;
                    });
                }
        );
        System.out.println("Created WRPRC registrar root CA: " + cert.getData().getSubjectDn());
        return cert;
    }
}
