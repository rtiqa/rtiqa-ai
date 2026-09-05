import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier

fun main() {
    val verifier = object : JWTVerifier {
        override fun verify(token: String): DecodedJWT {
            return JWT.decode(token)
        }
        override fun verify(jwt: DecodedJWT): DecodedJWT {
            return jwt
        }
    }
    println("Compiled")
}
