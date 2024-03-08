package crypto.sign_verify

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.keys.LocalKeyMetadata
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

suspend fun main() {

    val payloadString = JsonObject(
        mapOf(
            "sub" to JsonPrimitive("16bb17e0-e733-4622-9384-122bc2fc6290"),
            "iss" to JsonPrimitive("http://localhost:3000"),
            "aud" to JsonPrimitive("TOKEN"),
        )
    )
    val key = LocalKey.generate(KeyType.secp256r1, LocalKeyMetadata())
    val signature = key.signJws(payloadString.toString().encodeToByteArray())

    println(signature)
}