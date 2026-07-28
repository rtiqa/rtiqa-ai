package com.rtiqa.feature.auth

import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.LoginUseCase
import com.rtiqa.core.domain.usecase.ObserveUserSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAuthRepository : AuthRepositoryContract {
        private val session = MutableStateFlow<UserProfile?>(null)

        override fun observeUserSession(): Flow<UserProfile?> = session

        override suspend fun login(email: String, pass: String): RtiqaResult<UserProfile> {
            val user = UserProfile("u1", "Alex", email, null, 100, 3, false, false)
            session.value = user
            return RtiqaResult.Success(user)
        }

        override suspend fun register(name: String, email: String, pass: String): RtiqaResult<UserProfile> {
            val user = UserProfile("u2", name, email, null, 0, 0, false, false)
            session.value = user
            return RtiqaResult.Success(user)
        }

        override suspend fun logout(): RtiqaResult<Unit> {
            session.value = null
            return RtiqaResult.Success(Unit)
        }

        override suspend fun getCurrentUserId(): String? = session.value?.id

        override suspend fun resetPassword(email: String): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginAction_updatesEmailAndPasswordState() {
        val repo = FakeAuthRepository()
        val viewModel = LoginViewModel(LoginUseCase(repo), ObserveUserSessionUseCase(repo))

        viewModel.onAction(LoginUiAction.EmailChanged("alex@rtiqa.com"))
        viewModel.onAction(LoginUiAction.PasswordChanged("password123"))

        assertEquals("alex@rtiqa.com", viewModel.currentState.email)
        assertEquals("password123", viewModel.currentState.password)
    }

    @Test
    fun submitLogin_success_triggersUserSession() = runTest {
        val repo = FakeAuthRepository()
        val viewModel = LoginViewModel(LoginUseCase(repo), ObserveUserSessionUseCase(repo))

        viewModel.onAction(LoginUiAction.EmailChanged("alex@rtiqa.com"))
        viewModel.onAction(LoginUiAction.PasswordChanged("password123"))
        viewModel.onAction(LoginUiAction.SubmitLogin)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("alex@rtiqa.com", viewModel.currentState.email)
    }
}
