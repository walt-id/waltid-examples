package crypto;

import id.walt.credentials.CredentialBuilder;
import id.walt.credentials.CredentialBuilderType;
import id.walt.credentials.vc.vcs.W3CVC;
import id.walt.credentials.verification.Verifier;
import id.walt.credentials.verification.models.PolicyRequest;
import id.walt.credentials.verification.models.PolicyResult;
import id.walt.credentials.verification.policies.JwtSignaturePolicy;
import id.walt.crypto.keys.Key;
import id.walt.crypto.keys.KeyType;
import id.walt.crypto.keys.jwk.JWKKey;
import id.walt.crypto.utils.JsonUtils;
import id.walt.did.helpers.WaltidServices;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kotlinx.serialization.json.JsonObject;

public class VcExamples {

    public static String buildAndSignVC() {
        String entityIdentificationNumber = "12345";
        String issuingAuthorityId = "abc";
        String proofType = "document";
        String proofLocation = "Berlin-Brandenburg";

        Map<String, Object> issuingCircumstances = new HashMap<>();
        issuingCircumstances.put("proofType", proofType);
        issuingCircumstances.put("locationType", "physicalLocation");
        issuingCircumstances.put("location", proofLocation);

        Map<String, Object> credentialSubjectMap = new HashMap<>();
        credentialSubjectMap.put("entityIdentification", (entityIdentificationNumber));
        credentialSubjectMap.put("issuingAuthority", issuingAuthorityId);
        credentialSubjectMap.put("issuingCircumstances", issuingCircumstances);

        JsonObject credentialSubject = JsonUtils.INSTANCE.javaToJsonObject(credentialSubjectMap);

        CredentialBuilder credentialBuilder = new CredentialBuilder(CredentialBuilderType.W3CV2CredentialBuilder);
        credentialBuilder.addContext("https://www.w3.org/ns/credentials/examples/v2");
        credentialBuilder.addType("MyCustomCredential");
        credentialBuilder.randomCredentialSubjectUUID();
        credentialBuilder.setIssuerDid("did:key:abc");
        credentialBuilder.setSubjectDid("did:key:xyz");
        credentialBuilder.validFromNow();
        credentialBuilder.useStatusList2021Revocation("https://university.example/credentials/status/3", 94567);
        credentialBuilder.useCredentialSubject(credentialSubject);

        W3CVC vc = credentialBuilder.buildW3C();
        System.out.println("VC: " + vc.toPrettyJson());

        // setup signing
        Key key = JWKKey.Companion.generateBlocking(KeyType.Ed25519, null);

        WaltidServices.INSTANCE.minimalInitBlocking();
        String did = DidExamples.generateDidSync(key);

        // sign
        String signed = vc.signJwsBlocking(key, did, null, did, new HashMap<>(), new HashMap<>());
        System.out.println("Signed: " + signed);

        return signed;
    }

    private static void verify(String signed) {
        System.out.println("Verifying...");
        List<PolicyResult> results = Verifier.INSTANCE.verifyCredentialBlocking(signed,
                List.of(new PolicyRequest(new JwtSignaturePolicy(), null)),
                new HashMap<>()
        );

        for (PolicyResult result : results) {
            System.out.println("Result: " + result);
        }
    }

    public static void main(String[] args) {
        String signed = buildAndSignVC();
        verify(signed);

    }

}
