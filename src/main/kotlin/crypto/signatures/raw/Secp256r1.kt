package crypto.signatures.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    signVerifyRawSecp256r1Key()
}

suspend fun signVerifyRawSecp256r1Key() {
    val payload = "This is an important message".encodeToByteArray()

    val key = JWKKey.generate(KeyType.secp256r1)
    val signature = key.signRaw(payload)

    val verificationResult = key.getPublicKey().verifyRaw(signature, payload)
    println(verificationResult)
}
