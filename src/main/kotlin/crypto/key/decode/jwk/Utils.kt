package crypto.key.decode.jwk

import id.walt.crypto.keys.jwk.JWKKey

internal suspend fun printImportedJwkStringInfo(jwk: String) {
    val keyImportResult = JWKKey.importJWK(jwk)
    println("Success: " + keyImportResult.isSuccess)
    if (keyImportResult.isSuccess) {
        println("Getting key from result...")
        val key = keyImportResult.getOrThrow()
        val keyDescription = "${key.keyType} " + if (key.hasPrivateKey) "private key" else "public key"
        println("Decoded ${keyDescription}: ${key.exportJWK()}" )
    }
}