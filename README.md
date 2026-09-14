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

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Project Structure](#project-structure)
- [Available Examples](#available-examples)
- [Trust-list formats](#trust-list-formats)
- [🔏 X.509 Certificates](#-x509-certificates)
- [🔑 Crypto2 (new library)](#-crypto2-new-library)
- [Running Examples](#running-examples)
- [Key Features](#key-features)
- [Documentation](#documentation)
- [Community](#community)
- [License](#license)

## 🔧 Prerequisites

- **Java 21** (the Gradle daemon and compilation toolchain are provisioned automatically)
- **Gradle 7.0+** (or Maven 3.6+)
- **IDE** (IntelliJ IDEA recommended)

## 🚀 Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/walt-id/waltid-examples.git
   cd waltid-examples
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run all examples (Kotlin):**
   ```bash
   ./gradlew run -PmainClass=RunAllKt
   ```

4. **Run all examples (Java):**
   ```bash
   ./gradlew run -PmainClass=waltid.RunAll
   ```

5. **Run individual examples:**
   ```bash
   # Generate cryptographic keys
   ./gradlew run -PmainClass=crypto.key.create.Ed25519Kt
   
   # Create a DID
   ./gradlew run -PmainClass=did.create.KeyKt
   
   # Sign a verifiable credential
   ./gradlew run -PmainClass=vc.jwt.SignKt
   ```

## 📁 Project Structure

```
waltid-examples/
├── src/main/
│   ├── kotlin/                    # Kotlin examples
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
│   │   ├── x509/                 # X.509 certificates (signing, trust stores, ISO mDL onboarding)
│   │   └── crypto2/              # New crypto2 library: keys, signatures, serialization, provider selection
│   └── java/                     # Java examples
│       └── waltid/               # Java implementation
│           ├── x509/             # Java ports of the X.509 examples
│           └── crypto2/          # Java ports of the crypto2 examples
│   └── resources/trust-registry/ # Synthetic LoTE and signed-JWS fixtures
└── build.gradle.kts               # Build configuration
```

## 📚 Available Examples

### 🔐 Cryptographic Operations

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **Key Generation** | Create cryptographic keys (Ed25519, RSA, Secp256k1, Secp256r1) | [📁](src/main/kotlin/crypto/key/create) | [📄](src/main/java/waltid/KeysExamples.java) |
| **Key Import** | Import keys from JWK, PEM, or raw formats | [📁](src/main/kotlin/crypto/key/decode) | [📄](src/main/java/waltid/KeysExamples.java) |
| **Key Export** | Export keys to various formats | [📁](src/main/kotlin/crypto/key/encode) | [📄](src/main/java/waltid/KeysExamples.java) |
| **Raw Signatures** | Sign and verify raw data | [📁](src/main/kotlin/crypto/signatures/raw) | [📄](src/main/java/waltid/KeysExamples.java) |
| **JWS Signatures** | JSON Web Signature operations | [📁](src/main/kotlin/crypto/signatures/jws) | [📄](src/main/java/waltid/KeysExamples.java) |

### 🆔 Decentralized Identifiers (DIDs)

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **DID Creation** | Generate DIDs using various methods (did:key, did:web, did:jwk, did:cheqd) | [📁](src/main/kotlin/did/create) | [📄](src/main/java/waltid/DidExamples.java) |
| **DID Resolution** | Resolve DIDs to DID documents | [📁](src/main/kotlin/did/resolve) | [📄](src/main/java/waltid/DidExamples.java) |

### 🎫 Verifiable Credentials (VCs)

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **JWT VCs** | Create and verify JWT-based verifiable credentials | [📁](src/main/kotlin/vc/jwt) | [📄](src/main/java/waltid/VcExamples.java) |
| **SD-JWT VCs** | Selective disclosure JWT credentials | [📁](src/main/kotlin/vc/sdjwt) | [📄](src/main/java/waltid/VcExamples.java) |

### 🎭 Verifiable Presentations (VPs)

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **VP Operations** | Create and verify verifiable presentations | [📁](src/main/kotlin/vp) | [📄](src/main/java/waltid/VpExamples.java) |

## Trust-list formats

The trust-list examples validate both the public URLs advertised by the Enterprise API and the library's local format
fixtures. They are also executed by the Kotlin [RunAll.kt](src/main/kotlin/RunAll.kt) entry point.

- [TrustListUrls.kt](src/main/kotlin/trustregistry/TrustListUrls.kt) checks Austria, Italy, and the EU LoTL
- Signed lists must pass XMLDSig integrity validation
- The EU LoTL must load as a pointer-only list with no providers
- [TrustListFormats.kt](src/main/kotlin/trustregistry/TrustListFormats.kt) additionally checks normative ETSI TS 119 602 JSON and XML fixtures

Publish the current library to Maven Local before testing unpublished changes:

```bash
cd /path/to/waltid-identity
./gradlew :waltid-libraries:credentials:waltid-trust-registry:publishToMavenLocal

cd /path/to/waltid-examples
./gradlew validateTrustListUrls
./gradlew runTrustListFormats
```

`validateTrustListUrls` is fail-fast: it returns a non-zero exit code if fetching, format detection, signature handling,
parsing, or expected list contents do not match. It requires outbound HTTPS access to:

```text
https://www.signatur.rtr.at/vertrauensliste.xml
https://eidas.agid.gov.it/TL/TSL-IT.xml
https://ec.europa.eu/tools/lotl/eu-lotl.xml
```

Signed TSLs report `INTEGRITY_VERIFIED`; the unsigned local TS 119 602 fixtures report `UNVERIFIED` because the example
explicitly opts into `ALLOW_UNSIGNED`. The EU LoTL check validates its distinct format and pointer count. Pointer targets
are not fetched automatically.

### 🔏 X.509 Certificates

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **Sign Certificates** | Create a self-signed root, sign a leaf certificate, and validate the chain | [📄](src/main/kotlin/x509/SignCertificateExample.kt) | [📄](src/main/java/waltid/x509/SignCertificateExample.java) |
| **Configure Trust Stores** | Combine trust stores and configure a custom `X509CertificateUtil` | [📄](src/main/kotlin/x509/ConfigureTrustStoreExample.kt) | [📄](src/main/java/waltid/x509/ConfigureTrustStoreExample.java) |
| **ISO mDL Onboarding** | Build an ISO/IEC 18013-5 IACA root and Document Signer certificate | [📄](src/main/kotlin/x509/IsoMdlOnboardingExample.kt) | [📄](src/main/java/waltid/x509/IsoMdlOnboardingExample.java) |

The Java ports are direct translations of the Kotlin sources and use the crypto2 library (via `Crypto2Keys`, a
shared internal helper) to generate the EC P-256 keys used to sign the certificates. See the
[waltid-x509 README](https://github.com/walt-id/waltid-identity/tree/main/waltid-libraries/crypto/waltid-x509#readme)
for the underlying library docs.

## 🔑 Crypto2 (new library)

`crypto2` is walt.id's next-generation crypto library, built around a `CryptoRuntime` that generates and manages
keys through pluggable providers (software or hardware-backed providers) instead of a fixed key
type per algorithm.

| Feature | Description | Kotlin | Java |
|---------|-------------|--------|------|
| **Key Generation** | Generate Ed25519, RSA, and secp256r1 (P-256) software keys | [📁](src/main/kotlin/crypto2/key/create) | [📄](src/main/java/waltid/crypto2/key/create) |
| **Signatures** | Sign and verify with Ed25519 and secp256r1 keys | [📁](src/main/kotlin/crypto2/signatures) | [📄](src/main/java/waltid/crypto2/signatures/Secp256r1Sign.java) |
| **PEM Export** | Export a key's public/private material to SPKI/PKCS8 PEM | [📄](src/main/kotlin/crypto2/key/encode/PemExport.kt) | — |
| **Serialization & Restoration** | Serialize a key to JSON and restore it into an operational key via `CryptoRuntime` | [📄](src/main/kotlin/crypto2/key/SerializationExample.kt) | — |
| **Provider Selection** | Automatic, explicit, and fallback-list provider selection, including the failure case | [📄](src/main/kotlin/crypto2/ProviderSelectionDemo.kt) | — |
| **Simplified Java Usage** | Idiomatic blocking-call Java example (`.get()` on the `CompletionStage` API) instead of coroutine interop | — | [📄](src/main/java/waltid/crypto2/simple/SimpleSigningExample.java) |

None of the crypto2 examples are wired into `RunAllKt` / `waltid.RunAll` yet - run them individually (see
[Running Examples](#running-examples)).

## 🏃‍♂️ Running Examples

### Using Gradle

**Run all examples:**
```bash
# Kotlin version
./gradlew run -PmainClass=RunAllKt

# Java version  
./gradlew run -PmainClass=waltid.RunAll
```

**Run specific examples:**
```bash
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

# Only the four live Enterprise API URL claims
./gradlew validateTrustListUrls

# X.509 certificates (Kotlin)
./gradlew run -PmainClass=x509.SignCertificateExampleKt
./gradlew run -PmainClass=x509.ConfigureTrustStoreExampleKt
./gradlew run -PmainClass=x509.IsoMdlOnboardingExampleKt

# X.509 certificates (Java)
./gradlew run -PmainClass=waltid.x509.SignCertificateExample
./gradlew run -PmainClass=waltid.x509.ConfigureTrustStoreExample
./gradlew run -PmainClass=waltid.x509.IsoMdlOnboardingExample

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

### Using Maven

If you prefer Maven, add the walt.id repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>walt.id</id>
        <url>https://maven.waltid.dev/releases</url>
    </repository>
</repositories>
```

## ✨ Key Features

- **🔐 Multi-algorithm Support**: Ed25519, RSA, Secp256k1, Secp256r1
- **📦 Multiple Key Formats**: JWK, PEM, Raw (Base58)
- **🆔 DID Methods**: did:key, did:web, did:jwk, did:cheqd
- **🎫 VC Standards**: JWT VCs, SD-JWT (Selective Disclosure)
- **🎭 VP Support**: Verifiable Presentations
- **✅ Trust Lists**: TSL XML, LoTE JSON/XML, XMLDSig, and compact-JWS validation
- **🔏 X.509 Certificates**: Signing, trust store configuration, ISO/IEC 18013-5 (mDL) IACA/Document Signer onboarding
- **🔑 Crypto2**: Provider-based key generation, signatures, PEM export, serialization/restoration, and provider selection (walt.id's next-generation crypto library)
- **🌐 Cross-platform**: Java and Kotlin implementations
- **📚 Comprehensive**: From basic key generation to complex credential workflows

## 📖 Documentation

- **walt.id SDK Documentation**: [https://docs.walt.id/](https://docs.walt.id/)
- **Identity Repository**: [https://github.com/walt-id/waltid-identity](https://github.com/walt-id/waltid-identity)
- **Maven Repository**: [https://maven.waltid.dev/#/releases/id/walt](https://maven.waltid.dev/#/releases/id/walt)
- **API Reference**: Available in the test directories of the [identity repository](https://github.com/walt-id/waltid-identity)

## 🤝 Community

Connect with the walt.id community:

* **💬 Discord**: [Join our Discord server](https://discord.gg/AW8AgqJthZ)
* **📧 Newsletter**: [Subscribe to updates](https://walt.id/newsletter)
* **📺 YouTube**: [Watch tutorials](https://www.youtube.com/channel/UCXfOzrv3PIvmur_CmwwmdLA)
* **🐦 Twitter**: [Follow @walt_id](https://mobile.twitter.com/walt_id)
* **💭 GitHub Discussions**: [Get help and discuss features](https://github.com/walt-id/.github/discussions)

## 📄 License

This project is licensed under the **Apache License, Version 2.0**. See the [LICENSE](LICENSE) file for details.

---

<div align="center">
  <p>Made with ❤️ by the <a href="https://walt.id">walt.id</a> team</p>
  <p>
    <a href="https://walt.id">Website</a> •
    <a href="https://docs.walt.id">Documentation</a> •
    <a href="https://github.com/walt-id">GitHub</a> •
    <a href="https://discord.gg/AW8AgqJthZ">Discord</a>
  </p>
</div>
