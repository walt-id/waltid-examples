package crypto.key.decode.jwk

suspend fun main() {
    importSecp256r1Jwk()
}

suspend fun importSecp256r1Jwk() {
    val secp256r1JwkPrivateKeyString = "{\"kty\":\"EC\",\"d\":\"YBbyAktE6eGnUoJs3BKeajPvdN-bimX4e4S-Lhs3NRI\",\"crv\":\"P-256\",\"kid\":\"2d_fdfE2YlUvVPZpyNHgmzqnwAtDueIXDoRCbxOY_sQ\",\"x\":\"rUe9ncPqFiaaeEKorQZ7iVuYJ7tKJeZ5VMzfIaTXe2s\",\"y\":\"RJJsAL7DG0pidNeqEo1HrU_ow6DwW4gF5TjWgAYph2g\"}"

    println("Importing Secp256r1 key from JWK String...")
    printImportedJwkStringInfo(secp256r1JwkPrivateKeyString)
}
