package crypto.key.decode.pem

suspend fun main() {
    importSecp256r1PEM()
}

suspend fun importSecp256r1PEM() {
    //PEM-encoded private key
    val privateKeyPEMString = """
-----BEGIN PRIVATE KEY-----
MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBCeEpllNHkGwvLiLZ6
YOegc2k6wL2CaPLO1iYbdvAVZw==
-----END PRIVATE KEY-----
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEx/HbXb1zjeFnPK3Ulv88cpf63rH9
k4zDZCYa5+P7mV4kmzohTV14Do3OOD4ql70pViFqiPBoOehZxgMQe8PsiQ==
-----END PUBLIC KEY-----
""".trimIndent()
    println("Importing PEM-encoded Secp256r1 private key:\n${privateKeyPEMString}")
    printImportedPemStringInfo(privateKeyPEMString)

    //PEM-encoded public key
    val publicKeyPEMString = """
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEx/HbXb1zjeFnPK3Ulv88cpf63rH9
k4zDZCYa5+P7mV4kmzohTV14Do3OOD4ql70pViFqiPBoOehZxgMQe8PsiQ==
-----END PUBLIC KEY-----
    """.trimIndent()
    println("Importing PEM-encoded Secp256r1 public key:\n${publicKeyPEMString}")
    printImportedPemStringInfo(publicKeyPEMString)
}
