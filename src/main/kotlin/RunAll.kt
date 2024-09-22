import crypto.key.create.createEd25519
import crypto.key.create.createRSA
import crypto.key.create.createSecp256k1
import crypto.key.create.createSecp256r1
import crypto.key.decode.jwk.importEd25519Jwk
import crypto.key.decode.jwk.importRSAJwk
import crypto.key.decode.jwk.importSecp256k1Jwk
import crypto.key.decode.jwk.importSecp256r1Jwk
import crypto.key.decode.pem.importRSAPEM
import crypto.key.decode.pem.importSecp256k1PEM
import crypto.key.decode.pem.importSecp256r1PEM
import crypto.key.decode.raw.importEd25519RawPublicKey
import crypto.key.decode.raw.importRSARawPublicKey
import crypto.key.decode.raw.importSecp256k1RawPublicKey
import crypto.key.decode.raw.importSecp256r1RawPublicKey
import crypto.key.encode.jwk.*
import crypto.key.encode.pem.exportRSAPEM
import crypto.key.encode.pem.exportSecp256k1PEM
import crypto.key.encode.pem.exportSecp256r1PEM
import crypto.key.encode.raw.exportEd25519RawPublicKey
import crypto.key.encode.raw.exportRSARawPublicKey
import crypto.key.encode.raw.exportSecp256k1RawPublicKey
import crypto.key.encode.raw.exportSecp256r1RawPublicKey
import crypto.signatures.jws.signVerifyJwsEd25519Key
import crypto.signatures.jws.signVerifyJwsRSAKey
import crypto.signatures.jws.signVerifyJwsSecp256k1Key
import crypto.signatures.jws.signVerifyJwsSecp256r1Key
import crypto.signatures.raw.signVerifyRawEd25519Key
import crypto.signatures.raw.signVerifyRawRSAKey
import crypto.signatures.raw.signVerifyRawSecp256k1Key
import crypto.signatures.raw.signVerifyRawSecp256r1Key
import did.create.createDidCheqd
import did.create.createDidJwk
import did.create.createDidKey
import did.create.createDidWeb
import did.resolve.resolveDidJwk
import did.resolve.resolveDidKey
import did.resolve.resolveDidWeb
import vc.*
import vp.verify_vp


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

    // Crypto Start
    // Key Generation
    println("createEd25519() -----------------------------------------------------------------------------------------")
    createEd25519()
    println("createRSA() ---------------------------------------------------------------------------------------------")
    createRSA()
    println("createSecp256k1() ---------------------------------------------------------------------------------------")
    createSecp256k1()
    println("createSecp256r1() ---------------------------------------------------------------------------------------")
    createSecp256r1()

    // Export Start
    // Key JWK
    println("exportEd25519Jwk() --------------------------------------------------------------------------------------")
    exportEd25519Jwk()
    println("exportRSAJwk() ------------------------------------------------------------------------------------------")
    exportRSAJwk()
    println("exportSecp256k1Jwk() ------------------------------------------------------------------------------------")
    exportSecp256k1Jwk()
    println("exportSecp256r1Jwk() ------------------------------------------------------------------------------------")
    exportSecp256r1Jwk()
    // Key PEM
    println("exportRSAPEM() ------------------------------------------------------------------------------------------")
    exportRSAPEM()
    println("exportSecp256k1PEM() ------------------------------------------------------------------------------------")
    exportSecp256k1PEM()
    println("exportSecp256r1PEM() ------------------------------------------------------------------------------------")
    exportSecp256r1PEM()
    // Key RAW Base58 String
    println("exportEd25519RawPublicKey() -----------------------------------------------------------------------------")
    exportEd25519RawPublicKey()
    println("exportRSARawPublicKey() ---------------------------------------------------------------------------------")
    exportRSARawPublicKey()
    println("exportSecp256k1RawPublicKey() ---------------------------------------------------------------------------")
    exportSecp256k1RawPublicKey()
    println("exportSecp256r1RawPublicKey() ---------------------------------------------------------------------------")
    exportSecp256r1RawPublicKey()
    // Export End
    // Import Start
    // Key JWK
    println("importEd25519Jwk() --------------------------------------------------------------------------------------")
    importEd25519Jwk()
    println("importRSAJwk() ------------------------------------------------------------------------------------------")
    importRSAJwk()
    println("importSecp256k1Jwk() ------------------------------------------------------------------------------------")
    importSecp256k1Jwk()
    println("importSecp256r1Jwk() ------------------------------------------------------------------------------------")
    importSecp256r1Jwk()
    // Key PEM
    println("importRSAPEM() ------------------------------------------------------------------------------------------")
    importRSAPEM()
    println("importSecp256k1PEM() ------------------------------------------------------------------------------------")
    importSecp256k1PEM()
    println("importSecp256r1PEM() ------------------------------------------------------------------------------------")
    importSecp256r1PEM()
    //Key RAW Base58 String
    println("importEd25519RawPublicKey() -----------------------------------------------------------------------------")
    importEd25519RawPublicKey()
    println("importRSARawPublicKey() ---------------------------------------------------------------------------------")
    importRSARawPublicKey()
    println("importSecp256k1RawPublicKey() ---------------------------------------------------------------------------")
    importSecp256k1RawPublicKey()
    println("importSecp256r1RawPublicKey() ---------------------------------------------------------------------------")
    importSecp256r1RawPublicKey()
    // Import End
    // Signatures Start
    // JWS
    println("signVerifyJwsEd25519Key() -------------------------------------------------------------------------------")
    signVerifyJwsEd25519Key()
    println("signVerifyJwsRSAKey() -----------------------------------------------------------------------------------")
    signVerifyJwsRSAKey()
    println("signVerifyJwsSecp256k1Key() -----------------------------------------------------------------------------")
    signVerifyJwsSecp256k1Key()
    println("signVerifyJwsSecp256r1Key() -----------------------------------------------------------------------------")
    signVerifyJwsSecp256r1Key()
    // RAW
    println("signVerifyRawEd25519Key() -------------------------------------------------------------------------------")
    signVerifyRawEd25519Key()
    println("signVerifyRawRSAKey() -----------------------------------------------------------------------------------")
    signVerifyRawRSAKey()
    println("signVerifyRawSecp256k1Key() -----------------------------------------------------------------------------")
    signVerifyRawSecp256k1Key()
    println("signVerifyRawSecp256r1Key() -----------------------------------------------------------------------------")
    signVerifyRawSecp256r1Key()
    //Signatures End
    // Crypto End

    // DIDs
    println("createDidCheqd() ----------------------------------------------------------------------------------------")
    createDidCheqd()
    println("createDidJwk() ------------------------------------------------------------------------------------------")
    createDidJwk()
    println("createDidKey() ------------------------------------------------------------------------------------------")
    createDidKey()
    println("createDidWeb() ------------------------------------------------------------------------------------------")
    createDidWeb()

    println("resolveDidJwk() -----------------------------------------------------------------------------------------")
    resolveDidJwk()
    println("resolveDidKey() -----------------------------------------------------------------------------------------")
    resolveDidKey()
    println("resolveDidWeb() -----------------------------------------------------------------------------------------")
    resolveDidWeb()

    // VCs
    println("build_vc() ----------------------------------------------------------------------------------------------")
    build()
    println("create_sign_presentation() ------------------------------------------------------------------------------")
    create_sign_presentation()
    println("sign_vc() -----------------------------------------------------------------------------------------------")
    sign()
    println("verify_vc() ---------------------------------------------------------------------------------------------")
    verify_vc()
    println("verify_vp() ---------------------------------------------------------------------------------------------")
    verify_vp()
}