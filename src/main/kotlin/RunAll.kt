
import crypto.create.create_ed25519
import crypto.create.create_rsa
import crypto.create.create_secp256k1
import crypto.create.create_secp256r1
import crypto.export_key.export_jwk
import crypto.export_key.export_jwk_object
import crypto.import_key.*
import vc.*


suspend fun main() {

    println("                _ _     _     _                                  _           \n" +
            "               | | |   (_)   | |                                | |          \n" +
            " __      ____ _| | |_   _  __| |   _____  ____ _ _ __ ___  _ __ | | ___  ___ \n" +
            " \\ \\ /\\ / / _` | | __| | |/ _` |  / _ \\ \\/ / _` | '_ ` _ \\| '_ \\| |/ _ \\/ __|\n" +
            "  \\ V  V / (_| | | |_ _| | (_| | |  __/>  < (_| | | | | | | |_) | |  __/\\__ \\\n" +
            "   \\_/\\_/ \\__,_|_|\\__(_)_|\\__,_|  \\___/_/\\_\\__,_|_| |_| |_| .__/|_|\\___||___/\n" +
            "                                                          | |                \n" +
            "                                                          |_|    ")

    // Crypto
    println("create_ed25519() ----------------------------------------------------------------------------------------")
    create_ed25519()
    println("create_rsa() --------------------------------------------------------------------------------------------")
    create_rsa()
    println("create_secp256k1() --------------------------------------------------------------------------------------")
    create_secp256k1()
    println("create_secp256r1() --------------------------------------------------------------------------------------")
    create_secp256r1()

    println("create_ed25519() ----------------------------------------------------------------------------------------")
    export_jwk()
    println("create_ed25519() ----------------------------------------------------------------------------------------")
    export_jwk_object()
    println("create_ed25519() ----------------------------------------------------------------------------------------")
    create_ed25519()

    println("import_ed25519_jwk() ----------------------------------------------------------------------------------------")
    import_ed25519_jwk()
    println("import_rsa_jwk() ----------------------------------------------------------------------------------------")
    import_rsa_jwk()
    println("import_rsa_raw() ----------------------------------------------------------------------------------------")
    // FIXME import_rsa_raw()
    println("import_secp256k1_jwk() ----------------------------------------------------------------------------------------")
    import_secp256k1_jwk()
    println("import_secp256r1_jwk() ----------------------------------------------------------------------------------------")
    import_secp256r1_jwk()

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