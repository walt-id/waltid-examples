package did.create

import id.walt.did.dids.registrar.DidResult

internal fun printDidResult(didResult: DidResult) {
    println("DID: ${didResult.did}")
    println("DID Document: ${didResult.didDocument.toJsonObject()}")
}