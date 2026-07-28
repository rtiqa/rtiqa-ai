package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.validation.EmailValidator
import com.rtiqa.core.domain.validation.RegistrationValidator
import kotlinx.coroutines.flow.Flow

/**
 * Use case for authenticating an existing user with email and password.
 */
class LoginUseCase(
    private val authRepository: AuthRepositoryContract
) {
    suspend operator fun invoke(email: String, pass: String): RtiqaResult<UserProfile> {
        val validation = EmailValidator.validate(email)
        if (!validation.isValid()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(validation.getErrorsOrEmpty()))
        }
        if (pass.isBlank()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(listOf("Password cannot be blank.")))
        }
        return authRepository.login(email.trim(), pass)
    }
}

/**
 * Use case for registering a new user account.
 */
class RegisterUseCase(
    private val authRepository: AuthRepositoryContract
) {
    suspend operator fun invoke(name: String, email: String, pass: String): RtiqaResult<UserProfile> {
        val validation = RegistrationValidator.validate(name, email, pass)
        if (!validation.isValid()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(validation.getErrorsOrEmpty()))
        }
        return authRepository.register(name.trim(), email.trim(), pass)
    }
}

/**
 * Use case for logging out the current active user session.
 */
class LogoutUseCase(
    private val authRepository: AuthRepositoryContract
) {
    suspend operator fun invoke(): RtiqaResult<Unit> {
        return authRepository.logout()
    }
}

/**
 * Use case for requesting a password reset email.
 */
class ResetPasswordUseCase(
    private val authRepository: AuthRepositoryContract
) {
    suspend operator fun invoke(email: String): RtiqaResult<Unit> {
        val validation = EmailValidator.validate(email)
        if (!validation.isValid()) {
            return RtiqaResult.Error(RtiqaError.ValidationError(validation.getErrorsOrEmpty()))
        }
        return authRepository.resetPassword(email.trim())
    }
}

/**
 * Use case for observing reactive session status of the active user.
 */
class ObserveUserSessionUseCase(
    private val authRepository: AuthRepositoryContract
) {
    operator fun invoke(): Flow<UserProfile?> {
        return authRepository.observeUserSession()
    }
}
