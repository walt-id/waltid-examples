package crypto.key.encode.jwk

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    exportEd25519Jwk()
}

suspend fun exportEd25519Jwk() {
    val keyType = KeyType.Ed25519
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