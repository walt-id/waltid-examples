package trustregistry

import id.walt.trust.model.SourceAcceptancePolicy
import id.walt.trust.model.SourceLoadOptions
import id.walt.trust.service.DefaultTrustRegistryService
import id.walt.trust.store.InMemoryTrustStore
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

/**
 * Loads Germany's national ETSI TS 119 612 trust list (Bundesnetzagentur, `tl.bundesnetzagentur.de/TL-DE.xml`)
 * from a local snapshot in `src/main/resources/trust-registry/tl-de.xml`.
 *
 * Germany's list is signed with RSASSA-PSS (SHA-256 / MGF1) rather than plain PKCS#1 v1.5 - the signer
 * certificate's SubjectPublicKeyInfo uses the explicit id-RSASSA-PSS OID (1.2.840.113549.1.1.10), not
 * the generic rsaEncryption OID (1.2.840.113549.1.1.1) most trust lists use. This example exists to
 * demonstrate that path end-to-end against real content.
 *
 * A local snapshot is used instead of `loadSourceFromUrl` because this is a large (~5 MB), slowly
 * changing document; re-fetching it on every run is unnecessary network weight for an example. See
 * [TrustListUrls.kt] for a live-network check against smaller lists (Austria, Italy) plus the EU LoTL.
 */
fun main() = runBlocking { germanTrustList() }

suspend fun germanTrustList() {
    val service = DefaultTrustRegistryService(InMemoryTrustStore())
    val result = service.loadSourceFromContent(
        sourceId = "de-tsl",
        content = resource("/trust-registry/tl-de.xml"),
        sourceUrl = "https://tl.bundesnetzagentur.de/TL-DE.xml",
        options = SourceLoadOptions(SourceAcceptancePolicy.REQUIRE_VALID_SIGNATURE)
    )
    check(result.success) { "German TSL could not be loaded: ${result.error}" }

    val source = service.listSources().toList().first { it.sourceId == "de-tsl" }
    println("German TSL (Bundesnetzagentur)")
    println("  territory:            ${source.territory}")
    println("  signature status:     ${source.assurance.signatureStatus}")
    println("  authenticity state:   ${source.assurance.authenticityState}")
    println("  freshness:            ${source.freshnessState}")
    println("  entities loaded:      ${result.entitiesLoaded}")
    println("  services loaded:      ${result.servicesLoaded}")

    println("\nFirst 5 trusted entities:")
    service.listTrustedEntities().take(5).toList().forEach { entity ->
        println("  - ${entity.legalName} (${entity.entityType}, ${entity.country})")
    }
}
