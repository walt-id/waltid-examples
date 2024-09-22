package crypto.key.encode.pem

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    exportSecp256r1PEM()
}

suspend fun exportSecp256r1PEM() {
    val keyType = KeyType.secp256r1
    println("Generating key of type: $keyType")
    val privateKey = JWKKey.generate(keyType)

    //Export as PEM String
    println("Exporting key as PEM String...")
    println("$keyType PEM String:\n${privateKey.exportPEM()}\n")
}