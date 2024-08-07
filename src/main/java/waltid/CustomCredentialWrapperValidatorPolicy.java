package waltid;

import id.walt.credentials.schemes.JwsSignatureScheme;
import id.walt.credentials.verification.HolderBindingException;
import id.walt.credentials.verification.JavaCredentialWrapperValidatorPolicy;
import id.walt.crypto.utils.JwsUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kotlinx.serialization.json.JsonArray;
import kotlinx.serialization.json.JsonElement;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomCredentialWrapperValidatorPolicy extends JavaCredentialWrapperValidatorPolicy {
    @NotNull
    @Override
    public String getName() {
        return "java-custom-credential-wrapper-validator";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "This is a custom credential wrapper validator for Java";
    }

    @NotNull
    @Override
    public Object javaVerify(@NotNull JsonObject data, @Nullable Object args, @NotNull Map<String, ?> context) {
        // Getting the 'presenterDid' from data
        JsonElement issuerElement = data.get(JwsSignatureScheme.JwsOption.ISSUER);
        if (!(issuerElement instanceof JsonPrimitive)) {
            throw new IllegalArgumentException("Missing or invalid issuer in data");
        }
        String presenterDid = ((JsonPrimitive) issuerElement).getContent();

        // Getting the 'vp' from data
        JsonElement vpElement = data.get("vp");
        if (!(vpElement instanceof JsonObject vp)) {
            throw new IllegalArgumentException("No \"vp\" field in VP!");
        }

        // Getting the 'verifiableCredential' from 'vp'
        JsonElement credentialsElement = vp.get("verifiableCredential");
        if (!(credentialsElement instanceof JsonArray credentials)) {
            throw new IllegalArgumentException("No \"verifiableCredential\" field in \"vp\"!");
        }

        // Processing the credentials to get 'credentialSubjects'
        List<String> credentialSubjects = credentials.stream()
                .map(it -> {
                    if (!(it instanceof JsonPrimitive primitive)) {
                        throw new IllegalArgumentException("Invalid credential in \"verifiableCredential\"!");
                    }
                    String content = primitive.getContent();
                    JwsUtils.JwsParts decodedJws = JwsUtils.INSTANCE.decodeJws(content, false, false);
                    JsonObject payload = decodedJws.getPayload();
                    JsonElement subElement = payload.get("sub");
                    if (!(subElement instanceof JsonPrimitive)) {
                        throw new IllegalArgumentException("Invalid 'sub' in payload!");
                    }
                    return ((JsonPrimitive) subElement).getContent().split("#")[0];
                })
                .collect(Collectors.toList());

        // Returning the result based on 'credentialSubjects'
        boolean allMatch = credentialSubjects.stream().allMatch(sub -> sub.equals(presenterDid));
        if (allMatch) {
            return presenterDid;
        } else {
            throw new HolderBindingException(presenterDid, credentialSubjects);
        }
    }
}
