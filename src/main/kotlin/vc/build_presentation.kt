package vc

import id.walt.credentials.PresentationBuilder

fun main (){
    val vp = PresentationBuilder().apply {
        did = "did:key:123456789"    // sets sub, iss, vp.holder

        // nbf, iat, jti set automatically to sane default values

        nonce = "ABC123DEF456GHI789JKL"

        // vp.context, vp.type, vp.id set automatically to sane default values

        addCredential()
    }.buildPresentationJson()

    println(vp)
}