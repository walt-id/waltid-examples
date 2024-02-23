import id.walt.credentials.CredentialBuilder
import id.walt.credentials.CredentialBuilderType
import id.walt.credentials.vc.vcs.W3CVC
import id.walt.credentials.verification.Verifier
import id.walt.credentials.verification.models.PolicyRequest
import id.walt.credentials.verification.policies.JwtSignaturePolicy
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import id.walt.crypto.utils.JsonUtils.toJsonObject
import id.walt.did.dids.DidService
import id.walt.sdjwt.DecoyMode
import id.walt.sdjwt.SDField
import id.walt.sdjwt.SDMap
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.instanceOf
import kotlinx.serialization.json.JsonPrimitive
import kotlin.reflect.typeOf
import kotlin.time.Duration.Companion.days

class jwt : StringSpec({
    "Create Credential".config() {
        val vc = CredentialBuilder(CredentialBuilderType.W3CV11CredentialBuilder).apply {
            addContext("https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json")
            addType("OpenBadgeCredential")
            credentialId = "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439"
            issuerDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            validFromNow()
            validFor(2.days)
            subjectDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            useData("name", JsonPrimitive("JFF x vc-edu PlugFest 3 Interoperability"))
            useCredentialSubject(
                mapOf(
                    "type" to listOf("AchievementSubject"),
                    "achievement" to mapOf(
                        "id" to "urn:uuid:ac254bd5-8fad-4bb1-9d29-efd938536926",
                        "type" to listOf("Achievement"),
                        "name" to "JFF x vc-edu PlugFest 3 Interoperability",
                        "description" to "This wallet supports the use of W3C Verifiable Credentials and has demonstrated interoperability during the presentation request workflow during JFF x VC-EDU PlugFest 3.",
                        "criteria" to mapOf(
                            "type" to "Criteria",
                            "narrative" to "Wallet solutions providers earned this badge by demonstrating interoperability during the presentation request workflow. This includes successfully receiving a presentation request, allowing the holder to select at least two types of verifiable credentials to create a verifiable presentation, returning the presentation to the requestor, and passing verification of the presentation and the included credentials."
                        ),
                        "image" to mapOf(
                            "id" to "https://w3c-ccg.github.io/vc-ed/plugfest-3-2023/images/JFF-VC-EDU-PLUGFEST3-badge-image.png",
                            "type" to "Image"
                        )
                    )
                ).toJsonObject()
            )
        }.buildW3C()

        vc shouldBe instanceOf<W3CVC>()
        vc shouldNotBe null
    }

    "Sign Credential JWT".config() {
        DidService.minimalInit()
        val myIssuerKey = LocalKey.generate(KeyType.Ed25519)
        val myIssuerDid = DidService.registerByKey("key", myIssuerKey).did
        val mySubjectDid = "did:key:xyz"
        val vc = CredentialBuilder(CredentialBuilderType.W3CV11CredentialBuilder).apply {
            addContext("https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json")
            addType("OpenBadgeCredential")
            credentialId = "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439"
            issuerDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            validFromNow()
            validFor(2.days)
            subjectDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            useData("name", JsonPrimitive("JFF x vc-edu PlugFest 3 Interoperability"))
            useCredentialSubject(
                mapOf(
                    "type" to listOf("AchievementSubject"),
                    "achievement" to mapOf(
                        "id" to "urn:uuid:ac254bd5-8fad-4bb1-9d29-efd938536926",
                        "type" to listOf("Achievement"),
                        "name" to "JFF x vc-edu PlugFest 3 Interoperability",
                        "description" to "This wallet supports the use of W3C Verifiable Credentials and has demonstrated interoperability during the presentation request workflow during JFF x VC-EDU PlugFest 3.",
                        "criteria" to mapOf(
                            "type" to "Criteria",
                            "narrative" to "Wallet solutions providers earned this badge by demonstrating interoperability during the presentation request workflow. This includes successfully receiving a presentation request, allowing the holder to select at least two types of verifiable credentials to create a verifiable presentation, returning the presentation to the requestor, and passing verification of the presentation and the included credentials."
                        ),
                        "image" to mapOf(
                            "id" to "https://w3c-ccg.github.io/vc-ed/plugfest-3-2023/images/JFF-VC-EDU-PLUGFEST3-badge-image.png",
                            "type" to "Image"
                        )
                    )
                ).toJsonObject()
            )
        }.buildW3C()
        // vc refers to the credential created in the previous section
        val signed: String = vc.signJws(myIssuerKey, myIssuerDid, mySubjectDid)

        val results = Verifier.verifyCredential(
            signed,
            listOf(
                PolicyRequest(JwtSignaturePolicy())
            )
        )
        results.forEach { verification ->
            verification.isSuccess() shouldBe true
        }
    }

    "Sign Credential SD-JWT".config() {
        DidService.minimalInit()
        val myIssuerKey = LocalKey.generate(KeyType.Ed25519)
        val myIssuerDid = DidService.registerByKey("key", myIssuerKey).did
        val mySubjectDid = "did:key:xyz"
        val disclosureMap = SDMap(
            fields = mapOf("name" to SDField(true)),
            decoyMode = DecoyMode.RANDOM,
            decoys = 2
        )
        val vc = CredentialBuilder(CredentialBuilderType.W3CV11CredentialBuilder).apply {
            addContext("https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json")
            addType("OpenBadgeCredential")
            credentialId = "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439"
            issuerDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            validFromNow()
            validFor(2.days)
            subjectDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            useData("name", JsonPrimitive("JFF x vc-edu PlugFest 3 Interoperability"))
            useCredentialSubject(
                mapOf(
                    "type" to listOf("AchievementSubject"),
                    "achievement" to mapOf(
                        "id" to "urn:uuid:ac254bd5-8fad-4bb1-9d29-efd938536926",
                        "type" to listOf("Achievement"),
                        "name" to "JFF x vc-edu PlugFest 3 Interoperability",
                        "description" to "This wallet supports the use of W3C Verifiable Credentials and has demonstrated interoperability during the presentation request workflow during JFF x VC-EDU PlugFest 3.",
                        "criteria" to mapOf(
                            "type" to "Criteria",
                            "narrative" to "Wallet solutions providers earned this badge by demonstrating interoperability during the presentation request workflow. This includes successfully receiving a presentation request, allowing the holder to select at least two types of verifiable credentials to create a verifiable presentation, returning the presentation to the requestor, and passing verification of the presentation and the included credentials."
                        ),
                        "image" to mapOf(
                            "id" to "https://w3c-ccg.github.io/vc-ed/plugfest-3-2023/images/JFF-VC-EDU-PLUGFEST3-badge-image.png",
                            "type" to "Image"
                        )
                    )
                ).toJsonObject()
            )
        }.buildW3C()
        val signed: String = vc.signJws(myIssuerKey, myIssuerDid, mySubjectDid)
        val results = Verifier.verifyCredential(
            signed,
            listOf(
                PolicyRequest(JwtSignaturePolicy())
            )
        )
        results.forEach { verification ->
            verification.isSuccess() shouldBe true
        }
    }
})