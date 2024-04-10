package did.create

import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidCheqdCreateOptions

suspend fun main() {
    cheqd_method()
}

suspend fun cheqd_method() {

    DidService.minimalInit()
    val options = DidCheqdCreateOptions(
        network = "testnet"
    )
    println("Create and register did")
    val didResult = DidService.register(options)
    println("Key did result: " + didResult + "\n")

}