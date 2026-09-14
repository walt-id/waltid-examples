package waltid.x509;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;

/**
 * Small bridge so the Java ports of the x509 examples can call the Kotlin
 * {@code suspend} APIs of {@code waltid-x509} / {@code waltid-crypto2}.
 *
 * <p>The whole certificate/key API is written as Kotlin coroutines, so every call that is
 * {@code suspend} in Kotlin takes a trailing {@link Continuation} in Java and returns
 * {@code Object} (either the result or {@code COROUTINE_SUSPENDED}). {@link #await(SuspendCall)}
 * drives a single such call to completion on the current thread via
 * {@link BuildersKt#runBlocking}. This mirrors what {@code suspend fun main()} does implicitly
 * in the Kotlin originals.
 */
final class JavaInterop {

    private JavaInterop() {
    }

    /** A single Kotlin {@code suspend} call, exposed to Java as {@code fn(continuation)}. */
    @FunctionalInterface
    interface SuspendCall<T> {
        Object invoke(Continuation<? super T> continuation);
    }

    @SuppressWarnings("unchecked")
    static <T> T await(SuspendCall<T> call) {
        try {
            return BuildersKt.runBlocking(
                    EmptyCoroutineContext.INSTANCE,
                    (Function2<CoroutineScope, Continuation<? super T>, Object>)
                            (scope, continuation) -> call.invoke(continuation)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while awaiting a suspend call", e);
        }
    }
}
