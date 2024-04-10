package did.resolve

import id.walt.did.dids.DidService

suspend fun main() {
    web_method()
}

suspend fun web_method() {
    DidService.minimalInit()
    val did = "did:web:wallet.walt-test.cloud:wallet-api:registry:6ae92b09-b4ee-4b8f-ac4c-1bf3b837caca"
    val didDocumentResult = DidService.resolve(did)
    val document = didDocumentResult.getOrNull()
    println(document)
    val keyResult = DidService.resolveToKey(did)
    val key = keyResult.getOrNull()
    println(key)
}