package waltid;

import id.walt.policies.JavaCredentialDataValidatorPolicy;
import java.util.Map;
import java.util.Set;
import id.walt.w3c.utils.VCFormat;
import java.util.Objects;
import kotlinx.serialization.json.JsonObject;
import kotlinx.serialization.json.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomCredentialDataValidatorPolicy extends JavaCredentialDataValidatorPolicy {

    @NotNull
    @Override
    public Object javaVerify(@NotNull JsonObject data, @Nullable Object args, @NotNull Map<String, ?> context) {
        if (!data.containsKey("my_verification")) {
            throw new IllegalArgumentException("Required attribute not found!");
        }

        String myVerificationContent = ((JsonPrimitive) Objects.requireNonNull(data.get("my_verification"))).getContent();

        if (!myVerificationContent.equalsIgnoreCase("hello world")) {
            throw new IllegalArgumentException("Greet the world!");
        }

        return myVerificationContent;
    }

    @NotNull
    @Override
    public Set<VCFormat> getSupportedVCFormats() {
        return Set.of(VCFormat.jwt_vc, VCFormat.jwt_vc_json, VCFormat.ldp_vc);
    }

    @NotNull
    @Override
    public String getName() {
        return "java-custom-data-validator";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "This is a custom data validator for Java";
    }
}
