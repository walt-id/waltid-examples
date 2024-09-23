package did.resolve

import id.walt.did.dids.DidService

suspend fun main() {
    resolveDidWeb()
}

suspend fun resolveDidWeb() {
    DidService.minimalInit()
    val did = "did:web:wallet.walt.id:wallet-api:registry:c5cd4ec6-0463-4083-8178-89f6a5ddbfd4"
    println("Resolve $did")
    val didResult = DidService.resolve(did)
    println("Success: ${didResult.isSuccess}")
    if (didResult.isSuccess) println("Did Document: ${didResult.getOrThrow()}")
    println("Resolve to key $did")
    val keyResult = DidService.resolveToKey(did)
    println("Success: ${didResult.isSuccess}")
    if (keyResult.isSuccess) println("Key: ${keyResult.getOrThrow().exportJWK()}")
}