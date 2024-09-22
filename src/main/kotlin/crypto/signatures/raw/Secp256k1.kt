package crypto.signatures.raw

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import io.ktor.util.*

suspend fun main() {
    signVerifyRawSecp256k1Key()
}

suspend fun signVerifyRawSecp256k1Key() {
    val payload = "This is an important message"
    val key = JWKKey.generate(KeyType.secp256k1)

    println("Raw Sign:")
    println("Payload: $payload")
    println("Signing Key: ${key.exportJWK()}")
    val signature = key.signRaw(payload.encodeToByteArray())
    println("Signature (base64): ${signature.encodeBase64()}")

    println("Raw Verify:")
    println("Verification Key: ${key.getPublicKey().exportJWK()}")
    val verificationResult = key.getPublicKey().verifyRaw(signature, payload.encodeToByteArray())
    println("Result: $verificationResult")
}
