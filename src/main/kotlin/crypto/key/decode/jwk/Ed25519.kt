package crypto.key.decode.jwk

suspend fun main() {
    importEd25519Jwk()
}

suspend fun importEd25519Jwk() {
    val ed25519JwkPrivateKeyString = "{\"kty\":\"OKP\",\"d\":\"CJaLP-XF1lWGl9Vh22oFi28aHr9euqiCtpwJwDskA0k\",\"crv\":\"Ed25519\",\"kid\":\"H-jin4WOk-NHHmyCeNyemNNiIMQGFRPFVeRjwPd3yc4\",\"x\":\"8ju1e4kE5NW6rd99jj-59QToPQo1LYLodNlAuiQHPss\"}"

    println("Importing Ed25519 key from JWK String...")
    printImportedJwkStringInfo(ed25519JwkPrivateKeyString)
}
