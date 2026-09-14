package waltid.x509;

import java.util.List;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.profile.IsoDocumentSignerX509CertificateProfile;
import id.walt.certificate.x509.profile.IsoIaCaRootX509CertificateProfile;
import id.walt.certificate.x509.validation.ValidationResult;
import id.walt.certificate.x509.validation.validator.X509CertificateBasicConstraintsValidator;
import id.walt.certificate.x509.validation.validator.X509CertificateValidityValidator;
import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.algorithms.SignatureAlgorithm;
import id.walt.crypto2.keys.Key;

import static waltid.x509.Crypto2Support.ecP256;
import static waltid.x509.Crypto2Support.ecdsaSha256Der;
import static waltid.x509.Crypto2Support.generateSoftwareKey;
import static waltid.x509.Crypto2Support.softwareKeyRequest;

/**
 * Java port of {@code IsoMdlOnboardingExample.kt}.
 *
 * <p>Building an ISO/IEC 18013-5 (mDL) IACA root and Document Signer certificate using the
 * ISO profile helpers/validators, and why a caller-supplied "root" certificate must always be
 * validated against the IACA root profile before it's trusted as a signing anchor for a Document
 * Signer certificate - it's not enough for the Document Signer certificate itself to be
 * profile-compliant and correctly signed by the root's key, since that alone doesn't guarantee the
 * root itself satisfies the constraints (CA=true, pathLenConstraint=0, restricted keyUsage,
 * mandatory issuer-alt-name, ...) that make it trustworthy as an IACA root in the first place.
 */
public class IsoMdlOnboardingExample {

    private static final CryptoRuntime cryptoRuntime = Crypto2Support.defaultRuntime();
    private static final SignatureAlgorithm signingAlg = ecdsaSha256Der();

    // A util for validating a certificate presented as an IACA root, before trusting it as an anchor
    private static final JavaX509CertificateUtil iaCaRootCertUtil = JavaX509CertificateUtil.configure(
            builder -> builder.addValidators(
                    IsoIaCaRootX509CertificateProfile.INSTANCE,
                    new X509CertificateBasicConstraintsValidator(true), // leafCanBeCa = true
                    new X509CertificateValidityValidator(true)          // allowValidityInFuture = true
            )
    );

    // A util for validating a Document Signer certificate against its (already-trusted) IACA root
    private static final JavaX509CertificateUtil documentSignerCertUtil = JavaX509CertificateUtil.configure(
            builder -> builder.addValidators(
                    IsoDocumentSignerX509CertificateProfile.INSTANCE,
                    new X509CertificateValidityValidator(true)          // allowValidityInFuture = true
            )
    );

    private record RootCertificate(X509Certificate cert, Key key) {
    }

    public static void main(String[] args) {
        RootCertificate validRoot = createIaCaRoot();
        System.out.println();
        issueDocumentSignerUnder(validRoot, "a genuine, profile-compliant IACA root");
        System.out.println();

        // A "root" that isn't profile-compliant, but whose key can still be used to sign a perfectly
        // valid-looking Document Signer certificate - e.g. it's missing the mandatory issuer-alt-name
        // extension, isn't marked as a CA, and has no key usage restriction at all.
        RootCertificate notARealRoot = createNonCompliantSelfSignedCertificate();
        System.out.println();
        issueDocumentSignerUnder(notARealRoot, "an IACA root that is NOT profile-compliant");
    }

    private static RootCertificate createIaCaRoot() {
        Key key = generateSoftwareKey(cryptoRuntime, softwareKeyRequest("iaca", ecP256()));
        X509Certificate root = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                key,
                signingAlg,
                builder -> IsoIaCaRootX509CertificateProfile.INSTANCE.profileIaCaRootCertificate(
                        builder,
                        "AT",                 // issuerDnCountryCode
                        null,                 // issuerDnStateOrProvinceName
                        "Walt.id",            // issuerDnOrganizationName
                        "Walt ID IACA Root",  // issuerDnCommonName
                        null,                 // issuerDnSerialNumber
                        "example@walt.id",    // issuerEmailAddress
                        null                  // issuerUri
                )
        );
        System.out.println("Created IACA root: " + root.getData().getSubjectDn());
        return new RootCertificate(root, key);
    }

    private static RootCertificate createNonCompliantSelfSignedCertificate() {
        Key key = generateSoftwareKey(cryptoRuntime, softwareKeyRequest("not-a-real-root", ecP256()));
        X509Certificate cert = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                key,
                signingAlg,
                builder -> builder.setSubjectDn("CN=Not A Real IACA Root, O=Walt.id, C=AT")
        );
        System.out.println("Created certificate (not built via the IACA profile helper): "
                + cert.getData().getSubjectDn());
        return new RootCertificate(cert, key);
    }

    /**
     * Mirrors {@code OnboardingService.onboardDocumentSigner()}: validate the caller-supplied "root"
     * against the IACA root profile <em>before</em> using it as a trust anchor to sign/validate a
     * Document Signer certificate. Skipping this check would let anyone who controls a root's private
     * key get a Document Signer certificate issued "under" a root that was never actually a valid
     * IACA root.
     */
    private static void issueDocumentSignerUnder(RootCertificate root, String description) {
        System.out.println("Attempting to onboard a Document Signer under " + description);

        ValidationResult rootValidationResult =
                iaCaRootCertUtil.validateCertificateChain(List.of(root.cert()), root.cert());
        if (!rootValidationResult.getValid()) {
            System.out.println("Rejected: the supplied root is not IACA-profile-compliant, "
                    + "refusing to sign a Document Signer under it.");
            for (ValidationResult.ValidationLogEntry entry : rootValidationResult.getLog()) {
                if (entry.getSeverity() == ValidationResult.Severity.ERROR) {
                    System.out.println("  ERROR " + entry.getValidatorId() + ": " + entry.getMessage());
                }
            }
            return;
        }

        Key documentSignerKey = generateSoftwareKey(cryptoRuntime, softwareKeyRequest("document-signer", ecP256()));
        X509Certificate documentSignerCert = JavaX509CertificateUtil.getDefault().createCertificate(
                root.key(),
                root.cert(),
                signingAlg,
                builder -> IsoDocumentSignerX509CertificateProfile.INSTANCE.profileDocumentSignerCertificate(
                        builder,
                        "https://crl.walt.id/crl.der", // crlDistributionPointUri
                        "example@walt.id",             // issuerEmailAddress
                        null,                          // issuerUri
                        documentSignerKey,             // subjectKey
                        "AT",                          // subjectDnCountryCode
                        null,                          // subjectDnStateOrProvinceName
                        null,                          // subjectDnLocalityName
                        "Walt.id",                     // subjectDnOrganizationName
                        "Walt ID mDL DS",              // subjectDnCommonName
                        null                           // subjectDnSerialNumber
                )
        );

        ValidationResult validationResult =
                documentSignerCertUtil.validateCertificateChain(List.of(documentSignerCert), root.cert());
        System.out.println("Document Signer issued and profile-compliant: " + validationResult.getValid());
    }
}
