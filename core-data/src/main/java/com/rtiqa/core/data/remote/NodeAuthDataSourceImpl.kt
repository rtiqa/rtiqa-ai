package com.rtiqa.core.data.remote

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.repository.AuthRemoteDataSource
import com.rtiqa.core.domain.repository.RemoteAuthUser
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.RestApiContract
import com.rtiqa.core.network.api.RestLoginRequest
import com.rtiqa.core.network.session.RestSessionStore
import com.rtiqa.core.logging.RtiqaLog
import retrofit2.HttpException

class NodeAuthDataSourceImpl(
    private val restApiContract: RestApiContract,
    private val sessionStore: RestSessionStore
) : AuthRemoteDataSource {

    private val tag = "NodeAuthDataSource"

    override suspend fun login(email: String, pass: String): RtiqaResult<RemoteAuthUser> {
        return try {
            val response = restApiContract.login(RestLoginRequest(email = email, password = pass))
            
            sessionStore.saveSession(response.token, response.organization_id)
            
            RtiqaResult.Success(
                RemoteAuthUser(
                    uid = response.user.id,
                    email = response.user.email,
                    displayName = response.user.full_name
                )
            )
        } catch (e: HttpException) {
            val errorMessage = "REST login failed: HTTP ${e.code()}"
            RtiqaLog.w(tag, errorMessage, e)
            val mappedError = when (e.code()) {
                400, 422 -> RtiqaError.AuthError("Validation error. Please check your inputs.", cause = e)
                401 -> RtiqaError.AuthError("Invalid credentials or session.", cause = e)
                403 -> RtiqaError.AuthError("Authorization or tenant issue.", cause = e)
                429 -> RtiqaError.NetworkError("Too many requests. Please try again later.", cause = e)
                in 500..599 -> RtiqaError.NetworkError("Temporary server error.", cause = e)
                else -> RtiqaError.UnknownError(errorMessage, cause = e)
            }
            RtiqaResult.Error(mappedError)
        } catch (e: Exception) {
            RtiqaLog.w(tag, "REST login failed with unknown exception", e)
            RtiqaResult.Error(RtiqaError.UnknownError("An unexpected error occurred during login.", e))
        }
    }

    override suspend fun register(name: String, email: String, pass: String): RtiqaResult<RemoteAuthUser> {
        RtiqaLog.w(tag, "REST register is not currently implemented in REST pilot")
        return RtiqaResult.Error(RtiqaError.AuthError("Registration via REST is currently unavailable."))
    }

    override suspend fun resetPassword(email: String): RtiqaResult<Unit> {
        RtiqaLog.w(tag, "REST resetPassword is not currently implemented in REST pilot")
        return RtiqaResult.Error(RtiqaError.AuthError("Password reset via REST is currently unavailable."))
    }

    override suspend fun logout(): RtiqaResult<Unit> {
        RtiqaLog.i(tag, "Backend logout POST /api/v1/auth/logout is not defined in RestApiContract. Gap reported.")
        sessionStore.clearSession()
        return RtiqaResult.Success(Unit)
    }

    override suspend fun getCurrentUserId(): String? {
        return null // Allows AuthRepositoryImpl to fallback to its local securityManager stored user_id
    }
}
