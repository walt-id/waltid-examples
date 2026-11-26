package trustregistry

import id.walt.trust.model.EntityFilter
import id.walt.trust.model.SourceAcceptancePolicy
import id.walt.trust.model.SourceLoadOptions
import id.walt.trust.model.TrustedEntityType
import id.walt.trust.service.DefaultTrustRegistryService
import id.walt.trust.store.InMemoryTrustStore
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking

/**
 * Loads a real List of Trusted Entities (ETSI TS 119 602) for Wallet Relying Party Access Certificate
 * (WRPAC) providers - `TrustedEntityType.ACCESS_CERTIFICATE_PROVIDER`, not the generic
 * `TRUST_SERVICE_PROVIDER` type most eIDAS trust lists (Austria, Italy, Germany) produce.
 *
 * Snapshot taken from the WE BUILD WP4 Trust Infrastructure group's public pilot LoTE
 * (https://webuild-consortium.github.io/wp4-trust-group/), saved locally at
 * `src/main/resources/trust-registry/wrpac-providers-lote.json`. As of this snapshot it lists one
 * real participant (Raidiam). The published document wraps the `LoTE` body in a top-level
 * `signature` sibling property (a detached-signature envelope this library's ETSI TS 119 602 JSON
 * schema validation does not yet recognise as a supported signed form - it's neither a bare LoTE
 * nor a compact JWS); the local snapshot has that wrapper stripped down to `{"LoTE": ...}` so the
 * entity content - unmodified - loads under `ALLOW_UNSIGNED`. See [WrprcTrustListExample] for the paired WRPRC (registration
 * certificate) list, and [PidProviderTrustListExample] / [WalletProviderTrustListExample] for
 * `PID_PROVIDER` / `WALLET_PROVIDER` - roles no public LoTE publisher lists yet as of this writing;
 * those two national trust authorities currently only publish TSLs for those roles, which this
 * library (correctly) reads as `TRUST_SERVICE_PROVIDER` rather than guessing at the role.
 */
fun main() = runBlocking { wrpacTrustList() }

suspend fun wrpacTrustList() {
    val service = DefaultTrustRegistryService(InMemoryTrustStore())
    val result = service.loadSourceFromContent(
        sourceId = "wrpac-providers",
        content = resource("/trust-registry/wrpac-providers-lote.json"),
        sourceUrl = "https://webuild-consortium.github.io/wp4-trust-group/lotl/wrpac-providers-lote.json",
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    )
    check(result.success) { "WRPAC providers list could not be loaded: ${result.error}" }

    println("WRPAC (Wallet Relying Party Access Certificate) providers")
    println("  entities loaded: ${result.entitiesLoaded}")
    println("  services loaded: ${result.servicesLoaded}")

    val accessCertificateProviders = service.listTrustedEntities(
        EntityFilter(entityType = TrustedEntityType.ACCESS_CERTIFICATE_PROVIDER)
    ).toList()
    println("\nACCESS_CERTIFICATE_PROVIDER entities:")
    accessCertificateProviders.forEach { entity ->
        println("  - ${entity.legalName} (${entity.entityType})")
    }
}
