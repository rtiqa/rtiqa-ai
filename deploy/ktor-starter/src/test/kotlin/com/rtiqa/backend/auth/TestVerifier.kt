package com.rtiqa.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier

class TestVerifier : JWTVerifier {
    override fun verify(token: String?): DecodedJWT {
        return JWT.decode(token)
    }
    override fun verify(jwt: DecodedJWT?): DecodedJWT {
        return jwt!!
    }
}
