package crypto.key.decode.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.utils.decodeBase58

suspend fun main() {
    importSecp256r1RawPublicKey()
}

suspend fun importSecp256r1RawPublicKey() {
    val keyType = KeyType.secp256r1
    val rawPublicKeyBase58String =
        "cPd6oAUrPrnWCf5mD97ex8psT1wH1onDFPu6L8L4pGXB"

    println("Importing Raw-encoded $keyType public key from Base58-encoded string: $rawPublicKeyBase58String")
    val publicKey = JWKKey.importRawPublicKey(
        type = keyType,
        rawPublicKey = rawPublicKeyBase58String.decodeBase58(),
    )
    println("Decoded $keyType public key as JWK Object : ${publicKey.exportJWKObject()}")
}
