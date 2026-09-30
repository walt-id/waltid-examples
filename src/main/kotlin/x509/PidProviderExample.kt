package x509

import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.extension.BasicConstraintsExtension.Companion.extensionBasicConstraints
import id.walt.certificate.x509.profile.EtsiPidProviderX509CertificateProfile
import id.walt.certificate.x509.profile.EtsiPidProviderX509CertificateProfile.profileEtsiPidProviderCertificate
import id.walt.certificate.x509.validation.ValidationResult
import id.walt.certificate.x509.validation.validator.X509CertificateValidityValidator
import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.DigestAlgorithm
import id.walt.crypto2.algorithms.EcdsaSignatureEncoding
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.*
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.defaultSoftwareKeyProviders

/**
 * Creating and validating PID Provider certificates - ETSI TS 119 412-6 clause 4.
 *
 * A PID Provider's private key signs EUDI Wallet PID attestations (SD-JWT VC / mdoc). This
 * example shows both shapes a PID Provider certificate can take: issued under a separate CA, and
 * self-signed - a LoTE (ETSI TS 119 602) PID Provider list accepts either an end-entity or a CA
 * certificate as its trust anchor, so both are valid.
 */

private val cryptoRuntime = CryptoRuntime(defaultSoftwareKeyProviders())
private val keyGen = GenerateSoftwareKeyRequest(
    id = KeyId("pid-provider-root"),
    spec = KeySpec.Ec(EcCurve.P256),
    usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
)
private val signingAlg = SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)

private val pidProviderCertUtil = X509CertificateUtil {
    addValidators(
        EtsiPidProviderX509CertificateProfile,
        X509CertificateValidityValidator(allowValidityInFuture = true)
    )
}

suspend fun main() {
    pidProvider()
}

suspend fun pidProvider() {
    issueCaIssuedCertificate()
    println()
    issueSelfSignedCertificate()
}

private suspend fun issueCaIssuedCertificate() {
    val rootKey = cryptoRuntime.generateSoftwareKey(keyGen)
    val rootCert = X509CertificateUtil.createSelfSignedCertificate(rootKey, signingAlg) {
        subjectDn = "CN=Example Root CA,O=Walt.id,OrganizationIdentifier=VATAT-U12345678,C=AT"
        extensionBasicConstraints { critical = true; cA = true }
    }
    println("Created root CA: ${rootCert.data.subjectDn}")
    println(rootCert.encodedPem)

    val pidProviderKey = cryptoRuntime.generateSoftwareKey(keyGen.copy(id = KeyId("pid-provider-ca-issued")))
    val pidProviderCert = X509CertificateUtil.createCertificate(rootKey, rootCert, signingAlg) {
        profileEtsiPidProviderCertificate(
            subjectKey = pidProviderKey,
            subjectDn = "CN=Example PID Provider,O=Walt.id,OrganizationIdentifier=VATAT-U87654321,C=AT",
            certificatePolicyOids = listOf("0.4.0.194112.1.1"),
            caIssuerUri = "https://ca.example.com/root.crt",
        )
    }
    println("Issued CA-issued PID Provider cert: ${pidProviderCert.data.subjectDn}")
    println(pidProviderCert.encodedPem)

    val result = pidProviderCertUtil.validateCertificateChain(listOf(pidProviderCert), rootCert)
    println("CA-issued PID Provider cert valid: ${result.valid}")
    result.log
        .filter { it.severity == ValidationResult.Severity.ERROR }
        .forEach { println("  ERROR ${it.validatorId}: ${it.message}") }
}

private suspend fun issueSelfSignedCertificate() {
    val pidProviderKey = cryptoRuntime.generateSoftwareKey(keyGen.copy(id = KeyId("pid-provider-self-signed")))
    val pidProviderCert = X509CertificateUtil.createSelfSignedCertificate(pidProviderKey, signingAlg) {
        profileEtsiPidProviderCertificate(
            subjectDn = "CN=Example Self-Signed PID Provider,O=Walt.id,OrganizationIdentifier=VATAT-U11223344,C=AT",
            certificatePolicyOids = listOf("0.4.0.194112.1.1"),
        )
    }
    println("Issued self-signed PID Provider cert: ${pidProviderCert.data.subjectDn}")
    println(pidProviderCert.encodedPem)

    val result = pidProviderCertUtil.validateCertificateChain(listOf(pidProviderCert), pidProviderCert)
    println("Self-signed PID Provider cert valid: ${result.valid}")
    result.log
        .filter { it.severity == ValidationResult.Severity.ERROR }
        .forEach { println("  ERROR ${it.validatorId}: ${it.message}") }
}
