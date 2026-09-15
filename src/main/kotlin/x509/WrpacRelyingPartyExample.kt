package x509

import id.walt.certificate.x509.X509Certificate
import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.extension.BasicConstraintsExtension.Companion.extensionBasicConstraints
import id.walt.certificate.x509.profile.Etsi119411Part8
import id.walt.certificate.x509.profile.EtsiWrpacX509CertificateProfile
import id.walt.certificate.x509.profile.EtsiWrpacX509CertificateProfile.profileWrpAccessCertificate
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
 * Creating and validating a Wallet Relying Party Access Certificate (WRPAC) - ETSI TS 119 411-8.
 *
 * A Relying Party uses its WRPAC private key to authenticate a presentation request, so the
 * wallet can confirm it's talking to a registered Relying Party before releasing any credential.
 * This example issues both a legal-person qualified (QCP-l) and a natural-person non-qualified
 * (NCP-n) certificate, to show the two ends of the four policy variants [Etsi119411Part8]
 * defines - see [EtsiWrpacX509CertificateProfile] for the full requirement set and what's
 * deliberately not implemented yet (the short-term/no-revocation exemption, telephone contact
 * info).
 */

private val cryptoRuntime = CryptoRuntime(defaultSoftwareKeyProviders())
private val keyGen = GenerateSoftwareKeyRequest(
    id = KeyId("wrpac-root"),
    spec = KeySpec.Ec(EcCurve.P256),
    usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
)
private val signingAlg = SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)

private val wrpacCertUtil = X509CertificateUtil {
    addValidators(
        EtsiWrpacX509CertificateProfile,
        X509CertificateValidityValidator(allowValidityInFuture = true)
    )
}

suspend fun main() {
    wrpacRelyingParty()
}

suspend fun wrpacRelyingParty() {
    val rootKey = cryptoRuntime.generateSoftwareKey(keyGen)
    val rootCert = createRootCa(rootKey)
    println()

    issueAndValidate(
        rootKey, rootCert,
        keyIdSuffix = "qcp-l",
        subjectDn = "CN=Example Relying Party,O=Walt.id,OrganizationIdentifier=VATAT-U55667788,C=AT",
        policyOid = Etsi119411Part8.QCP_L_EUDIWRP,
        contactUri = null,
        contactEmail = "relying-party@example.com",
    )
    println()

    issueAndValidate(
        rootKey, rootCert,
        keyIdSuffix = "ncp-n",
        subjectDn = "CN=Jane Doe,GivenName=Jane,Surname=Doe,SerialNumber=RP-12345,C=AT",
        policyOid = Etsi119411Part8.NCP_N_EUDIWRP,
        contactUri = "https://relying-party.example.com/contact",
        contactEmail = null,
    )
}

private suspend fun createRootCa(key: Key): X509Certificate {
    val cert = X509CertificateUtil.createSelfSignedCertificate(key, signingAlg) {
        subjectDn = "CN=Example WRPAC Root CA,O=Walt.id,OrganizationIdentifier=VATAT-U99999999,C=AT"
        extensionBasicConstraints { critical = true; cA = true }
    }
    println("Created WRPAC root CA: ${cert.data.subjectDn}")
    return cert
}

private suspend fun issueAndValidate(
    rootKey: Key,
    rootCert: X509Certificate,
    keyIdSuffix: String,
    subjectDn: String,
    policyOid: String,
    contactUri: String?,
    contactEmail: String?,
) {
    val relyingPartyKey = cryptoRuntime.generateSoftwareKey(keyGen.copy(id = KeyId("wrpac-$keyIdSuffix")))
    val wrpacCert = X509CertificateUtil.createCertificate(rootKey, rootCert, signingAlg) {
        profileWrpAccessCertificate(
            subjectKey = relyingPartyKey,
            subjectDn = subjectDn,
            policyOid = policyOid,
            contactUri = contactUri,
            contactEmail = contactEmail,
            caIssuerUri = "https://ca.example.com/root.crt",
            crlDistributionPointUri = "https://ca.example.com/crl",
        )
    }
    println("Issued WRPAC ($policyOid): ${wrpacCert.data.subjectDn}")

    val result = wrpacCertUtil.validateCertificateChain(listOf(wrpacCert), rootCert)
    println("WRPAC valid: ${result.valid}")
    result.log
        .filter { it.severity == ValidationResult.Severity.ERROR }
        .forEach { println("  ERROR ${it.validatorId}: ${it.message}") }
}
