package vc

import id.walt.credentials.verification.Verifier
import id.walt.credentials.verification.models.PolicyRequest
import id.walt.credentials.verification.policies.ExpirationDatePolicy
import id.walt.credentials.verification.policies.JwtSignaturePolicy
import id.walt.credentials.verification.policies.vp.HolderBindingPolicy
import id.walt.did.dids.DidService


suspend fun main() {
    verify_vp()
}

suspend fun verify_vp() {
    // Initialise DID Service to be able to resolve DIDs
    DidService.minimalInit()

    val verifier = Verifier
    val presentationSDJWT =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6ejZNa3E5YUV2RXIyWjNYTDladXdMVlVhUUpwVm1kd0ZXd1puSnhIMmFxeHU3WHVDI3o2TWtxOWFFdkVyMlozWEw5WnV3TFZVYVFKcFZtZHdGV3dabkp4SDJhcXh1N1h1QyJ9.eyJzdWIiOiJkaWQ6a2V5Ono2TWtxOWFFdkVyMlozWEw5WnV3TFZVYVFKcFZtZHdGV3dabkp4SDJhcXh1N1h1QyIsIm5iZiI6MTcxMTU5MTkzMywiaWF0IjoxNzExNTkxOTkzLCJqdGkiOiJ1cm46dXVpZDpkZGQxNDQ0Mi04Mzg1LTQ1ODMtODllNS01ZGE4ZWE4NjRmYjkiLCJpc3MiOiJkaWQ6a2V5Ono2TWtxOWFFdkVyMlozWEw5WnV3TFZVYVFKcFZtZHdGV3dabkp4SDJhcXh1N1h1QyIsIm5vbmNlIjoiMjAzOTQwMjkzNDAiLCJhdWQiOiIiLCJ2cCI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVQcmVzZW50YXRpb24iXSwiaWQiOiJ1cm46dXVpZDpkZGQxNDQ0Mi04Mzg1LTQ1ODMtODllNS01ZGE4ZWE4NjRmYjkiLCJob2xkZXIiOiJkaWQ6a2V5Ono2TWtxOWFFdkVyMlozWEw5WnV3TFZVYVFKcFZtZHdGV3dabkp4SDJhcXh1N1h1QyIsInZlcmlmaWFibGVDcmVkZW50aWFsIjpbImV5SmpkSGtpT2lKamNtVmtaVzUwYVdGc0xXTnNZV2x0Y3kxelpYUXJhbk52YmlJc0luUjVjQ0k2SW5aakszTmtMV3AzZENJc0ltRnNaeUk2SWtWa1JGTkJJaXdpYTJsa0lqb2laR2xrT210bGVUcDZOazFyYVhwTmRUbDVXRkJ1Y25KVU9FUmxabVl5Y25oak0zZGpSbnBrYm5keFIyRkJPRWhsWkZWQlRqbDZSM0lpZlEuZXlKQVkyOXVkR1Y0ZENJNld5Sm9kSFJ3Y3pvdkwzZDNkeTUzTXk1dmNtY3ZNakF4T0M5amNtVmtaVzUwYVdGc2N5OTJNU0lzSW1oMGRIQnpPaTh2Y0hWeWJDNXBiWE5uYkc5aVlXd3ViM0puTDNOd1pXTXZiMkl2ZGpOd01DOWpiMjUwWlhoMExUTXVNQzR5TG1wemIyNGlYU3dpZEhsd1pTSTZXeUpXWlhKcFptbGhZbXhsUTNKbFpHVnVkR2xoYkNJc0lrOXdaVzVDWVdSblpVTnlaV1JsYm5ScFlXd2lYU3dpWTNKbFpHVnVkR2xoYkZOMVltcGxZM1FpT25zaWRIbHdaU0k2V3lKQlkyaHBaWFpsYldWdWRGTjFZbXBsWTNRaVhTd2lZV05vYVdWMlpXMWxiblFpT25zaWFXUWlPaUoxY200NmRYVnBaRHBoWXpJMU5HSmtOUzA0Wm1Ga0xUUmlZakV0T1dReU9TMWxabVE1TXpnMU16WTVNallpTENKMGVYQmxJanBiSWtGamFHbGxkbVZ0Wlc1MElsMHNJbTVoYldVaU9pSktSa1lnZUNCMll5MWxaSFVnVUd4MVowWmxjM1FnTXlCSmJuUmxjbTl3WlhKaFltbHNhWFI1SWl3aVpHVnpZM0pwY0hScGIyNGlPaUpVYUdseklIZGhiR3hsZENCemRYQndiM0owY3lCMGFHVWdkWE5sSUc5bUlGY3pReUJXWlhKcFptbGhZbXhsSUVOeVpXUmxiblJwWVd4eklHRnVaQ0JvWVhNZ1pHVnRiMjV6ZEhKaGRHVmtJR2x1ZEdWeWIzQmxjbUZpYVd4cGRIa2daSFZ5YVc1bklIUm9aU0J3Y21WelpXNTBZWFJwYjI0Z2NtVnhkV1Z6ZENCM2IzSnJabXh2ZHlCa2RYSnBibWNnU2taR0lIZ2dWa010UlVSVklGQnNkV2RHWlhOMElETXVJaXdpWTNKcGRHVnlhV0VpT25zaWRIbHdaU0k2SWtOeWFYUmxjbWxoSWl3aWJtRnljbUYwYVhabElqb2lWMkZzYkdWMElITnZiSFYwYVc5dWN5QndjbTkyYVdSbGNuTWdaV0Z5Ym1Wa0lIUm9hWE1nWW1Ga1oyVWdZbmtnWkdWdGIyNXpkSEpoZEdsdVp5QnBiblJsY205d1pYSmhZbWxzYVhSNUlHUjFjbWx1WnlCMGFHVWdjSEpsYzJWdWRHRjBhVzl1SUhKbGNYVmxjM1FnZDI5eWEyWnNiM2N1SUZSb2FYTWdhVzVqYkhWa1pYTWdjM1ZqWTJWemMyWjFiR3g1SUhKbFkyVnBkbWx1WnlCaElIQnlaWE5sYm5SaGRHbHZiaUJ5WlhGMVpYTjBMQ0JoYkd4dmQybHVaeUIwYUdVZ2FHOXNaR1Z5SUhSdklITmxiR1ZqZENCaGRDQnNaV0Z6ZENCMGQyOGdkSGx3WlhNZ2IyWWdkbVZ5YVdacFlXSnNaU0JqY21Wa1pXNTBhV0ZzY3lCMGJ5QmpjbVZoZEdVZ1lTQjJaWEpwWm1saFlteGxJSEJ5WlhObGJuUmhkR2x2Yml3Z2NtVjBkWEp1YVc1bklIUm9aU0J3Y21WelpXNTBZWFJwYjI0Z2RHOGdkR2hsSUhKbGNYVmxjM1J2Y2l3Z1lXNWtJSEJoYzNOcGJtY2dkbVZ5YVdacFkyRjBhVzl1SUc5bUlIUm9aU0J3Y21WelpXNTBZWFJwYjI0Z1lXNWtJSFJvWlNCcGJtTnNkV1JsWkNCamNtVmtaVzUwYVdGc2N5NGlmU3dpYVcxaFoyVWlPbnNpYVdRaU9pSm9kSFJ3Y3pvdkwzY3pZeTFqWTJjdVoybDBhSFZpTG1sdkwzWmpMV1ZrTDNCc2RXZG1aWE4wTFRNdE1qQXlNeTlwYldGblpYTXZTa1pHTFZaRExVVkVWUzFRVEZWSFJrVlRWRE10WW1Ga1oyVXRhVzFoWjJVdWNHNW5JaXdpZEhsd1pTSTZJa2x0WVdkbEluMTlMQ0pwWkNJNkltUnBaRHByWlhrNmVqWk5hM05HYm1GNE5uaERRbGRrYnpab2FHWmFOM0JGYzJKNVlYRTVUVTFPWkcxQlFsTXhUbkpqUTBSYVNuSXpJbjBzSW1sa0lqb2lkWEp1T25WMWFXUTZOREUzTjJVd05EZ3RPV0UwWVMwME56UmxMVGxrWXpZdFlXVmtOR1UyTVdFMk5ETTVJaXdpYVhOemRXVnlJam9pWkdsa09tdGxlVHA2TmsxcmMwWnVZWGcyZUVOQ1YyUnZObWhvWmxvM2NFVnpZbmxoY1RsTlRVNWtiVUZDVXpGT2NtTkRSRnBLY2pNaUxDSnBjM04xWVc1alpVUmhkR1VpT2lJeU1ESTBMVEF6TFRJNFZEQXlPakV6T2pFekxqTTVNVFE0TmpFd01Gb2lMQ0psZUhCcGNtRjBhVzl1UkdGMFpTSTZJakl3TWpRdE1ETXRNekJVTURJNk1UTTZNVE11TXprek5EZzROREF3V2lJc0lsOXpaQ0k2V3lKWFIyOWhYMHhLVEZFM2FVSlVNVXhKTUU5TlJHaGZRbXRETVRGNU1XNTJVRTkzY25GSmVreEdORkpOSWl3aWNHdHhaMEZYTTBZME1WOURZbFF5TXpaR01qWXplR2xKTURWQmVUSklPUzFYTVRkbE1sTmxVVlYzU1NKZGZRLllBVzNVSWRjM2JIbFRQV0M1aXJFd1gtdkMzVWxPYTY1TFNDUVBLMnVFU0NYdVFSRk1URm8wbnBKd3U2aWFMOVdKQnA3QldpMzc0RWVXSHpHaWRhYURBfld5Sk1iR05oYmpseVh6UmZTbmx6UTNkQlJWaDBZMDVCUFQwaUxDSnVZVzFsSWl3aVNrWkdJSGdnZG1NdFpXUjFJRkJzZFdkR1pYTjBJRE1nU1c1MFpYSnZjR1Z5WVdKcGJHbDBlU0pkIl19fQ.aDja7bqkmaHymNofbiw8dOJaKMHplopFkvablZ1hwm9BdIbJR9vtrApwMjuurIbO7HB2RSC3xt1-pchVVSomCQ"
    // JWT of the presentation to verify
    val presentationJwt =
        "eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6ejZNa2hmcGNjV2JaS1J1cUZ2RkxwWmh5ZUI0WVl0VjhiSnkzN2pMNmlpRTdOZjdiI3o2TWtoZnBjY1diWktSdXFGdkZMcFpoeWVCNFlZdFY4Ykp5MzdqTDZpaUU3TmY3YiJ9.eyJzdWIiOiJkaWQ6a2V5Ono2TWtoZnBjY1diWktSdXFGdkZMcFpoeWVCNFlZdFY4Ykp5MzdqTDZpaUU3TmY3YiIsIm5iZiI6MTcwNTUwMzE2NCwiaWF0IjoxNzA1NTAzMjI0LCJqdGkiOiJ1cm46dXVpZDo5YWI2YTMzNi03MTFlLTQwMWEtOWQ3NS1mMGNkM2EzMWFkMTAiLCJpc3MiOiJkaWQ6a2V5Ono2TWtoZnBjY1diWktSdXFGdkZMcFpoeWVCNFlZdFY4Ykp5MzdqTDZpaUU3TmY3YiIsIm5vbmNlIjoiMjAzOTQwMjkzNDAiLCJ2cCI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVQcmVzZW50YXRpb24iXSwiaWQiOiJ1cm46dXVpZDo5YWI2YTMzNi03MTFlLTQwMWEtOWQ3NS1mMGNkM2EzMWFkMTAiLCJob2xkZXIiOiJkaWQ6a2V5Ono2TWtoZnBjY1diWktSdXFGdkZMcFpoeWVCNFlZdFY4Ykp5MzdqTDZpaUU3TmY3YiIsInZlcmlmaWFibGVDcmVkZW50aWFsIjpbImV5SmhiR2NpT2lKRlpFUlRRU0lzSW10cFpDSTZJbVJwWkRwclpYazZlalpOYTNKaFVUZG1URk5HWTB4dk1tSnpORmhNTmtjelMwZE5VRmhaZDJWUWIxbFVkemhFWVVOQ1VVeFNWRlJ3SW4wLmV5SnBjM01pT2lKa2FXUTZhMlY1T25vMlRXdHlZVkUzWmt4VFJtTk1iekppY3pSWVREWkhNMHRIVFZCWVdYZGxVRzlaVkhjNFJHRkRRbEZNVWxSVWNDSXNJbk4xWWlJNkltUnBaRHByWlhrNmVqWk5hMmhtY0dOalYySmFTMUoxY1VaMlJreHdXbWg1WlVJMFdWbDBWamhpU25rek4ycE1ObWxwUlRkT1pqZGlJaXdpZG1NaU9uc2lRR052Ym5SbGVIUWlPbHNpYUhSMGNITTZMeTkzZDNjdWR6TXViM0puTHpJd01UZ3ZZM0psWkdWdWRHbGhiSE12ZGpFaUxDSm9kSFJ3Y3pvdkwzQjFjbXd1YVcxeloyeHZZbUZzTG05eVp5OXpjR1ZqTDI5aUwzWXpjREF2WTI5dWRHVjRkQzB6TGpBdU1pNXFjMjl1SWwwc0luUjVjR1VpT2xzaVZtVnlhV1pwWVdKc1pVTnlaV1JsYm5ScFlXd2lMQ0pQY0dWdVFtRmtaMlZEY21Wa1pXNTBhV0ZzSWwwc0ltTnlaV1JsYm5ScFlXeFRkV0pxWldOMElqcDdJblI1Y0dVaU9sc2lRV05vYVdWMlpXMWxiblJUZFdKcVpXTjBJbDBzSW1GamFHbGxkbVZ0Wlc1MElqcDdJbWxrSWpvaWRYSnVPblYxYVdRNllXTXlOVFJpWkRVdE9HWmhaQzAwWW1JeExUbGtNamt0Wldaa09UTTROVE0yT1RJMklpd2lkSGx3WlNJNld5SkJZMmhwWlhabGJXVnVkQ0pkTENKdVlXMWxJam9pU2taR0lIZ2dkbU10WldSMUlGQnNkV2RHWlhOMElETWdTVzUwWlhKdmNHVnlZV0pwYkdsMGVTSXNJbVJsYzJOeWFYQjBhVzl1SWpvaVZHaHBjeUIzWVd4c1pYUWdjM1Z3Y0c5eWRITWdkR2hsSUhWelpTQnZaaUJYTTBNZ1ZtVnlhV1pwWVdKc1pTQkRjbVZrWlc1MGFXRnNjeUJoYm1RZ2FHRnpJR1JsYlc5dWMzUnlZWFJsWkNCcGJuUmxjbTl3WlhKaFltbHNhWFI1SUdSMWNtbHVaeUIwYUdVZ2NISmxjMlZ1ZEdGMGFXOXVJSEpsY1hWbGMzUWdkMjl5YTJac2IzY2daSFZ5YVc1bklFcEdSaUI0SUZaRExVVkVWU0JRYkhWblJtVnpkQ0F6TGlJc0ltTnlhWFJsY21saElqcDdJblI1Y0dVaU9pSkRjbWwwWlhKcFlTSXNJbTVoY25KaGRHbDJaU0k2SWxkaGJHeGxkQ0J6YjJ4MWRHbHZibk1nY0hKdmRtbGtaWEp6SUdWaGNtNWxaQ0IwYUdseklHSmhaR2RsSUdKNUlHUmxiVzl1YzNSeVlYUnBibWNnYVc1MFpYSnZjR1Z5WVdKcGJHbDBlU0JrZFhKcGJtY2dkR2hsSUhCeVpYTmxiblJoZEdsdmJpQnlaWEYxWlhOMElIZHZjbXRtYkc5M0xpQlVhR2x6SUdsdVkyeDFaR1Z6SUhOMVkyTmxjM05tZFd4c2VTQnlaV05sYVhacGJtY2dZU0J3Y21WelpXNTBZWFJwYjI0Z2NtVnhkV1Z6ZEN3Z1lXeHNiM2RwYm1jZ2RHaGxJR2h2YkdSbGNpQjBieUJ6Wld4bFkzUWdZWFFnYkdWaGMzUWdkSGR2SUhSNWNHVnpJRzltSUhabGNtbG1hV0ZpYkdVZ1kzSmxaR1Z1ZEdsaGJITWdkRzhnWTNKbFlYUmxJR0VnZG1WeWFXWnBZV0pzWlNCd2NtVnpaVzUwWVhScGIyNHNJSEpsZEhWeWJtbHVaeUIwYUdVZ2NISmxjMlZ1ZEdGMGFXOXVJSFJ2SUhSb1pTQnlaWEYxWlhOMGIzSXNJR0Z1WkNCd1lYTnphVzVuSUhabGNtbG1hV05oZEdsdmJpQnZaaUIwYUdVZ2NISmxjMlZ1ZEdGMGFXOXVJR0Z1WkNCMGFHVWdhVzVqYkhWa1pXUWdZM0psWkdWdWRHbGhiSE11SW4wc0ltbHRZV2RsSWpwN0ltbGtJam9pYUhSMGNITTZMeTkzTTJNdFkyTm5MbWRwZEdoMVlpNXBieTkyWXkxbFpDOXdiSFZuWm1WemRDMHpMVEl3TWpNdmFXMWhaMlZ6TDBwR1JpMVdReTFGUkZVdFVFeFZSMFpGVTFRekxXSmhaR2RsTFdsdFlXZGxMbkJ1WnlJc0luUjVjR1VpT2lKSmJXRm5aU0o5ZlN3aWFXUWlPaUprYVdRNmEyVjVPbm8yVFd0elJtNWhlRFo0UTBKWFpHODJhR2htV2pkd1JYTmllV0Z4T1UxTlRtUnRRVUpUTVU1eVkwTkVXa3B5TXlKOUxDSnBaQ0k2SW5WeWJqcDFkV2xrT2pReE56ZGxNRFE0TFRsaE5HRXRORGMwWlMwNVpHTTJMV0ZsWkRSbE5qRmhOalF6T1NJc0ltbHpjM1ZsY2lJNkltUnBaRHByWlhrNmVqWk5hM05HYm1GNE5uaERRbGRrYnpab2FHWmFOM0JGYzJKNVlYRTVUVTFPWkcxQlFsTXhUbkpqUTBSYVNuSXpJaXdpYVhOemRXRnVZMlZFWVhSbElqb2lNakF5TkMwd01TMHhOMVF4TkRvMU16bzBOQzQzTmpRNE1qTmFJaXdpWlhod2FYSmhkR2x2YmtSaGRHVWlPaUl5TURJNUxUQTNMVEE1VkRFME9qVXpPalEwTGpjMk5UTTJOVm9pTENKdVlXMWxJam9pU2taR0lIZ2dkbU10WldSMUlGQnNkV2RHWlhOMElETWdTVzUwWlhKdmNHVnlZV0pwYkdsMGVTSjlmUS5BS1RsUW5INUZUb3EyZ0hHdGxoc1NscHp6TGZfdU12ZmhxbV80OWhsMTBpN0N3MnBOWkhJYjMyTi1QV2lWOFVTcmVZZmJ2MklkTC1NcGFLWF9IekNDdyJdfX0.U0MEUTd6OfCXXO3PPMnywpytEQoeCuPqkqJ2fhBQlPr0SPVAM7R9_SiFr54urKknD73YSWlZ_73gfxE0zNnxAA"


    // Policies that should be applied to Verifiable Presentation.
    val vpPolicies = listOf(
        PolicyRequest(HolderBindingPolicy())
        // other policies ...
    )

    // Global policies that are applied to ALL verifiable credentials in the Verifiable Presentation.
    val globalVcPolicies = listOf(
        PolicyRequest(JwtSignaturePolicy())
        // other policies ...
    )

    // Specify policies for specific credential types.
    val specificCredentialPolicies = mapOf(
        "OpenBadgeCredential" to listOf(PolicyRequest(ExpirationDatePolicy()))
        // other policies ...
    )

    // Context is optional and it's an empty map by default.
    val presentationContext = mapOf<String, Any>()

    // Verify the presentation
    val resultJWT = verifier.verifyPresentation(
        presentationJwt,
        vpPolicies,
        globalVcPolicies,
        specificCredentialPolicies,
        presentationContext
    )

    println(resultJWT.overallSuccess())

}
