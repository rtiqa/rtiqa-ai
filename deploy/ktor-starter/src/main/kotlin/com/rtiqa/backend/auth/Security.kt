package com.rtiqa.backend.auth

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.rtiqa.backend.config.SupabaseConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.net.URL
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.TimeUnit

fun Application.configureSecurity(config: SupabaseConfig) {
    val jwtAudience = "authenticated"
    val baseUrl = config.url.trimEnd('/')
    val jwtIssuer = "$baseUrl/auth/v1"
    val jwksUrl = "$jwtIssuer/.well-known/jwks.json"
    
    // In-memory cache for JWKS to avoid fetching on every request.
    // Handles key rotation natively by fetching new key if kid is missing.
    val jwkProvider = JwkProviderBuilder(URL(jwksUrl))
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    // Explicit allowlist of supported asymmetric algorithms
    val allowedAlgorithms = setOf("RS256", "ES256")

    val customVerifier = object : JWTVerifier {
        override fun verify(token: String?): DecodedJWT {
            if (token == null) throw Exception("Token is null")
            val decoded = JWT.decode(token)
            return verify(decoded)
        }

        override fun verify(jwt: DecodedJWT?): DecodedJWT {
            if (jwt == null) throw Exception("JWT is null")
            
            val jwtAlg = jwt.algorithm
            val kid = jwt.keyId ?: throw Exception("Missing kid in JWT")
            
            // 1. Explicit allowlist check
            if (jwtAlg !in allowedAlgorithms) {
                throw Exception("Unsupported JWT algorithm: $jwtAlg")
            }
            
            // 2. Fetch JWK (handles unknown kid by refreshing cache)
            val jwk = jwkProvider.get(kid)
            val jwkAlg = jwk.algorithm
            
            // 3. Consistency check: JWT alg must match JWK alg (if JWK specifies one)
            if (jwkAlg != null && jwkAlg != jwtAlg) {
                throw Exception("JWT algorithm ($jwtAlg) does not match JWK algorithm ($jwkAlg)")
            }
            
            // 4. Construct verification algorithm, strictly checking public key types
            val algorithm = when (jwtAlg) {
                "RS256" -> {
                    val publicKey = jwk.publicKey as? RSAPublicKey
                        ?: throw Exception("Invalid key type: RS256 requires RSA public key")
                    Algorithm.RSA256(publicKey, null)
                }
                "ES256" -> {
                    val publicKey = jwk.publicKey as? ECPublicKey
                        ?: throw Exception("Invalid key type: ES256 requires EC public key")
                    Algorithm.ECDSA256(publicKey, null)
                }
                else -> throw Exception("Algorithm $jwtAlg not explicitly supported")
            }
            
            // 5. Cryptographic verification, issuer, audience, and expiration
            val verifier = JWT.require(algorithm)
                .withIssuer(jwtIssuer)
                .withAudience(jwtAudience)
                .acceptLeeway(3)
                .build()
                
            return verifier.verify(jwt)
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(customVerifier)
            
            validate { credential ->
                // Ensure the issuer and audience are correct (double check post-verification)
                if (credential.payload.audience.contains(jwtAudience) && credential.payload.issuer == jwtIssuer) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            
            challenge { defaultScheme, realm ->
                call.respondText(
                    """{"status":401,"message":"Token is not valid, expired, or has invalid signature"}""",
                    ContentType.Application.Json,
                    HttpStatusCode.Unauthorized
                )
            }
        }
    }
}
