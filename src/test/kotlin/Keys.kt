

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe
import id.walt.crypto.keys.*


class Keys : StringSpec({
    "Create key".config() {
        val key  = LocalKey.generate(KeyType.Ed25519, LocalKeyMetadata())
        val jws = key.signJws("payloadString".encodeToByteArray())
        val raw = key.signRaw("payloadString".encodeToByteArray())

        val verificationResultJws = key.getPublicKey().verifyJws(jws)
        println(verificationResultJws.isSuccess)

        key.getKeyId() shouldNotBe null
        jws shouldNotBe null
        raw shouldNotBe null
    }




})