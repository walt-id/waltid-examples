package crypto.key.decode.jwk

suspend fun main() {
    importRSAJwk()
}

suspend fun importRSAJwk() {
    val rsaJwkPrivateKeyString = "{\"p\":\"tQ_S_7iu_Wsxg7KlAR_WjIyRDQj2XTQZcb5oR5wcYkek5_E4ChW1HbmgJX7-dIYyCfPxq4mOFOIgr4Gm4FoL7z--YFRg7xV9P0i6uKorFSyw6QmZgSv-jua4xE5DOv37tNs3qTKkVpU3IlInK3c8V2PTg5a23NP96Ey84vM19xE\",\"kty\":\"RSA\",\"q\":\"-juh9XcjCG9ht0WVIBVVruzKirnjcOJYQS1utrvI2h1V1O3XA4RVPT8qaX-aDxp0lQ79Oz6mKps1U1-tztlEY3Tk60TBEYZq0Pv8rIjnKbmuFmBl-SDAN4-mflbWUxxFXNVztrdryfkfbAPmnDAf72fpfSaIdytzUt2PRnTOZK8\",\"d\":\"Fzoe8n2ZQk0JmF-JBtV6FoYLASdj5tCpkESSs3lnUp3lIWQ0kvS2GCz2wUz1l8bAhkV_dzpNdtzlrYLusTqcKuSj-kEUoVbcMlSGuLzcPeyv4ExMkrMfKW9fJ8T8oNNa_PoEY-EijLfZ-pvGjFtOUqx6VdsTJaeaMnKHl_fYRI0QqCkiM9sq5Vjp732h_p815RY_jBIEVMWIOvZeYhW01WdE3c2ycaY_Kl_wCVI76ccAiG1-TBDjcPEBzm-UfvxTASvHGPT9KHpYKKsKXEIiWGA7r85Vz6kCakiYG_WCBRSLwps9XxNF6OBfj-sA3Z3DJJACoKQxA6dxO4uXj5Kx4Q\",\"e\":\"AQAB\",\"kid\":\"SUlM_IQteXB6HUBzWYMzHs6a5hMDF45vdq_Kml7SLTY\",\"qi\":\"IssQG4_gIfFX89zgSuJFKzaHAJQZSL6IHOw18NX2TpF9AaHYCEYoUbrxQdV0itOCybctLjZA2_AzB98BjK6WpWWK4HWwZDSyk-Y8cgAWZ-73S5h6lJKmRzjhpky8vzwGZMg-MX4TF7gU_4MAsXcKhwJFicYKZ7Lk_sX4p8X7WQs\",\"dp\":\"RVKdspL_TL-x4X6QdnG-L2nST29TBDRiWY5RVrmKlIZhuPw-PH47LfpYOoL44ZxPpEfmC4tK_uvlH81AGz3eu-dn-HHV-zP7FDPAadudSyolQdQPFcnGWOg1bdOZIgkESiZ0lS9yhEP1LArQPHAWhk8OOJ-Hu9zepgZbe3kq_EE\",\"dq\":\"seuQnr3U9PxtGCirfaJtx_CpmrXNV8g4RC-PlvIyP5O4Iavyw5dmHCQ7fcOywKZo2ktMFsHW2Fh_NAbFyFuHWsgUtIuwU0uly25AHFVDN9HETDjCiL6WrWrAUPhhb7qcPwOuQO5t_6_Yfa7QW6GEM29I_ylUwpbsarEKXGhVEBU\",\"n\":\"sPuhP-tlV-bgSKhjAYhvji-6NZZADuYWO0s-Dy7IbmatnzARW3GFdVTTRcm8sh4wi7Z5oyEl3V2QdO5bJGFt51HoGK0S-N8JlvRkwkltWLHN2OU1ZYQPBIKfNjRWQBxy5hsBwMRlQLBm8AJqYWIm7kE2kTsNREVnupytxuVeRjrO2e5JW-lwkrB_K8Pzjq3ercbDE8QeeVDmKOdQgWOcgW_WWfiWtHYvS_wRfM2XH4_cZce5Ub-8s5vrWZ5ZI80nJVj7jhAtW7DXLnV4DIqosj3m62H0kfDuBwZ8busA63PIPLV-ZumNd4R-sF_kbJhkgo2f0T5OTuz8gv0r2hSInw\"}"

    println("Importing RSA key from JWK String...")
    printImportedJwkStringInfo(rsaJwkPrivateKeyString)
}
