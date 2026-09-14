package trustregistry

import id.walt.trust.model.AuthenticityState
import id.walt.trust.model.FreshnessState
import id.walt.trust.model.RefreshResult
import id.walt.trust.model.SignatureStatus
import id.walt.trust.model.SourceAcceptancePolicy
import id.walt.trust.model.SourceFamily
import id.walt.trust.model.SourceLoadOptions
import id.walt.trust.model.TrustSource
import id.walt.trust.model.TrustListFormat
import id.walt.trust.service.DefaultTrustRegistryService
import id.walt.trust.store.InMemoryTrustStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Executable interoperability check for every public URL advertised by the
 * Enterprise Trust Registry OpenAPI documentation.
 *
 * The command exits with a failure when fetching, format detection, signature
 * handling, parsing, or the expected source contents change.
 */
fun main() = runBlocking { runTrustListUrlValidation() }

suspend fun runTrustListUrlValidation() {
    println("\nValidating public trust-list URLs advertised by the Enterprise API")
    println("(live network check; a publisher or network outage will fail the run)\n")

    LIVE_TRUST_LISTS.forEach { validate(it) }

    println("\nAll advertised trust-list URLs passed.")
}

private suspend fun validate(expectation: TrustListExpectation) {
    val service = DefaultTrustRegistryService(InMemoryTrustStore())
    val result = service.loadSourceFromUrl(
        sourceId = expectation.id,
        url = expectation.url,
        options = SourceLoadOptions(expectation.acceptancePolicy)
    )
    check(result.success) {
        "${expectation.label} failed: ${result.errorCode}: ${result.error}"
    }

    val source = service.listSources().first { it.sourceId == expectation.id }
    check(source.sourceFamily == SourceFamily.TSL) {
        "${expectation.label}: expected TSL format, got ${source.sourceFamily}"
    }
    check(source.format == expectation.format) {
        "${expectation.label}: expected ${expectation.format}, got ${source.format}"
    }
    check(source.assurance.signatureStatus == expectation.signatureStatus) {
        "${expectation.label}: expected signature ${expectation.signatureStatus}, got ${source.assurance}"
    }
    check(source.assurance.authenticityState == expectation.authenticityState) {
        "${expectation.label}: expected ${expectation.authenticityState}, got ${source.assurance}"
    }
    check(result.entitiesLoaded >= expectation.minimumEntities) {
        "${expectation.label}: expected at least ${expectation.minimumEntities} entities, got ${result.entitiesLoaded}"
    }
    check(result.servicesLoaded >= expectation.minimumServices) {
        "${expectation.label}: expected at least ${expectation.minimumServices} services, got ${result.servicesLoaded}"
    }
    if (expectation.pointerOnly) {
        check(result.entitiesLoaded == 0 && result.servicesLoaded == 0) {
            "${expectation.label}: a pointer-only LoTL must not be reported as containing trust providers"
        }
        check(result.pointersLoaded > 0) { "${expectation.label}: expected member-state pointers" }
    }
    check(source.freshnessState == expectation.freshnessState) {
        "${expectation.label}: expected freshness ${expectation.freshnessState}, got ${source.freshnessState}"
    }

    printResult(expectation, result, source)
}

private fun printResult(
    expectation: TrustListExpectation,
    result: RefreshResult,
    source: TrustSource
) {
    println(
        "%-16s format=%-44s signature=%-11s freshness=%-7s entities=%-4d services=%-4d pointers=%-3d".format(
            expectation.label,
            source.format,
            source.assurance.signatureStatus,
            source.freshnessState,
            result.entitiesLoaded,
            result.servicesLoaded,
            result.pointersLoaded
        )
    )
}

private data class TrustListExpectation(
    val id: String,
    val label: String,
    val url: String,
    val acceptancePolicy: SourceAcceptancePolicy,
    val format: TrustListFormat,
    val signatureStatus: SignatureStatus,
    val authenticityState: AuthenticityState,
    val minimumEntities: Int,
    val minimumServices: Int,
    val freshnessState: FreshnessState,
    val pointerOnly: Boolean = false
)

private val LIVE_TRUST_LISTS = listOf(
    TrustListExpectation(
        id = "at-tsl",
        label = "Austria TSL",
        url = "https://www.signatur.rtr.at/vertrauensliste.xml",
        acceptancePolicy = SourceAcceptancePolicy.REQUIRE_VALID_SIGNATURE,
        format = TrustListFormat.ETSI_TS_119_612_TRUST_LIST_XML,
        signatureStatus = SignatureStatus.VALID,
        authenticityState = AuthenticityState.INTEGRITY_VERIFIED,
        minimumEntities = 1,
        minimumServices = 1,
        freshnessState = FreshnessState.FRESH
    ),
    TrustListExpectation(
        id = "it-tsl",
        label = "Italy TSL",
        url = "https://eidas.agid.gov.it/TL/TSL-IT.xml",
        acceptancePolicy = SourceAcceptancePolicy.REQUIRE_VALID_SIGNATURE,
        format = TrustListFormat.ETSI_TS_119_612_TRUST_LIST_XML,
        signatureStatus = SignatureStatus.VALID,
        authenticityState = AuthenticityState.INTEGRITY_VERIFIED,
        minimumEntities = 1,
        minimumServices = 1,
        freshnessState = FreshnessState.FRESH
    ),
    TrustListExpectation(
        id = "eu-lotl",
        label = "EU LoTL",
        url = "https://ec.europa.eu/tools/lotl/eu-lotl.xml",
        acceptancePolicy = SourceAcceptancePolicy.REQUIRE_VALID_SIGNATURE,
        format = TrustListFormat.ETSI_TS_119_612_LIST_OF_TRUST_LISTS_XML,
        signatureStatus = SignatureStatus.VALID,
        authenticityState = AuthenticityState.INTEGRITY_VERIFIED,
        minimumEntities = 0,
        minimumServices = 0,
        freshnessState = FreshnessState.FRESH,
        pointerOnly = true
    )
)
