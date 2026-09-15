package waltid.x509;

import java.util.List;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.extension.BasicConstraintsExtension;
import id.walt.certificate.x509.profile.Etsi119411Part8;
import id.walt.certificate.x509.profile.EtsiWrpacX509CertificateProfile;
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
 * Creating and validating a Wallet Relying Party Access Certificate (WRPAC) - ETSI TS 119 411-8.
 *
 * <p>A Relying Party uses its WRPAC private key to authenticate a presentation request, so the
 * wallet can confirm it's talking to a registered Relying Party before releasing any credential.
 * This example issues both a legal-person qualified (QCP-l) and a natural-person non-qualified
 * (NCP-n) certificate, to show the two ends of the four policy variants
 * {@link Etsi119411Part8} defines - see {@link EtsiWrpacX509CertificateProfile} for the full
 * requirement set and what's deliberately not implemented yet (the short-term/no-revocation
 * exemption, telephone contact info).
 */
public class WrpacRelyingPartyExample {

    private static final CryptoRuntime cryptoRuntime = JavaSoftwareKeys.defaultRuntime();
    private static final SignatureAlgorithm signingAlg =
            JavaSoftwareKeys.toKotlin(JavaSignatureAlgorithm.ecdsa(new JavaDigestAlgorithm("SHA-256"), "DER"));

    private static final JavaX509CertificateUtil wrpacCertUtil = JavaX509CertificateUtil.configure(
            builder -> builder.addValidators(
                    EtsiWrpacX509CertificateProfile.INSTANCE,
                    new X509CertificateValidityValidator(true) // allowValidityInFuture = true
            )
    );

    public static void main(String[] args) {
        Key rootKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "wrpac-root");
        X509Certificate rootCert = createRootCa(rootKey);
        System.out.println();

        issueAndValidate(
                rootKey, rootCert,
                "CN=Example Relying Party,O=Walt.id,OrganizationIdentifier=VATAT-U55667788,C=AT",
                Etsi119411Part8.QCP_L_EUDIWRP,
                null, "relying-party@example.com"
        );
        System.out.println();

        issueAndValidate(
                rootKey, rootCert,
                "CN=Jane Doe,GivenName=Jane,Surname=Doe,SerialNumber=RP-12345,C=AT",
                Etsi119411Part8.NCP_N_EUDIWRP,
                "https://relying-party.example.com/contact", null
        );
    }

    private static X509Certificate createRootCa(Key key) {
        X509Certificate cert = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                key,
                signingAlg,
                builder -> {
                    builder.setSubjectDn("CN=Example WRPAC Root CA,O=Walt.id,OrganizationIdentifier=VATAT-U99999999,C=AT");
                    BasicConstraintsExtension.Companion.extensionBasicConstraints(builder, bc -> {
                        bc.setCritical(true);
                        bc.setCA(true);
                        return Unit.INSTANCE;
                    });
                }
        );
        System.out.println("Created WRPAC root CA: " + cert.getData().getSubjectDn());
        return cert;
    }

    private static void issueAndValidate(
            Key rootKey,
            X509Certificate rootCert,
            String subjectDn,
            String policyOid,
            String contactUri,
            String contactEmail
    ) {
        Key relyingPartyKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "wrpac-" + subjectDn.hashCode());
        X509Certificate wrpacCert = JavaX509CertificateUtil.getDefault().createCertificate(
                rootKey,
                rootCert,
                signingAlg,
                builder -> EtsiWrpacX509CertificateProfile.INSTANCE.profileWrpAccessCertificate(
                        builder,
                        relyingPartyKey,               // subjectKey
                        subjectDn,                      // subjectDn
                        policyOid,                       // policyOid
                        contactUri,                      // contactUri
                        contactEmail,                    // contactEmail
                        "https://ca.example.com/root.crt", // caIssuerUri
                        null,                            // ocspResponderUri
                        "https://ca.example.com/crl"     // crlDistributionPointUri
                )
        );
        System.out.println("Issued WRPAC (" + policyOid + "): " + wrpacCert.getData().getSubjectDn());

        ValidationResult result = wrpacCertUtil.validateCertificateChain(List.of(wrpacCert), rootCert);
        System.out.println("WRPAC valid: " + result.getValid());
        for (ValidationResult.ValidationLogEntry entry : result.getLog()) {
            if (entry.getSeverity() == ValidationResult.Severity.ERROR) {
                System.out.println("  ERROR " + entry.getValidatorId() + ": " + entry.getMessage());
            }
        }
    }
}
