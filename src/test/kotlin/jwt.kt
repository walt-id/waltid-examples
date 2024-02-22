import id.walt.credentials.CredentialBuilder
import id.walt.credentials.CredentialBuilderType
import id.walt.crypto.utils.JsonUtils.toJsonObject
import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.json.JsonPrimitive
import kotlin.time.Duration.Companion.days

class jwt : StringSpec({
    "".config() {
        val vc = CredentialBuilder(CredentialBuilderType.W3CV11CredentialBuilder).apply {
            // Adds context next to default "https://www.w3.org/2018/credentials/v1"
            addContext("https://purl.imsglobal.org/spec/ob/v3p0/context-3.0.2.json")
            // Adds type next to default "VerifiableCredential"
            addType("OpenBadgeCredential")
            credentialId = "urn:uuid:4177e048-9a4a-474e-9dc6-aed4e61a6439"
            issuerDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            // Sets issuance date to current time - 1.5 min
            validFromNow()
            // Adds expiration date
            validFor(2.days)
            subjectDid = "did:key:z6MksFnax6xCBWdo6hhfZ7pEsbyaq9MMNdmABS1NrcCDZJr3"
            // Used to add any custom data
            useData("name", JsonPrimitive("JFF x vc-edu PlugFest 3 Interoperability"))
            // Used to insert credential subject data
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
        }.buildW3C() // Builds the credential and returns W3CVC object

        println(vc.toPrettyJson())
    }
})