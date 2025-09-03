package wallet.ui

import kotlinx.serialization.json.*
import wallet.api.WalletApi
import wallet.api.ApiResponse

/**
 * Key Management UI Components
 * 
 * Provides UI logic and data models for:
 * - Key upload interface
 * - Key listing and display  
 * - Key deletion with confirmation
 * - Alias management
 * - Certificate information display
 */
class KeyManagementUI {
    private val walletApi = WalletApi()
    
    /**
     * Handle key upload from UI
     */
    suspend fun handleKeyUpload(uploadRequest: KeyUploadRequest): KeyUploadResponse {
        return try {
            // Validate upload request
            validateUploadRequest(uploadRequest)
            
            // Import the key
            val result = walletApi.importRSA4096Key(
                privateKeyPem = uploadRequest.privateKeyPem,
                certificatePem = uploadRequest.certificatePem,
                alias = uploadRequest.alias
            )
            
            when (result) {
                is ApiResponse.Success -> {
                    KeyUploadResponse(
                        success = true,
                        message = "Key imported successfully",
                        keyId = result.data,
                        validationErrors = emptyList()
                    )
                }
                is ApiResponse.Error -> {
                    KeyUploadResponse(
                        success = false,
                        message = result.message,
                        keyId = null,
                        validationErrors = listOf(result.message)
                    )
                }
            }
        } catch (e: Exception) {
            KeyUploadResponse(
                success = false,
                message = "Upload failed: ${e.message}",
                keyId = null,
                validationErrors = listOf(e.message ?: "Unknown error")
            )
        }
    }
    
    /**
     * Get key list for UI display
     */
    fun getKeyListForDisplay(): KeyListDisplayData {
        val keysResult = walletApi.listKeys()
        
        return when (keysResult) {
            is ApiResponse.Success -> {
                val displayItems = keysResult.data.map { keyInfo ->
                    KeyDisplayItem(
                        keyId = keyInfo.keyId,
                        alias = keyInfo.alias,
                        type = formatKeyType(keyInfo.type),
                        status = if (keyInfo.hasPrivateKey && keyInfo.hasCertificate) "Complete" else "Incomplete",
                        importDate = formatTimestamp(keyInfo.importTimestamp),
                        actions = listOf("view", "edit-alias", "delete"),
                        certificateInfo = if (keyInfo.hasCertificate) getCertificateDisplayInfo(keyInfo.keyId) else null
                    )
                }
                
                KeyListDisplayData(
                    keys = displayItems,
                    totalCount = displayItems.size,
                    hasKeys = displayItems.isNotEmpty(),
                    message = if (displayItems.isEmpty()) "No keys imported yet" else "${displayItems.size} keys available"
                )
            }
            is ApiResponse.Error -> {
                KeyListDisplayData(
                    keys = emptyList(),
                    totalCount = 0,
                    hasKeys = false,
                    message = "Error loading keys: ${keysResult.message}"
                )
            }
        }
    }
    
    /**
     * Handle key deletion with confirmation
     */
    fun handleKeyDeletion(keyIdOrAlias: String, confirmed: Boolean = false): KeyDeletionResponse {
        if (!confirmed) {
            // Return confirmation request
            return KeyDeletionResponse(
                success = false,
                requiresConfirmation = true,
                message = "Please confirm deletion of key: $keyIdOrAlias",
                confirmationPrompt = "This action cannot be undone. Are you sure you want to delete this key and its associated certificate?"
            )
        }
        
        val result = walletApi.deleteKey(keyIdOrAlias)
        
        return when (result) {
            is ApiResponse.Success -> {
                KeyDeletionResponse(
                    success = true,
                    requiresConfirmation = false,
                    message = "Key deleted successfully"
                )
            }
            is ApiResponse.Error -> {
                KeyDeletionResponse(
                    success = false,
                    requiresConfirmation = false,
                    message = result.message
                )
            }
        }
    }
    
    /**
     * Handle alias update
     */
    fun handleAliasUpdate(keyIdOrAlias: String, newAlias: String): AliasUpdateResponse {
        return try {
            // Validate new alias
            if (newAlias.isBlank()) {
                return AliasUpdateResponse(false, "Alias cannot be empty")
            }
            
            if (newAlias.length > 50) {
                return AliasUpdateResponse(false, "Alias must be 50 characters or less")
            }
            
            val result = walletApi.updateKeyAlias(keyIdOrAlias, newAlias)
            
            when (result) {
                is ApiResponse.Success -> AliasUpdateResponse(true, "Alias updated successfully")
                is ApiResponse.Error -> AliasUpdateResponse(false, result.message)
            }
        } catch (e: Exception) {
            AliasUpdateResponse(false, "Error updating alias: ${e.message}")
        }
    }
    
    /**
     * Get upload form configuration for UI
     */
    fun getUploadFormConfig(): KeyUploadFormConfig {
        return KeyUploadFormConfig(
            title = "Import RSA4096 Key with eIDAS QWAC Certificate",
            description = "Upload your organization's RSA4096 private key and associated eIDAS QWAC certificate for wallet integration.",
            fields = listOf(
                FormField(
                    name = "alias",
                    label = "Key Alias",
                    type = "text",
                    required = true,
                    placeholder = "e.g., my-company-signing-key",
                    validation = FormValidation(
                        minLength = 1,
                        maxLength = 50,
                        pattern = "^[a-zA-Z0-9-_]+$",
                        errorMessage = "Alias must contain only letters, numbers, hyphens, and underscores"
                    )
                ),
                FormField(
                    name = "privateKeyPem",
                    label = "RSA4096 Private Key (PEM format)",
                    type = "textarea",
                    required = true,
                    placeholder = "-----BEGIN RSA PRIVATE KEY-----\n...\n-----END RSA PRIVATE KEY-----",
                    validation = FormValidation(
                        pattern = "-----BEGIN.*PRIVATE KEY-----",
                        errorMessage = "Must be a valid PEM-formatted private key"
                    )
                ),
                FormField(
                    name = "certificatePem",
                    label = "eIDAS QWAC Certificate (PEM format)",
                    type = "textarea", 
                    required = true,
                    placeholder = "-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----",
                    validation = FormValidation(
                        pattern = "-----BEGIN CERTIFICATE-----",
                        errorMessage = "Must be a valid PEM-formatted certificate"
                    )
                )
            ),
            securityNotice = "Your private key will be encrypted and stored securely. The key never leaves your wallet instance.",
            supportedFormats = listOf("PEM (Privacy-Enhanced Mail) format", "RSA4096 bit keys only", "eIDAS QWAC certificates")
        )
    }
    
    // ========================================
    // Helper Functions
    // ========================================
    
    private fun validateUploadRequest(request: KeyUploadRequest) {
        if (request.alias.isBlank()) {
            throw IllegalArgumentException("Key alias is required")
        }
        
        if (!request.privateKeyPem.contains("-----BEGIN") || !request.privateKeyPem.contains("PRIVATE KEY")) {
            throw IllegalArgumentException("Invalid private key PEM format")
        }
        
        if (!request.certificatePem.contains("-----BEGIN CERTIFICATE-----")) {
            throw IllegalArgumentException("Invalid certificate PEM format")
        }
    }
    
    private fun formatKeyType(type: String): String {
        return when (type) {
            "RSA4096_EIDAS_QWAC" -> "RSA4096 with eIDAS QWAC"
            else -> type
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        return if (timestamp > 0) {
            java.time.Instant.ofEpochMilli(timestamp).toString()
        } else {
            "Unknown"
        }
    }
    
    private fun getCertificateDisplayInfo(keyId: String): CertificateDisplayInfo? {
        // This would load certificate info from storage
        return CertificateDisplayInfo(
            subject = "CN=Example Organization",
            issuer = "CN=eIDAS QWAC CA",
            validityPeriod = "2024-2025",
            isValid = true
        )
    }
}

// ========================================
// UI Data Models
// ========================================

/**
 * Key upload request from UI
 */
data class KeyUploadRequest(
    val alias: String,
    val privateKeyPem: String,
    val certificatePem: String
)

/**
 * Key upload response to UI
 */
data class KeyUploadResponse(
    val success: Boolean,
    val message: String,
    val keyId: String?,
    val validationErrors: List<String>
)

/**
 * Key display item for UI lists
 */
data class KeyDisplayItem(
    val keyId: String,
    val alias: String,
    val type: String,
    val status: String,
    val importDate: String,
    val actions: List<String>,
    val certificateInfo: CertificateDisplayInfo?
)

/**
 * Key list display data
 */
data class KeyListDisplayData(
    val keys: List<KeyDisplayItem>,
    val totalCount: Int,
    val hasKeys: Boolean,
    val message: String
)

/**
 * Certificate display information
 */
data class CertificateDisplayInfo(
    val subject: String,
    val issuer: String,
    val validityPeriod: String,
    val isValid: Boolean
)

/**
 * Key deletion response
 */
data class KeyDeletionResponse(
    val success: Boolean,
    val requiresConfirmation: Boolean,
    val message: String,
    val confirmationPrompt: String? = null
)

/**
 * Alias update response
 */
data class AliasUpdateResponse(
    val success: Boolean,
    val message: String
)

/**
 * Upload form configuration
 */
data class KeyUploadFormConfig(
    val title: String,
    val description: String,
    val fields: List<FormField>,
    val securityNotice: String,
    val supportedFormats: List<String>
)

/**
 * Form field definition
 */
data class FormField(
    val name: String,
    val label: String,
    val type: String,
    val required: Boolean,
    val placeholder: String? = null,
    val validation: FormValidation? = null
)

/**
 * Form validation rules
 */
data class FormValidation(
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val errorMessage: String
)

/**
 * Test the Key Management UI
 */
suspend fun testKeyManagementUI() {
    val keyUI = KeyManagementUI()
    
    println("=== Testing Key Management UI ===")
    
    // Get form configuration
    val formConfig = keyUI.getUploadFormConfig()
    println("Upload form: ${formConfig.title}")
    println("Fields: ${formConfig.fields.size}")
    
    // Get key list display
    val keyList = keyUI.getKeyListForDisplay()
    println("Key list: ${keyList.message}")
    println("Total keys: ${keyList.totalCount}")
}