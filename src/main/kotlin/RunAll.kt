import crypto.key.create.createEd25519
import crypto.key.create.createRSA
import crypto.key.create.createSecp256k1
import crypto.key.create.createSecp256r1
import crypto.key.export.exportJwkString
import crypto.key.export.exportJwkObject
import crypto.key.export.exportJwkPretty
import crypto.key.import.jwk.importEd25519Jwk
import crypto.key.import.jwk.importRSAJwk
import crypto.key.import.jwk.importSecp256k1Jwk
import crypto.key.import.jwk.importSecp256r1Jwk
import crypto.key.import.raw.importRSARawPublicKey
import vc.*


suspend fun main() {

    println(
        "                _ _     _     _                                  _           \n" +
                "               | | |   (_)   | |                                | |          \n" +
                " __      ____ _| | |_   _  __| |   _____  ____ _ _ __ ___  _ __ | | ___  ___ \n" +
                " \\ \\ /\\ / / _` | | __| | |/ _` |  / _ \\ \\/ / _` | '_ ` _ \\| '_ \\| |/ _ \\/ __|\n" +
                "  \\ V  V / (_| | | |_ _| | (_| | |  __/>  < (_| | | | | | | |_) | |  __/\\__ \\\n" +
                "   \\_/\\_/ \\__,_|_|\\__(_)_|\\__,_|  \\___/_/\\_\\__,_|_| |_| |_| .__/|_|\\___||___/\n" +
                "                                                          | |                \n" +
                "                                                          |_|    "
    )

    // Crypto
    println("createEd25519() -----------------------------------------------------------------------------------------")
    createEd25519()
    println("createRSA() ---------------------------------------------------------------------------------------------")
    createRSA()
    println("createSecp256k1() ---------------------------------------------------------------------------------------")
    createSecp256k1()
    println("createSecp256r1() ---------------------------------------------------------------------------------------")
    createSecp256r1()

    println("exportJwkString() ---------------------------------------------------------------------------------------")
    exportJwkString()
    println("exportJwkObject() ---------------------------------------------------------------------------------------")
    exportJwkObject()
    println("exportJwkPretty() ---------------------------------------------------------------------------------------")
    exportJwkPretty()

    println("importEd25519Jwk() --------------------------------------------------------------------------------------")
    importEd25519Jwk()
    println("importRSAJwk() ------------------------------------------------------------------------------------------")
    importRSAJwk()
    println("importSecp256k1Jwk() ------------------------------------------------------------------------------------")
    importSecp256k1Jwk()
    println("importSecp256r1Jwk() ------------------------------------------------------------------------------------")
    importSecp256r1Jwk()
    println("importRSARawPublicKey() ---------------------------------------------------------------------------------")
    importRSARawPublicKey()

    // DIDs
    println("cheqd_method() ------------------------------------------------------------------------------------------")
    did.create.cheqd_method()
    println("jwk_method() --------------------------------------------------------------------------------------------")
    did.create.jwk_method()
    println("key_method() --------------------------------------------------------------------------------------------")
    did.create.key_method()
    println("web_method() --------------------------------------------------------------------------------------------")
    did.create.web_method()

    println("jwk_method() --------------------------------------------------------------------------------------------")
    did.resolve.jwk_method()
    println("key_method() --------------------------------------------------------------------------------------------")
    did.resolve.key_method()
    println("web_method() --------------------------------------------------------------------------------------------")
    // FIXME did.resolve.web_method()

    // VCs
    println("build_vc() ----------------------------------------------------------------------------------------------")
    build_vc()
    println("create_sign_presentation() ------------------------------------------------------------------------------")
    create_sign_presentation()
    println("sign_vc() -----------------------------------------------------------------------------------------------")
    sign_vc()
    println("verify_vc() ---------------------------------------------------------------------------------------------")
    verify_vc()
    println("verify_vp() ---------------------------------------------------------------------------------------------")
    verify_vp()
}