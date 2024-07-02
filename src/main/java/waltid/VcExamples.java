package waltid;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.serialization.json.JsonObject;
import org.jetbrains.annotations.NotNull;

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


        Key key = (Key) JWKKey.Companion.generate(KeyType.Ed25519, null , null);
        System.out.println("Key: " + key);
// TODO: Uncomment this code when the async API is fixed

//        WaltidServices.INSTANCE.minimalInit(
//                new Continuation<Unit>() {
//                    @NotNull
//                    @Override
//                    public CoroutineContext getContext() {
//                        return null;
//                    }
//
//                    @Override
//                    public void resumeWith(@NotNull Object result) {
//                        if (result instanceof Result.Failure) {
//                            Throwable exception = ((Result.Failure) result).exception;
//                            System.err.println("Failed to initialize: " + exception);
//                        } else {
//                            System.out.println("Minimal init: " + result);
//                        }
//                    }
//                }
//        );

        String did = credentialBuilder.getIssuerDid();
        // sign
        String signed = (String) vc.signJws(key, did, null, did, new HashMap<>(), new HashMap<>() , null);
        System.out.println("Signed: " + signed);

        return signed;
    }

    private static String verify(String signed) {
        System.out.println("Verifying...");

        Verifier.INSTANCE.verifyCredential(
                signed,
                List.of(new PolicyRequest(new JwtSignaturePolicy(), null)),
                new HashMap<>(),
                new Continuation<List<PolicyResult>>() {
                    @NotNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE; // Properly initialize the context
                    }

                    @Override
                    public void resumeWith(@NotNull Object result) {
                        if (result instanceof Result.Failure) {
                            Throwable exception = ((Result.Failure) result).exception;
                            System.err.println("Failed to verify: " + exception);
                        } else {
                            List<PolicyResult> policyResults = (List<PolicyResult>) result;
                            for (PolicyResult policyResult : policyResults) {
                                System.out.println("Result: " + policyResult);
                            }
                        }
                    }
                }
        );
        return signed;
    }


    public static void main(String[] args) {
        String signed = buildAndSignVC();
        String verified = verify(signed);
        System.out.println("Verified: " + verified);

    }

}
