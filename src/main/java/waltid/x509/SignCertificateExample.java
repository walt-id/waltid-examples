package waltid.x509;

import java.util.List;

import id.walt.certificate.x509.JavaX509CertificateUtil;
import id.walt.certificate.x509.X509Certificate;
import id.walt.certificate.x509.extension.AuthorityKeyIdentifierExtension;
import id.walt.certificate.x509.extension.KeyUsageExtension;
import id.walt.certificate.x509.extension.SubjectAlternativeNameExtension;
import id.walt.certificate.x509.extension.SubjectKeyIdentifierExtension;
import id.walt.certificate.x509.truststore.InMemoryTrustStore;
import id.walt.certificate.x509.validation.ValidationResult;
import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.algorithms.SignatureAlgorithm;
import id.walt.crypto2.jvm.JavaDigestAlgorithm;
import id.walt.crypto2.jvm.JavaSignatureAlgorithm;
import id.walt.crypto2.jvm.JavaSoftwareKeys;
import id.walt.crypto2.keys.Key;
import kotlin.Unit;

/**
 * Java port of {@code SignCertificateExample.kt}.
 *
 * <p>Creates a self-signed root CA, issues a leaf certificate under it, and validates the chain
 * against the root as trust anchor - showing that neither chain order nor the presence of the
 * root in the chain matters.
 */
public class SignCertificateExample {

    private static final CryptoRuntime cryptoRuntime = JavaSoftwareKeys.defaultRuntime();
    private static final SignatureAlgorithm certSigningAlg =
            JavaSoftwareKeys.toKotlin(JavaSignatureAlgorithm.ecdsa(new JavaDigestAlgorithm("SHA-256"), "DER"));

    public static void main(String[] args) {
        Key caKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "ca");
        X509Certificate caCert = createSelfSignedRootCa(caKey);
        System.out.println();
        Key leafKey = Crypto2Keys.generateEcP256Key(cryptoRuntime, "ca");
        X509Certificate leafCert = createLeafCertificate(caKey, caCert, leafKey);
        System.out.println();
        validateCertificateChain(List.of(leafCert), caCert);
        System.out.println();
        // including the root in the chain is not necessary but allowed
        validateCertificateChain(List.of(leafCert, caCert), caCert);
        System.out.println();
        // order doesn't matter
        validateCertificateChain(List.of(caCert, leafCert), caCert);
    }

    private static X509Certificate createSelfSignedRootCa(Key key) {
        X509Certificate cert = JavaX509CertificateUtil.getDefault().createSelfSignedCertificate(
                key,
                certSigningAlg,
                builder -> {
                    builder.setSubjectDn("cn=My Root, o=Walt.id, c=AT");
                    // add key usage constraint
                    KeyUsageExtension.Companion.extensionKeyUsage(builder, keyUsage -> {
                        keyUsage.addKeyUsage(
                                KeyUsageExtension.KeyUsage.digitalSignature,
                                KeyUsageExtension.KeyUsage.keyCertSign
                        );
                        return Unit.INSTANCE;
                    });
                    // add subject alternative names
                    SubjectAlternativeNameExtension.Companion.extensionSan(builder, san -> {
                        san.addEmail("example@walt.id");
                        san.addUri("https://walt.id");
                        return Unit.INSTANCE;
                    });
                }
        );
        System.out.println("Created root Ca certificate");
        System.out.println("Issuer DN / Subject DN: " + cert.getData().getIssuerDn());
        SubjectKeyIdentifierExtension ski =
                SubjectKeyIdentifierExtension.Companion.getExtensionSubjectKeyIdentifier(cert.getData());
        System.out.println("Subject Key ID: " + (ski != null ? ski.getKeyIdentifier() : null));
        System.out.println("Fingerprint: " + cert.getFingerprintSha256Hex());
        return cert;
    }

    private static X509Certificate createLeafCertificate(Key issuerKey, X509Certificate issuerCert, Key subjectKey) {
        X509Certificate cert = JavaX509CertificateUtil.getDefault().createCertificate(
                issuerKey,
                issuerCert,
                certSigningAlg,
                builder -> {
                    builder.setSubjectDn("cn=My Leaf Certificate, o=Walt.id, c=AT");
                    builder.subjectPublicKey(subjectKey);
                }
        );
        System.out.println("Created leaf certificate");
        System.out.println("Issuer DN (same as caCert DN): " + cert.getData().getIssuerDn());
        AuthorityKeyIdentifierExtension aki =
                AuthorityKeyIdentifierExtension.Companion.getExtensionAuthorityKeyIdentifier(cert.getData());
        System.out.println("Issuer Key ID (same as subject KeyId of parent Cert): "
                + (aki != null ? aki.getKeyIdentifier() : null));
        System.out.println("Subject DN: " + cert.getData().getSubjectDn());
        System.out.println("Fingerprint: " + cert.getFingerprintSha256Hex());
        // restored subject public key - not used further, mirrors the Kotlin example
        Key publicKey = JavaX509CertificateUtil.getDefault().restoreSubjectPublicKey(cert, cryptoRuntime);
        return cert;
    }

    private static void validateCertificateChain(List<X509Certificate> chain, X509Certificate trustRoot) {
        InMemoryTrustStore trustStore = new InMemoryTrustStore(List.of(trustRoot));
        // Passing trustStore here scopes trust to exactly trustRoot for this call - it replaces
        // X509CertificateUtil.Default's configured trust store rather than merging with it, so this
        // does not also trust the platform's system CA store. See ConfigureTrustStoreExample for
        // more on how trust stores are combined/configured.
        ValidationResult validationResult =
                JavaX509CertificateUtil.getDefault().validateCertificateChain(chain, trustStore);
        System.out.println("Validation result - isValid: " + validationResult.getValid());
        System.out.println("Validation result - log:");
        for (ValidationResult.ValidationLogEntry entry : validationResult.getLog()) {
            System.out.println(entry.getSeverity() + " " + entry.getSubjectDn() + "/"
                    + entry.getValidatorId() + ": '" + entry.getMessage() + "'");
        }
    }
}
