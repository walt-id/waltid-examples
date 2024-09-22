package crypto.key.decode.pem

suspend fun main() {
    importSecp256k1PEM()
}

suspend fun importSecp256k1PEM() {
    //PEM-encoded private key
    val privateKeyPEMString = """
-----BEGIN PRIVATE KEY-----
MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCBPuR9rHGY1GfEZ8AfBy/X5
ZmRNCoZwqxGJJLsdvhZm0w==
-----END PRIVATE KEY-----
-----BEGIN PUBLIC KEY-----
MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAE7aTT81hGnDBfxDJvIqgME5EW7LyIzSlZ
jpJzb9L1T2Bq/CVwB8X0S9MrnA2zNKRqPEq/GdT4ypK3KJBKcp6MhQ==
-----END PUBLIC KEY-----  
""".trimIndent()
    println("Importing PEM-encoded Secp256k1 private key:\n${privateKeyPEMString}")
    printImportedPemStringInfo(privateKeyPEMString)

    //PEM-encoded public key
    val publicKeyPEMString = """
-----BEGIN PUBLIC KEY-----
MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAE7aTT81hGnDBfxDJvIqgME5EW7LyIzSlZ
jpJzb9L1T2Bq/CVwB8X0S9MrnA2zNKRqPEq/GdT4ypK3KJBKcp6MhQ==
-----END PUBLIC KEY-----  
    """.trimIndent()
    println("Importing PEM-encoded Secp256k1 public key:\n${publicKeyPEMString}")
    printImportedPemStringInfo(publicKeyPEMString)
}


