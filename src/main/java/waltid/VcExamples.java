package waltid;

import id.walt.crypto.keys.Key;
import id.walt.crypto.keys.KeyType;
import id.walt.crypto.keys.jwk.JWKKey;
import id.walt.crypto.utils.JsonUtils;
import id.walt.did.helpers.WaltidServices;
import id.walt.policies.Verifier;
import id.walt.policies.models.PolicyRequest;
import id.walt.policies.models.PolicyResult;
import id.walt.policies.policies.JwtSignaturePolicy;
import id.walt.sdjwt.DecoyMode;
import id.walt.sdjwt.SDField;
import id.walt.sdjwt.SDMap;
import id.walt.w3c.CredentialBuilder;
import id.walt.w3c.CredentialBuilderType;
import id.walt.w3c.vc.vcs.W3CVC;
import kotlinx.serialization.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class VcExamples {

    public static String buildAndSignSDJWTVC() {
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
        Map<String, SDField> fields = new HashMap<>();
        fields.put("name", new SDField(Boolean.TRUE, null));
        SDMap disclosureMap = new SDMap(fields, DecoyMode.RANDOM, 2);
        String signed = vc.signSdJwtBlocking(key, did, null, did, disclosureMap, new HashMap<>(), new HashMap<>());
        System.out.println("Signed SD-JWT VC: " + signed);
        return signed;
    }

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

    public static void runVcExample() {
        String signed = buildAndSignVC();
        verify(signed);
        String signedSdJwt = buildAndSignSDJWTVC();
        verify(signedSdJwt);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        runVcExample();
    }

}
