# walt.id Examples

<div align="center">
 <h1>walt.id Identity SDK Examples</h1>
 <p>Comprehensive examples and tutorials for the walt.id Identity SDK, covering cryptographic operations, DID management, and verifiable credentials.</p>

<a href="https://walt.id/community">
<img src="https://img.shields.io/badge/Join-The Community-blue.svg?style=flat" alt="Join community!" />
</a>
<a href="https://twitter.com/intent/follow?screen_name=walt_id">
<img src="https://img.shields.io/twitter/follow/walt_id.svg?label=Follow%20@walt_id" alt="Follow @walt_id" />
</a>
<a href="https://github.com/walt-id/waltid-examples/blob/main/LICENSE">
<img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License: Apache 2.0" />
</a>
</div>

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Available Examples](#available-examples)
- [Trust-list formats](#trust-list-formats)
- [X.509 Certificates](#x509-certificates)
- [Crypto2 (new library)](#crypto2-new-library)
- [Running Examples](#running-examples)
- [Documentation](#documentation)
- [Community](#community)
- [License](#license)

## Prerequisites

- **Java 21** - the Gradle wrapper (`./gradlew`, included) provisions the daemon and Kotlin/Java toolchain itself,
  no separate Gradle install needed
- **IDE** (IntelliJ IDEA recommended) - optional, only for [Using IDE](#using-ide)

## Quick Start

```bash
git clone https://github.com/walt-id/waltid-examples.git
cd waltid-examples
./gradlew build --rerun-tasks
./gradlew run -PmainClass=RunAllKt
```

That's the whole loop: clone, build, run. For running one example at a time instead of everything,
jump to [Running Examples](#running-examples) - every `-PmainClass=...` command is listed there.

## Project Structure

```
waltid-examples/
├── src/main/
│   ├── kotlin/                   # Kotlin examples
│   │   ├── crypto/               # Cryptographic operations
│   │   │   ├── key/              # Key management
│   │   │   │   ├── create/       # Key generation
│   │   │   │   ├── decode/       # Key import (JWK, PEM, Raw)
│   │   │   │   └── encode/       # Key export (JWK, PEM, Raw)
│   │   │   └── signatures/       # Digital signatures
│   │   │       ├── jws/          # JSON Web Signatures
│   │   │       └── raw/          # Raw signatures
│   │   ├── did/                  # Decentralized Identifiers
│   │   │   ├── create/           # DID creation methods
│   │   │   └── resolve/          # DID resolution
│   │   ├── vc/                   # Verifiable Credentials
│   │   │   ├── jwt/              # JWT-based VCs
│   │   │   └── sdjwt/            # Selective Disclosure JWTs
│   │   ├── vp/                   # Verifiable Presentations
│   │   ├── x509/                 # X.509 certificates (signing, trust stores, ISO mDL onboarding, ETSI Provider/WRPAC/WRPRC)
│   │   ├── trustregistry/        # Trust-list formats (TSL/LoTE) and entity types (PID/Wallet/WRPAC/WRPRC Provider)
│   │   └── crypto2/              # New crypto2 library: keys, signatures, serialization, provider selection
│   └── java/                     # Java examples
│       └── waltid/               # Java implementation
│           ├── x509/             # Java ports of the X.509 examples, including ETSI Provider/WRPAC/WRPRC
│           └── crypto2/          # Java ports of the crypto2 examples
│   └── resources/trust-registry/ # LoTE fixtures, plus real snapshots (German TSL, WE BUILD WRPAC/WRPRC LoTE)
└── build.gradle.kts              # Build configuration
```

## Available Examples

### Cryptographic Operations

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **Key Generation** | Create cryptographic keys (Ed25519, RSA, Secp256k1, Secp256r1) | [Kotlin](src/main/kotlin/crypto/key/create) | [Java](src/main/java/waltid/KeysExamples.java) |
| **Key Import** | Import keys from JWK, PEM, or raw formats | [Kotlin](src/main/kotlin/crypto/key/decode) | [Java](src/main/java/waltid/KeysExamples.java) |
| **Key Export** | Export keys to various formats | [Kotlin](src/main/kotlin/crypto/key/encode) | [Java](src/main/java/waltid/KeysExamples.java) |
| **Raw Signatures** | Sign and verify raw data | [Kotlin](src/main/kotlin/crypto/signatures/raw) | [Java](src/main/java/waltid/KeysExamples.java) |
| **JWS Signatures** | JSON Web Signature operations | [Kotlin](src/main/kotlin/crypto/signatures/jws) | [Java](src/main/java/waltid/KeysExamples.java) |

### Decentralized Identifiers (DIDs)

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **DID Creation** | Generate DIDs using various methods (did:key, did:web, did:jwk, did:cheqd) | [Kotlin](src/main/kotlin/did/create) | [Java](src/main/java/waltid/DidExamples.java) |
| **DID Resolution** | Resolve DIDs to DID documents | [Kotlin](src/main/kotlin/did/resolve) | [Java](src/main/java/waltid/DidExamples.java) |

### Verifiable Credentials (VCs)

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **JWT VCs** | Create and verify JWT-based verifiable credentials | [Kotlin](src/main/kotlin/vc/jwt) | [Java](src/main/java/waltid/VcExamples.java) |
| **SD-JWT VCs** | Selective disclosure JWT credentials | [Kotlin](src/main/kotlin/vc/sdjwt) | [Java](src/main/java/waltid/VcExamples.java) |

### Verifiable Presentations (VPs)

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **VP Operations** | Create and verify verifiable presentations | [Kotlin](src/main/kotlin/vp) | [Java](src/main/java/waltid/VpExamples.java) |

### Trust-list Examples

The trust-list examples validate both the public URLs advertised by the Enterprise API and the library's local format
fixtures. They are also executed by the Kotlin [RunAll.kt](src/main/kotlin/RunAll.kt) entry point.

- [TrustListUrls.kt](src/main/kotlin/trustregistry/TrustListUrls.kt) checks Austria, Italy, and the EU LoTL
- Signed lists must pass XMLDSig integrity validation
- The EU LoTL must load as a pointer-only list with no providers
- [TrustListFormats.kt](src/main/kotlin/trustregistry/TrustListFormats.kt) additionally checks normative ETSI TS 119 602 JSON and XML fixtures
- [GermanTrustListExample.kt](src/main/kotlin/trustregistry/GermanTrustListExample.kt) - Germany's
  national TSL (`TRUST_SERVICE_PROVIDER`), signed with RSASSA-PSS rather than plain PKCS#1 v1.5;
  loaded from a local snapshot (`src/main/resources/trust-registry/tl-de.xml`, ~5 MB) rather than
  refetched live on every run
- [WrpacTrustListExample.kt](src/main/kotlin/trustregistry/WrpacTrustListExample.kt) /
  [WrprcTrustListExample.kt](src/main/kotlin/trustregistry/WrprcTrustListExample.kt) -
  `ACCESS_CERTIFICATE_PROVIDER` / `RELYING_PARTY_PROVIDER`, from a real public LoTE snapshot
  published by the WE BUILD WP4 Trust Infrastructure pilot
  ([webuild-consortium.github.io/wp4-trust-group](https://webuild-consortium.github.io/wp4-trust-group/))
- [PidProviderTrustListExample.kt](src/main/kotlin/trustregistry/PidProviderTrustListExample.kt) /
  [WalletProviderTrustListExample.kt](src/main/kotlin/trustregistry/WalletProviderTrustListExample.kt) -
  `PID_PROVIDER` / `WALLET_PROVIDER`, from the local LoTE fixtures (`lote.xml` / `lote.json`); no
  public LoTE publisher lists these two roles yet as of this writing - the one real pilot checked
  for this still publishes them as TSLs instead

### X.509 Certificates

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **Sign Certificates** | Create a self-signed root, sign a leaf certificate, and validate the chain | [Kotlin](src/main/kotlin/x509/SignCertificateExample.kt) | [Java](src/main/java/waltid/x509/SignCertificateExample.java) |
| **Configure Trust Stores** | Combine trust stores and configure a custom `X509CertificateUtil` | [Kotlin](src/main/kotlin/x509/ConfigureTrustStoreExample.kt) | [Java](src/main/java/waltid/x509/ConfigureTrustStoreExample.java) |
| **ISO mDL Onboarding** | Build an ISO/IEC 18013-5 IACA root and Document Signer certificate | [Kotlin](src/main/kotlin/x509/IsoMdlOnboardingExample.kt) | [Java](src/main/java/waltid/x509/IsoMdlOnboardingExample.java) |
| **PID Provider** | Build and validate a PID Provider certificate (ETSI TS 119 412-6), both CA-issued and self-signed | [Kotlin](src/main/kotlin/x509/PidProviderExample.kt) | [Java](src/main/java/waltid/x509/PidProviderExample.java) |
| **Wallet Provider** | Build and validate a Wallet Provider certificate (ETSI TS 119 412-6, WAL-5.1-01) | [Kotlin](src/main/kotlin/x509/WalletProviderExample.kt) | [Java](src/main/java/waltid/x509/WalletProviderExample.java) |
| **WRPAC (Relying Party Access)** | Build and validate a Wallet Relying Party Access Certificate (ETSI TS 119 411-8) across both policy extremes (legal/qualified and natural/non-qualified) | [Kotlin](src/main/kotlin/x509/WrpacRelyingPartyExample.kt) | [Java](src/main/java/waltid/x509/WrpacRelyingPartyExample.java) |
| **WRPRC (Relying Party Registration)** | Build and validate a Wallet Relying Party Registration Certificate (ETSI TS 119 475) - draft profile, doesn't yet validate the registered intended use | [Kotlin](src/main/kotlin/x509/WrprcRelyingPartyExample.kt) | [Java](src/main/java/waltid/x509/WrprcRelyingPartyExample.java) |

The Java ports are direct translations of the Kotlin sources and use the crypto2 library (via `Crypto2Keys`, a
shared internal helper) to generate the EC P-256 keys used to sign the certificates. Every example prints the full
PEM of each certificate it creates, so you can paste it into an ASN.1/X.509 decoder to inspect it directly. See the
[waltid-x509 README](https://github.com/walt-id/waltid-identity/tree/main/waltid-libraries/crypto/waltid-x509#readme)
for the underlying library docs, including what the WRPRC profile deliberately doesn't validate yet.

### Crypto2 (new library)

`crypto2` is walt.id's next-generation crypto library, built around a `CryptoRuntime` that generates and manages
keys through pluggable providers (software or hardware-backed providers) instead of a fixed key
type per algorithm.

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **Key Generation** | Generate Ed25519, RSA, and secp256r1 (P-256) software keys | [Kotlin](src/main/kotlin/crypto2/key/create) | [Java](src/main/java/waltid/crypto2/key/create) |
| **Signatures** | Sign and verify with Ed25519 and secp256r1 keys | [Kotlin](src/main/kotlin/crypto2/signatures) | [Java](src/main/java/waltid/crypto2/signatures/Secp256r1Sign.java) |
| **PEM Export** | Export a key's public/private material to SPKI/PKCS8 PEM | [Kotlin](src/main/kotlin/crypto2/key/encode/PemExport.kt) | — |
| **Serialization & Restoration** | Serialize a key to JSON and restore it into an operational key via `CryptoRuntime` | [Kotlin](src/main/kotlin/crypto2/key/SerializationExample.kt) | — |
| **Provider Selection** | Automatic, explicit, and fallback-list provider selection, including the failure case | [Kotlin](src/main/kotlin/crypto2/ProviderSelectionDemo.kt) | — |
| **Simplified Java Usage** | Idiomatic blocking-call Java example (`.get()` on the `CompletionStage` API) instead of coroutine interop | — | [Java](src/main/java/waltid/crypto2/simple/SimpleSigningExample.java) |

None of the crypto2 examples are wired into `RunAllKt` / `waltid.RunAll` yet - run them individually (see
[Running Examples](#running-examples)).

## Running Examples

### Using Gradle

Every example is a `main()` function, run via `-PmainClass=<fully.qualified.Name>` (Kotlin files get a `Kt` suffix
unless they define a top-level object). `RunAllKt` / `waltid.RunAll` run everything in one call:

```bash
./gradlew run -PmainClass=RunAllKt        # Kotlin, all examples
./gradlew run -PmainClass=waltid.RunAll   # Java, all examples

# Key generation
./gradlew run -PmainClass=crypto.key.create.Ed25519Kt
./gradlew run -PmainClass=crypto.key.create.RSAKt

# DID operations
./gradlew run -PmainClass=did.create.KeyKt
./gradlew run -PmainClass=did.resolve.KeyKt

# Verifiable credentials
./gradlew run -PmainClass=vc.jwt.SignKt
./gradlew run -PmainClass=vc.sdjwt.SignKt

# Every supported trust-list format
./gradlew runTrustListFormats

# Only the three live Enterprise API URL claims
./gradlew validateTrustListUrls

# Entity types beyond TRUST_SERVICE_PROVIDER
./gradlew run -PmainClass=trustregistry.GermanTrustListExampleKt
./gradlew run -PmainClass=trustregistry.WrpacTrustListExampleKt
./gradlew run -PmainClass=trustregistry.WrprcTrustListExampleKt
./gradlew run -PmainClass=trustregistry.PidProviderTrustListExampleKt
./gradlew run -PmainClass=trustregistry.WalletProviderTrustListExampleKt

# X.509 certificates (Kotlin)
./gradlew run -PmainClass=x509.SignCertificateExampleKt
./gradlew run -PmainClass=x509.ConfigureTrustStoreExampleKt
./gradlew run -PmainClass=x509.IsoMdlOnboardingExampleKt
./gradlew run -PmainClass=x509.PidProviderExampleKt
./gradlew run -PmainClass=x509.WalletProviderExampleKt
./gradlew run -PmainClass=x509.WrpacRelyingPartyExampleKt
./gradlew run -PmainClass=x509.WrprcRelyingPartyExampleKt

# X.509 certificates (Java)
./gradlew run -PmainClass=waltid.x509.SignCertificateExample
./gradlew run -PmainClass=waltid.x509.ConfigureTrustStoreExample
./gradlew run -PmainClass=waltid.x509.IsoMdlOnboardingExample
./gradlew run -PmainClass=waltid.x509.PidProviderExample
./gradlew run -PmainClass=waltid.x509.WalletProviderExample
./gradlew run -PmainClass=waltid.x509.WrpacRelyingPartyExample
./gradlew run -PmainClass=waltid.x509.WrprcRelyingPartyExample

# crypto2 (Kotlin)
./gradlew run -PmainClass=crypto2.key.create.Ed25519Kt
./gradlew run -PmainClass=crypto2.signatures.Ed25519Kt
./gradlew run -PmainClass=crypto2.key.SerializationExampleKt
./gradlew run -PmainClass=crypto2.ProviderSelectionDemoKt

# crypto2 (Java)
./gradlew run -PmainClass=waltid.crypto2.key.create.Ed25519
./gradlew run -PmainClass=waltid.crypto2.signatures.Secp256r1Sign
./gradlew run -PmainClass=waltid.crypto2.simple.SimpleSigningExample
```

### Using IDE

1. **IntelliJ IDEA:**
   - Open the project
   - Navigate to any example file
   - Right-click and select "Run"

2. **VS Code:**
   - Install Kotlin and Java extensions
   - Use the integrated terminal to run Gradle commands

## Documentation

- **walt.id SDK Documentation**: [https://docs.walt.id/](https://docs.walt.id/)
- **Identity Repository**: [https://github.com/walt-id/waltid-identity](https://github.com/walt-id/waltid-identity)
- **Maven Repository**: [https://maven.waltid.dev/#/releases/id/walt](https://maven.waltid.dev/#/releases/id/walt) - this repo builds with Gradle only; add this as a Maven repository if you're consuming walt.id libraries from your own Maven project instead
- **API Reference**: Available in the test directories of the [identity repository](https://github.com/walt-id/waltid-identity)

## Community

Connect with the walt.id community:

* **Discord**: [Join our Discord server](https://discord.gg/AW8AgqJthZ)
* **Newsletter**: [Subscribe to updates](https://walt.id/newsletter)
* **YouTube**: [Watch tutorials](https://www.youtube.com/channel/UCXfOzrv3PIvmur_CmwwmdLA)
* **Twitter**: [Follow @walt_id](https://mobile.twitter.com/walt_id)
* **GitHub Discussions**: [Get help and discuss features](https://github.com/walt-id/.github/discussions)

## License

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <p>Made by the <a href="https://walt.id">walt.id</a> team</p>
  <p>
    <a href="https://walt.id">Website</a> •
    <a href="https://docs.walt.id">Documentation</a> •
    <a href="https://github.com/walt-id">GitHub</a> •
    <a href="https://discord.gg/AW8AgqJthZ">Discord</a>
  </p>
</div>
