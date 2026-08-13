package x509

import id.walt.certificate.x509.X509Certificate
import id.walt.certificate.x509.X509CertificateTrustStore
import id.walt.certificate.x509.X509CertificateUtil
import id.walt.certificate.x509.truststore.CompositeTrustStore
import id.walt.certificate.x509.truststore.InMemoryTrustStore

val rootCertPem = """
-----BEGIN CERTIFICATE-----
MIIB3jCCAYSgAwIBAgIULu5VAXkCuF+V/BRXZhLfwUSsiqwwCgYIKoZIzj0EAwIw
JDEVMBMGA1UEAwwMV2FsdCBJRCBSb290MQswCQYDVQQGEwJBVDAeFw0yNjA4MTAx
MjUyNDdaFw00NjA4MDUxMjUyNDdaMCQxFTATBgNVBAMMDFdhbHQgSUQgUm9vdDEL
MAkGA1UEBhMCQVQwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQbREg0GIX6hBQP
d3kMad6BC5d6cjb0kNowagy+KgpEE3nd3hRrNqRLa6e7wGewS3G61LaSpGFgE9iT
1ECuJTeBo4GTMIGQMBIGA1UdEwEB/wQIMAYBAf8CAQAwDgYDVR0PAQH/BAQDAgEG
MB0GA1UdDgQWBBS5uwPge8/AkJhc5xPe+k1acwcF2jAqBgNVHRIEIzAhgQ5vZmZp
Y2VAd2FsdC5pZIYPaHR0cHM6Ly93YWx0LmlkMB8GA1UdIwQYMBaAFLm7A+B7z8CQ
mFznE976TVpzBwXaMAoGCCqGSM49BAMCA0gAMEUCIEpLzmKtA1bv/80hXVEJ3eIi
S14KNHCfiXkUQxMKjNnuAiEA7JTFpHHYXi2zPTNovOXNP7mov8llZkgbKZwbkEn4
fkc=
-----END CERTIFICATE-----
""".trimIndent()

val leafCertPem = """
-----BEGIN CERTIFICATE-----
MIICETCCAbegAwIBAgIUMJAkGLbeyDnDaACHF2MwwUs/j1kwCgYIKoZIzj0EAwIw
JDEVMBMGA1UEAwwMV2FsdCBJRCBSb290MQswCQYDVQQGEwJBVDAeFw0yNjA4MTAx
MjUyNDdaFw0yNzExMTAxMjUyNDdaMCYxFzAVBgNVBAMMDldhbHQgSUQgbURMIERT
MQswCQYDVQQGEwJBVDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBtESDQYhfqE
FA93eQxp3oELl3pyNvSQ2jBqDL4qCkQTed3eFGs2pEtrp7vAZ7BLcbrUtpKkYWAT
2JPUQK4lN4GjgcQwgcEwHQYDVR0OBBYEFLm7A+B7z8CQmFznE976TVpzBwXaMA4G
A1UdDwEB/wQEAwIHgDAVBgNVHSUBAf8ECzAJBgcogYxdBQECMCoGA1UdEgQjMCGB
Dm9mZmljZUB3YWx0Lmlkhg9odHRwczovL3dhbHQuaWQwLAYDVR0fBCUwIzAhoB+g
HYYbaHR0cHM6Ly9jcmwud2FsdC5pZC9jcmwuZGVyMB8GA1UdIwQYMBaAFLm7A+B7
z8CQmFznE976TVpzBwXaMAoGCCqGSM49BAMCA0gAMEUCIQD44E8Mukk3WwFeHbB6
RZZPy85lVEyNqFZs6aNLq2kq4QIgXrURrzy1iLEYmsnna6YYhRrvGaYEjk1GqCn2
w+skfmw=
-----END CERTIFICATE-----
""".trimIndent()

suspend fun main() {

    configureTrustStore()

}

object MyTrustStore : X509CertificateTrustStore {

    override fun findCertificateBySubjectDn(subjectDn: String): List<X509Certificate> {
        // my trust store implementation
        return listOf()
    }
}


suspend fun configureTrustStore() {

    println("Configure trust store")

    val trustedCertificatePemList: List<String> = listOf(rootCertPem)

    // use your own trust store implementation
    val myTrustStore = MyTrustStore

    // use a set of trust anchors
    val trustAnchors = InMemoryTrustStore(trustedCertificatePemList.map { X509CertificateUtil.parseCertificatePem(it) })

    // trust stores can be combined
    val wholeTrust = CompositeTrustStore(listOf(
        myTrustStore,
        trustAnchors))

    // configure the trust store for the certificate util
    val utilWithMyTrust = X509CertificateUtil {
        setTrust(wholeTrust)
    }

    // now you can use the util with your own trust store
    val validationResult = utilWithMyTrust.validatePemCertificateChain(leafCertPem)
    println("Certificate valid: ${validationResult.valid}")
    validationResult.log.forEach { println("${it.severity} ${it.subjectDn}/${it.validatorId}: '${it.message}'") }
    println()
    val validationResultWithoutStore = X509CertificateUtil.validatePemCertificateChain(leafCertPem)
    println("Certificate valid (without trust): ${validationResultWithoutStore.valid}")
    validationResultWithoutStore.log.forEach { println("${it.severity} ${it.subjectDn}/${it.validatorId}: '${it.message}'") }
}