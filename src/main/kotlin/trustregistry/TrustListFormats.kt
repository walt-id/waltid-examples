package trustregistry

import id.walt.trust.model.AuthenticityState
import id.walt.trust.model.RefreshResult
import id.walt.trust.model.SourceFamily
import id.walt.trust.model.SourceAcceptancePolicy
import id.walt.trust.model.SourceLoadOptions
import id.walt.trust.model.TrustDecisionCode
import id.walt.trust.service.DefaultTrustRegistryService
import id.walt.trust.store.InMemoryTrustStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock

private const val AUSTRIAN_TSL_URL = "https://www.signatur.rtr.at/vertrauensliste.xml"

/**
 * Loads and checks every trust-list representation currently supported by the SDK:
 *
 * 1. ETSI TS 119 612 TSL XML with XMLDSig validation
 * 2. Provisional LoTE JSON
 * 3. Provisional LoTE XML
 * 4. LoTE JSON in a compact-JWS envelope with an independently pinned signer
 *
 * The LoTE fixtures are synthetic and non-normative. They demonstrate the current
 * MVP input shape; they are not examples of a finalized ETSI schema.
 */
fun main() = runBlocking { runTrustListFormats() }

suspend fun runTrustListFormats() {
    proveTslXml()
    proveLoteJson()
    proveLoteXml()
    proveSignedLoteJson()
    println("\nAll supported trust-list format checks passed.")
}

private suspend fun proveTslXml() {
    val service = newService()
    val result = service.loadSourceFromUrl(
        sourceId = "at-tsl",
        url = AUSTRIAN_TSL_URL,
        options = SourceLoadOptions(SourceAcceptancePolicy.REQUIRE_VALID_SIGNATURE)
    ).requireSuccess("ETSI TSL XML")

    val source = service.listSources().first { it.sourceId == "at-tsl" }
    check(source.sourceFamily == SourceFamily.TSL)
    check(source.assurance.authenticityState == AuthenticityState.INTEGRITY_VERIFIED) {
        "Austrian TSL XML signature integrity was not verified: ${source.assurance}"
    }
    check(result.entitiesLoaded > 0 && result.servicesLoaded > 0)
    printResult("ETSI TSL XML + XMLDSig", result, source.assurance.authenticityState)
}

private suspend fun proveLoteJson() {
    val service = newService()
    val result = service.loadSourceFromContent(
        sourceId = "lote-json",
        content = resource("/trust-registry/lote.json"),
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    ).requireSuccess("LoTE JSON")

    requireTrustedProvider(service, "JSON-WALLET-001")
    val source = service.listSources().first { it.sourceId == "lote-json" }
    check(source.sourceFamily == SourceFamily.LOTE)
    check(source.assurance.authenticityState == AuthenticityState.UNVERIFIED)
    printResult("LoTE JSON (synthetic, unsigned)", result, source.assurance.authenticityState)
}

private suspend fun proveLoteXml() {
    val service = newService()
    val result = service.loadSourceFromContent(
        sourceId = "lote-xml",
        content = resource("/trust-registry/lote.xml"),
        options = SourceLoadOptions(SourceAcceptancePolicy.ALLOW_UNSIGNED)
    ).requireSuccess("LoTE XML")

    requireTrustedProvider(service, "XML-PID-001")
    val source = service.listSources().first { it.sourceId == "lote-xml" }
    check(source.sourceFamily == SourceFamily.LOTE)
    check(source.assurance.authenticityState == AuthenticityState.UNVERIFIED)
    printResult("LoTE XML (synthetic, unsigned)", result, source.assurance.authenticityState)
}

private suspend fun proveSignedLoteJson() {
    val service = newService()
    val result = service.loadSourceFromContent(
        sourceId = "signed-lote-json",
        content = resource("/trust-registry/signed-lote.jws").trim(),
        options = SourceLoadOptions(
            acceptancePolicy = SourceAcceptancePolicy.REQUIRE_AUTHENTICATED,
            trustedSignerCertificates = listOf(resource("/trust-registry/signed-lote-signer.pem"))
        )
    ).requireSuccess("compact-JWS LoTE JSON")

    requireTrustedProvider(service, "SIGNED-WALLET-001")
    val source = service.listSources().first { it.sourceId == "signed-lote-json" }
    check(source.assurance.authenticityState == AuthenticityState.AUTHENTICATED)
    check(source.metadata["signatureFormat"] == "JWS_COMPACT")
    printResult("LoTE JSON + compact JWS", result, source.assurance.authenticityState)
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
