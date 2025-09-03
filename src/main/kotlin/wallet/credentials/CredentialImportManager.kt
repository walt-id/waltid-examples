package wallet.credentials

import id.walt.w3c.vc.vcs.W3CVC
import kotlinx.serialization.json.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Credential Import Manager for wallet storage
 * 
 * Handles import and management of:
 * - LegalPerson credentials
 * - LRN (Legal Registration Number) credentials  
 * - Terms&Conditions credentials
 * 
 * Required for Gaia-X VP creation and compliance
 */
class CredentialImportManager(
    private val storagePath: String = "./wallet-storage"
) {
    private val credentialsDirectory = "$storagePath/credentials"
    private val metadataFile = "$storagePath/credentials-metadata.json"
    
    init {
        initializeStorage()
    }
    
    private fun initializeStorage() {
        Files.createDirectories(Paths.get(credentialsDirectory))
        
        if (!File(metadataFile).exists()) {
            File(metadataFile).writeText(buildJsonObject {}.toString())
        }
    }
    
    /**
     * Import LegalPerson credential
     * 
     * LegalPerson credentials contain organization identity information
     * required for Gaia-X compliance
     */
    suspend fun importLegalPersonCredential(
        credentialJwt: String,
        alias: String? = null
    ): Result<CredentialImportResult> {
        return importCredential(
            credentialJwt = credentialJwt,
            credentialType = CredentialType.LEGAL_PERSON,
            alias = alias ?: "legal-person-${System.currentTimeMillis()}"
        )
    }
    
    /**
     * Import LRN (Legal Registration Number) credential
     * 
     * LRN credentials contain legal registration information
     * for the organization
     */
    suspend fun importLrnCredential(
        credentialJwt: String,
        alias: String? = null
    ): Result<CredentialImportResult> {
        return importCredential(
            credentialJwt = credentialJwt,
            credentialType = CredentialType.LRN,
            alias = alias ?: "lrn-${System.currentTimeMillis()}"
        )
    }
    
    /**
     * Import Terms&Conditions credential
     * 
     * T&C credentials contain compliance and agreement information
     */
    suspend fun importTermsAndConditionsCredential(
        credentialJwt: String,
        alias: String? = null
    ): Result<CredentialImportResult> {
        return importCredential(
            credentialJwt = credentialJwt,
            credentialType = CredentialType.TERMS_CONDITIONS,
            alias = alias ?: "terms-conditions-${System.currentTimeMillis()}"
        )
    }
    
    /**
     * Generic credential import function
     */
    private suspend fun importCredential(
        credentialJwt: String,
        credentialType: CredentialType,
        alias: String
    ): Result<CredentialImportResult> {
        return try {
            println("Importing ${credentialType.displayName} credential...")
            
            // Verify and parse the credential
            val credential = parseAndValidateCredential(credentialJwt, credentialType)
            
            // Generate unique ID for storage
            val credentialId = generateCredentialId(credential, credentialType)
            
            // Store credential
            val credentialFile = File("$credentialsDirectory/$credentialId.jwt")
            credentialFile.writeText(credentialJwt)
            
            // Extract credential metadata
            val metadata = extractCredentialMetadata(credential, credentialType, alias)
            
            // Update metadata index
            updateCredentialsMetadata(credentialId, metadata)
            
            val result = CredentialImportResult(
                credentialId = credentialId,
                alias = alias,
                credentialType = credentialType,
                issuer = credential["issuer"]?.jsonPrimitive?.content ?: "unknown",
                subject = credential["credentialSubject"]?.jsonObject?.get("id")?.jsonPrimitive?.content ?: "unknown",
                issuanceDate = credential["issuanceDate"]?.jsonPrimitive?.content ?: "",
                expirationDate = credential["expirationDate"]?.jsonPrimitive?.content,
                importTimestamp = System.currentTimeMillis()
            )
            
            println("✅ ${credentialType.displayName} credential imported successfully")
            println("Credential ID: $credentialId")
            println("Alias: $alias")
            
            Result.success(result)
            
        } catch (e: Exception) {
            println("❌ Error importing ${credentialType.displayName} credential: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Parse and validate credential JWT
     */
    private suspend fun parseAndValidateCredential(
        credentialJwt: String, 
        expectedType: CredentialType
    ): JsonObject {
        try {
            // Basic JWT parsing (header.payload.signature)
            val parts = credentialJwt.split(".")
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid JWT format")
            }
            
            // Decode payload
            val payloadJson = String(java.util.Base64.getUrlDecoder().decode(parts[1]))
            val payload = Json.parseToJsonElement(payloadJson).jsonObject
            
            // Extract VC from payload
            val vc = payload["vc"]?.jsonObject 
                ?: throw IllegalArgumentException("JWT does not contain VC claim")
            
            // Validate credential type
            val types = vc["type"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            
            if (!isValidCredentialType(types, expectedType)) {
                throw IllegalArgumentException("Credential type mismatch. Expected ${expectedType.displayName}, got: $types")
            }
            
            println("✅ Credential JWT validation passed")
            return vc
            
        } catch (e: Exception) {
            println("❌ Credential validation failed: ${e.message}")
            throw e
        }
    }
    
    /**
     * Check if credential types match expected type
     */
    private fun isValidCredentialType(types: List<String>, expectedType: CredentialType): Boolean {
        return when (expectedType) {
            CredentialType.LEGAL_PERSON -> types.contains("LegalPersonCredential") || types.contains("LegalPerson")
            CredentialType.LRN -> types.contains("LRNCredential") || types.contains("LegalRegistrationNumberCredential")
            CredentialType.TERMS_CONDITIONS -> types.contains("TermsAndConditionsCredential") || types.contains("ParticipantTermsAndConditions")
        }
    }
    
    /**
     * Generate unique credential ID
     */
    private fun generateCredentialId(credential: JsonObject, type: CredentialType): String {
        val id = credential["id"]?.jsonPrimitive?.content
        val timestamp = System.currentTimeMillis()
        return id ?: "${type.name.lowercase()}-${timestamp}"
    }
    
    /**
     * Extract metadata from credential
     */
    private fun extractCredentialMetadata(
        credential: JsonObject,
        credentialType: CredentialType,
        alias: String
    ): JsonObject {
        val credentialSubject = credential["credentialSubject"]?.jsonObject
        
        return buildJsonObject {
            put("alias", alias)
            put("credentialType", credentialType.name)
            put("issuer", credential["issuer"]?.jsonPrimitive?.content ?: "")
            put("issuanceDate", credential["issuanceDate"]?.jsonPrimitive?.content ?: "")
            put("expirationDate", credential["expirationDate"]?.jsonPrimitive?.content ?: "")
            
            // Extract type-specific metadata
            when (credentialType) {
                CredentialType.LEGAL_PERSON -> {
                    put("organizationName", credentialSubject?.get("legalName")?.jsonPrimitive?.content ?: "")
                    put("registrationNumber", credentialSubject?.get("registrationNumber")?.jsonPrimitive?.content ?: "")
                    put("legalIdentifier", credentialSubject?.get("legalIdentifier")?.jsonPrimitive?.content ?: "")
                }
                CredentialType.LRN -> {
                    put("lrn", credentialSubject?.get("lrn")?.jsonPrimitive?.content ?: "")
                    put("registrationAuthority", credentialSubject?.get("registrationAuthority")?.jsonPrimitive?.content ?: "")
                    put("registrationDate", credentialSubject?.get("registrationDate")?.jsonPrimitive?.content ?: "")
                }
                CredentialType.TERMS_CONDITIONS -> {
                    put("termsUrl", credentialSubject?.get("termsUrl")?.jsonPrimitive?.content ?: "")
                    put("acceptanceDate", credentialSubject?.get("acceptanceDate")?.jsonPrimitive?.content ?: "")
                    put("version", credentialSubject?.get("version")?.jsonPrimitive?.content ?: "")
                }
            }
            
            put("importTimestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Update credentials metadata index
     */
    private fun updateCredentialsMetadata(credentialId: String, metadata: JsonObject) {
        try {
            val currentMetadata = loadCredentialsMetadata()
            val updatedMetadata = buildJsonObject {
                currentMetadata.forEach { (key, value) ->
                    put(key, value)
                }
                put(credentialId, metadata)
            }
            
            File(metadataFile).writeText(updatedMetadata.toString())
        } catch (e: Exception) {
            println("❌ Error updating credentials metadata: ${e.message}")
            throw e
        }
    }
    
    /**
     * Load credentials metadata
     */
    private fun loadCredentialsMetadata(): JsonObject {
        return try {
            if (File(metadataFile).exists()) {
                Json.parseToJsonElement(File(metadataFile).readText()).jsonObject
            } else {
                buildJsonObject {}
            }
        } catch (e: Exception) {
            buildJsonObject {}
        }
    }
    
    /**
     * List all imported credentials
     */
    fun listCredentials(type: CredentialType? = null): List<CredentialInfo> {
        return try {
            val metadata = loadCredentialsMetadata()
            
            metadata.mapNotNull { (credentialId, credentialMetadata) ->
                val credMeta = credentialMetadata.jsonObject
                val credType = CredentialType.valueOf(credMeta["credentialType"]?.jsonPrimitive?.content ?: "")
                
                if (type == null || credType == type) {
                    CredentialInfo(
                        credentialId = credentialId,
                        alias = credMeta["alias"]?.jsonPrimitive?.content ?: "",
                        credentialType = credType,
                        issuer = credMeta["issuer"]?.jsonPrimitive?.content ?: "",
                        subject = credMeta["subject"]?.jsonPrimitive?.content ?: "",
                        issuanceDate = credMeta["issuanceDate"]?.jsonPrimitive?.content ?: "",
                        expirationDate = credMeta["expirationDate"]?.jsonPrimitive?.content
                    )
                } else null
            }
        } catch (e: Exception) {
            println("❌ Error listing credentials: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Load credential JWT by ID or alias
     */
    fun loadCredentialJwt(idOrAlias: String): String? {
        return try {
            // Try to find by alias first
            val metadata = loadCredentialsMetadata()
            val credentialId = metadata.entries.find { (_, meta) ->
                meta.jsonObject["alias"]?.jsonPrimitive?.content == idOrAlias
            }?.key ?: idOrAlias
            
            val credentialFile = File("$credentialsDirectory/$credentialId.jwt")
            if (credentialFile.exists()) {
                credentialFile.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            println("❌ Error loading credential: ${e.message}")
            null
        }
    }
    
    /**
     * Delete credential by ID or alias
     */
    fun deleteCredential(idOrAlias: String): Boolean {
        return try {
            // Find credential ID
            val metadata = loadCredentialsMetadata()
            val credentialId = metadata.entries.find { (_, meta) ->
                meta.jsonObject["alias"]?.jsonPrimitive?.content == idOrAlias
            }?.key ?: idOrAlias
            
            // Delete credential file
            val credentialFile = File("$credentialsDirectory/$credentialId.jwt")
            if (credentialFile.exists()) {
                credentialFile.delete()
            }
            
            // Remove from metadata
            val updatedMetadata = buildJsonObject {
                metadata.forEach { (key, value) ->
                    if (key != credentialId) {
                        put(key, value)
                    }
                }
            }
            
            File(metadataFile).writeText(updatedMetadata.toString())
            
            println("✅ Credential deleted successfully")
            true
        } catch (e: Exception) {
            println("❌ Error deleting credential: ${e.message}")
            false
        }
    }
    
    /**
     * Get credentials required for Gaia-X VP creation
     * Returns all three required credential types
     */
    fun getGaiaXRequiredCredentials(): GaiaXCredentialSet? {
        val legalPersonCreds = listCredentials(CredentialType.LEGAL_PERSON)
        val lrnCreds = listCredentials(CredentialType.LRN)
        val tcCreds = listCredentials(CredentialType.TERMS_CONDITIONS)
        
        return if (legalPersonCreds.isNotEmpty() && lrnCreds.isNotEmpty() && tcCreds.isNotEmpty()) {
            GaiaXCredentialSet(
                legalPersonCredential = legalPersonCreds.first(),
                lrnCredential = lrnCreds.first(), 
                termsConditionsCredential = tcCreds.first()
            )
        } else {
            println("❌ Missing required credentials for Gaia-X VP:")
            println("  LegalPerson: ${legalPersonCreds.size} available")
            println("  LRN: ${lrnCreds.size} available") 
            println("  Terms&Conditions: ${tcCreds.size} available")
            null
        }
    }
}

/**
 * Supported credential types
 */
enum class CredentialType(val displayName: String) {
    LEGAL_PERSON("Legal Person Credential"),
    LRN("Legal Registration Number Credential"),
    TERMS_CONDITIONS("Terms & Conditions Credential")
}

/**
 * Credential information for display and management
 */
data class CredentialInfo(
    val credentialId: String,
    val alias: String,
    val credentialType: CredentialType,
    val issuer: String,
    val subject: String,
    val issuanceDate: String,
    val expirationDate: String?
)

/**
 * Import result for tracking
 */
data class CredentialImportResult(
    val credentialId: String,
    val alias: String,
    val credentialType: CredentialType,
    val issuer: String,
    val subject: String,
    val issuanceDate: String,
    val expirationDate: String?,
    val importTimestamp: Long
)

/**
 * Set of credentials required for Gaia-X VP creation
 */
data class GaiaXCredentialSet(
    val legalPersonCredential: CredentialInfo,
    val lrnCredential: CredentialInfo,
    val termsConditionsCredential: CredentialInfo
)

/**
 * Test credential import functionality
 */
suspend fun testCredentialImport() {
    val credentialManager = CredentialImportManager()
    
    println("=== Testing Credential Import Manager ===")
    
    // Example Legal Person credential (sample)
    val sampleLegalPersonJwt = createSampleLegalPersonCredential()
    
    // Import credential
    val importResult = credentialManager.importLegalPersonCredential(
        credentialJwt = sampleLegalPersonJwt,
        alias = "my-organization"
    )
    
    if (importResult.isSuccess) {
        println("✅ Legal Person credential imported")
    }
    
    // List credentials
    println("\n=== Stored Credentials ===")
    credentialManager.listCredentials().forEach { credential ->
        println("- ${credential.alias} (${credential.credentialType.displayName})")
        println("  Issuer: ${credential.issuer}")
        println("  Subject: ${credential.subject}")
    }
}

/**
 * Create a sample Legal Person credential for testing
 * In production, this would come from the actual issuer
 */
private fun createSampleLegalPersonCredential(): String {
    // This is a sample JWT credential structure
    // In production, this would be issued by the proper eIDAS authority
    return "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkaWQ6d2ViOmVpZGFzLWF1dGhvcml0eS5ldSIsInN1YiI6ImRpZDp3ZWI6bXktY29tcGFueS5jb20iLCJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSIsImh0dHBzOi8vd3d3LmVpZGFzLmV1L2NyZWRlbnRpYWxzL3YxIl0sInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJMZWdhbFBlcnNvbkNyZWRlbnRpYWwiXSwiaWQiOiJ1cm46dXVpZDoxMjM0NTY3OC1hYmNkLWVmMTItMzQ1Ni03ODkwYWJjZGVmMTIiLCJpc3N1ZXIiOiJkaWQ6d2ViOmVpZGFzLWF1dGhvcml0eS5ldSIsImlzc3VhbmNlRGF0ZSI6IjIwMjQtMDEtMTVUMTA6MDA6MDBaIiwiZXhwaXJhdGlvbkRhdGUiOiIyMDI1LTAxLTE1VDEwOjAwOjAwWiIsImNyZWRlbnRpYWxTdWJqZWN0Ijp7ImlkIjoiZGlkOndlYjpteS1jb21wYW55LmNvbSIsInR5cGUiOiJMZWdhbFBlcnNvbiIsImxlZ2FsTmFtZSI6Ik15IENvbXBhbnkgR21iSCIsInJlZ2lzdHJhdGlvbk51bWJlciI6IkRFMTIzNDU2Nzg5MCIsImxlZ2FsSWRlbnRpZmllciI6IkxFSTpERTEyMzQ1Njc4OTAiLCJoZWFkcXVhcnRlcnNBZGRyZXNzIjp7InN0cmVldEFkZHJlc3MiOiJNYWluIFN0cmVldCAxMjMiLCJsb2NhbGl0eSI6IkJlcmxpbiIsInBvc3RhbENvZGUiOiIxMjM0NSIsImNvdW50cnlOYW1lIjoiR2VybWFueSJ9fX0sImV4cCI6MTcwNTMxNDAwMCwiaWF0IjoxNzA0NzA5MjAwfQ.sample_signature_here"
}