package crypto.sign_verify

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend fun main() {
    sv_secp256r1_jws()
}

suspend fun sv_secp256r1_jws() {
    val payloadString = JsonObject(
        mapOf(
            "sub" to JsonPrimitive("16bb17e0-e733-4622-9384-122bc2fc6290"),
            "iss" to JsonPrimitive("http://localhost:3000"),
            "aud" to JsonPrimitive("TOKEN"),
        )
    )
    val key = JWKKey.generate(KeyType.secp256r1)
    val signature = key.signJws(payloadString.toString().encodeToByteArray())
    val verificationResult = key.getPublicKey().verifyJws(signature)

    println(verificationResult)
}
