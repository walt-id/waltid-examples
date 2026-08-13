package x509

import id.walt.certificate.x509.X509Certificate
import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.extension.AuthorityKeyIdentifierExtension.Companion.extensionAuthorityKeyIdentifier
import id.walt.certificate.x509.extension.KeyUsageExtension
import id.walt.certificate.x509.extension.KeyUsageExtension.Companion.extensionKeyUsage
import id.walt.certificate.x509.extension.SubjectAlternativeNameExtension.Companion.extensionSan
import id.walt.certificate.x509.extension.SubjectKeyIdentifierExtension.Companion.extensionSubjectKeyIdentifier
import id.walt.certificate.x509.truststore.InMemoryTrustStore
import id.walt.crypto2.CryptoRuntime
import id.walt.crypto2.algorithms.DigestAlgorithm
import id.walt.crypto2.algorithms.EcdsaSignatureEncoding
import id.walt.crypto2.algorithms.SignatureAlgorithm
import id.walt.crypto2.keys.*
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest
import id.walt.crypto2.providers.cryptography.defaultSoftwareKeyProviders


private val cryptoRuntime = CryptoRuntime(defaultSoftwareKeyProviders())
private val keyGen = GenerateSoftwareKeyRequest(
    id = KeyId("ca"),
    spec = KeySpec.Ec(EcCurve.P256),
    usages = setOf(KeyUsage.SIGN, KeyUsage.VERIFY)
)
private val certSigningAlg = SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)


suspend fun main() {
    val caKey = cryptoRuntime.generateSoftwareKey(keyGen)
    val caCert = createSelfSignedRootCa(caKey)
    println()
    val leafKey = cryptoRuntime.generateSoftwareKey(keyGen)
    val leafCert = createLeafCertificate(caKey, caCert, leafKey)
    println()
    validateCertificateChain(listOf(leafCert), caCert)
    println()
    //including the root in the chain is not necessary but allowed
    validateCertificateChain(listOf(leafCert, caCert), caCert)
    println()
    //order doesn't matter
    validateCertificateChain(listOf(caCert, leafCert), caCert)
}


private suspend fun createSelfSignedRootCa(key: Key): X509Certificate {
    val cert = X509CertificateUtil.createSelfSignedCertificate(key, certSigningAlg) {
        subjectDn = "cn=My Root, o=Walt.id, c=AT"
        //add key usage constraint
        extensionKeyUsage {
            addKeyUsage(KeyUsageExtension.KeyUsage.digitalSignature, KeyUsageExtension.KeyUsage.keyCertSign)
        }
        //add subject alternative names
        extensionSan {
            addEmail("office@walt.id")
            addUri("https://walt.id")
        }
    }
    println("Created root Ca certificate")
    println("Issuer DN / Subject DN: ${cert.data.issuerDn}")
    println("Subject Key ID: ${cert.data.extensionSubjectKeyIdentifier?.keyIdentifier}")
    println("Fingerprint: ${cert.fingerprintSha256Hex}")
    return cert
}

private suspend fun createLeafCertificate(
    issuerKey: Key,
    issuerCert: X509Certificate,
    subjectKey: Key
): X509Certificate {
    val cert = X509CertificateUtil.createCertificate(issuerKey, issuerCert, certSigningAlg) {
        subjectDn = "cn=My Leaf Certificate, o=Walt.id, c=AT"
        subjectPublicKey(subjectKey)
    }
    println("Created leaf certificate")
    println("Issuer DN (same as caCert DN): ${cert.data.issuerDn}")
    println("Issuer Key ID (same as subject KeyId of parent Cert): ${cert.data.extensionAuthorityKeyIdentifier?.keyIdentifier}")
    println("Subject DN: ${cert.data.subjectDn}")
    println("Fingerprint: ${cert.fingerprintSha256Hex}")
    val publicKey = cert.restoreSubjectPublicKey(cryptoRuntime)
    return cert
}

private suspend fun validateCertificateChain(chain: List<X509Certificate>, trustRoot: X509Certificate) {
    val trustStore = InMemoryTrustStore(listOf(trustRoot))
    val validationResult = X509CertificateUtil.validateCertificateChain(chain, trustStore)
    println("Validation result - isValid: ${validationResult.valid}")
    println("Validation result - log:")
    validationResult.log.forEach { println("${it.severity} ${it.subjectDn}/${it.validatorId}: '${it.message}'") }
}