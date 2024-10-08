package crypto.key.decode.pem

suspend fun main() {
    importRSAPEM()
}

suspend fun importRSAPEM() {
    //PEM-encoded private key
    val privateKeyPEMString = """
-----BEGIN RSA PRIVATE KEY-----
MIIEpAIBAAKCAQEAqV+fGqTo8r6L52sIJpt44bxLkODaF0/wvIL/eYYDL55H+Ap+
b1q4pd4YyZb7pARor/1mob6sMRnnAr5htmO1XucmKBEiNY+12zza0q9smjLm3+eN
qq+8PgsEqBz4lU1YIBeQzsCR0NTa3J3OHfr+bADVystQeonSPoRLqSoO78oAtonQ
WLX1MUfS9778+ECcxlM21+JaUjqMD0nQR6wl8L6oWGcR7PjcjPQAyuS/ASTy7MO0
SqunpkGzj/H7uFbK9Np/dLIOr9ZqrkCSdioA/PgDyk36E8ayuMnN1HDy4ak/Q7yE
X4R/C75T0JxuuYio06hugwyREgOQNID+DVUoLwIDAQABAoIBAQCGd1HbV11RipGL
0l+QNxJLNLBRfxHmPCMFpoKougpBfcnpVHt4cG/zz1WihemWF6H9RpJ6iuQtv0C1
3uu4X4SYqa6TVLbyCvv36GJZrcfsy8ibrju8bPRn1VuHFCkOb28tW0gtvJiHUNXJ
HMeM6b2fhTI2ZB+qiUyPMXzX+noNR+mQxwMElKWxOZEoxY8bS7yYWbxoH1l+1uvI
87fpA6fLuDj8zxJkbXLEZNvKmPINM/1aoyYeloL7rqAuzOPLHARtWHr2b4ewMSzt
CD2gxUbYWLC0NJcgCnB8uTa/ZiTj5N4APj2tnPD5BwnTKo95V2rxyDIU6HBf5w+1
AibxfsCBAoGBANGzaSc8zjoM6amlpG/uT7lIGPQHnVLJ83tuALbpF2jfVWO3uQS2
HGxNdURnso/JJIaaLb2UOlq2BH70pH8Qb6FXNavOhV4qpacJlC/v4zuAW4BEjivX
mlj9Ylplja2aasHZyHhd1WR3wiKBcUVf02/0qfFDUrnhYAh+iFSZX8EhAoGBAM7E
2q0WKPZSxPF786Rs6Y7azh7Jy7pNp+Rlz+jNl/PQS/MnPP+RaffZvxJvbxgDoWQS
vQHLWbmRBklVYX1dSJhsNe7S8kGUggL0s0KWUmKU/RA3mYJ/6EqK31IDCqaZtV+K
5eWM+5TEdZIZY3Srf1+oBQOgAy9ofwEStWwUY69PAoGBAJTn6k5jfil4i9/ccHTO
66usx5NZaNyl7RCDn1xDDk148UCa8HWo/2vkYNYPMJurgBVYnAxXmkxZnb2s6LYV
rL8Ll2AFiWzBqdmAEssrc9cHoXHmvHHjaoWwf8ui+0UANriqdhEKyIHMDH3GHvHd
Rt3kBVz9qlu17yR4/UPdmUIhAoGAE7TDOpfQE5nT10f+8n7Gy6yi1GBbIEhiZewm
IoPlpYEGnAfzUlAjj1GbWkBwkBNYgFcg2FjvFjZyKO8QOYh4cL5vbXGBUSq8MVfs
9b2p4Gdervr9kGhsVR5jJkfP7gzcMlzkiDoliAopQmFVDzuBCjbTM4M+inglEo8b
508SKRUCgYBomwMGHFAUNulCvbROODEibAGyVU/3Ehq7LedwKaMeLHoVzWXnWAvv
VXU5YaddpK0Z+hbLSAqRScm5Tf9f/ED9DDMBc8lhr6O5GUPwHFN/uuZH8cL5eJV3
I2eRTdp8e0ridKFLYS43YRc6tgOttCgDXf9roi2T/nm8OkneNyLBLw==
-----END RSA PRIVATE KEY-----
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqV+fGqTo8r6L52sIJpt4
4bxLkODaF0/wvIL/eYYDL55H+Ap+b1q4pd4YyZb7pARor/1mob6sMRnnAr5htmO1
XucmKBEiNY+12zza0q9smjLm3+eNqq+8PgsEqBz4lU1YIBeQzsCR0NTa3J3OHfr+
bADVystQeonSPoRLqSoO78oAtonQWLX1MUfS9778+ECcxlM21+JaUjqMD0nQR6wl
8L6oWGcR7PjcjPQAyuS/ASTy7MO0SqunpkGzj/H7uFbK9Np/dLIOr9ZqrkCSdioA
/PgDyk36E8ayuMnN1HDy4ak/Q7yEX4R/C75T0JxuuYio06hugwyREgOQNID+DVUo
LwIDAQAB
-----END PUBLIC KEY----- 
""".trimIndent()
    println("Importing PEM-encoded RSA private key:\n${privateKeyPEMString}")
    printImportedPemStringInfo(privateKeyPEMString)

    //PEM-encoded public key
    val publicKeyPEMString = """
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqV+fGqTo8r6L52sIJpt4
4bxLkODaF0/wvIL/eYYDL55H+Ap+b1q4pd4YyZb7pARor/1mob6sMRnnAr5htmO1
XucmKBEiNY+12zza0q9smjLm3+eNqq+8PgsEqBz4lU1YIBeQzsCR0NTa3J3OHfr+
bADVystQeonSPoRLqSoO78oAtonQWLX1MUfS9778+ECcxlM21+JaUjqMD0nQR6wl
8L6oWGcR7PjcjPQAyuS/ASTy7MO0SqunpkGzj/H7uFbK9Np/dLIOr9ZqrkCSdioA
/PgDyk36E8ayuMnN1HDy4ak/Q7yEX4R/C75T0JxuuYio06hugwyREgOQNID+DVUo
LwIDAQAB
-----END PUBLIC KEY-----     
    """.trimIndent()
    println("Importing PEM-encoded RSA public key:\n${publicKeyPEMString}")
    printImportedPemStringInfo(publicKeyPEMString)
}
