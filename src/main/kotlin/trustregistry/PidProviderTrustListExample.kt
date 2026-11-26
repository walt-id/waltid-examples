package trustregistry

import id.walt.trust.model.EntityFilter
import id.walt.trust.model.SourceAcceptancePolicy
import id.walt.trust.model.SourceLoadOptions
import id.walt.trust.model.TrustDecisionCode
import id.walt.trust.model.TrustedEntityType
import id.walt.trust.service.DefaultTrustRegistryService
import id.walt.trust.store.InMemoryTrustStore
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock

/**
 * Resolves `TrustedEntityType.PID_PROVIDER` entities from an ETSI TS 119 602 List of Trusted
 * Entities (LoTE) XML document.
 *
 * Uses the local fixture at `src/main/resources/trust-registry/lote.xml` rather than a real public
 * list: as of this writing, no public LoTE publisher lists PID Providers. The one real ecosystem
 * checked for this (the WE BUILD WP4 Trust Infrastructure pilot - see [WrpacTrustListExample])
 * currently publishes its PID Provider list as a plain ETSI TS 119 612 TSL instead, which this
 * library correctly resolves as `TRUST_SERVICE_PROVIDER` (the role a TSL cannot express) rather
 * than guessing the role from context. When a public PID Provider LoTE becomes available, this
 * example should load it from `loadSourceFromUrl` the same way [GermanTrustListExample] does.
 */
fun main() = runBlocking { pidProviderTrustList() }

suspend fun pidProviderTrustList() {
    val service = DefaultTrustRegistryService(InMemoryTrustStore())
    val result = service.loadSourceFromContent(
        sourceId = "pid-providers",
        content = resource("/trust-registry/lote.xml"),
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    )
    check(result.success) { "PID provider list could not be loaded: ${result.error}" }

    val pidProviders = service.listTrustedEntities(
        EntityFilter(entityType = TrustedEntityType.PID_PROVIDER)
    ).toList()
    println("PID_PROVIDER entities:")
    pidProviders.forEach { entity ->
        println("  - ${entity.legalName} (${entity.entityType}, ${entity.country})")
    }

    val decision = service.resolveByProviderId(
        providerId = "https://example.org/ListOfTrustedEntities/PIDProvider/AT",
        instant = Clock.System.now(),
        expectedEntityType = TrustedEntityType.PID_PROVIDER
    )
    check(decision.decision == TrustDecisionCode.TRUSTED) {
        "Expected the PID provider to be trusted, got ${decision.decision} (${decision.warnings})"
    }
    println("\nResolved by provider ID: ${decision.matchedEntity?.legalName} -> ${decision.decision}")
}
