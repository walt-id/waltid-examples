package did.create.web

import id.walt.did.dids.DidService

suspend fun main() {
    createDidWeb()
}

suspend fun createDidWeb() {
    DidService.minimalInit()
    simpleRegister()
    simpleRegisterByKey()
    createDidWebDidDocOptionsFromPublicKeySetExamples()
    createDidWebDidDocOptionsFromPublicKeySetVerificationConfigurationExamples()
    createDidWebDidDocOptionsPrimaryConstructorExamples()
}



