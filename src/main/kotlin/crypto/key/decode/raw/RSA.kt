package crypto.key.decode.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.decodeBase58

suspend fun main() {
    importRSARawPublicKey()
}

suspend fun importRSARawPublicKey() {
    val keyType = KeyType.RSA
    //force add of BouncyCastle security provider - temporary
    JWKKey.generate(keyType)
    val rawPublicKeyBase58String =
        "4e1BUTgGBfqVWpVuCyERhiYcKSRhu3mmX566DwrrtjvRBC8VMgQncb9mJ2cW4X1jT4KDNsT8duAB6Lv69gEBJNWkSCyeMfYuXNaZt39RaHJLcwLH5CrAeUAmmhqPSKLcV7FiKmfxKJTAFCYy2ZBDoh1UafMr55JQ36oEzd6u83TqZrco4xBodwHQcd1eyKmwmSYkyT2Sd95W5TQZje7jTLkqfpfoZJJ4PMd7P8BJmdbKkeixZi9BSTNA7VaADmTjvs4GfJ5JaFkyU3iiTK4YMHtiG2zHwTzRJ2huu1u6yrijVZHznDRrQdPYgsmv3f7CWVMV7KQYDb6B5dxNFFMNdZL1BiBPaavGnw35E2Yvcug5pjwMa"

    println("Importing Raw-encoded $keyType public key from Base58-encoded string: $rawPublicKeyBase58String")
    val publicKey = JWKKey.importRawPublicKey(
        type = keyType,
        rawPublicKey = rawPublicKeyBase58String.decodeBase58(),
    )
    println("Decoded $keyType public key as JWK Object : ${publicKey.exportJWKObject()}")
}
