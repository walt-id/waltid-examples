package did.resolve

import id.walt.did.dids.DidService

suspend fun main(){
    DidService.minimalInit()
    val didDocumentResult = DidService.resolve("did:web:wallet.walt-test.cloud:wallet-api:registry:1234")
    val document = didDocumentResult.getOrNull()
    println(document)
    val keyResult = DidService.resolveToKey("did:web:wallet.walt-test.cloud:wallet-api:registry:1234")
    val key = keyResult.getOrNull()
    println(key)
}