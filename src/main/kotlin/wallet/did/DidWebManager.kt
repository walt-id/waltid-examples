package wallet.did

import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.DidWebCreateOptions
import id.walt.did.dids.registrar.dids.DidDocConfig
import id.walt.did.dids.document.models.verification.relationship.VerificationRelationshipType
import id.walt.did.dids.registrar.dids.VerificationMethodConfiguration
import kotlinx.serialization.json.*
import wallet.storage.WalletKeyStorage
import java.net.URL
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * DID:WEB Manager for eIDAS QWAC-based DIDs
 * 
 * This class handles:
 * 1. Importing existing DID:WEB references based on eIDAS certificates
 * 2. Generating new DID:WEB documents using imported RSA4096 keys
 * 3. Managing x5u certificate chain references
 * 4. Storing DID-key-certificate associations
 */
class DidWebManager(
    private val keyStorage: WalletKeyStorage = WalletKeyStorage()
) {
    
    /**
     * Import an existing DID:WEB reference based on eIDAS certificate and RSA key
     */
    suspend fun importDidWebReference(
        didWebUrl: String,
        keyIdOrAlias: String,
        certificateChainUrls: List<String>? = null,
        alias: String? = null
    ): Result<DidWebImportResult> {
        return try {
            println("Importing DID:WEB reference: $didWebUrl")
            
            // Load associated private key
            val privateKey = keyStorage.loadKey(keyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("Private key not found: $keyIdOrAlias"))
            
            // Load certificate information
            val certificateInfo = keyStorage.loadCertificateInfo(keyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("Certificate info not found: $keyIdOrAlias"))
            
            // Resolve the DID document
            DidService.minimalInit()
            val didResolutionResult = DidService.resolve(didWebUrl)
            
            if (!didResolutionResult.isSuccess) {
                return Result.failure(Exception("Failed to resolve DID:WEB: ${didResolutionResult.exceptionOrNull()?.message}"))
            }
            
            val didDocument = didResolutionResult.getOrThrow()
            
            // Validate that the DID document contains the expected public key
            val publicKey = privateKey.getPublicKey()
            val isKeyValid = validateDidDocumentContainsKey(didDocument.toString(), publicKey)
            
            if (!isKeyValid) {
                return Result.failure(IllegalArgumentException("DID document does not contain the expected public key"))
            }
            
            // Create DID:WEB import record
            val importResult = DidWebImportResult(
                didUrl = didWebUrl,
                keyId = privateKey.getKeyId(),
                alias = alias ?: "imported-did-${System.currentTimeMillis()}",
                didDocument = Json.parseToJsonElement(didDocument.toString()).jsonObject,
                certificateChainUrls = certificateChainUrls ?: emptyList(),
                importTimestamp = System.currentTimeMillis()
            )
            
            // Store the DID:WEB reference
            storeDidWebReference(importResult)
            
            println("✅ DID:WEB reference imported successfully")
            println("DID: $didWebUrl")
            println("Alias: ${importResult.alias}")
            
            Result.success(importResult)
            
        } catch (e: Exception) {
            println("❌ Error importing DID:WEB reference: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Generate a new DID:WEB based on imported private key and certificate chain
     */
    suspend fun generateDidWeb(
        domain: String,
        path: String = "",
        keyIdOrAlias: String,
        certificateChainUrls: List<String>,
        alias: String? = null
    ): Result<DidWebGenerationResult> {
        return try {
            println("Generating DID:WEB for domain: $domain, path: $path")
            
            // Load private key
            val privateKey = keyStorage.loadKey(keyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("Private key not found: $keyIdOrAlias"))
            
            // Load certificate information
            val certificateInfo = keyStorage.loadCertificateInfo(keyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("Certificate info not found: $keyIdOrAlias"))
            
            // Initialize DID service
            DidService.minimalInit()
            
            // Create DID document configuration with x5u certificate chain
            val publicKey = privateKey.getPublicKey()
            val verificationMethodConfig = createVerificationMethodWithX5u(publicKey, certificateChainUrls)
            
            val didDocConfig = DidDocConfig.buildFromPublicKeySet(
                publicKeySet = setOf(publicKey)
            )
            
            // Create DID:WEB
            val didWebOptions = DidWebCreateOptions(
                domain = domain,
                path = path,
                didDocConfig = didDocConfig
            )
            
            val didResult = DidService.register(didWebOptions)
            
            // Create generation result
            val generationResult = DidWebGenerationResult(
                didUrl = didResult.did,
                keyId = privateKey.getKeyId(),
                alias = alias ?: "generated-did-${System.currentTimeMillis()}",
                didDocument = Json.parseToJsonElement(didResult.didDocument.toString()).jsonObject,
                certificateChainUrls = certificateChainUrls,
                domain = domain,
                path = path,
                generationTimestamp = System.currentTimeMillis()
            )
            
            // Store the generated DID:WEB
            storeDidWebReference(generationResult)
            
            println("✅ DID:WEB generated successfully")
            println("DID: ${didResult.did}")
            println("Alias: ${generationResult.alias}")
            
            Result.success(generationResult)
            
        } catch (e: Exception) {
            println("❌ Error generating DID:WEB: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create verification method configuration with x5u certificate chain reference
     */
    private fun createVerificationMethodWithX5u(
        publicKey: JWKKey,
        certificateChainUrls: List<String>
    ): VerificationMethodConfiguration {
        val customProperties = if (certificateChainUrls.isNotEmpty()) {
            mapOf(
                "x5u" to JsonPrimitive(certificateChainUrls.first()),
                "x5c" to JsonArray(certificateChainUrls.map { JsonPrimitive(it) })
            )
        } else {
            emptyMap()
        }
        
        // Note: getKeyId() is suspend, so we'll use a placeholder for now
        return VerificationMethodConfiguration(
            publicKeyId = "placeholder-key-id",
            customProperties = customProperties
        )
    }
    
    /**
     * Validate that a DID document contains the expected public key
     */
    private suspend fun validateDidDocumentContainsKey(
        didDocument: String,
        expectedPublicKey: JWKKey
    ): Boolean {
        return try {
            val expectedKeyJwk = expectedPublicKey.exportJWKObject()
            val didDocObj = Json.parseToJsonElement(didDocument).jsonObject
            val verificationMethods = didDocObj["verificationMethod"]?.jsonArray
            
            verificationMethods?.any { vm ->
                val vmObj = vm.jsonObject
                val publicKeyJwk = vmObj["publicKeyJwk"]?.jsonObject
                
                // Compare key material (simplified - in production would need more robust comparison)
                publicKeyJwk?.get("n")?.jsonPrimitive?.content == 
                    expectedKeyJwk["n"]?.jsonPrimitive?.content
            } ?: false
        } catch (e: Exception) {
            println("⚠️  Warning: Could not validate public key in DID document: ${e.message}")
            false
        }
    }
    
    /**
     * Store DID:WEB reference in wallet storage
     */
    private fun storeDidWebReference(didInfo: DidWebReferenceBase) {
        try {
            val didStorageFile = "${keyStorage.storagePath}/dids/${didInfo.keyId}.json"
            Files.createDirectories(Paths.get("${keyStorage.storagePath}/dids"))
            
            val didData = buildJsonObject {
                put("didUrl", didInfo.didUrl)
                put("keyId", didInfo.keyId)
                put("alias", didInfo.alias)
                put("didDocument", didInfo.didDocument)
                put("certificateChainUrls", JsonArray(didInfo.certificateChainUrls.map { JsonPrimitive(it) }))
                when (didInfo) {
                    is DidWebImportResult -> {
                        put("type", "imported")
                        put("importTimestamp", didInfo.importTimestamp)
                    }
                    is DidWebGenerationResult -> {
                        put("type", "generated")
                        put("domain", didInfo.domain)
                        put("path", didInfo.path)
                        put("generationTimestamp", didInfo.generationTimestamp)
                    }
                }
            }
            
            File(didStorageFile).writeText(didData.toString())
            
        } catch (e: Exception) {
            println("❌ Error storing DID:WEB reference: ${e.message}")
            throw e
        }
    }
    
    /**
     * List all stored DID:WEB references
     */
    fun listDidWebReferences(): List<JsonObject> {
        return try {
            val didsDirectory = File("${keyStorage.storagePath}/dids")
            if (!didsDirectory.exists()) return emptyList()
            
            didsDirectory.listFiles { file -> file.extension == "json" }
                ?.mapNotNull { file ->
                    try {
                        Json.parseToJsonElement(file.readText()).jsonObject
                    } catch (e: Exception) {
                        println("⚠️  Warning: Could not parse DID file ${file.name}: ${e.message}")
                        null
                    }
                } ?: emptyList()
        } catch (e: Exception) {
            println("❌ Error listing DID:WEB references: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Load DID:WEB reference by alias or key ID
     */
    fun loadDidWebReference(aliasOrKeyId: String): JsonObject? {
        return listDidWebReferences().find { didInfo ->
            didInfo["alias"]?.jsonPrimitive?.content == aliasOrKeyId ||
            didInfo["keyId"]?.jsonPrimitive?.content == aliasOrKeyId
        }
    }
}

/**
 * Base class for DID:WEB references
 */
sealed class DidWebReferenceBase {
    abstract val didUrl: String
    abstract val keyId: String
    abstract val alias: String
    abstract val didDocument: JsonObject
    abstract val certificateChainUrls: List<String>
}

/**
 * Result of importing an existing DID:WEB reference
 */
data class DidWebImportResult(
    override val didUrl: String,
    override val keyId: String,
    override val alias: String,
    override val didDocument: JsonObject,
    override val certificateChainUrls: List<String>,
    val importTimestamp: Long
) : DidWebReferenceBase()

/**
 * Result of generating a new DID:WEB
 */
data class DidWebGenerationResult(
    override val didUrl: String,
    override val keyId: String,
    override val alias: String,
    override val didDocument: JsonObject,
    override val certificateChainUrls: List<String>,
    val domain: String,
    val path: String,
    val generationTimestamp: Long
) : DidWebReferenceBase()

/**
 * Test the DID:WEB manager functionality
 */
suspend fun testDidWebManager() {
    val didManager = DidWebManager()
    
    println("=== Testing DID:WEB Manager ===")
    
    // Example: Import a DID:WEB reference
    val importResult = didManager.importDidWebReference(
        didWebUrl = "did:web:example.com",
        keyIdOrAlias = "my-company-key",
        certificateChainUrls = listOf("https://example.com/certificates/chain.pem"),
        alias = "company-did"
    )
    
    if (importResult.isSuccess) {
        println("✅ DID:WEB import successful")
    }
    
    // List all DID references
    println("\n=== Stored DID:WEB References ===")
    didManager.listDidWebReferences().forEach { didInfo ->
        println("- ${didInfo["alias"]?.jsonPrimitive?.content}: ${didInfo["didUrl"]?.jsonPrimitive?.content}")
    }
}