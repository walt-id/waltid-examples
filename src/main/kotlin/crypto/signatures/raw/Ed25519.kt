package crypto.signatures.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey

suspend fun main() {
    signVerifyRawEd25519Key()
}

suspend fun signVerifyRawEd25519Key() {
    val payload = "This is an important message".encodeToByteArray()

    val key = JWKKey.generate(KeyType.Ed25519)
    val signature = key.signRaw(payload)

    val verificationResult = key.getPublicKey().verifyRaw(signature, payload)
    println(verificationResult)
}
