package crypto.signatures.jws

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend fun main() {
    signVerifyJwsRSAKey()
}

suspend fun signVerifyJwsRSAKey() {
    val payload = JsonObject(
        mapOf(
            "sub" to JsonPrimitive("16bb17e0-e733-4622-9384-122bc2fc6290"),
            "iss" to JsonPrimitive("http://localhost:3000"),
            "aud" to JsonPrimitive("TOKEN"),
        )
    )
    val key = JWKKey.generate(KeyType.RSA)

    println("JWS Sign:")
    println("Payload: $payload")
    println("Signing Key: ${key.exportJWK()}")
    val signature = key.signJws(payload.toString().encodeToByteArray())
    println("Signature: $signature")

    println("JWS Verify:")
    println("Verification Key: ${key.getPublicKey().exportJWK()}")
    val verificationResult = key.getPublicKey().verifyJws(signature)
    println("Result: $verificationResult")
}
