package com.rtiqa.backend.auth

import com.auth0.jwk.Jwk
import com.auth0.jwk.JwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Date

class AuthTest {

    companion object {
        lateinit var rsaPublicKey: RSAPublicKey
        lateinit var rsaPrivateKey: RSAPrivateKey
        lateinit var rsaAlgorithm: Algorithm

        lateinit var ecPublicKey: ECPublicKey
        lateinit var ecPrivateKey: ECPrivateKey
        lateinit var ecAlgorithm: Algorithm

        lateinit var wrongRsaAlgorithm: Algorithm

        lateinit var mockJwkProvider: JwkProvider
        lateinit var verifier: JWTVerifier

        var jwksFetchCount = 0

        @JvmStatic
        @BeforeAll
        fun setup() {
            // Generate RSA Key
            val rsaGen = KeyPairGenerator.getInstance("RSA")
            rsaGen.initialize(2048)
            val rsaPair = rsaGen.generateKeyPair()
            rsaPublicKey = rsaPair.public as RSAPublicKey
            rsaPrivateKey = rsaPair.private as RSAPrivateKey
            rsaAlgorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey)

            // Generate EC Key
            val ecGen = KeyPairGenerator.getInstance("EC")
            ecGen.initialize(256)
            val ecPair = ecGen.generateKeyPair()
            ecPublicKey = ecPair.public as ECPublicKey
            ecPrivateKey = ecPair.private as ECPrivateKey
            ecAlgorithm = Algorithm.ECDSA256(ecPublicKey, ecPrivateKey)

            val wrongPair = rsaGen.generateKeyPair()
            wrongRsaAlgorithm = Algorithm.RSA256(wrongPair.public as RSAPublicKey, wrongPair.private as RSAPrivateKey)

            mockJwkProvider = object : JwkProvider {
                override fun get(keyId: String?): Jwk {
                    jwksFetchCount++
                    return when (keyId) {
                        "rsa-key" -> {
                            val map = mapOf("kty" to "RSA", "kid" to "rsa-key", "alg" to "RS256", "use" to "sig")
                            Jwk.fromValues(map).let {
                                object : Jwk(it.id, it.type, it.algorithm, it.usage, it.operations, null, it.certificateChain, it.certificateThumbprint, it.additionalAttributes) {
                                    override fun getPublicKey(): java.security.PublicKey = rsaPublicKey
                                }
                            }
                        }
                        "ec-key" -> {
                            val map = mapOf("kty" to "EC", "kid" to "ec-key", "alg" to "ES256", "use" to "sig")
                            Jwk.fromValues(map).let {
                                object : Jwk(it.id, it.type, it.algorithm, it.usage, it.operations, null, it.certificateChain, it.certificateThumbprint, it.additionalAttributes) {
                                    override fun getPublicKey(): java.security.PublicKey = ecPublicKey
                                }
                            }
                        }
                        "mismatch-alg-key" -> {
                            val map = mapOf("kty" to "RSA", "kid" to "mismatch-alg-key", "alg" to "RS256", "use" to "sig")
                            Jwk.fromValues(map).let {
                                object : Jwk(it.id, it.type, it.algorithm, it.usage, it.operations, null, it.certificateChain, it.certificateThumbprint, it.additionalAttributes) {
                                    override fun getPublicKey(): java.security.PublicKey = rsaPublicKey
                                }
                            }
                        }
                        else -> throw Exception("Key not found")
                    }
                }
            }

            verifier = createCustomVerifier(mockJwkProvider, "https://example.supabase.co/auth/v1", "authenticated")
        }

        private fun createCustomVerifier(jwkProvider: JwkProvider, jwtIssuer: String, jwtAudience: String): JWTVerifier {
            val allowedAlgorithms = setOf("RS256", "ES256")
            return object : JWTVerifier {
                override fun verify(token: String?): DecodedJWT {
                    val decoded = JWT.decode(token!!)
                    return verify(decoded)
                }

                override fun verify(jwt: DecodedJWT?): DecodedJWT {
                    val decoded = jwt!!
                    val jwtAlg = decoded.algorithm
                    val kid = decoded.keyId ?: throw Exception("Missing kid in JWT")
                    
                    if (jwtAlg !in allowedAlgorithms) {
                        throw Exception("Unsupported JWT algorithm: $jwtAlg")
                    }
                    
                    val jwk = jwkProvider.get(kid)
                    val jwkAlg = jwk.algorithm
                    
                    if (jwkAlg != null && jwkAlg != jwtAlg) {
                        throw Exception("JWT algorithm ($jwtAlg) does not match JWK algorithm ($jwkAlg)")
                    }
                    
                    val algorithm = when (jwtAlg) {
                        "RS256" -> {
                            val publicKey = jwk.publicKey as? RSAPublicKey ?: throw Exception("Invalid key type: RS256 requires RSA public key")
                            Algorithm.RSA256(publicKey, null)
                        }
                        "ES256" -> {
                            val publicKey = jwk.publicKey as? ECPublicKey ?: throw Exception("Invalid key type: ES256 requires EC public key")
                            Algorithm.ECDSA256(publicKey, null)
                        }
                        else -> throw Exception("Unsupported algorithm")
                    }
                    
                    return JWT.require(algorithm)
                        .withIssuer(jwtIssuer)
                        .withAudience(jwtAudience)
                        .acceptLeeway(3)
                        .build()
                        .verify(decoded)
                }
            }
        }
    }

    @Test
    fun `A - Valid RS256 token passes`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(rsaAlgorithm)
        assertNotNull(verifier.verify(token))
    }

    @Test
    fun `B - Valid ES256 token passes`() {
        val token = JWT.create()
            .withKeyId("ec-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(ecAlgorithm)
        assertNotNull(verifier.verify(token))
    }

    @Test
    fun `C - Invalid signature fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(wrongRsaAlgorithm)
        assertThrows(Exception::class.java) { verifier.verify(token) }
    }

    @Test
    fun `D - Expired token fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() - 3600000))
            .sign(rsaAlgorithm)
        assertThrows(Exception::class.java) { verifier.verify(token) }
    }

    @Test
    fun `E - Wrong issuer fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://wrong-issuer.com")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(rsaAlgorithm)
        assertThrows(Exception::class.java) { verifier.verify(token) }
    }

    @Test
    fun `F - Wrong audience fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("wrong-audience")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(rsaAlgorithm)
        assertThrows(Exception::class.java) { verifier.verify(token) }
    }

    @Test
    fun `G - alg=none fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(Algorithm.none())
        
        val ex = assertThrows(Exception::class.java) { verifier.verify(token) }
        assertEquals("Unsupported JWT algorithm: none", ex.message)
    }

    @Test
    fun `H - HS256 with RSA public key fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(Algorithm.HMAC256("secret"))
        val ex = assertThrows(Exception::class.java) { verifier.verify(token) }
        assertEquals("Unsupported JWT algorithm: HS256", ex.message)
    }

    @Test
    fun `I - HS256 with EC public key fails`() {
        val token = JWT.create()
            .withKeyId("ec-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(Algorithm.HMAC256("secret"))
        val ex = assertThrows(Exception::class.java) { verifier.verify(token) }
        assertEquals("Unsupported JWT algorithm: HS256", ex.message)
    }

    @Test
    fun `J - RS256 token using EC key fails`() {
        val token = JWT.create()
            .withKeyId("ec-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(rsaAlgorithm) // But points to ec-key
        
        val ex = assertThrows(Exception::class.java) { verifier.verify(token) }
        assertEquals("JWT algorithm (RS256) does not match JWK algorithm (ES256)", ex.message)
    }

    @Test
    fun `L - JWT alg does not match JWK alg fails`() {
        val token = JWT.create()
            .withKeyId("mismatch-alg-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(ecAlgorithm)
            
        val ex = assertThrows(Exception::class.java) { verifier.verify(token) }
        assertEquals("JWT algorithm (ES256) does not match JWK algorithm (RS256)", ex.message)
    }

    @Test
    fun `M - Unsupported algorithm fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(Algorithm.HMAC512("secret"))
            
        val ex = assertThrows(Exception::class.java) { verifier.verify(token) }
        assertEquals("Unsupported JWT algorithm: HS512", ex.message)
    }

    @Test
    fun `N - Modified payload fails`() {
        val token = JWT.create()
            .withKeyId("rsa-key")
            .withIssuer("https://example.supabase.co/auth/v1")
            .withAudience("authenticated")
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
            .sign(rsaAlgorithm)
            
        val parts = token.split(".")
        val tamperedToken = "${parts[0]}.eyJhIjoibWFsaWNpb3VzIn0.${parts[2]}"
        
        assertThrows(Exception::class.java) { verifier.verify(tamperedToken) }
    }
}
