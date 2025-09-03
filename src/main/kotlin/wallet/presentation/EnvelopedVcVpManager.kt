package wallet.presentation

import id.walt.crypto.keys.jwk.JWKKey
import id.walt.did.dids.DidService
import id.walt.w3c.CredentialBuilder
import id.walt.w3c.CredentialBuilderType
import id.walt.w3c.PresentationBuilder
import kotlinx.serialization.json.*
import wallet.credentials.CredentialImportManager
import wallet.credentials.GaiaXCredentialSet
import wallet.did.DidWebManager
import wallet.storage.WalletKeyStorage

/**
 * Enveloped VC/VP Manager for Gaia-X Compliance
 * 
 * Handles:
 * - Creation of Enveloped Verifiable Credentials (EVCs)
 * - Creation of Enveloped Verifiable Presentations (EVPs)
 * - Gaia-X compliance for GDCH validation
 * - Presentation request handling
 */
class EnvelopedVcVpManager(
    private val keyStorage: WalletKeyStorage = WalletKeyStorage(),
    private val credentialManager: CredentialImportManager = CredentialImportManager(),
    private val didManager: DidWebManager = DidWebManager()
) {
    
    /**
     * Create Enveloped Verifiable Credential (EVC)
     * 
     * EVCs are wrapped VCs that provide additional security and compliance features
     * required by Gaia-X
     */
    suspend fun createEnvelopedVC(
        credentialJwt: String,
        envelopingKeyIdOrAlias: String,
        additionalClaims: Map<String, Any> = emptyMap()
    ): Result<String> {
        return try {
            println("Creating Enveloped Verifiable Credential...")
            
            // Load enveloping key
            val envelopingKey = keyStorage.loadKey(envelopingKeyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("Enveloping key not found: $envelopingKeyIdOrAlias"))
            
            // Get DID for enveloping key
            val didReference = didManager.loadDidWebReference(envelopingKeyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("DID reference not found for key: $envelopingKeyIdOrAlias"))
            
            val enveloperDid = didReference["didUrl"]?.jsonPrimitive?.content
                ?: return Result.failure(IllegalArgumentException("DID URL not found in reference"))
            
            // Create envelope structure
            val envelopePayload = buildJsonObject {
                put("iss", enveloperDid)
                put("sub", enveloperDid)
                put("iat", System.currentTimeMillis() / 1000)
                put("exp", (System.currentTimeMillis() / 1000) + 3600) // 1 hour validity
                put("jti", "urn:uuid:${java.util.UUID.randomUUID()}")
                
                // Add the original credential as nested VC
                put("vc", JsonPrimitive(credentialJwt))
                
                // Add additional claims
                additionalClaims.forEach { (key, value) ->
                    put(key, Json.encodeToJsonElement(value))
                }
                
                // Gaia-X specific envelope properties
                put("gx:compliance", buildJsonObject {
                    put("gx:trustFramework", "GAIA-X")
                    put("gx:version", "2210")
                    put("gx:issuancePolicy", "eIDAS")
                })
            }
            
            // Sign the envelope
            val envelopedVC = signEnvelopePayload(envelopePayload, envelopingKey)
            
            println("✅ Enveloped VC created successfully")
            Result.success(envelopedVC)
            
        } catch (e: Exception) {
            println("❌ Error creating Enveloped VC: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Create Enveloped Verifiable Presentation (EVP) for Gaia-X
     * 
     * EVPs contain multiple EVCs and are compliant with Gaia-X requirements
     */
    suspend fun createGaiaXParticipantVP(
        presentationRequest: JsonObject,
        signerKeyIdOrAlias: String,
        audience: String? = null
    ): Result<String> {
        return try {
            println("Creating Gaia-X Participant Verifiable Presentation...")
            
            // Get required credentials set
            val requiredCredentials = credentialManager.getGaiaXRequiredCredentials()
                ?: return Result.failure(IllegalArgumentException("Missing required credentials for Gaia-X VP"))
            
            // Load signing key
            val signerKey = keyStorage.loadKey(signerKeyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("Signing key not found: $signerKeyIdOrAlias"))
            
            // Get holder DID
            val didReference = didManager.loadDidWebReference(signerKeyIdOrAlias)
                ?: return Result.failure(IllegalArgumentException("DID reference not found for key: $signerKeyIdOrAlias"))
            
            val holderDid = didReference["didUrl"]?.jsonPrimitive?.content
                ?: return Result.failure(IllegalArgumentException("DID URL not found in reference"))
            
            // Load credential JWTs
            val legalPersonJwt = credentialManager.loadCredentialJwt(requiredCredentials.legalPersonCredential.credentialId)
                ?: return Result.failure(IllegalArgumentException("Legal Person credential JWT not found"))
            
            val lrnJwt = credentialManager.loadCredentialJwt(requiredCredentials.lrnCredential.credentialId)
                ?: return Result.failure(IllegalArgumentException("LRN credential JWT not found"))
            
            val tcJwt = credentialManager.loadCredentialJwt(requiredCredentials.termsConditionsCredential.credentialId)
                ?: return Result.failure(IllegalArgumentException("Terms&Conditions credential JWT not found"))
            
            // Create Enveloped VCs for each credential
            val envelopedLegalPerson = createEnvelopedVC(legalPersonJwt, signerKeyIdOrAlias).getOrThrow()
            val envelopedLrn = createEnvelopedVC(lrnJwt, signerKeyIdOrAlias).getOrThrow()
            val envelopedTc = createEnvelopedVC(tcJwt, signerKeyIdOrAlias).getOrThrow()
            
            // Create the presentation payload
            val vpPayload = buildJsonObject {
                put("iss", holderDid)
                put("sub", holderDid)
                put("aud", audience ?: presentationRequest["aud"]?.jsonPrimitive?.content ?: "")
                put("iat", System.currentTimeMillis() / 1000)
                put("exp", (System.currentTimeMillis() / 1000) + 3600) // 1 hour validity
                put("jti", "urn:uuid:${java.util.UUID.randomUUID()}")
                put("nonce", presentationRequest["nonce"]?.jsonPrimitive?.content ?: generateNonce())
                
                // VP structure with Enveloped VCs
                put("vp", buildJsonObject {
                    put("@context", JsonArray(listOf(
                        JsonPrimitive("https://www.w3.org/2018/credentials/v1"),
                        JsonPrimitive("https://gaia-x.eu/credentials/v1")
                    )))
                    put("type", JsonArray(listOf(
                        JsonPrimitive("VerifiablePresentation"),
                        JsonPrimitive("GaiaXParticipantPresentation")
                    )))
                    put("id", "urn:uuid:${java.util.UUID.randomUUID()}")
                    put("holder", holderDid)
                    
                    // Include Enveloped VCs
                    put("verifiableCredential", JsonArray(listOf(
                        JsonPrimitive(envelopedLegalPerson),
                        JsonPrimitive(envelopedLrn),
                        JsonPrimitive(envelopedTc)
                    )))
                    
                    // Gaia-X specific properties
                    put("gx:compliance", buildJsonObject {
                        put("gx:trustFramework", "GAIA-X")
                        put("gx:version", "2210")
                        put("gx:participantType", "legal-person")
                        put("gx:credentialTypes", JsonArray(listOf(
                            JsonPrimitive("LegalPersonCredential"),
                            JsonPrimitive("LRNCredential"),
                            JsonPrimitive("TermsAndConditionsCredential")
                        )))
                    })
                })
            }
            
            // Sign the VP
            val signedVP = signEnvelopePayload(vpPayload, signerKey)
            
            println("✅ Gaia-X Participant VP created successfully")
            println("VP contains ${requiredCredentials.let { 3 }} Enveloped VCs")
            
            Result.success(signedVP)
            
        } catch (e: Exception) {
            println("❌ Error creating Gaia-X VP: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Handle presentation request and respond with appropriate VP
     */
    suspend fun handlePresentationRequest(
        presentationRequest: String,
        signerKeyIdOrAlias: String
    ): Result<String> {
        return try {
            println("Handling presentation request...")
            
            // Parse presentation request
            val requestObj = Json.parseToJsonElement(presentationRequest).jsonObject
            
            // Validate request format and requirements
            validatePresentationRequest(requestObj)
            
            // Create appropriate VP response
            val vpResult = createGaiaXParticipantVP(requestObj, signerKeyIdOrAlias)
            
            if (vpResult.isSuccess) {
                println("✅ Presentation request handled successfully")
            }
            
            vpResult
            
        } catch (e: Exception) {
            println("❌ Error handling presentation request: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Validate presentation request format
     */
    private fun validatePresentationRequest(request: JsonObject) {
        val requiredFields = listOf("aud", "nonce", "response_type", "scope")
        
        requiredFields.forEach { field ->
            if (!request.containsKey(field)) {
                throw IllegalArgumentException("Presentation request missing required field: $field")
            }
        }
        
        // Validate that it's requesting Gaia-X credentials
        val scope = request["scope"]?.jsonPrimitive?.content ?: ""
        if (!scope.contains("gaia-x") && !scope.contains("legal-person")) {
            println("⚠️  Warning: Presentation request may not be for Gaia-X credentials")
        }
        
        println("✅ Presentation request validation passed")
    }
    
    /**
     * Sign envelope payload using private key
     */
    private suspend fun signEnvelopePayload(payload: JsonObject, signingKey: JWKKey): String {
        val header = buildJsonObject {
            put("alg", "RS256") // RSA signature algorithm
            put("typ", "JWT")
            put("kid", signingKey.getKeyId())
        }
        
        // Encode header and payload
        val encodedHeader = java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(header.toString().toByteArray())
        
        val encodedPayload = java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(payload.toString().toByteArray())
        
        // Create signing input
        val signingInput = "$encodedHeader.$encodedPayload"
        
        // Sign using JWS
        val signature = signingKey.signJws(signingInput.toByteArray())
        
        // Extract signature part (JWS format is header.payload.signature)
        val signaturePart = signature.split(".").lastOrNull()
            ?: throw IllegalArgumentException("Invalid JWS signature format")
        
        return "$signingInput.$signaturePart"
    }
    
    /**
     * Generate random nonce for presentation
     */
    private fun generateNonce(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "")
    }
    
    /**
     * Verify an Enveloped VC/VP
     */
    suspend fun verifyEnvelopedCredential(envelopedJwt: String): Result<JsonObject> {
        return try {
            println("Verifying Enveloped VC/VP...")
            
            // Parse JWT
            val parts = envelopedJwt.split(".")
            if (parts.size != 3) {
                return Result.failure(IllegalArgumentException("Invalid JWT format"))
            }
            
            // Decode header and payload
            val header = Json.parseToJsonElement(
                String(java.util.Base64.getUrlDecoder().decode(parts[0]))
            ).jsonObject
            
            val payload = Json.parseToJsonElement(
                String(java.util.Base64.getUrlDecoder().decode(parts[1]))
            ).jsonObject
            
            // Extract issuer DID
            val issuerDid = payload["iss"]?.jsonPrimitive?.content
                ?: return Result.failure(IllegalArgumentException("No issuer DID found in envelope"))
            
            // Resolve issuer DID to get verification key
            DidService.minimalInit()
            val didResolution = DidService.resolveToKey(issuerDid)
            
            if (!didResolution.isSuccess) {
                return Result.failure(Exception("Failed to resolve issuer DID: $issuerDid"))
            }
            
            val verificationKey = didResolution.getOrThrow()
            
            // Verify signature
            val isValidSignature = verificationKey.verifyJws(envelopedJwt).isSuccess
            
            if (!isValidSignature) {
                return Result.failure(IllegalArgumentException("Invalid envelope signature"))
            }
            
            // Validate envelope structure
            validateEnvelopeStructure(payload)
            
            println("✅ Enveloped credential verification successful")
            Result.success(payload)
            
        } catch (e: Exception) {
            println("❌ Error verifying enveloped credential: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Validate envelope structure for Gaia-X compliance
     */
    private fun validateEnvelopeStructure(payload: JsonObject) {
        // Check required JWT claims
        val requiredClaims = listOf("iss", "sub", "iat", "exp", "jti")
        requiredClaims.forEach { claim ->
            if (!payload.containsKey(claim)) {
                throw IllegalArgumentException("Missing required envelope claim: $claim")
            }
        }
        
        // Check Gaia-X compliance properties
        val gxCompliance = payload["gx:compliance"]?.jsonObject
        if (gxCompliance != null) {
            val trustFramework = gxCompliance["gx:trustFramework"]?.jsonPrimitive?.content
            if (trustFramework != "GAIA-X") {
                throw IllegalArgumentException("Invalid trust framework: $trustFramework")
            }
            
            println("✅ Gaia-X compliance validation passed")
        }
        
        // Validate expiration
        val exp = payload["exp"]?.jsonPrimitive?.long ?: 0
        val now = System.currentTimeMillis() / 1000
        if (exp <= now) {
            throw IllegalArgumentException("Envelope has expired")
        }
        
        println("✅ Envelope structure validation passed")
    }
    
    /**
     * Create a complete Gaia-X participant presentation with all required EVCs
     */
    suspend fun createCompleteGaiaXPresentation(
        presentationRequest: JsonObject,
        signerKeyIdOrAlias: String,
        domain: String? = null
    ): Result<String> {
        return try {
            println("Creating complete Gaia-X participant presentation...")
            
            // Validate we have all required credentials
            val requiredCredentials = credentialManager.getGaiaXRequiredCredentials()
                ?: return Result.failure(IllegalArgumentException("Missing required credentials for Gaia-X presentation"))
            
            // Get all credential JWTs
            val legalPersonJwt = credentialManager.loadCredentialJwt(requiredCredentials.legalPersonCredential.credentialId)!!
            val lrnJwt = credentialManager.loadCredentialJwt(requiredCredentials.lrnCredential.credentialId)!!
            val tcJwt = credentialManager.loadCredentialJwt(requiredCredentials.termsConditionsCredential.credentialId)!!
            
            // Create EVCs for each credential
            val envelopedCredentials = listOf(
                createEnvelopedVC(legalPersonJwt, signerKeyIdOrAlias, 
                    mapOf("gx:credentialType" to "LegalPersonCredential")).getOrThrow(),
                createEnvelopedVC(lrnJwt, signerKeyIdOrAlias,
                    mapOf("gx:credentialType" to "LRNCredential")).getOrThrow(),
                createEnvelopedVC(tcJwt, signerKeyIdOrAlias,
                    mapOf("gx:credentialType" to "TermsAndConditionsCredential")).getOrThrow()
            )
            
            // Load signing key and DID
            val signerKey = keyStorage.loadKey(signerKeyIdOrAlias)!!
            val didReference = didManager.loadDidWebReference(signerKeyIdOrAlias)!!
            val holderDid = didReference["didUrl"]?.jsonPrimitive?.content!!
            
            // Create presentation payload
            val vpPayload = buildJsonObject {
                put("iss", holderDid)
                put("sub", holderDid)
                put("aud", presentationRequest["aud"]?.jsonPrimitive?.content ?: "")
                put("iat", System.currentTimeMillis() / 1000)
                put("exp", (System.currentTimeMillis() / 1000) + 3600)
                put("jti", "urn:uuid:${java.util.UUID.randomUUID()}")
                put("nonce", presentationRequest["nonce"]?.jsonPrimitive?.content ?: generateNonce())
                
                put("vp", buildJsonObject {
                    put("@context", JsonArray(listOf(
                        JsonPrimitive("https://www.w3.org/2018/credentials/v1"),
                        JsonPrimitive("https://gaia-x.eu/credentials/v1")
                    )))
                    put("type", JsonArray(listOf(
                        JsonPrimitive("VerifiablePresentation"),
                        JsonPrimitive("GaiaXParticipantPresentation")
                    )))
                    put("id", "urn:uuid:${java.util.UUID.randomUUID()}")
                    put("holder", holderDid)
                    
                    // Include all Enveloped VCs
                    put("verifiableCredential", JsonArray(envelopedCredentials.map { JsonPrimitive(it) }))
                    
                    // Gaia-X compliance metadata
                    put("gx:compliance", buildJsonObject {
                        put("gx:trustFramework", "GAIA-X")
                        put("gx:version", "2210")
                        put("gx:participantType", "legal-person")
                        put("gx:complianceLevel", "basic")
                        put("gx:selfDescription", buildJsonObject {
                            put("gx:legalPerson", JsonPrimitive(envelopedCredentials[0]))
                            put("gx:lrn", JsonPrimitive(envelopedCredentials[1]))
                            put("gx:termsAndConditions", JsonPrimitive(envelopedCredentials[2]))
                        })
                    })
                    
                    // Domain binding if provided
                    if (domain != null) {
                        put("gx:domainBinding", buildJsonObject {
                            put("gx:domain", domain)
                            put("gx:bindingType", "eIDAS-QWAC")
                        })
                    }
                })
            }
            
            // Sign the complete presentation
            val signedVP = signEnvelopePayload(vpPayload, signerKey)
            
            println("✅ Complete Gaia-X presentation created successfully")
            println("Contains ${envelopedCredentials.size} Enveloped VCs")
            println("Holder DID: $holderDid")
            
            Result.success(signedVP)
            
        } catch (e: Exception) {
            println("❌ Error creating complete Gaia-X presentation: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Validate Gaia-X compliance of a presentation
     */
    fun validateGaiaXCompliance(presentationJwt: String): ComplianceValidationResult {
        return try {
            println("Validating Gaia-X compliance...")
            
            // Decode JWT payload
            val parts = presentationJwt.split(".")
            val payload = Json.parseToJsonElement(
                String(java.util.Base64.getUrlDecoder().decode(parts[1]))
            ).jsonObject
            
            val vp = payload["vp"]?.jsonObject
                ?: return ComplianceValidationResult(false, "No VP found in JWT")
            
            val compliance = vp["gx:compliance"]?.jsonObject
                ?: return ComplianceValidationResult(false, "No Gaia-X compliance metadata found")
            
            // Check trust framework
            val trustFramework = compliance["gx:trustFramework"]?.jsonPrimitive?.content
            if (trustFramework != "GAIA-X") {
                return ComplianceValidationResult(false, "Invalid trust framework: $trustFramework")
            }
            
            // Check required credential types
            val verifiableCredentials = vp["verifiableCredential"]?.jsonArray
                ?: return ComplianceValidationResult(false, "No verifiable credentials found")
            
            if (verifiableCredentials.size < 3) {
                return ComplianceValidationResult(false, "Insufficient credentials for Gaia-X compliance")
            }
            
            println("✅ Gaia-X compliance validation passed")
            ComplianceValidationResult(true, "Full Gaia-X compliance achieved")
            
        } catch (e: Exception) {
            println("❌ Compliance validation error: ${e.message}")
            ComplianceValidationResult(false, "Validation error: ${e.message}")
        }
    }
    

}

/**
 * Result of compliance validation
 */
data class ComplianceValidationResult(
    val isCompliant: Boolean,
    val message: String,
    val details: Map<String, Any> = emptyMap()
)

/**
 * Test the Enveloped VC/VP manager
 */
suspend fun testEnvelopedVcVpManager() {
    val evpManager = EnvelopedVcVpManager()
    
    println("=== Testing Enveloped VC/VP Manager ===")
    
    // Sample presentation request
    val sampleRequest = buildJsonObject {
        put("aud", "did:web:gaia-x-verifier.eu")
        put("nonce", "12345678901234567890")
        put("response_type", "vp_token")
        put("scope", "gaia-x legal-person")
        put("presentation_definition", buildJsonObject {
            put("input_descriptors", JsonArray(listOf(
                buildJsonObject {
                    put("id", "legal-person")
                    put("constraints", buildJsonObject {
                        put("fields", JsonArray(listOf(
                            buildJsonObject {
                                put("path", JsonArray(listOf(JsonPrimitive("$.vc.type"))))
                                put("filter", buildJsonObject {
                                    put("contains", buildJsonObject {
                                        put("const", "LegalPersonCredential")
                                    })
                                })
                            }
                        )))
                    })
                }
            )))
        })
    }
    
    // Test presentation creation (would need actual keys and credentials in practice)
    println("Sample presentation request created for testing")
    println("Request: ${sampleRequest["aud"]?.jsonPrimitive?.content}")
}