package crypto.key.decode.jwk

suspend fun main() {
    importSecp256k1Jwk()
}

suspend fun importSecp256k1Jwk() {
    val secp256k1JwkPrivateKeyString = "{\"kty\":\"EC\",\"d\":\"EXcfgjfhMfDJFcwKzWzyfuXYo26w2jxK9XbnT8Zy69M\",\"crv\":\"secp256k1\",\"kid\":\"mixubyTDAztHhaTHU154XWNa1QWMSwdT492jvxwOiIM\",\"x\":\"39HguYqzqpIvzhOV82Wfcq2ttx1wQc80ivzbZSsroOQ\",\"y\":\"7I0-pX6ehLC-X9IyBmm3S8z6b_PhqjJZNRey2ax-gBM\"}"

    println("Importing Secp256k1 key from JWK String...")
    printImportedJwkStringInfo(secp256k1JwkPrivateKeyString)
}
