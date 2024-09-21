package crypto.key.export


import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    exportJwkPretty()
}

suspend fun exportJwkPretty() {
    println("Generating key: RSA")
    val rsaPrivateKey = JWKKey.generate(KeyType.RSA)
    printPrettyExportedJwkInfo(rsaPrivateKey)

    println("Generating key: Ed25519")
    val ed25519PrivateKey = JWKKey.generate(KeyType.Ed25519)
    printPrettyExportedJwkInfo(ed25519PrivateKey)

    println("Generating key: secp256k1")
    val secp256k1PrivateKey = JWKKey.generate(KeyType.secp256k1)
    printPrettyExportedJwkInfo(secp256k1PrivateKey)

    println("Generating key: secp256r1")
    val secp256r1PrivateKey = JWKKey.generate(KeyType.secp256r1)
    printPrettyExportedJwkInfo(secp256r1PrivateKey)
}

private suspend fun printPrettyExportedJwkInfo(key: JWKKey) {
    println("${key.keyType} Key id: " + key.getKeyId())
    println("Exporting ${key.keyType} as JWK Pretty Object...")
    println("${key.keyType} JWK Pretty Object: ${key.exportJWKPretty()}\n")
}
