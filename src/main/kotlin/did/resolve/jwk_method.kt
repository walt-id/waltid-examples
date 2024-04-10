package did.resolve

import id.walt.did.dids.DidService

suspend fun main() {
    jwk_method()
}

suspend fun jwk_method() {
    DidService.minimalInit()
    val did =
        "did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiclJGNlZjTlQ0dXdHVFhTa3Q3VGt4R1BmWjFXaUpTYUpZOWRVaUxQcVJOVSIsIngiOiJBWEMxOXhGN3NKUXVEcm9pSWZPMW8xZTRNdEgzeGdKcnJpLVVxbnVrSW1ZIn0"
    val didDocumentResult = DidService.resolve(did)
    val document = didDocumentResult.getOrNull()
    println(document)
    val keyResult = DidService.resolveToKey(did)
    val key = keyResult.getOrNull()
    println(key!!.getKeyId())
}