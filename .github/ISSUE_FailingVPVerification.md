# VP verification fails: Overall Verification Result is false

- Detected on: 2025-09-03 16:01 local time
- Module: vp.Verify
- Affected version: current main (see build.gradle.kts version WALTID 0.13.0, Gradle 8.6)

## Summary
Running the Verifiable Presentation verification example prints `Overall Verification Result: false`. A dedicated JUnit 5 test has been added that documents this failure and currently fails.

## Failing test
- File: `src/test/kotlin/vp/VerifyVpTest.kt`
- Test: `verifiable presentation should verify successfully`
- Current result: Fails with assertion `Expected verifyVP() to result in overall success, but it currently fails.`

```
Overall Verification Result: false
Expected verifyVP() to result in overall success, but it currently fails. Output: Success(kotlin.Unit)
```

## Steps to reproduce
1. Clone repo and build dependencies:
   ```bash
   ./gradlew build
   ```
2. Run the VP verification example directly:
   ```bash
   ./gradlew run -PmainClass=vp.VerifyKt
   ```
   Observe the log line: `Overall Verification Result: false`.
3. Run the failing test:
   ```bash
   ./gradlew test --tests vp.VerifyVpTest
   ```
   The test fails, confirming the issue.

## Expected behavior
- VP verification for the included sample should pass, resulting in `Overall Verification Result: true` and the test should pass.

## Actual behavior
- `Overall Verification Result: false` and the test fails.

## Environment
- Gradle: 8.6
- Java/Kotlin: Kotlin JVM plugin 2.1.20; Java 11+
- Dependencies (walt.id): 0.13.0
- OS: (CI-dependent / local)

## Notes / Suspicions
- The policies configured in `vp/Verify.kt` may not align with the signed VP and embedded VC(s). For instance:
  - AllowedIssuerPolicy is set to a specific `issuerDid` which may not match the VC issuer.
  - HolderBindingPolicy requires matching holder DID; mismatch would fail.
  - ExpirationDatePolicy is configured for type mapping; sample data may already be expired.
- The example prints success/failure but does not expose the result to callers; for easier testing, consider returning the result or exposing a helper that returns `overallSuccess()`.

## Proposed next steps
- Verify and align the `issuerDid`, `holderDid`, and the embedded credential’s issuer.
- Check the sample VP/VC dates for `nbf`, `exp`, and ensure they are valid.
- Optionally, update `verifyVP()` to return the boolean so tests can assert directly instead of relying on logs.

/cc maintainers