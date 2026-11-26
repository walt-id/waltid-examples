package crypto.key.decode.pem

import id.walt.crypto.keys.jwk.JWKKey

internal suspend fun printImportedPemStringInfo(pem: String) {
    val keyImportResult = JWKKey.importPEM(pem)
    println("Success: " + keyImportResult.isSuccess)
    if (keyImportResult.isSuccess) {
        println("Getting key from result...")
        val key = keyImportResult.getOrThrow()
        val keyDescription = "${key.keyType} " + if (key.hasPrivateKey) "private key" else "public key"
        println("Decoded ${keyDescription}: ${key.exportJWK()}" )
    } else {
        println(keyImportResult)
    }
}