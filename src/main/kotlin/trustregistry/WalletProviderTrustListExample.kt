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
 * Resolves `TrustedEntityType.WALLET_PROVIDER` entities from an ETSI TS 119 602 List of Trusted
 * Entities (LoTE) JSON document.
 *
 * Uses the local fixture at `src/main/resources/trust-registry/lote.json` rather than a real
 * public list - see [PidProviderTrustListExample] for why: no public LoTE publisher lists Wallet
 * Providers yet either, for the same reason (the checked pilot ecosystem still publishes this role
 * as a TSL, not a LoTE).
 */
fun main() = runBlocking { walletProviderTrustList() }

suspend fun walletProviderTrustList() {
    val service = DefaultTrustRegistryService(InMemoryTrustStore())
    val result = service.loadSourceFromContent(
        sourceId = "wallet-providers",
        content = resource("/trust-registry/lote.json"),
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    )
    check(result.success) { "Wallet provider list could not be loaded: ${result.error}" }

    val walletProviders = service.listTrustedEntities(
        EntityFilter(entityType = TrustedEntityType.WALLET_PROVIDER)
    ).toList()
    println("WALLET_PROVIDER entities:")
    walletProviders.forEach { entity ->
        println("  - ${entity.legalName} (${entity.entityType}, ${entity.country})")
    }

    val decision = service.resolveByProviderId(
        providerId = "https://example.org/ListOfTrustedEntities/WalletProvider/AT",
        instant = Clock.System.now(),
        expectedEntityType = TrustedEntityType.WALLET_PROVIDER
    )
    check(decision.decision == TrustDecisionCode.TRUSTED) {
        "Expected the wallet provider to be trusted, got ${decision.decision} (${decision.warnings})"
    }
    println("\nResolved by provider ID: ${decision.matchedEntity?.legalName} -> ${decision.decision}")
}
