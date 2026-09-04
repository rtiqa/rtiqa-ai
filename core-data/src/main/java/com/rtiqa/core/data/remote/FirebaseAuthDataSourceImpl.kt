package com.rtiqa.core.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.repository.AuthRemoteDataSource
import com.rtiqa.core.domain.repository.RemoteAuthUser
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.logging.RtiqaLog
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSourceImpl : AuthRemoteDataSource {
    private val tag = "FirebaseAuthDataSource"

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            RtiqaLog.w(tag, "FirebaseAuth initialization failed: ${e.message}")
            null
        }
    }

    override suspend fun login(email: String, pass: String): RtiqaResult<RemoteAuthUser> {
        val fbAuth = firebaseAuth ?: return RtiqaResult.Error(RtiqaError.AuthError("Firebase Auth unavailable"))
        return try {
            val result = fbAuth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user ?: return RtiqaResult.Error(RtiqaError.AuthError("User is null"))
            RtiqaResult.Success(RemoteAuthUser(user.uid, user.email, user.displayName))
        } catch (e: Exception) {
            RtiqaLog.w(tag, "Firebase login failed", e)
            RtiqaResult.Error(RtiqaError.AuthError(message = e.message ?: "Login failed", cause = e))
        }
    }

    override suspend fun register(name: String, email: String, pass: String): RtiqaResult<RemoteAuthUser> {
        val fbAuth = firebaseAuth ?: return RtiqaResult.Error(RtiqaError.AuthError("Firebase Auth unavailable"))
        return try {
            val result = fbAuth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user ?: return RtiqaResult.Error(RtiqaError.AuthError("User is null"))
            
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (e: Exception) {
                RtiqaLog.w(tag, "Failed to update Firebase user display name", e)
            }
            
            RtiqaResult.Success(RemoteAuthUser(user.uid, user.email, name))
        } catch (e: Exception) {
            RtiqaLog.w(tag, "Firebase register failed", e)
            RtiqaResult.Error(RtiqaError.AuthError(message = e.message ?: "Register failed", cause = e))
        }
    }

    override suspend fun resetPassword(email: String): RtiqaResult<Unit> {
        val fbAuth = firebaseAuth ?: return RtiqaResult.Success(Unit)
        return try {
            fbAuth.sendPasswordResetEmail(email).await()
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaLog.e(tag, "Failed to send Firebase password reset email", e)
            RtiqaResult.Error(RtiqaError.AuthError(e.message ?: "Failed to send reset email."))
        }
    }

    override suspend fun logout(): RtiqaResult<Unit> {
        return try {
            firebaseAuth?.signOut()
            RtiqaResult.Success(Unit)
        } catch (e: Exception) {
            RtiqaResult.Error(RtiqaError.UnknownError("Failed to logout cleanly.", e))
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return firebaseAuth?.currentUser?.uid
    }
}
