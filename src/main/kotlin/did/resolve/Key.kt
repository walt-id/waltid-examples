package did.resolve

import id.walt.did.dids.DidService

suspend fun main() {
    resolveDidKey()
}

suspend fun resolveDidKey() {
    DidService.minimalInit()
    val did =
        "did:key:zBhBLmYmyihtomRdJJNEKzbPj51o4a3GYFeZoRHSABKUwqdjiQPY2cc3SKCmkYnqk94qTDsAV1ntvdvCCVvqcqvxMt7QUUt2hLNjv6u4yr6qpQy1CAwomcLjcQ8TPYuVVSppvGzq2cYFCXdCXJUrhAH9bKCCSVDSAvaFoM3tiNpdJazWjLjwAnQ"
    println("Resolve $did")
    val didResult = DidService.resolve(did)
    println("Success: ${didResult.isSuccess}")
    if (didResult.isSuccess) println("Did Document: ${didResult.getOrThrow()}")
    println("Resolve to key $did")
    val keyResult = DidService.resolveToKey(did)
    println("Success: ${didResult.isSuccess}")
    if (keyResult.isSuccess) println("Key: ${keyResult.getOrThrow().exportJWK()}")
}