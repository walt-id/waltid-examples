package x509

import id.walt.certificate.x509.X509Certificate
import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.profile.IsoDocumentSignerX509CertificateProfile
import id.walt.certificate.x509.profile.IsoDocumentSignerX509CertificateProfile.profileDocumentSignerCertificate
import id.walt.certificate.x509.profile.IsoIaCaRootX509CertificateProfile
import id.walt.certificate.x509.profile.IsoIaCaRootX509CertificateProfile.profileIaCaRootCertificate
import id.walt.certificate.x509.validation.ValidationResult
import id.walt.certificate.x509.validation.validator.X509CertificateBasicConstraintsValidator
import id.walt.certificate.x509.validation.validator.X509CertificateValidityValidator
import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.DigestAlgorithm
import id.walt.crypto2.algorithms.EcdsaSignatureEncoding
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.*
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.defaultSoftwareKeyProviders

/**
 * Building an ISO/IEC 18013-5 (mDL) IACA root and Document Signer certificate using the
 * ISO profile helpers/validators, and why a caller-supplied "root" certificate must always be
 * validated against the IACA root profile before it's trusted as a signing anchor for a Document
 * Signer certificate - it's not enough for the Document Signer certificate itself to be
 * profile-compliant and correctly signed by the root's key, since that alone doesn't guarantee the
 * root itself satisfies the constraints (CA=true, pathLenConstraint=0, restricted keyUsage,
 * mandatory issuer-alt-name, ...) that make it trustworthy as an IACA root in the first place.
 */

private val cryptoRuntime = CryptoRuntime(defaultSoftwareKeyProviders())
private val keyGen = GenerateSoftwareKeyRequest(
    id = KeyId("iaca"),
    spec = KeySpec.Ec(EcCurve.P256),
    usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
)
private val signingAlg = SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)

// A util for validating a certificate presented as an IACA root, before trusting it as an anchor
private val iaCaRootCertUtil = X509CertificateUtil {
    addValidators(
        IsoIaCaRootX509CertificateProfile,
        X509CertificateBasicConstraintsValidator(leafCanBeCa = true),
        X509CertificateValidityValidator(allowValidityInFuture = true)
    )
}

// A util for validating a Document Signer certificate against its (already-trusted) IACA root
private val documentSignerCertUtil = X509CertificateUtil {
    addValidators(
        IsoDocumentSignerX509CertificateProfile,
        X509CertificateValidityValidator(allowValidityInFuture = true)
    )
}

private data class RootCertificate(val cert: X509Certificate, val key: Key)

suspend fun main() {
    isoMdlOnboarding()
}

suspend fun isoMdlOnboarding() {
    val validRoot = createIaCaRoot()
    println()
    issueDocumentSignerUnder(validRoot, "a genuine, profile-compliant IACA root")
    println()

    // A "root" that isn't profile-compliant, but whose key can still be used to sign a perfectly
    // valid-looking Document Signer certificate - e.g. it's missing the mandatory issuer-alt-name
    // extension, isn't marked as a CA, and has no key usage restriction at all.
    val notARealRoot = createNonCompliantSelfSignedCertificate()
    println()
    issueDocumentSignerUnder(notARealRoot, "an IACA root that is NOT profile-compliant")
}

private suspend fun createIaCaRoot(): RootCertificate {
    val key = cryptoRuntime.generateSoftwareKey(keyGen)
    val root = X509CertificateUtil.createSelfSignedCertificate(key, signingAlg) {
        profileIaCaRootCertificate(
            issuerDnCountryCode = "AT",
            issuerDnOrganizationName = "Walt.id",
            issuerDnCommonName = "Walt ID IACA Root",
            issuerEmailAddress = "example@walt.id",
        )
    }
    println("Created IACA root: ${root.data.subjectDn}")
    return RootCertificate(root, key)
}

private suspend fun createNonCompliantSelfSignedCertificate(): RootCertificate {
    val key = cryptoRuntime.generateSoftwareKey(keyGen.copy(id = KeyId("not-a-real-root")))
    val cert = X509CertificateUtil.createSelfSignedCertificate(key, signingAlg) {
        subjectDn = "CN=Not A Real IACA Root, O=Walt.id, C=AT"
    }
    println("Created certificate (not built via the IACA profile helper): ${cert.data.subjectDn}")
    return RootCertificate(cert, key)
}

/**
 * Mirrors OnboardingService.onboardDocumentSigner(): validate the caller-supplied "root" against
 * the IACA root profile *before* using it as a trust anchor to sign/validate a Document Signer
 * certificate. Skipping this check would let anyone who controls a root's private key get a
 * Document Signer certificate issued "under" a root that was never actually a valid IACA root.
 */
private suspend fun issueDocumentSignerUnder(root: RootCertificate, description: String) {
    println("Attempting to onboard a Document Signer under $description")

    val rootValidationResult = iaCaRootCertUtil.validateCertificateChain(listOf(root.cert), root.cert)
    if (!rootValidationResult.valid) {
        println("Rejected: the supplied root is not IACA-profile-compliant, refusing to sign a Document Signer under it.")
        rootValidationResult.log
            .filter { it.severity == ValidationResult.Severity.ERROR }
            .forEach { println("  ERROR ${it.validatorId}: ${it.message}") }
        return
    }

    val documentSignerKey = cryptoRuntime.generateSoftwareKey(keyGen.copy(id = KeyId("document-signer")))
    val documentSignerCert = X509CertificateUtil.createCertificate(root.key, root.cert, signingAlg) {
        profileDocumentSignerCertificate(
            crlDistributionPointUri = "https://crl.walt.id/crl.der",
            issuerEmailAddress = "example@walt.id",
            subjectKey = documentSignerKey,
            subjectDnCountryCode = "AT",
            subjectDnOrganizationName = "Walt.id",
            subjectDnCommonName = "Walt ID mDL DS",
        )
    }

    val validationResult = documentSignerCertUtil.validateCertificateChain(listOf(documentSignerCert), root.cert)
    println("Document Signer issued and profile-compliant: ${validationResult.valid}")
}
