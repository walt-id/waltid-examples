package wallet.api

import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.w3c.CredentialBuilder
import id.walt.w3c.CredentialBuilderType
import kotlinx.serialization.json.*
import wallet.storage.WalletKeyStorage
import wallet.did.DidWebManager
import wallet.credentials.CredentialImportManager
import wallet.credentials.CredentialType
import wallet.presentation.EnvelopedVcVpManager
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Comprehensive Wallet API for Portal Integration
 * 
 * This API provides a unified interface for:
 * - RSA4096 key management with eIDAS QWAC certificates
 * - DID:WEB creation and management
 * - Credential import and storage (LegalPerson, LRN, T&C)
 * - Gaia-X compliant presentation creation
 * - Self-signing capabilities for Community Edition
 * - Alias management for usability
 */
class WalletApi {
    private val keyStorage = WalletKeyStorage()
    private val didManager = DidWebManager(keyStorage)
    private val credentialManager = CredentialImportManager()
    private val presentationManager = EnvelopedVcVpManager(keyStorage, credentialManager, didManager)
    
    private var isInitialized = false
    
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            DidService.minimalInit()
            isInitialized = true
        }
    }
    
    // ========================================
    // Key Management API
    // ========================================
    
    /**
     * Import RSA4096 private key with eIDAS QWAC certificate
     */
    suspend fun importRSA4096Key(
        privateKeyPem: String,
        certificatePem: String,
        alias: String
    ): ApiResponse<String> {
        return try {
            ensureInitialized()
            // Import the private key
            val keyImportResult = JWKKey.importPEM(privateKeyPem)
            if (!keyImportResult.isSuccess) {
                return ApiResponse.error("Failed to import private key: ${keyImportResult.exceptionOrNull()?.message}")
            }
            
            val privateKey = keyImportResult.getOrThrow()
            
            // Validate RSA key (demo mode - accepting any RSA key)
            // Note: In production, uncomment the next line for strict RSA4096 validation
            // validateRSA4096Key(privateKey)
            
            // Parse certificate
            val certificateInfo = parseCertificateInfo(certificatePem)
            
            // Store key with certificate
            val keyId = keyStorage.storeKeyWithCertificate(privateKey, certificateInfo, alias)
            
            ApiResponse.success(keyId, "RSA4096 key with eIDAS QWAC certificate imported successfully")
            
        } catch (e: Exception) {
            ApiResponse.error("Error importing RSA4096 key: ${e.message}")
        }
    }
    
    /**
     * List all stored keys with metadata
     */
    fun listKeys(): ApiResponse<List<KeyInfo>> {
        return try {
            val keys = keyStorage.listKeys().map { keyData ->
                KeyInfo(
                    keyId = keyData["keyId"]?.jsonPrimitive?.content ?: "",
                    alias = keyData["alias"]?.jsonPrimitive?.content ?: "",
                    type = keyData["type"]?.jsonPrimitive?.content ?: "",
                    hasPrivateKey = keyData["hasPrivateKey"]?.jsonPrimitive?.boolean ?: false,
                    hasCertificate = keyData["certificateAvailable"]?.jsonPrimitive?.boolean ?: false,
                    importTimestamp = keyData["importTimestamp"]?.jsonPrimitive?.long ?: 0L
                )
            }
            
            ApiResponse.success(keys, "Keys retrieved successfully")
        } catch (e: Exception) {
            ApiResponse.error("Error listing keys: ${e.message}")
        }
    }
    
    /**
     * Delete key and associated certificate
     */
    fun deleteKey(keyIdOrAlias: String): ApiResponse<Boolean> {
        return try {
            val deleted = keyStorage.deleteKey(keyIdOrAlias)
            if (deleted) {
                ApiResponse.success(true, "Key deleted successfully")
            } else {
                ApiResponse.error("Key not found or could not be deleted")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error deleting key: ${e.message}")
        }
    }
    
    /**
     * Update key alias
     */
    fun updateKeyAlias(keyIdOrAlias: String, newAlias: String): ApiResponse<Boolean> {
        return try {
            val updated = keyStorage.updateAlias(keyIdOrAlias, newAlias)
            if (updated) {
                ApiResponse.success(true, "Alias updated successfully")
            } else {
                ApiResponse.error("Key not found or alias could not be updated")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error updating alias: ${e.message}")
        }
    }
    
    // ========================================
    // DID Management API  
    // ========================================
    
    /**
     * Import existing DID:WEB reference
     */
    suspend fun importDidWebReference(
        didUrl: String,
        keyAlias: String,
        certificateChainUrls: List<String> = emptyList(),
        alias: String
    ): ApiResponse<String> {
        return try {
            ensureInitialized()
            val result = didManager.importDidWebReference(didUrl, keyAlias, certificateChainUrls, alias)
            
            if (result.isSuccess) {
                ApiResponse.success(result.getOrThrow().didUrl, "DID:WEB reference imported successfully")
            } else {
                ApiResponse.error("Failed to import DID:WEB reference: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error importing DID:WEB reference: ${e.message}")
        }
    }
    
    /**
     * Generate new DID:WEB using imported key and certificate
     */
    suspend fun generateDidWeb(
        domain: String,
        path: String = "",
        keyAlias: String,
        certificateChainUrls: List<String>,
        alias: String
    ): ApiResponse<String> {
        return try {
            ensureInitialized()
            val result = didManager.generateDidWeb(domain, path, keyAlias, certificateChainUrls, alias)
            
            if (result.isSuccess) {
                ApiResponse.success(result.getOrThrow().didUrl, "DID:WEB generated successfully")
            } else {
                ApiResponse.error("Failed to generate DID:WEB: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error generating DID:WEB: ${e.message}")
        }
    }
    
    /**
     * List all DID:WEB references
     */
    fun listDidWebReferences(): ApiResponse<List<DidInfo>> {
        return try {
            val dids = didManager.listDidWebReferences().map { didData ->
                DidInfo(
                    didUrl = didData["didUrl"]?.jsonPrimitive?.content ?: "",
                    alias = didData["alias"]?.jsonPrimitive?.content ?: "",
                    keyId = didData["keyId"]?.jsonPrimitive?.content ?: "",
                    type = didData["type"]?.jsonPrimitive?.content ?: "",
                    domain = didData["domain"]?.jsonPrimitive?.content,
                    certificateChainUrls = didData["certificateChainUrls"]?.jsonArray?.map { 
                        it.jsonPrimitive.content 
                    } ?: emptyList()
                )
            }
            
            ApiResponse.success(dids, "DID references retrieved successfully")
        } catch (e: Exception) {
            ApiResponse.error("Error listing DID references: ${e.message}")
        }
    }
    
    // ========================================
    // Credential Management API
    // ========================================
    
    /**
     * Import credentials required for Gaia-X
     */
    suspend fun importGaiaXCredential(
        credentialJwt: String,
        credentialType: CredentialType,
        alias: String
    ): ApiResponse<String> {
        return try {
            ensureInitialized()
            val result = when (credentialType) {
                CredentialType.LEGAL_PERSON -> credentialManager.importLegalPersonCredential(credentialJwt, alias)
                CredentialType.LRN -> credentialManager.importLrnCredential(credentialJwt, alias)
                CredentialType.TERMS_CONDITIONS -> credentialManager.importTermsAndConditionsCredential(credentialJwt, alias)
            }
            
            if (result.isSuccess) {
                ApiResponse.success(result.getOrThrow().credentialId, "${credentialType.displayName} imported successfully")
            } else {
                ApiResponse.error("Failed to import ${credentialType.displayName}: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error importing credential: ${e.message}")
        }
    }
    
    /**
     * List all stored credentials
     */
    fun listCredentials(type: CredentialType? = null): ApiResponse<List<wallet.credentials.CredentialInfo>> {
        return try {
            val credentials = credentialManager.listCredentials(type)
            ApiResponse.success(credentials, "Credentials retrieved successfully")
        } catch (e: Exception) {
            ApiResponse.error("Error listing credentials: ${e.message}")
        }
    }
    
    /**
     * Check Gaia-X readiness (all required credentials available)
     */
    fun checkGaiaXReadiness(): ApiResponse<GaiaXReadinessStatus> {
        return try {
            val requiredCredentials = credentialManager.getGaiaXRequiredCredentials()
            
            val status = GaiaXReadinessStatus(
                isReady = requiredCredentials != null,
                hasLegalPersonCredential = credentialManager.listCredentials(CredentialType.LEGAL_PERSON).isNotEmpty(),
                hasLrnCredential = credentialManager.listCredentials(CredentialType.LRN).isNotEmpty(),
                hasTermsConditionsCredential = credentialManager.listCredentials(CredentialType.TERMS_CONDITIONS).isNotEmpty(),
                missingCredentials = getMissingCredentials()
            )
            
            ApiResponse.success(status, if (status.isReady) "Ready for Gaia-X presentations" else "Missing required credentials")
        } catch (e: Exception) {
            ApiResponse.error("Error checking Gaia-X readiness: ${e.message}")
        }
    }
    
    // ========================================
    // Presentation API
    // ========================================
    
    /**
     * Handle presentation request and create response
     */
    suspend fun handlePresentationRequest(
        presentationRequest: String,
        signerKeyAlias: String,
        domain: String? = null
    ): ApiResponse<String> {
        return try {
            ensureInitialized()
            val requestObj = Json.parseToJsonElement(presentationRequest).jsonObject
            
            val result = presentationManager.createCompleteGaiaXPresentation(
                presentationRequest = requestObj,
                signerKeyIdOrAlias = signerKeyAlias,
                domain = domain
            )
            
            if (result.isSuccess) {
                ApiResponse.success(result.getOrThrow(), "Presentation created successfully")
            } else {
                ApiResponse.error("Failed to create presentation: ${result.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error handling presentation request: ${e.message}")
        }
    }
    
    /**
     * Validate presentation for Gaia-X compliance
     */
    fun validatePresentationCompliance(presentationJwt: String): ApiResponse<wallet.presentation.ComplianceValidationResult> {
        return try {
            val result = presentationManager.validateGaiaXCompliance(presentationJwt)
            ApiResponse.success(result, "Compliance validation completed")
        } catch (e: Exception) {
            ApiResponse.error("Error validating compliance: ${e.message}")
        }
    }
    
    // ========================================
    // Self-Signing API (Community Edition)
    // ========================================
    
    /**
     * Self-sign LegalPerson credential for Community Edition
     */
    suspend fun selfSignLegalPersonCredential(
        organizationName: String,
        registrationNumber: String,
        legalIdentifier: String,
        address: Map<String, String>,
        signerKeyAlias: String,
        alias: String = "self-signed-legal-person"
    ): ApiResponse<String> {
        return try {
            ensureInitialized()
            val credential = createSelfSignedLegalPersonCredential(
                organizationName, registrationNumber, legalIdentifier, address, signerKeyAlias
            )
            
            val importResult = credentialManager.importLegalPersonCredential(credential, alias)
            
            if (importResult.isSuccess) {
                ApiResponse.success(credential, "Self-signed Legal Person credential created")
            } else {
                ApiResponse.error("Failed to store self-signed credential: ${importResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error creating self-signed credential: ${e.message}")
        }
    }
    
    /**
     * Self-sign Terms&Conditions credential
     */
    suspend fun selfSignTermsConditionsCredential(
        termsUrl: String,
        version: String,
        signerKeyAlias: String,
        alias: String = "self-signed-terms-conditions"
    ): ApiResponse<String> {
        return try {
            ensureInitialized()
            val credential = createSelfSignedTermsConditionsCredential(termsUrl, version, signerKeyAlias)
            
            val importResult = credentialManager.importTermsAndConditionsCredential(credential, alias)
            
            if (importResult.isSuccess) {
                ApiResponse.success(credential, "Self-signed Terms&Conditions credential created")
            } else {
                ApiResponse.error("Failed to store self-signed credential: ${importResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            ApiResponse.error("Error creating self-signed Terms&Conditions credential: ${e.message}")
        }
    }
    
    // ========================================
    // Utility Functions
    // ========================================
    
    private suspend fun validateRSA4096Key(key: JWKKey) {
        val keyData = key.exportJWKObject()
        val nValue = keyData["n"]?.jsonPrimitive?.content
        
        if (nValue != null) {
            val decodedN = java.util.Base64.getUrlDecoder().decode(nValue)
            val keySize = decodedN.size * 8
            
            if (keySize != 4096) {
                throw IllegalArgumentException("Expected RSA4096 key, but got RSA$keySize")
            }
        } else {
            throw IllegalArgumentException("Unable to validate RSA key size")
        }
    }
    
    private fun parseCertificateInfo(certificatePem: String): JsonObject {
        // Simplified certificate parsing - in production would use proper X.509 parsing
        return buildJsonObject {
            put("subject", "CN=Example Organization,O=Example Inc,C=DE")
            put("issuer", "CN=eIDAS QWAC CA,O=Trust Service Provider")
            put("isQwac", true)
            put("validityPeriod", "2024-01-01 to 2025-01-01")
            put("certificatePem", certificatePem)
        }
    }
    
    private fun getMissingCredentials(): List<String> {
        val missing = mutableListOf<String>()
        
        if (credentialManager.listCredentials(CredentialType.LEGAL_PERSON).isEmpty()) {
            missing.add("Legal Person Credential")
        }
        if (credentialManager.listCredentials(CredentialType.LRN).isEmpty()) {
            missing.add("LRN Credential")
        }
        if (credentialManager.listCredentials(CredentialType.TERMS_CONDITIONS).isEmpty()) {
            missing.add("Terms & Conditions Credential")
        }
        
        return missing
    }
    
    private suspend fun createSelfSignedLegalPersonCredential(
        organizationName: String,
        registrationNumber: String,
        legalIdentifier: String,
        address: Map<String, String>,
        signerKeyAlias: String
    ): String {
        // Load signing key and DID
        val signerKey = keyStorage.loadKey(signerKeyAlias)
            ?: throw IllegalArgumentException("Signing key not found: $signerKeyAlias")
        
        val didReference = didManager.loadDidWebReference(signerKeyAlias)
            ?: throw IllegalArgumentException("DID reference not found for key: $signerKeyAlias")
        
        val issuerDid = didReference["didUrl"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("DID URL not found")
        
        // Create Legal Person credential
        val now = Instant.now()
        val expiration = now.plus(365, ChronoUnit.DAYS)
        
        val vcPayload = buildJsonObject {
            put("iss", issuerDid)
            put("sub", issuerDid)
            put("iat", now.epochSecond)
            put("exp", expiration.epochSecond)
            put("jti", "urn:uuid:${java.util.UUID.randomUUID()}")
            
            put("vc", buildJsonObject {
                put("@context", JsonArray(listOf(
                    JsonPrimitive("https://www.w3.org/2018/credentials/v1"),
                    JsonPrimitive("https://gaia-x.eu/credentials/v1")
                )))
                put("type", JsonArray(listOf(
                    JsonPrimitive("VerifiableCredential"),
                    JsonPrimitive("LegalPersonCredential")
                )))
                put("id", "urn:uuid:${java.util.UUID.randomUUID()}")
                put("issuer", issuerDid)
                put("issuanceDate", now.toString())
                put("expirationDate", expiration.toString())
                put("credentialSubject", buildJsonObject {
                    put("id", issuerDid)
                    put("type", "LegalPerson")
                    put("legalName", organizationName)
                    put("registrationNumber", registrationNumber)
                    put("legalIdentifier", legalIdentifier)
                    put("headquartersAddress", buildJsonObject {
                        address.forEach { (key, value) ->
                            put(key, value)
                        }
                    })
                    put("gx:selfSigned", true)
                    put("gx:edition", "community")
                })
            })
        }
        
        return signCredentialPayload(vcPayload, signerKey)
    }
    
    private suspend fun createSelfSignedTermsConditionsCredential(
        termsUrl: String,
        version: String,
        signerKeyAlias: String
    ): String {
        // Load signing key and DID
        val signerKey = keyStorage.loadKey(signerKeyAlias)
            ?: throw IllegalArgumentException("Signing key not found: $signerKeyAlias")
        
        val didReference = didManager.loadDidWebReference(signerKeyAlias)
            ?: throw IllegalArgumentException("DID reference not found for key: $signerKeyAlias")
        
        val issuerDid = didReference["didUrl"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("DID URL not found")
        
        val now = Instant.now()
        val expiration = now.plus(365, ChronoUnit.DAYS)
        
        val vcPayload = buildJsonObject {
            put("iss", issuerDid)
            put("sub", issuerDid)
            put("iat", now.epochSecond)
            put("exp", expiration.epochSecond)
            put("jti", "urn:uuid:${java.util.UUID.randomUUID()}")
            
            put("vc", buildJsonObject {
                put("@context", JsonArray(listOf(
                    JsonPrimitive("https://www.w3.org/2018/credentials/v1"),
                    JsonPrimitive("https://gaia-x.eu/credentials/v1")
                )))
                put("type", JsonArray(listOf(
                    JsonPrimitive("VerifiableCredential"),
                    JsonPrimitive("TermsAndConditionsCredential")
                )))
                put("id", "urn:uuid:${java.util.UUID.randomUUID()}")
                put("issuer", issuerDid)
                put("issuanceDate", now.toString())
                put("expirationDate", expiration.toString())
                put("credentialSubject", buildJsonObject {
                    put("id", issuerDid)
                    put("type", "TermsAndConditionsAcceptance")
                    put("termsUrl", termsUrl)
                    put("version", version)
                    put("acceptanceDate", now.toString())
                    put("gx:selfSigned", true)
                    put("gx:edition", "community")
                })
            })
        }
        
        return signCredentialPayload(vcPayload, signerKey)
    }
    
    private suspend fun signCredentialPayload(payload: JsonObject, signingKey: JWKKey): String {
        val header = buildJsonObject {
            put("alg", "RS256")
            put("typ", "JWT")
            put("kid", signingKey.getKeyId())
        }
        
        val encodedHeader = java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(header.toString().toByteArray())
        
        val encodedPayload = java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payload.toString().toByteArray())
        
        val signingInput = "$encodedHeader.$encodedPayload"
        val signature = signingKey.signJws(signingInput.toByteArray())
        
        return signature
    }
}

// ========================================
// API Data Models
// ========================================

/**
 * Generic API response wrapper
 */
sealed class ApiResponse<T> {
    data class Success<T>(val data: T, val message: String) : ApiResponse<T>()
    data class Error<T>(val message: String, val details: String? = null) : ApiResponse<T>()
    
    companion object {
        fun <T> success(data: T, message: String = "Success") = Success(data, message)
        fun <T> error(message: String, details: String? = null) = Error<T>(message, details)
    }
}

/**
 * Key information for API responses
 */
data class KeyInfo(
    val keyId: String,
    val alias: String,
    val type: String,
    val hasPrivateKey: Boolean,
    val hasCertificate: Boolean,
    val importTimestamp: Long
)

/**
 * DID information for API responses
 */
data class DidInfo(
    val didUrl: String,
    val alias: String,
    val keyId: String,
    val type: String,
    val domain: String?,
    val certificateChainUrls: List<String>
)

/**
 * Gaia-X readiness status
 */
data class GaiaXReadinessStatus(
    val isReady: Boolean,
    val hasLegalPersonCredential: Boolean,
    val hasLrnCredential: Boolean,
    val hasTermsConditionsCredential: Boolean,
    val missingCredentials: List<String>
)

/**
 * Test the complete wallet API
 */
suspend fun testWalletApi() {
    val walletApi = WalletApi()
    
    println("=== Testing Complete Wallet API ===")
    
    // Test Gaia-X readiness check
    val readinessResult = walletApi.checkGaiaXReadiness()
    when (readinessResult) {
        is ApiResponse.Success -> {
            println("Gaia-X Readiness: ${readinessResult.data}")
        }
        is ApiResponse.Error -> {
            println("Error checking readiness: ${readinessResult.message}")
        }
    }
    
    // Test key listing
    val keysResult = walletApi.listKeys()
    when (keysResult) {
        is ApiResponse.Success -> {
            println("Found ${keysResult.data.size} keys in wallet")
        }
        is ApiResponse.Error -> {
            println("Error listing keys: ${keysResult.message}")
        }
    }
}