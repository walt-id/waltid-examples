package crypto.key.encode.jwk

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    exportSecp256r1Jwk()
}

suspend fun exportSecp256r1Jwk() {
    val keyType = KeyType.secp256r1
    println("Generating key of type: $keyType")
    val privateKey = JWKKey.generate(keyType)

    println("$keyType Key id: " + privateKey.getKeyId())
    //Export as JWK String
    println("Exporting key as JWK String...")
    println("$keyType JWK String: ${privateKey.exportJWK()}\n")

    //Export as JWK Object
    println("Exporting key as JWK Object...")
    println("$keyType JWK Object: ${privateKey.exportJWKObject()}\n")

    //Export as Pretty-Printed JWK Object
    println("Exporting key as Pretty-Printed JWK Object...")
    println("$keyType Pretty-Printed JWK Object: ${privateKey.exportJWKPretty()}\n")
}