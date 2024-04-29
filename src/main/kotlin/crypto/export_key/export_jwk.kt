package crypto.export_key


import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    export_jwk()
}

suspend fun export_jwk() {
    println("Generating key: RSA")
    val key = JWKKey.generate(KeyType.RSA)
    println("Key id: " + key.getKeyId())
    println("Exporting JWK...")
    val exportedJwk = key.exportJWK()
    println("JWK: $exportedJwk \n")

    println("Generating key: Ed25519")
    val key1 = JWKKey.generate(KeyType.Ed25519)
    println("Key id: " + key1.getKeyId())
    println("Exporting JWK...")
    val exportedJwk1 = key1.exportJWK()
    println("JWK: $exportedJwk1\n")


    println("Generating key: secp256k1")
    val key2 = JWKKey.generate(KeyType.secp256k1)
    println("Key id: " + key2.getKeyId())
    println("Exporting JWK...")
    val exportedJwk2 = key2.exportJWK()
    println("JWK: $exportedJwk2\n")


    println("Generating key: secp256r1")
    val key3 = JWKKey.generate(KeyType.secp256r1)
    println("Key id: " + key3.getKeyId())
    println("Exporting JWK...")
    val exportedJwk3 = key3.exportJWK()
    println("JWK: $exportedJwk3\n")

}
