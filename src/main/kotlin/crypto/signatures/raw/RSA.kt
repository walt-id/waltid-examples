package crypto.signatures.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    signVerifyRawRSAKey()
}

suspend fun signVerifyRawRSAKey() {
    val payload = "This is an important message".encodeToByteArray()

    val key = JWKKey.generate(KeyType.RSA)
    val signature = key.signRaw(payload)

    val verificationResult = key.getPublicKey().verifyRaw(signature, payload)
    println(verificationResult)
}
