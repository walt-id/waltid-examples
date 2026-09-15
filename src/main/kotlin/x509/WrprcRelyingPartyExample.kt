package x509

import id.walt.certificate.x509.X509Certificate
import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.extension.BasicConstraintsExtension.Companion.extensionBasicConstraints
import id.walt.certificate.x509.profile.EtsiWrprcX509CertificateProfile
import id.walt.certificate.x509.profile.EtsiWrprcX509CertificateProfile.profileWrpRegistrationCertificate
import id.walt.certificate.x509.validation.validator.X509CertificateValidityValidator
import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.DigestAlgorithm
import id.walt.crypto2.algorithms.EcdsaSignatureEncoding
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.*
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.defaultSoftwareKeyProviders

/**
 * Creating and validating a Wallet Relying Party Registration Certificate (WRPRC) - ETSI TS 119 475.
 *
 * [EtsiWrprcX509CertificateProfile] is a draft. Unlike every other profile shown in these
 * examples, there is no reference implementation to cross-check WRPRC against, and the encoding
 * of its defining feature - the Relying Party's *registered intended use* (what attribute scope
 * it's authorized to request, and why) - isn't confirmed; it likely needs a new custom X.509
 * extension. This example only exercises the baseline end-entity certificate shape the profile
 * currently implements, and deliberately prints the full validation log, including the WARNING
 * [EtsiWrprcX509CertificateProfile.validate] always emits, to make that gap visible rather than
 * silently passing a certificate a real WRPRC issuer might reject.
 */

private val cryptoRuntime = CryptoRuntime(defaultSoftwareKeyProviders())
private val keyGen = GenerateSoftwareKeyRequest(
    id = KeyId("wrprc-root"),
    spec = KeySpec.Ec(EcCurve.P256),
    usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
)
private val signingAlg = SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)

private val wrprcCertUtil = X509CertificateUtil {
    addValidators(
        EtsiWrprcX509CertificateProfile,
        X509CertificateValidityValidator(allowValidityInFuture = true)
    )
}

suspend fun main() {
    wrprcRelyingParty()
}

suspend fun wrprcRelyingParty() {
    val rootKey = cryptoRuntime.generateSoftwareKey(keyGen)
    val rootCert = createRootCa(rootKey)
    println()

    val relyingPartyKey = cryptoRuntime.generateSoftwareKey(keyGen.copy(id = KeyId("wrprc-subject")))
    val wrprcCert = X509CertificateUtil.createCertificate(rootKey, rootCert, signingAlg) {
        profileWrpRegistrationCertificate(
            subjectKey = relyingPartyKey,
            subjectDn = "CN=Example Relying Party,O=Walt.id,OrganizationIdentifier=VATAT-U55667788,C=AT",
            certificatePolicyOids = listOf("0.4.0.194118.1.2"), // WRPRC-specific OIDs are unconfirmed
            caIssuerUri = "https://ca.example.com/root.crt",
        )
    }
    println("Issued WRPRC: ${wrprcCert.data.subjectDn}")

    val result = wrprcCertUtil.validateCertificateChain(listOf(wrprcCert), rootCert)
    println("WRPRC valid: ${result.valid}")
    println("Validation log:")
    result.log.forEach { println("  ${it.severity} ${it.validatorId}: ${it.message}") }
}

private suspend fun createRootCa(key: Key): X509Certificate {
    val cert = X509CertificateUtil.createSelfSignedCertificate(key, signingAlg) {
        subjectDn = "CN=Example WRPRC Registrar,O=Walt.id,OrganizationIdentifier=VATAT-U99999999,C=AT"
        extensionBasicConstraints { critical = true; cA = true }
    }
    println("Created WRPRC registrar root CA: ${cert.data.subjectDn}")
    return cert
}
