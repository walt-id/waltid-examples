package did.resolve

import id.walt.did.dids.DidService

suspend fun main() {
    resolveDidJwk()
}

suspend fun resolveDidJwk() {
    DidService.minimalInit()
    val did =
        "did:jwk:eyJrdHkiOiJPS1AiLCJjcnYiOiJFZDI1NTE5Iiwia2lkIjoiclJGNlZjTlQ0dXdHVFhTa3Q3VGt4R1BmWjFXaUpTYUpZOWRVaUxQcVJOVSIsIngiOiJBWEMxOXhGN3NKUXVEcm9pSWZPMW8xZTRNdEgzeGdKcnJpLVVxbnVrSW1ZIn0"
    println("Resolve $did")
    val didResult = DidService.resolve(did)
    println("Success: ${didResult.isSuccess}")
    if (didResult.isSuccess) println("Did Document: ${didResult.getOrThrow()}")
    println("Resolve to key $did")
    val keyResult = DidService.resolveToKey(did)
    println("Success: ${didResult.isSuccess}")
    if (keyResult.isSuccess) println("Key: ${keyResult.getOrThrow().exportJWK()}")
}