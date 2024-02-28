import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.LocalKey
import io.kotest.core.spec.style.StringSpec
import id.walt.did.dids.DidService
import id.walt.did.dids.registrar.dids.*
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class Dids : StringSpec({

    "Create the key and register the DID web".config() {
        DidService.init()
        val options = DidWebCreateOptions(
            domain = "walt.id",
            keyType = KeyType.Ed25519
        )
        val didResult = DidService.register(options = options)
        didResult.did shouldContain "walt.id"
    }

    "Register the DID with a given key".config() {
        DidService.init()

        val key = LocalKey.generate(KeyType.Ed25519)
        val options = DidKeyCreateOptions(

            useJwkJcsPub = true
        )
        val didResult = DidService.registerByKey(
            method = "crypto",
            key = key,
            options = options
        )
        val didDocumentResult = DidService.resolve(didResult.did)
        val document = didDocumentResult.getOrNull()
        didResult.did shouldContain "crypto"
        document shouldNotBe null
    }

    "Register the DID jwk".config() {
        DidService.init()
        val options = DidJwkCreateOptions(
            keyType = KeyType.Ed25519
        )
        val didResult = DidService.register(options = options)
        val didDocumentResult = DidService.resolve(didResult.did)
        val document = didDocumentResult.getOrNull()

        didResult.did shouldContain "jwk"
        document shouldNotBe null
    }

    "Resolve the DID url to its public Key".config() {
        DidService.init()
        val did = "did:key:zmYg9bgKmRiCqTTd9MA1ufVE9tfzUptwQp4GMRxptXquJWw4Uj5cqKBi2vyiwwxC3v7ixvJ8SB9DvDdrK7UemySWDPhvHhUcZ7pgtZtFchLtzK4YC"
        val keyResult = DidService.resolveToKey(did = did)
        val key = keyResult.getOrNull()
        key shouldNotBe null
    }

})