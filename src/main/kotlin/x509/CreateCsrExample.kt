package x509

import id.walt.certificate.x509.Pkcs10CertificateSigningRequest
import id.walt.certificate.x509.X509Certificate
import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.extension.SubjectAlternativeNameExtension.Companion.extensionSan
import id.walt.certificate.x509.truststore.InMemoryTrustStore
import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.DigestAlgorithm
import id.walt.crypto2.algorithms.EcdsaSignatureEncoding
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.*
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.defaultSoftwareKeyProviders


private val cryptoRuntime = CryptoRuntime(defaultSoftwareKeyProviders())
private val certSigningAlg = SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)

private fun keyGenRequest(id: String) = GenerateSoftwareKeyRequest(
    id = KeyId(id),
    spec = KeySpec.Ec(EcCurve.P256),
    usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
)

suspend fun main() {
    createCsr()
}

suspend fun createCsr() {
    val leafKey = cryptoRuntime.generateSoftwareKey(keyGenRequest("leaf"))
    val csr = createCsr(leafKey)
    println()

    // A CA typically only sees the PEM-encoded CSR; parse it back and check the CSR is
    // genuinely signed by the key it claims to be for, before issuing anything from it.
    val parsedCsr = X509CertificateUtil.parseCsrPem(csr.encodedPem)
    println("Parsed CSR signature valid: ${X509CertificateUtil.validateCsrSignature(parsedCsr)}")
    println()

    val caKey = cryptoRuntime.generateSoftwareKey(keyGenRequest("ca"))
    val caCert = createSelfSignedRootCa(caKey)
    println()
    val leafCert = issueCertificateFromCsr(caKey, caCert, parsedCsr, leafKey)
    println()
    validateCertificateChain(leafCert, caCert)
}

private suspend fun createCsr(subjectKey: Key): Pkcs10CertificateSigningRequest {
    val csr = X509CertificateUtil.createCsr(subjectKey, certSigningAlg) {
        requestedCertificate.apply {
            subjectDn = "cn=My Leaf Certificate, o=Walt.id, c=AT"
            extensionSan {
                addDnsName("leaf.walt.id")
                addEmail("example@walt.id")
            }
        }
    }
    println("Created CSR")
    println("Subject DN: ${csr.requestedCertificate.subjectDn}")
    println(csr.encodedPem)
    return csr
}

private suspend fun createSelfSignedRootCa(key: Key): X509Certificate {
    val cert = X509CertificateUtil.createSelfSignedCertificate(key, certSigningAlg) {
        subjectDn = "cn=My Root, o=Walt.id, c=AT"
    }
    println("Created root Ca certificate")
    println("Subject DN: ${cert.data.subjectDn}")
    return cert
}

private suspend fun issueCertificateFromCsr(
    issuerKey: Key,
    issuerCert: X509Certificate,
    csr: Pkcs10CertificateSigningRequest,
    subjectKey: Key,
): X509Certificate {
    val requested = csr.requestedCertificate
    val cert = X509CertificateUtil.createCertificate(issuerKey, issuerCert, certSigningAlg) {
        subjectDn = requested.subjectDn
        // The crypto2 Key-based issuer path signs the subject public key it is given directly
        // rather than one reconstructed from the CSR's SubjectPublicKeyInfo (that reconstruction
        // path only exists for the deprecated legacy-Key issuer overload) - since the CA in this
        // example already holds the subject's crypto2 key, pass it in here.
        subjectPublicKey(subjectKey)
        // carry the CSR's requested extensions (e.g. subject alternative names) onto the issued certificate
        extensions.putAll(requested.extensions)
    }
    println("Issued leaf certificate from CSR")
    println("Subject DN: ${cert.data.subjectDn}")
    println("Fingerprint: ${cert.fingerprintSha256Hex}")
    return cert
}

private suspend fun validateCertificateChain(leafCert: X509Certificate, trustRoot: X509Certificate) {
    val trustStore = InMemoryTrustStore(listOf(trustRoot))
    val validationResult = X509CertificateUtil.validateCertificateChain(listOf(leafCert), trustStore)
    println("Validation result - isValid: ${validationResult.valid}")
}
