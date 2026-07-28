package com.rtiqa.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.LoginUseCase
import com.rtiqa.core.domain.usecase.ObserveUserSessionUseCase
import com.rtiqa.core.domain.usecase.RegisterUseCase
import com.rtiqa.core.domain.usecase.ResetPasswordUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultAuthRepository : AuthRepositoryContract {
    private val userSession = MutableStateFlow<UserProfile?>(null)

    override fun observeUserSession(): Flow<UserProfile?> = userSession

    override suspend fun login(email: String, pass: String): RtiqaResult<UserProfile> {
        val profile = UserProfile(
            id = "user_001",
            name = email.substringBefore("@").ifBlank { "User" },
            email = email,
            levelXp = 150,
            streakDays = 3
        )
        userSession.value = profile
        return RtiqaResult.Success(profile)
    }

    override suspend fun register(name: String, email: String, pass: String): RtiqaResult<UserProfile> {
        val profile = UserProfile(
            id = "user_001",
            name = name.ifBlank { "User" },
            email = email,
            levelXp = 50,
            streakDays = 1
        )
        userSession.value = profile
        return RtiqaResult.Success(profile)
    }

    override suspend fun resetPassword(email: String): RtiqaResult<Unit> {
        return RtiqaResult.Success(Unit)
    }

    override suspend fun logout(): RtiqaResult<Unit> {
        userSession.value = null
        return RtiqaResult.Success(Unit)
    }

    override suspend fun getCurrentUserId(): String? = userSession.value?.id
}

class LoginViewModelFactory(
    private val authRepository: AuthRepositoryContract = DefaultAuthRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val loginUseCase = LoginUseCase(authRepository)
        val observeUserSessionUseCase = ObserveUserSessionUseCase(authRepository)
        val resetPasswordUseCase = ResetPasswordUseCase(authRepository)
        return LoginViewModel(loginUseCase, observeUserSessionUseCase, resetPasswordUseCase) as T
    }
}

class RegisterViewModelFactory(
    private val authRepository: AuthRepositoryContract = DefaultAuthRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val registerUseCase = RegisterUseCase(authRepository)
        val observeUserSessionUseCase = ObserveUserSessionUseCase(authRepository)
        return RegisterViewModel(registerUseCase, observeUserSessionUseCase) as T
    }
}
