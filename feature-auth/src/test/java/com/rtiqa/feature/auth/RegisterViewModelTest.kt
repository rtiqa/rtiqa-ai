package com.rtiqa.feature.auth

import com.rtiqa.core.domain.error.RtiqaError
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.AuthRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.domain.usecase.ObserveUserSessionUseCase
import com.rtiqa.core.domain.usecase.RegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private class FailingAuthRepository : AuthRepositoryContract {
        private val session = MutableStateFlow<UserProfile?>(null)
        override fun observeUserSession(): Flow<UserProfile?> = session
        override suspend fun login(email: String, pass: String) =
            RtiqaResult.Error(RtiqaError.AuthError("Not used"))
        override suspend fun register(name: String, email: String, pass: String) =
            RtiqaResult.Error(RtiqaError.AuthError("Server rejected registration"))
        override suspend fun resetPassword(email: String) = RtiqaResult.Success(Unit)
        override suspend fun logout() = RtiqaResult.Success(Unit)
        override suspend fun getCurrentUserId(): String? = null
    }

    @Before fun setUp() = Dispatchers.setMain(testDispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun RegisterViewModel_serverFailure_doesNotNavigate() = runTest {
        val repository = FailingAuthRepository()
        val viewModel = RegisterViewModel(
            RegisterUseCase(repository),
            ObserveUserSessionUseCase(repository)
        )
        val events = mutableListOf<RegisterUiEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvent.collect { events += it }
        }

        viewModel.onAction(RegisterUiAction.NameChanged("Sara Learner"))
        viewModel.onAction(RegisterUiAction.EmailChanged("sara@example.com"))
        viewModel.onAction(RegisterUiAction.PasswordChanged("secure-password1"))
        viewModel.onAction(RegisterUiAction.SubmitRegister)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Server rejected registration", viewModel.currentState.errorMessage)
        assertEquals(false, events.any { it is RegisterUiEvent.NavigateToHome })
    }
}
