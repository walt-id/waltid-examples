package trustregistry

import id.walt.trust.model.AuthenticityState
import id.walt.trust.model.RefreshResult
import id.walt.trust.model.SourceFamily
import id.walt.trust.model.SourceAcceptancePolicy
import id.walt.trust.model.SourceLoadOptions
import id.walt.trust.model.TrustDecisionCode
import id.walt.trust.model.TrustListFormat
import id.walt.trust.service.DefaultTrustRegistryService
import id.walt.trust.store.InMemoryTrustStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock

/**
 * Loads and checks every trust-list representation currently supported by the SDK:
 *
 * 1. The official live TS 119 612 URLs advertised by the Enterprise API
 * 2. Normative TS 119 602 Annex A.1 JSON and Annex A.2.1 XML fixtures
 */
fun main() = runBlocking { runTrustListFormats() }

suspend fun runTrustListFormats() {
    runTrustListUrlValidation()
    proveLoteJson()
    proveLoteXml()
    println("\nAll supported trust-list format checks passed.")
}

private suspend fun proveLoteJson() {
    val service = newService()
    val result = service.loadSourceFromContent(
        sourceId = "lote-json",
        content = resource("/trust-registry/lote.json"),
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    ).requireSuccess("LoTE JSON")

    requireTrustedProvider(service, "https://example.org/ListOfTrustedEntities/WalletProvider/AT")
    val source = service.listSources().first { it.sourceId == "lote-json" }
    check(source.sourceFamily == SourceFamily.LOTE)
    check(source.format == TrustListFormat.ETSI_TS_119_602_JSON)
    check(source.assurance.authenticityState == AuthenticityState.UNVERIFIED)
    printResult("TS 119 602 JSON fixture", result, source.assurance.authenticityState)
}

private suspend fun proveLoteXml() {
    val service = newService()
    val result = service.loadSourceFromContent(
        sourceId = "lote-xml",
        content = resource("/trust-registry/lote.xml"),
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    ).requireSuccess("LoTE XML")

    requireTrustedProvider(service, "https://example.org/ListOfTrustedEntities/PIDProvider/AT")
    val source = service.listSources().first { it.sourceId == "lote-xml" }
    check(source.sourceFamily == SourceFamily.LOTE)
    check(source.format == TrustListFormat.ETSI_TS_119_602_XML)
    check(source.assurance.authenticityState == AuthenticityState.UNVERIFIED)
    printResult("TS 119 602 XML fixture", result, source.assurance.authenticityState)
}

private fun newService() = DefaultTrustRegistryService(InMemoryTrustStore())

private suspend fun requireTrustedProvider(
    service: DefaultTrustRegistryService,
    providerId: String
) {
    val decision = service.resolveByProviderId(providerId, Clock.System.now())
    check(decision.decision == TrustDecisionCode.TRUSTED) {
        "Expected $providerId to be trusted, got ${decision.decision} (${decision.warnings})"
    }
}

private fun RefreshResult.requireSuccess(format: String): RefreshResult = apply {
    check(success) { "$format could not be loaded: $error" }
}

private fun printResult(
    format: String,
    result: RefreshResult,
    authenticityState: AuthenticityState
) {
    println(
        "%-34s entities=%-3d services=%-3d identities=%-3d authenticity=%s".format(
            format,
            result.entitiesLoaded,
            result.servicesLoaded,
            result.identitiesLoaded,
            authenticityState
        )
    )
}

private fun resource(path: String): String = checkNotNull(
    object {}.javaClass.getResource(path)
) { "Missing example resource: $path" }.readText()
