package did.resolve

import id.walt.did.dids.DidService

suspend fun main(){
    key_method()
}
suspend fun key_method(){
    DidService.minimalInit()
    val did = "did:key:zBhBLmYmyihtomRdJJNEKzbPj51o4a3GYFeZoRHSABKUwqdjiQPY2cc3SKCmkYnqk94qTDsAV1ntvdvCCVvqcqvxMt7QUUt2hLNjv6u4yr6qpQy1CAwomcLjcQ8TPYuVVSppvGzq2cYFCXdCXJUrhAH9bKCCSVDSAvaFoM3tiNpdJazWjLjwAnQ"
    val didDocumentResult = DidService.resolve(did)
    val document = didDocumentResult.getOrNull()
    println(document)
    val keyResult = DidService.resolveToKey(did)
    val key = keyResult.getOrNull()
    println(key!!.getKeyId())
}