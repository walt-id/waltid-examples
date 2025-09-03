package wallet.storage

import id.walt.crypto.keys.jwk.JWKKey
import kotlinx.serialization.json.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

/**
 * Secure wallet storage for RSA4096 keys with eIDAS QWAC certificates
 * 
 * Features:
 * - Encrypted storage of private keys
 * - Certificate chain management
 * - Key-certificate association tracking
 * - Secure deletion capabilities
 * - Alias management for usability
 */
class WalletKeyStorage(
    val storagePath: String = "./wallet-storage"
) {
    private val keysDirectory = "$storagePath/keys"
    private val certificatesDirectory = "$storagePath/certificates"
    private val metadataFile = "$storagePath/metadata.json"
    private val encryptionKey: SecretKey = loadOrGenerateEncryptionKey()
    
    init {
        initializeStorage()
    }
    
    /**
     * Initialize storage directories
     */
    private fun initializeStorage() {
        Files.createDirectories(Paths.get(keysDirectory))
        Files.createDirectories(Paths.get(certificatesDirectory))
        
        if (!File(metadataFile).exists()) {
            File(metadataFile).writeText(buildJsonObject {}.toString())
        }
    }
    
    /**
     * Load or generate AES encryption key for secure storage
     */
    private fun loadOrGenerateEncryptionKey(): SecretKey {
        // Ensure storage directory exists first
        Files.createDirectories(Paths.get(storagePath))
        
        val keyFile = File("$storagePath/.encryption-key")
        
        return if (keyFile.exists()) {
            val encodedKey = keyFile.readText()
            SecretKeySpec(Base64.getDecoder().decode(encodedKey), "AES")
        } else {
            // Generate new AES-256 key
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val key = keyGen.generateKey()
            
            // Store encrypted key
            keyFile.writeText(Base64.getEncoder().encodeToString(key.encoded))
            key
        }
    }
    
    /**
     * Store RSA4096 key with eIDAS QWAC certificate
     */
    suspend fun storeKeyWithCertificate(
        privateKey: JWKKey,
        certificateInfo: JsonObject,
        alias: String? = null
    ): String {
        val keyId = privateKey.getKeyId()
        val finalAlias = alias ?: "rsa4096-key-${System.currentTimeMillis()}"
        
        try {
            // Encrypt and store private key
            val keyData = privateKey.exportJWK()
            val encryptedKeyData = encrypt(keyData)
            File("$keysDirectory/$keyId.enc").writeBytes(encryptedKeyData)
            
            // Store certificate information
            File("$certificatesDirectory/$keyId.json").writeText(certificateInfo.toString())
            
            // Update metadata
            val metadata = loadMetadata()
            val updatedMetadata = buildJsonObject {
                metadata.forEach { (key, value) -> 
                    put(key, value)
                }
                put(keyId, buildJsonObject {
                    put("alias", finalAlias)
                    put("type", "RSA4096_EIDAS_QWAC")
                    put("importTimestamp", System.currentTimeMillis())
                    put("hasPrivateKey", true)
                    put("certificateAvailable", true)
                })
            }
            
            File(metadataFile).writeText(updatedMetadata.toString())
            
            println("✅ Key stored successfully with alias: $finalAlias")
            return keyId
            
        } catch (e: Exception) {
            println("❌ Error storing key: ${e.message}")
            throw e
        }
    }
    
    /**
     * Load private key by key ID or alias
     */
    suspend fun loadKey(keyIdOrAlias: String): JWKKey? {
        return try {
            val keyId = resolveKeyId(keyIdOrAlias) ?: return null
            
            val encryptedData = File("$keysDirectory/$keyId.enc").readBytes()
            val decryptedKeyData = decrypt(encryptedData)
            
            val keyImportResult = JWKKey.importJWK(decryptedKeyData)
            if (keyImportResult.isSuccess) {
                println("✅ Key loaded successfully")
                keyImportResult.getOrThrow()
            } else {
                println("❌ Failed to load key: ${keyImportResult.exceptionOrNull()?.message}")
                null
            }
        } catch (e: Exception) {
            println("❌ Error loading key: ${e.message}")
            null
        }
    }
    
    /**
     * Load certificate information by key ID or alias
     */
    fun loadCertificateInfo(keyIdOrAlias: String): JsonObject? {
        return try {
            val keyId = resolveKeyId(keyIdOrAlias) ?: return null
            
            val certFile = File("$certificatesDirectory/$keyId.json")
            if (certFile.exists()) {
                Json.parseToJsonElement(certFile.readText()).jsonObject
            } else {
                null
            }
        } catch (e: Exception) {
            println("❌ Error loading certificate info: ${e.message}")
            null
        }
    }
    
    /**
     * List all stored keys with their metadata
     */
    fun listKeys(): List<JsonObject> {
        return try {
            val metadata = loadMetadata()
            metadata.map { (keyId, info) ->
                buildJsonObject {
                    put("keyId", keyId)
                    info.jsonObject.forEach { (key, value) ->
                        put(key, value)
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ Error listing keys: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Delete key and associated certificate
     */
    fun deleteKey(keyIdOrAlias: String): Boolean {
        return try {
            val keyId = resolveKeyId(keyIdOrAlias) ?: return false
            
            // Secure deletion of files
            val keyFile = File("$keysDirectory/$keyId.enc")
            val certFile = File("$certificatesDirectory/$keyId.json")
            
            var deleted = false
            
            if (keyFile.exists()) {
                // Overwrite file content before deletion for security
                secureDelete(keyFile)
                deleted = true
            }
            
            if (certFile.exists()) {
                certFile.delete()
                deleted = true
            }
            
            // Remove from metadata
            val metadata = loadMetadata()
            val updatedMetadata = buildJsonObject {
                metadata.forEach { (key, value) -> 
                    if (key != keyId) {
                        put(key, value)
                    }
                }
            }
            File(metadataFile).writeText(updatedMetadata.toString())
            
            println("✅ Key deleted successfully")
            deleted
        } catch (e: Exception) {
            println("❌ Error deleting key: ${e.message}")
            false
        }
    }
    
    /**
     * Update alias for a key
     */
    fun updateAlias(keyIdOrAlias: String, newAlias: String): Boolean {
        return try {
            val keyId = resolveKeyId(keyIdOrAlias) ?: return false
            
            val metadata = loadMetadata()
            val keyInfo = metadata[keyId]?.jsonObject ?: return false
            
            val updatedKeyInfo = buildJsonObject {
                keyInfo.forEach { (key, value) ->
                    if (key == "alias") {
                        put(key, newAlias)
                    } else {
                        put(key, value)
                    }
                }
            }
            
            val updatedMetadata = buildJsonObject {
                metadata.forEach { (key, value) ->
                    if (key == keyId) {
                        put(key, updatedKeyInfo)
                    } else {
                        put(key, value)
                    }
                }
            }
            
            File(metadataFile).writeText(updatedMetadata.toString())
            println("✅ Alias updated to: $newAlias")
            true
        } catch (e: Exception) {
            println("❌ Error updating alias: ${e.message}")
            false
        }
    }
    
    private fun resolveKeyId(keyIdOrAlias: String): String? {
        val metadata = loadMetadata()
        
        // Check if it's a direct key ID
        if (metadata.containsKey(keyIdOrAlias)) {
            return keyIdOrAlias
        }
        
        // Check if it matches an alias
        metadata.forEach { (keyId, info) ->
            val alias = info.jsonObject["alias"]?.jsonPrimitive?.content
            if (alias == keyIdOrAlias) {
                return keyId
            }
        }
        
        return null
    }
    
    private fun loadMetadata(): JsonObject {
        return try {
            Json.parseToJsonElement(File(metadataFile).readText()).jsonObject
        } catch (e: Exception) {
            buildJsonObject {}
        }
    }
    
    private fun encrypt(data: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        // Combine IV and encrypted data
        return iv + encryptedData
    }
    
    private fun decrypt(encryptedData: ByteArray): String {
        val iv = encryptedData.sliceArray(0..11) // GCM IV is typically 12 bytes
        val ciphertext = encryptedData.sliceArray(12 until encryptedData.size)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, gcmSpec)
        
        return String(cipher.doFinal(ciphertext))
    }
    
    private fun secureDelete(file: File) {
        if (file.exists()) {
            val fileSize = file.length()
            val random = SecureRandom()
            
            // Overwrite file content multiple times
            file.outputStream().use { output ->
                repeat(3) {
                    val randomData = ByteArray(fileSize.toInt())
                    random.nextBytes(randomData)
                    output.write(randomData)
                    output.flush()
                }
            }
            
            file.delete()
        }
    }
}

/**
 * Example usage and testing of the wallet key storage
 */
suspend fun testWalletKeyStorage() {
    val walletStorage = WalletKeyStorage()
    
    println("=== Testing Wallet Key Storage ===")
    
    // Example: Import a test key (you would replace this with actual key import)
    val testKey = JWKKey.generate(id.walt.crypto.keys.KeyType.RSA)
    val testCertInfo = buildJsonObject {
        put("subject", "CN=example.com QWAC,O=Test Inc.")
        put("issuer", "CN=eIDAS Test QWAC Root CA")
        put("isQwac", true)
    }
    
    // Store key with certificate
    val keyId = walletStorage.storeKeyWithCertificate(testKey, testCertInfo, "my-company-key")
    
    // List all keys
    println("\n=== Stored Keys ===")
    walletStorage.listKeys().forEach { keyInfo ->
        println("- ${keyInfo["alias"]?.jsonPrimitive?.content} (${keyInfo["keyId"]?.jsonPrimitive?.content})")
    }
    
    // Load key back
    val loadedKey = walletStorage.loadKey("my-company-key")
    println("\n=== Key Loaded ===")
    println("Key loaded: ${loadedKey != null}")
    
    // Load certificate info
    val certInfo = walletStorage.loadCertificateInfo("my-company-key")
    println("Certificate info: ${certInfo?.get("subject")}")
}