package waltid.x509;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import id.walt.crypto2.CryptoRuntime;
import id.walt.crypto2.algorithms.EcdsaSignatureEncoding;
import id.walt.crypto2.algorithms.SignatureAlgorithm;
import id.walt.crypto2.keys.KeyEncodingFormat;
import id.walt.crypto2.keys.KeySpec;
import id.walt.crypto2.keys.KeyUsage;
import id.walt.crypto2.keys.SoftwareKey;
import id.walt.crypto2.providers.GenerateSoftwareKeyRequest;
import id.walt.crypto2.providers.ProviderSelection;
import id.walt.crypto2.providers.cryptography.DefaultSoftwareKeyProvidersKt;
import kotlin.jvm.internal.DefaultConstructorMarker;

import static waltid.x509.JavaInterop.await;

/**
 * Java-side construction helpers for {@code waltid-crypto2}.
 *
 * <p>{@code waltid-crypto2} models several of its value types as Kotlin
 * {@code @JvmInline value class} ({@code KeyId}, {@code EcCurve}, {@code DigestAlgorithm}). This has
 * two consequences for Java callers, both worked around here so the examples themselves stay clean:
 *
 * <ul>
 *   <li>The companion constants ({@code EcCurve.P256}, {@code DigestAlgorithm.SHA_256}, ...) compile
 *       to name-mangled accessors that Java source cannot call. They are substituted here by their
 *       documented underlying strings - {@code EcCurve.P256} wraps {@code "P-256"} and
 *       {@code DigestAlgorithm.SHA_256} wraps {@code "SHA-256"} (see the {@code waltid-crypto2}
 *       sources).</li>
 *   <li>Every constructor that takes an inline-class parameter ({@code KeySpec.Ec},
 *       {@code SignatureAlgorithm.Ecdsa}, {@code GenerateSoftwareKeyRequest}) is emitted by the
 *       Kotlin compiler as a {@code private} real constructor plus a {@code synthetic} public
 *       constructor with a trailing {@code DefaultConstructorMarker}. {@code javac} refuses to call
 *       either, so {@link #viaSyntheticConstructor} reaches the synthetic one by reflection - the
 *       standard fallback for constructing inline-parameter types from Java.</li>
 * </ul>
 */
final class Crypto2Support {

    /** Underlying value of {@code id.walt.crypto2.keys.EcCurve.P256}. */
    private static final String EC_CURVE_P256 = "P-256";

    /** Underlying value of {@code id.walt.crypto2.algorithms.DigestAlgorithm.SHA_256}. */
    private static final String DIGEST_SHA_256 = "SHA-256";

    private Crypto2Support() {
    }

    /** Equivalent of {@code CryptoRuntime(defaultSoftwareKeyProviders())}. */
    static CryptoRuntime defaultRuntime() {
        return new CryptoRuntime(
                DefaultSoftwareKeyProvidersKt.defaultSoftwareKeyProviders(),
                Collections.emptyList()
        );
    }

    /** Equivalent of {@code KeySpec.Ec(EcCurve.P256)}. */
    static KeySpec ecP256() {
        return viaSyntheticConstructor(KeySpec.Ec.class, EC_CURVE_P256);
    }

    /** Equivalent of {@code SignatureAlgorithm.Ecdsa(DigestAlgorithm.SHA_256, EcdsaSignatureEncoding.DER)}. */
    static SignatureAlgorithm ecdsaSha256Der() {
        return viaSyntheticConstructor(SignatureAlgorithm.Ecdsa.class, DIGEST_SHA_256, EcdsaSignatureEncoding.DER);
    }

    static Set<KeyUsage> signAndVerify() {
        return new LinkedHashSet<>(Arrays.asList(KeyUsage.SIGN, KeyUsage.VERIFY));
    }

    /**
     * Equivalent of
     * {@code GenerateSoftwareKeyRequest(id = KeyId(id), spec = spec, usages = setOf(SIGN, VERIFY))}.
     */
    static GenerateSoftwareKeyRequest softwareKeyRequest(String id, KeySpec spec) {
        return viaSyntheticConstructor(
                GenerateSoftwareKeyRequest.class,
                id,
                spec,
                signAndVerify(),
                KeyEncodingFormat.JWK,
                Collections.<String, String>emptyMap()
        );
    }

    /** Equivalent of {@code cryptoRuntime.generateSoftwareKey(request)}. */
    static SoftwareKey generateSoftwareKey(CryptoRuntime runtime, GenerateSoftwareKeyRequest request) {
        return await(cont -> runtime.generateSoftwareKey(request, ProviderSelection.Automatic.INSTANCE, cont));
    }

    /**
     * Invokes the Kotlin-generated {@code synthetic} constructor of {@code type} - the one whose
     * final parameter is a {@link DefaultConstructorMarker} - passing {@code args} followed by a
     * {@code null} marker.
     */
    private static <T> T viaSyntheticConstructor(Class<T> type, Object... args) {
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length == args.length + 1
                    && params[params.length - 1] == DefaultConstructorMarker.class) {
                Object[] withMarker = Arrays.copyOf(args, args.length + 1); // trailing marker stays null
                try {
                    constructor.setAccessible(true);
                    return type.cast(constructor.newInstance(withMarker));
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Could not construct " + type.getName(), e);
                }
            }
        }
        throw new IllegalStateException("No synthetic constructor found for " + type.getName());
    }
}
