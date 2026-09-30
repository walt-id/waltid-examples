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
 * Loads a real List of Trusted Entities (ETSI TS 119 602) for Wallet Relying Party Registration
 * Certificate (WRPRC) providers - `TrustedEntityType.RELYING_PARTY_PROVIDER`.
 *
 * Snapshot taken from the WE BUILD WP4 Trust Infrastructure group's public pilot LoTE
 * (https://webuild-consortium.github.io/wp4-trust-group/), saved locally at
 * `src/main/resources/trust-registry/wrprc-providers-lote.json`. See [WrpacTrustListExample] for
 * the paired WRPAC (access certificate) list and why `PID_PROVIDER` / `WALLET_PROVIDER` are
 * demonstrated from local fixtures instead of a public list.
 */
fun main() = runBlocking { wrprcTrustList() }

suspend fun wrprcTrustList() {
    val service = DefaultTrustRegistryService(InMemoryTrustStore())
    val result = service.loadSourceFromContent(
        sourceId = "wrprc-providers",
        content = resource("/trust-registry/wrprc-providers-lote.json"),
        sourceUrl = "https://webuild-consortium.github.io/wp4-trust-group/lotl/wrprc-providers-lote.json",
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    )
    check(result.success) { "WRPRC providers list could not be loaded: ${result.error}" }

    println("WRPRC (Wallet Relying Party Registration Certificate) providers")
    println("  entities loaded: ${result.entitiesLoaded}")
    println("  services loaded: ${result.servicesLoaded}")

    val relyingPartyProviders = service.listTrustedEntities(
        EntityFilter(entityType = TrustedEntityType.RELYING_PARTY_PROVIDER)
    ).toList()
    println("\nRELYING_PARTY_PROVIDER entities:")
    relyingPartyProviders.forEach { entity ->
        println("  - ${entity.legalName} (${entity.entityType})")
    }
}
