package x509

import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.extension.BasicConstraintsExtension.Companion.extensionBasicConstraints
import id.walt.certificate.x509.profile.EtsiWalletProviderX509CertificateProfile
import id.walt.certificate.x509.profile.EtsiWalletProviderX509CertificateProfile.profileEtsiWalletProviderCertificate
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
 * Creating and validating a Wallet Provider certificate - ETSI TS 119 412-6 clause 5.1 (WAL-5.1-01).
 *
 * A Wallet Provider's private key signs Wallet Instance/Unit Attestations (WIA/WUA). Identical in
 * shape to the PID Provider profile ([pidProvider]) - the only difference is the QcType OID
 * carried (id-etsi-qct-wal instead of id-etsi-qct-pid). The self-signed variant demonstrated there
 * works the same way here too.
 */

private val cryptoRuntime = CryptoRuntime(defaultSoftwareKeyProviders())
private val keyGen = GenerateSoftwareKeyRequest(
    id = KeyId("wallet-provider-root"),
    spec = KeySpec.Ec(EcCurve.P256),
    usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
)
private val signingAlg = SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)

private val walletProviderCertUtil = X509CertificateUtil {
    addValidators(
        EtsiWalletProviderX509CertificateProfile,
        X509CertificateValidityValidator(allowValidityInFuture = true)
    )
}

suspend fun main() {
    walletProvider()
}

suspend fun walletProvider() {
    val rootKey = cryptoRuntime.generateSoftwareKey(keyGen)
    val rootCert = X509CertificateUtil.createSelfSignedCertificate(rootKey, signingAlg) {
        subjectDn = "CN=Example Root CA,O=Walt.id,OrganizationIdentifier=VATAT-U12345678,C=AT"
        extensionBasicConstraints { critical = true; cA = true }
    }
    println("Created root CA: ${rootCert.data.subjectDn}")
    println(rootCert.encodedPem)

    val walletProviderKey = cryptoRuntime.generateSoftwareKey(keyGen.copy(id = KeyId("wallet-provider")))
    val walletProviderCert = X509CertificateUtil.createCertificate(rootKey, rootCert, signingAlg) {
        profileEtsiWalletProviderCertificate(
            subjectKey = walletProviderKey,
            subjectDn = "CN=Example Wallet Provider,O=Walt.id,OrganizationIdentifier=VATAT-U11223344,C=AT",
            certificatePolicyOids = listOf("0.4.0.194112.1.2"),
            caIssuerUri = "https://ca.example.com/root.crt",
        )
    }
    println("Issued Wallet Provider cert: ${walletProviderCert.data.subjectDn}")
    println(walletProviderCert.encodedPem)

    val result = walletProviderCertUtil.validateCertificateChain(listOf(walletProviderCert), rootCert)
    println("Wallet Provider cert valid: ${result.valid}")
    result.log
        .filter { it.severity == ValidationResult.Severity.ERROR }
        .forEach { println("  ERROR ${it.validatorId}: ${it.message}") }
}
